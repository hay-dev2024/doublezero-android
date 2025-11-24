package com.doublezero.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// DTOs for navigation route request/response
data class LatLngDto(val lat: Double, val lng: Double)

data class RouteRequestDto(
    val origin: LatLngDto,
    val destination: LatLngDto,
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
