package com.doublezero.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// DTOs for navigation route request/response
// renamed to LatLonDto to be clearer and avoid confusion with other LatLng types
data class LatLonDto(val lat: Double, val lon: Double)

data class RouteRequestDto(
    val origin: LatLonDto,
    val destination: LatLonDto,
    val travelMode: String = "DRIVE"
)

data class StepDto(
    val distance: String?,
    val duration: String?,
    val instruction: String?,
    val polyline: String?
)

data class RouteDto(
    val distance: String?,
    val duration: String?,
    val summary: String?,
    val polyline: String?,
    val warning: List<String>? = null,
    val bounds: Map<String, Any>? = null,
    val steps: List<StepDto>? = null,
    val trafficInfo: Map<String, Any>? = null
)

data class RoutesResponseDto(
    val routes: List<RouteDto>
)

interface NavigationApi {
    @POST("/navigation/route")
    suspend fun computeRoute(
        @Body req: RouteRequestDto,
        @Header("Authorization") auth: String? = null
    ): Response<RoutesResponseDto>
}
