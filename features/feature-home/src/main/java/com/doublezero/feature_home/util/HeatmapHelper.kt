package com.doublezero.feature_home.util

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileProvider
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng

/**
 * Helper class for creating and managing heatmap visualization
 * for risk data on Google Maps.
 */
object HeatmapHelper {

    /**
     * Creates a HeatmapTileProvider from a list of risk points.
     * Only displays the highest risk tier present with a SINGLE color (no gradient).
     *
     * @param riskPoints List of risk points with lat, lon, and weight
     * @return HeatmapTileProvider configured for risk visualization
     */
    fun createHeatmapProvider(
        riskPoints: List<RiskPointData>
    ): HeatmapTileProvider? {
        if (riskPoints.isEmpty()) return null

        // Group points by risk tier
        val lowRiskPoints = mutableListOf<WeightedLatLng>()
        val mediumRiskPoints = mutableListOf<WeightedLatLng>()
        val highRiskPoints = mutableListOf<WeightedLatLng>()

        riskPoints.forEach { point ->
            val weight = point.weight.coerceIn(0.0f, 1.0f)
            val latLng = LatLng(point.lat, point.lon)

            when {
                weight <= 0.33f -> {
                    // Low risk - Green group
                    lowRiskPoints.add(WeightedLatLng(latLng, 1.0))
                }
                weight <= 0.66f -> {
                    // Medium risk - Yellow group
                    mediumRiskPoints.add(WeightedLatLng(latLng, 1.0))
                }
                else -> {
                    // High risk - Red group
                    highRiskPoints.add(WeightedLatLng(latLng, 1.0))
                }
            }
        }

        // Only display the HIGHEST risk tier present (single color)
        return when {
            highRiskPoints.isNotEmpty() -> {
                // Show only RED circles
                val colors = intArrayOf(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.rgb(255, 82, 82) // Pure Red
                )
                val startPoints = floatArrayOf(0.0f, 1.0f)
                val gradient = Gradient(colors, startPoints)

                HeatmapTileProvider.Builder()
                    .weightedData(highRiskPoints)
                    .gradient(gradient)
                    .radius(80)     // Larger radius for circular appearance
                    .opacity(0.7)
                    .build()
            }
            mediumRiskPoints.isNotEmpty() -> {
                // Show only YELLOW circles
                val colors = intArrayOf(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.rgb(255, 235, 59) // Pure Yellow
                )
                val startPoints = floatArrayOf(0.0f, 1.0f)
                val gradient = Gradient(colors, startPoints)

                HeatmapTileProvider.Builder()
                    .weightedData(mediumRiskPoints)
                    .gradient(gradient)
                    .radius(80)
                    .opacity(0.7)
                    .build()
            }
            lowRiskPoints.isNotEmpty() -> {
                // Show only GREEN circles
                val colors = intArrayOf(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.rgb(0, 200, 83) // Pure Green
                )
                val startPoints = floatArrayOf(0.0f, 1.0f)
                val gradient = Gradient(colors, startPoints)

                HeatmapTileProvider.Builder()
                    .weightedData(lowRiskPoints)
                    .gradient(gradient)
                    .radius(80)
                    .opacity(0.7)
                    .build()
            }
            else -> null
        }
    }

    /**
     * Data class representing a risk point for heatmap visualization.
     */
    data class RiskPointData(
        val lat: Double,
        val lon: Double,
        val weight: Float  // 0.0 ~ 1.0
    )

    /**
     * Converts backend RiskPointDto to HeatmapHelper.RiskPointData
     */
    fun fromRiskPointDto(dto: com.doublezero.data.network.RiskPointDto): RiskPointData {
        return RiskPointData(
            lat = dto.lat,
            lon = dto.lon,
            weight = dto.weight.toFloat()
        )
    }
}

