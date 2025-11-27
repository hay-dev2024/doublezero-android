package com.doublezero.data.repository

import com.doublezero.data.network.RouteDto

interface NavigationRepository {
    suspend fun computeRoute(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
        sampleCount: Int = 3,
        includeRisk: Boolean = false,
        token: String? = null
    ): List<RouteDto>
}
