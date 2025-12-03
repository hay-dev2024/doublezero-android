package com.doublezero.data.repository

import android.util.Log
import com.doublezero.data.network.*
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationRepositoryImpl @Inject constructor() : NavigationRepository {

    // Create a Retrofit instance for local backend (matching AuthRepositoryImpl pattern)
    private val retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .callTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val navigationApi: NavigationApi by lazy { retrofit.create(NavigationApi::class.java) }

    override suspend fun getRoute(
        originLat: Double,
        originLon: Double,
        destLat: Double,
        destLon: Double,
        alternatives: Boolean,
        travelMode: String,
        token: String?
    ): List<RouteDto> {
        val req = RouteRequestDto(
            origin = PlaceInputDto(lat = originLat, lon = originLon),
            destination = PlaceInputDto(lat = destLat, lon = destLon),
            alternatives = alternatives,
            travelMode = travelMode
        )

        return try {
            val authHeader = token?.let { "Bearer $it" }
            val resp = navigationApi.computeRoute(req, authHeader)
            if (resp.isSuccessful) {
                val routes = resp.body()?.routes ?: emptyList()
                // limit to at most 2 routes (primary + one alternative)
                val limited = if (routes.size > 2) routes.take(2) else routes
                // Debug log: show how many routes backend returned vs limited
                try {
                    Log.d("NavRepo", "getRoute: received ${routes.size} routes from backend, limited to ${limited.size}")
                } catch (_: Throwable) {
                    // ignore logging errors in non-Android test contexts
                }
                limited
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            // network error or parsing error -> return empty list so caller can handle fallback
            Log.w("NavRepo", "getRoute failed", e)
            emptyList()
        }
    }

    override suspend fun startSession(
        sessionId: String,
        polyline: String,
        startTime: String,
        estimatedSpeedKmh: Int,
        token: String
    ): StartSessionResponse? {
        return try {
            val req = StartSessionRequest(
                sessionId = sessionId,
                polyline = polyline,
                startTime = startTime,
                estimatedSpeedKmh = estimatedSpeedKmh
            )
            val resp = navigationApi.startSession(req, "Bearer $token")
            if (resp.isSuccessful) {
                resp.body()
            } else {
                Log.e("NavRepo", "Failed to start session: ${resp.code()}")
                throw IllegalStateException("Failed to start session: ${resp.code()}")
            }
        } catch (e: Exception) {
            Log.e("NavRepo", "startSession error", e)
            null
        }
    }

    override suspend fun stopSession(sessionId: String, token: String): Boolean {
        return try {
            val req = StopSessionRequest(sessionId)
            val resp = navigationApi.stopSession(req, "Bearer $token")
            resp.isSuccessful
        } catch (e: Exception) {
            Log.e("NavRepo", "stopSession error", e)
            false
        }
    }

    override fun connectRiskStream(
        sessionId: String,
        token: String
    ): Flow<RiskUpdateEvent> = callbackFlow {
        Log.d("NavRepo SSE", "Setting up SSE connection for session: $sessionId")

        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            })
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(0, java.util.concurrent.TimeUnit.SECONDS)  // 무제한 - SSE는 장시간 연결 유지
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            // pingInterval 제거 - 백엔드가 30초마다 heartbeat 보냄
            .retryOnConnectionFailure(true)
            .build()

        val request = Request.Builder()
            .url("http://10.0.2.2:3000/navigation/session/stream?sessionId=$sessionId")
            .header("Authorization", "Bearer $token")
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        val gson = Gson()
        var eventSource: EventSource? = null

        try {
            eventSource = EventSources.createFactory(client).newEventSource(
                request,
                object : EventSourceListener() {
                    override fun onOpen(eventSource: EventSource, response: okhttp3.Response) {
                        Log.d("NavRepo SSE", "Connection opened successfully")
                    }

                    override fun onEvent(
                        eventSource: EventSource,
                        id: String?,
                        type: String?,
                        data: String
                    ) {
                        Log.d("NavRepo SSE", "Received event: type=$type, id=$id, data length=${data.length}")

                        try {
                            when (type) {
                                "risk-update" -> {
                                    Log.d("NavRepo SSE", "Parsing risk-update: $data")
                                    val update = gson.fromJson(data, RiskUpdateEvent::class.java)
                                    Log.d("NavRepo SSE", "Parsed successfully, sending to Flow")
                                    val result = trySend(update)
                                    if (result.isSuccess) {
                                        Log.d("NavRepo SSE", "Event sent to Flow successfully")
                                    } else {
                                        Log.e("NavRepo SSE", "Failed to send event to Flow: ${result.exceptionOrNull()}")
                                    }
                                }
                                "session-ended" -> {
                                    Log.d("NavRepo SSE", "Session ended event received - parsing reason")
                                    try {
                                        val endEvent = gson.fromJson(data, SessionEndedEvent::class.java)
                                        Log.d("NavRepo SSE", "Session ended: reason=${endEvent.reason}")
                                        // Send a special event to signal session end to ViewModel
                                        val endMarker = RiskUpdateEvent(
                                            sessionId = endEvent.sessionId ?: sessionId,
                                            timestamp = "",
                                            currentPosition = CurrentPositionDto(0.0, 0.0, 0.0, 0.0),
                                            riskPoints = emptyList(),
                                            summary = RiskSummaryDto(
                                                level = "End",
                                                avgWeight = 0.0,
                                                maxWeight = 0.0,
                                                hotspotCount = 0,
                                                hotspotThreshold = 0.0,
                                                message = "Destination reached - Navigation complete",
                                                urgency = "low"
                                            )
                                        )
                                        trySend(endMarker)
                                    } catch (e: Exception) {
                                        Log.e("NavRepo SSE", "Failed to parse session-ended event", e)
                                    }
                                    close()
                                }
                                "heartbeat" -> {
                                    Log.d("NavRepo SSE", "Heartbeat received")
                                }
                                else -> {
                                    Log.d("NavRepo SSE", "Unknown event type: $type")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("NavRepo SSE", "Parse/send error for $type: $data", e)
                        }
                    }

                    override fun onFailure(
                        eventSource: EventSource,
                        t: Throwable?,
                        response: okhttp3.Response?
                    ) {
                        val msg = "Connection failed: code=${response?.code}, message=${t?.message}"
                        Log.e("NavRepo SSE", "$msg", t)
                        close(t)
                    }

                    override fun onClosed(eventSource: EventSource) {
                        Log.d("NavRepo SSE", "Connection closed by server")
                        close()
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("NavRepo SSE", "Failed to create EventSource", e)
            close(e)
        }

        awaitClose {
            Log.d("NavRepo SSE", "Closing SSE connection")
            eventSource?.cancel()
        }
    }
}
