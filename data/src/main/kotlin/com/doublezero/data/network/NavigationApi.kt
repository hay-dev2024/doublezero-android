package com.doublezero.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// DTOs for navigation route request/response
// renamed to LatLonDto to be clearer and avoid confusion with other LatLng types
data class LatLonDto(val lat: Double, val lon: Double)

// Flexible place input: either lat/lon or placeId/text/address
data class PlaceInputDto(
    val lat: Double? = null,
    val lon: Double? = null,
    val placeId: String? = null,
    val address: String? = null,
    val text: String? = null
)

data class RouteRequestDto(
    // one of these should be provided (server requires origin & destination in practice)
    val origin: PlaceInputDto,
    val destination: PlaceInputDto,

    // Request alternative routes (optional). Backend default is false.
    val alternatives: Boolean? = null,
    val travelMode: String = "DRIVE",

    // Risk sampling/options (optional)
    val sampleCount: Int? = null, // client default handling done in caller (e.g. 3)
    val includeRisk: Boolean? = null
)

// Add numeric fields and maneuver to StepDto to match backend improvements
data class StepDto(
    val distance: String?,
    val duration: String?,
    val instruction: String?,
    val polyline: String?,
    val distanceMeters: Long? = null,
    val durationSeconds: Long? = null,
    val maneuver: String? = null
)

// Typed bounds instead of Map
data class BoundsDto(val northeast: LatLonDto, val southwest: LatLonDto)

// Risk point DTO as defined by backend
data class RiskPointDto(
    val lat: Double,
    val lon: Double,
    val weight: Double,
    val tier: Int,
    val severity3Probability: Double,
    val pointIndex: Int? = null,
    val distanceFromStartMeters: Int? = null,
    val timestamp: String? = null,
    val source: String? = null
)

// Structured risk summary returned by backend
data class RiskSummaryDto(
    val level: String?,
    val avgWeight: Double?,
    val maxWeight: Double?,
    val hotspotCount: Int?,
    val hotspotThreshold: Double?,
    val message: String?,
    val urgency: String? = null // "low", "medium", or "high"
)

data class RouteDto(
    val routeId: String? = null,
    val distance: String? = null,
    val duration: String? = null,
    val summary: String? = null,
    val polyline: String? = null,
    val warning: List<String>? = null,
    val bounds: BoundsDto? = null,
    val steps: List<StepDto>? = null,
    val trafficInfo: Map<String, Any>? = null,

    // riskPoints will be present when includeRisk=true; may be empty if prediction unavailable
    val riskPoints: List<RiskPointDto>? = null,
    // server-provided structured summary and a human-readable text fallback
    val riskSummary: RiskSummaryDto? = null,
    val riskSummaryText: String? = null
)

data class RoutesResponseDto(
    val routes: List<RouteDto>
)

// SSE Session DTOs
data class StartSessionRequest(
    val sessionId: String,
    val polyline: String,
    val startTime: String,
    val estimatedSpeedKmh: Int = 60
)

data class StartSessionResponse(
    val sessionId: String,
    val status: String,
    val streamUrl: String,
    val estimatedDuration: Int,
    val totalDistance: Int
)

data class StopSessionRequest(
    val sessionId: String
)

data class StopSessionResponse(
    val sessionId: String,
    val status: String,
    val duration: Int
)

// SSE Event DTOs
data class CurrentPositionDto(
    val lat: Double,
    val lon: Double,  // Backend uses 'lon' not 'lng'
    val distanceFromStart: Double,
    val remainingDistance: Double,
    val currentSegmentIndex: Int? = null
)

data class RiskUpdateEvent(
    val sessionId: String,
    val timestamp: String,
    val currentPosition: CurrentPositionDto,
    val riskPoints: List<RiskPointDto>,
    val summary: RiskSummaryDto?
)

data class SessionEndedEvent(
    val sessionId: String?,
    val reason: String?
)

interface NavigationApi {
    @POST("/navigation/route")
    suspend fun computeRoute(
        @Body req: RouteRequestDto,
        @Header("Authorization") auth: String? = null // navigation typically does not require auth
    ): Response<RoutesResponseDto>

    @POST("/navigation/session/start")
    suspend fun startSession(
        @Body req: StartSessionRequest,
        @Header("Authorization") auth: String
    ): Response<StartSessionResponse>

    @POST("/navigation/session/stop")
    suspend fun stopSession(
        @Body req: StopSessionRequest,
        @Header("Authorization") auth: String
    ): Response<StopSessionResponse>
}
