package com.doublezero.data.network

import com.google.gson.annotations.SerializedName

// LatLonDto and RiskSummaryDto are already defined in NavigationApi.kt
// No need to redeclare them here

data class WeatherSummaryDto(
    @SerializedName("precipitationPresent") val precipitationPresent: Boolean? = null,
    @SerializedName("avgPrecipIn") val avgPrecipIn: Double? = null
)

data class CreateHistoryDto(
    @SerializedName("origin") val origin: LatLonDto,
    @SerializedName("destination") val destination: LatLonDto,
    @SerializedName("polyline") val polyline: String? = null,
    @SerializedName("distanceMeters") val distanceMeters: Int? = null,
    @SerializedName("durationSeconds") val durationSeconds: Int? = null,
    @SerializedName("summary") val summary: String? = null,
    @SerializedName("sampleCount") val sampleCount: Int? = null,
    @SerializedName("includeRisk") val includeRisk: Boolean? = null,
    @SerializedName("riskSummary") val riskSummary: RiskSummaryDto? = null,
    @SerializedName("weatherSummary") val weatherSummary: WeatherSummaryDto? = null,
    @SerializedName("riskPoints") val riskPoints: List<Map<String, Any>>? = null,
    @SerializedName("startedAt") val startedAt: String? = null,
    @SerializedName("endedAt") val endedAt: String? = null
)

data class HistoryDto(
    @SerializedName("_id") val id: String,  // MongoDB uses "_id"
    @SerializedName("userId") val userId: String,
    @SerializedName("origin") val origin: LatLonDto,
    @SerializedName("destination") val destination: LatLonDto,
    @SerializedName("polyline") val polyline: String? = null,
    @SerializedName("distanceMeters") val distanceMeters: Int? = null,
    @SerializedName("durationSeconds") val durationSeconds: Int? = null,
    @SerializedName("summary") val summary: String? = null,
    @SerializedName("riskSummary") val riskSummary: RiskSummaryDto? = null,
    @SerializedName("weatherSummary") val weatherSummary: WeatherSummaryDto? = null,
    @SerializedName("startedAt") val startedAt: String? = null,
    @SerializedName("endedAt") val endedAt: String? = null,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

