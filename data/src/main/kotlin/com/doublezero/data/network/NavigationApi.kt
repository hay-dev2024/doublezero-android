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
    val riskPoints: List<RiskPointDto>? = null
)

data class RoutesResponseDto(
    val routes: List<RouteDto>
)

interface NavigationApi {
    @POST("/navigation/route")
    suspend fun computeRoute(
        @Body req: RouteRequestDto,
        @Header("Authorization") auth: String? = null // navigation typically does not require auth
    ): Response<RoutesResponseDto>
}
