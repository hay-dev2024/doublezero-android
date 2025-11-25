package com.doublezero.data.repository

import android.util.Log
import com.doublezero.data.network.LatLonDto
import com.doublezero.data.network.NavigationApi
import com.doublezero.data.network.RouteRequestDto
import com.doublezero.data.network.RouteDto
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationRepositoryImpl @Inject constructor() : NavigationRepository {

    // Create a Retrofit instance for local backend (matching AuthRepositoryImpl pattern)
    private val retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val navigationApi: NavigationApi by lazy { retrofit.create(NavigationApi::class.java) }

    override suspend fun computeRoute(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
        token: String?
    ): List<RouteDto> {
        val req = RouteRequestDto(
            origin = LatLonDto(originLat, originLng),
            destination = LatLonDto(destLat, destLng),
            // ask server to compute one alternative route if available
            alternatives = true,
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
                    Log.d("NavRepo", "computeRoute: received ${routes.size} routes from backend, limited to ${limited.size}")
                } catch (_: Throwable) {
                    // ignore logging errors in non-Android test contexts
                }
                limited
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            // network error or parsing error -> return empty list so caller can handle fallback
            emptyList()
        }
    }
}
