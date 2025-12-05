package com.doublezero.data.model

data class HistoryItem(
    val id: String,
    val originLat: Double,
    val originLon: Double,
    val destinationLat: Double,
    val destinationLon: Double,
    val distanceMeters: Int?,
    val durationSeconds: Int?,
    val summary: String?,
    val riskLevel: String?,
    val startedAt: String?,
    val endedAt: String?,
    val createdAt: String,
    // Computed properties for display names
    var originName: String? = null,
    var destinationName: String? = null
)

