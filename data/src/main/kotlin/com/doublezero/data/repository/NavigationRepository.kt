package com.doublezero.data.repository

import com.doublezero.data.network.RouteDto
import com.doublezero.data.network.RiskUpdateEvent
import com.doublezero.data.network.StartSessionResponse
import kotlinx.coroutines.flow.Flow

interface NavigationRepository {
    suspend fun getRoute(
        originLat: Double,
        originLon: Double,
        destLat: Double,
        destLon: Double,
        alternatives: Boolean = false,
        travelMode: String = "DRIVE",
        token: String? = null,
        includeRisk: Boolean = false,
        sampleCount: Int? = null
    ): List<RouteDto>

    suspend fun startSession(
        sessionId: String,
        polyline: String,
        startTime: String,
        estimatedSpeedKmh: Int,
        token: String
    ): StartSessionResponse?

    suspend fun stopSession(
        sessionId: String,
        token: String
    ): Boolean

    fun connectRiskStream(
        sessionId: String,
        token: String
    ): Flow<RiskUpdateEvent>
}
