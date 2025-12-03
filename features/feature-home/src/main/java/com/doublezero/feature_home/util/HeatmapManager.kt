package com.doublezero.feature_home.util

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.SphericalUtil
import com.google.maps.android.heatmaps.*

/**
 * HeatmapManager: manages two separate heatmap overlays on the map:
 *  - initial heatmap (created once or replaced when finding route)
 *  - periodic heatmap (updated every 30s with setWeightedData to avoid accumulation)
 *
 * To avoid visual darkening when periodic points overlap initial points, we remove
 * overlapping initial points (within a small distance threshold) before rendering the periodic overlay.
 */
class HeatmapManager(
    private val map: GoogleMap,
    private val radius: Int = 50,
    private val opacity: Double = 0.7,
    private val overlapThresholdMeters: Double = 30.0 // distance to consider two points "overlapping"
) {
    private var initialProvider: HeatmapTileProvider? = null
    private var periodicProvider: HeatmapTileProvider? = null
    private var initialOverlay: TileOverlay? = null
    private var periodicOverlay: TileOverlay? = null

    // Keep initial points so we can filter overlaps when periodic updates arrive
    private var initialPoints: List<WeightedLatLng> = emptyList()

    // Gradient for single-color circular appearance (we use pairs of transparent -> color)
    private val greenGradient = Gradient(
        intArrayOf(android.graphics.Color.TRANSPARENT, android.graphics.Color.rgb(0, 200, 83)),
        floatArrayOf(0.0f, 1.0f)
    )
    private val yellowGradient = Gradient(
        intArrayOf(android.graphics.Color.TRANSPARENT, android.graphics.Color.rgb(255, 235, 59)),
        floatArrayOf(0.0f, 1.0f)
    )
    private val redGradient = Gradient(
        intArrayOf(android.graphics.Color.TRANSPARENT, android.graphics.Color.rgb(255, 82, 82)),
        floatArrayOf(0.0f, 1.0f)
    )

    // Set or replace the initial route heatmap. This should not be affected by periodic updates
    // except when periodic points overlap — we remove overlapping initial points when periodic updates arrive.
    fun setInitialRouteHeatmap(points: List<WeightedLatLng>, tier: Int) {
        // store initial points
        initialPoints = points

        // remove previous initial overlay if any
        initialOverlay?.remove()

        val prov = HeatmapTileProvider.Builder()
            .weightedData(initialPoints)
            .radius(radius)
            .opacity(opacity)
            .gradient(selectGradientForTier(tier))
            .build()

        initialProvider = prov
        initialOverlay = map.addTileOverlay(TileOverlayOptions().tileProvider(prov))
    }

    // Update periodic heatmap for current 30s window. Replaces previous periodic data.
    // Also filter initial points to avoid overlapping visual darkening.
    fun updatePeriodicRiskHeatmap(points: List<WeightedLatLng>, tier: Int) {
        // Filter initialPoints to remove ones that overlap with any periodic point
        if (initialPoints.isNotEmpty() && points.isNotEmpty()) {
            val filtered = initialPoints.filter { initPt ->
                // keep initPt only if no periodic point is within threshold
                points.none { perPt ->
                    val d = SphericalUtil.computeDistanceBetween(initPt.latLng, perPt.latLng)
                    d <= overlapThresholdMeters
                }
            }
            // Update initial provider to the filtered set to avoid overlap
            initialPoints = filtered
            initialProvider?.updateData(initialPoints)
        }

        val grad = selectGradientForTier(tier)

        if (periodicProvider == null) {
            val prov = HeatmapTileProvider.Builder()
                .weightedData(points)
                .radius(radius)
                .opacity(opacity)
                .gradient(grad)
                .build()
            periodicProvider = prov
            periodicOverlay = map.addTileOverlay(TileOverlayOptions().tileProvider(prov))
        } else {
            // Replace data (avoid accumulation)
            periodicProvider!!.updateData(points)
            periodicProvider!!.setRadius(radius)
            periodicProvider!!.setOpacity(opacity)
            // Recreate provider if tier changed (to apply new gradient)
            periodicOverlay?.remove()
            val prov = HeatmapTileProvider.Builder()
                .weightedData(points)
                .radius(radius)
                .opacity(opacity)
                .gradient(grad)
                .build()
            periodicProvider = prov
            periodicOverlay = map.addTileOverlay(TileOverlayOptions().tileProvider(prov))
        }
    }

    fun clearPeriodicHeatmap() {
        periodicProvider = null
        periodicOverlay?.remove()
        periodicOverlay = null
    }

    fun clearInitialHeatmap() {
        initialPoints = emptyList()
        initialProvider = null
        initialOverlay?.remove()
        initialOverlay = null
    }

    private fun selectGradientForTier(tier: Int): Gradient {
        return when (tier) {
            2 -> redGradient
            1 -> yellowGradient
            else -> greenGradient
        }
    }
}
