package com.doublezero.feature_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.doublezero.data.repository.NavigationRepository
import com.doublezero.data.repository.PlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val placesRepository: PlacesRepository,
    private val navigationRepository: NavigationRepository
) : ViewModel() {

    data class HomeUiState(
        // Use the actual DTO types returned by the data module APIs
        val suggestions: List<com.doublezero.data.network.PlaceSuggestionDto> = emptyList(),
        val selectedOrigin: com.doublezero.data.network.PlaceDto? = null,
        val selectedDestination: com.doublezero.data.network.PlaceDto? = null,
        val routes: List<com.doublezero.data.network.RouteDto> = emptyList(),
        val selectedRouteIndex: Int = -1,
        val error: String? = null,
        // Simulation State
        val isSimulating: Boolean = false,
        val simPosition: LatLng? = null,
        val simStepIndex: Int = -1
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    private var simulationJob: Job? = null

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(300)
                .filter { it.length >= 2 }
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank()) {
                        fetchSuggestions(query)
                    } else {
                        _uiState.update { it.copy(suggestions = emptyList()) }
                    }
                }
        }
    }

    fun onQueryChanged(query: String) {
        queryFlow.value = query
    }

    private fun fetchSuggestions(query: String) {
        viewModelScope.launch {
            try {
                val result = placesRepository.autocomplete(query)
                _uiState.update { it.copy(suggestions = result.take(5)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to fetch suggestions: ${e.message}") }
            }
        }
    }

    fun selectSuggestionAsOrigin(placeId: String) {
        viewModelScope.launch {
            try {
                val place = placesRepository.getPlaceDetails(placeId)
                _uiState.update { it.copy(selectedOrigin = place, suggestions = emptyList()) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to get origin details: ${e.message}") }
            }
        }
    }

    fun selectSuggestionAsDestination(placeId: String) {
        viewModelScope.launch {
            try {
                val place = placesRepository.getPlaceDetails(placeId)
                _uiState.update { it.copy(selectedDestination = place, suggestions = emptyList()) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to get destination details: ${e.message}") }
            }
        }
    }

    fun findRoute() {
        val origin = _uiState.value.selectedOrigin
        val destination = _uiState.value.selectedDestination

        if (origin == null || destination == null) {
            _uiState.update { it.copy(error = "Origin and destination must be selected.") }
            return
        }

        viewModelScope.launch {
            try {
                // Use NavigationRepository.computeRoute which returns a List<RouteDto>
                val result = navigationRepository.computeRoute(
                    originLat = origin.lat,
                    originLng = origin.lon,
                    destLat = destination.lat,
                    destLng = destination.lon,
                    token = null
                )

                _uiState.update {
                    it.copy(
                        routes = result,
                        selectedRouteIndex = if (result.isNotEmpty()) 0 else -1
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to find route: ${e.message}") }
            }
        }
    }

    fun selectRoute(index: Int) {
        _uiState.update { it.copy(selectedRouteIndex = index) }
    }

    /**
     * Reset the search/route state so user can start a new origin/destination entry.
     * UI label suggestion: "New Search"
     */
    fun startNewSearch() {
        // cancel any running simulation
        simulationJob?.cancel()

        _uiState.update {
            it.copy(
                suggestions = emptyList(),
                selectedOrigin = null,
                selectedDestination = null,
                routes = emptyList(),
                selectedRouteIndex = -1,
                error = null,
                isSimulating = false,
                simPosition = null,
                simStepIndex = -1
            )
        }
    }

    /**
     * Start simulation using per-step durations when available.
     * - speedMultiplier: multiplies real-world speed (higher => faster simulation)
     * - maneuverPauseMs: pause duration at steps with maneuvers (in ms)
     * - updateIntervalMs: how often to update simPosition (in ms)
     */
    fun startSimulation(
        speedMultiplier: Float = 15.5f,
        maneuverPauseMs: Long = 1000L,
        updateIntervalMs: Long = 50L
    ) {
        simulationJob?.cancel() // Cancel any previous simulation
        val route = _uiState.value.routes.getOrNull(_uiState.value.selectedRouteIndex)
        if (route == null) return

        // If steps are present and have per-step info, prefer step-wise simulation
        val steps = route.steps

        simulationJob = viewModelScope.launch {
            _uiState.update { it.copy(isSimulating = true, simStepIndex = 0) }

            try {
                if (!steps.isNullOrEmpty()) {
                    // Step-wise simulation
                    for ((stepIndex, step) in steps.withIndex()) {
                        // decode step polyline if available, else fallback to empty list
                        val stepPoints = try { PolyUtil.decode(step.polyline ?: "") } catch (e: Exception) { emptyList() }
                        if (stepPoints.isEmpty()) {
                            // no segment points; continue
                            _uiState.update { it.copy(simStepIndex = stepIndex) }
                            continue
                        }

                        // determine step duration in ms
                        val stepDurationSeconds = step.durationSeconds ?: run {
                            // try parsing string duration like "4 min"
                            step.duration?.split(" ")?.firstOrNull()?.toLongOrNull()
                        } ?: 60L
                        val effectiveMs = (stepDurationSeconds * 1000L / speedMultiplier).toLong().coerceAtLeast(updateIntervalMs)

                        // precompute cumulative distances for interpolation within this step
                        val pts = stepPoints.map { LatLng(it.latitude, it.longitude) }
                        val segmentDistances = pts.zipWithNext { a, b -> distanceBetweenMeters(a, b) }
                        val totalStepDistance = segmentDistances.sum().coerceAtLeast(1.0)

                        val startTime = System.currentTimeMillis()
                        while (true) {
                            val elapsed = System.currentTimeMillis() - startTime
                            val frac = (elapsed.toDouble() / effectiveMs).coerceIn(0.0, 1.0)

                            // compute position along step based on distance fraction
                            val targetDistance = totalStepDistance * frac
                            var acc = 0.0
                            var pos: LatLng = pts.first()
                            for (i in 0 until pts.size - 1) {
                                val segDist = segmentDistances.getOrNull(i) ?: 0.0
                                if (acc + segDist >= targetDistance) {
                                    val segFrac = if (segDist <= 0.0) 0.0 else (targetDistance - acc) / segDist
                                    val s = pts[i]
                                    val e = pts[i + 1]
                                    pos = LatLng(
                                        s.latitude + (e.latitude - s.latitude) * segFrac,
                                        s.longitude + (e.longitude - s.longitude) * segFrac
                                    )
                                    break
                                }
                                acc += segDist
                            }

                            _uiState.update {
                                it.copy(
                                    simPosition = pos,
                                    simStepIndex = stepIndex
                                )
                            }

                            if (frac >= 1.0) break
                            delay(updateIntervalMs)
                        }

                        // after finishing step, if maneuver exists, brief pause to simulate signal/turn
                        val maneuver = step.maneuver?.uppercase()
                        if (maneuver != null && maneuver.isNotBlank()) {
                            // choose short pause durations for turn-like maneuvers
                            val turnSet = setOf("TURN_LEFT", "TURN_RIGHT", "UTURN", "DEPART")
                            if (maneuver in turnSet) {
                                delay(maneuverPauseMs)
                            }
                        }

                        // loop continues to next step
                    }

                    // simulation finished
                    _uiState.update { it.copy(isSimulating = false, simStepIndex = -1) }

                } else {
                    // Fallback: simulate along whole polyline using existing logic
                    val polyline = route.polyline ?: ""
                    val pathPoints = try { PolyUtil.decode(polyline) } catch (e: Exception) { emptyList() }
                    if (pathPoints.isEmpty()) {
                        _uiState.update { it.copy(isSimulating = false, simStepIndex = -1) }
                        return@launch
                    }

                    // The total duration is taken from the route data, with a fallback (minutes -> ms)
                    val totalDurationMinutes = route.duration?.split(" ")?.firstOrNull()?.toLongOrNull() ?: 60L
                    val totalDurationMs = totalDurationMinutes * 60 * 1000
                    val effectiveTotalDurationMs = (totalDurationMs / speedMultiplier).toLong()
                    val startTime = System.currentTimeMillis()

                    while (_uiState.value.isSimulating) {
                        val elapsedTime = System.currentTimeMillis() - startTime
                        val fraction = (elapsedTime.toFloat() / effectiveTotalDurationMs).coerceIn(0f, 1f)

                        if (fraction >= 1f) {
                            _uiState.update { it.copy(simPosition = pathPoints.last()) }
                            break
                        }

                        val currentPos = getPointAtFraction(pathPoints, fraction)
                        val currentStepIndex = findStepIndexForPosition(route.steps, pathPoints, currentPos)

                        _uiState.update {
                            it.copy(
                                simPosition = currentPos,
                                simStepIndex = currentStepIndex
                            )
                        }
                        delay(updateIntervalMs)
                    }

                    stopSimulation()
                }
            } catch (e: Exception) {
                // ensure we reset sim state on failure
                _uiState.update { it.copy(isSimulating = false, simPosition = null, simStepIndex = -1) }
            }
        }
    }

    fun stopSimulation() {
        simulationJob?.cancel()
        _uiState.update {
            it.copy(
                isSimulating = false,
                simPosition = null,
                simStepIndex = -1
            )
        }
    }

    /**
     * Calculates the LatLng of a point at a given fraction (0.0 to 1.0) along a path.
     * This version is more accurate as it's based on distance.
     */
    private fun getPointAtFraction(points: List<LatLng>, fraction: Float): LatLng {
        if (points.isEmpty()) return LatLng(0.0, 0.0)
        if (points.size == 1) return points[0]

        // 1. Calculate total distance of the polyline
        val totalDistance = points.zipWithNext { a, b -> distanceBetweenMeters(a, b) }.sum()
        val distanceToTravel = totalDistance * fraction

        // 2. Find the segment where the target distance falls
        var traveled = 0.0
        for (i in 0 until points.size - 1) {
            val start = points[i]
            val end = points[i + 1]
            val segmentDistance = distanceBetweenMeters(start, end)

            if (traveled + segmentDistance >= distanceToTravel) {
                // 3. Interpolate within that segment
                val segmentFraction = (distanceToTravel - traveled) / segmentDistance
                return LatLng(
                    start.latitude + (end.latitude - start.latitude) * segmentFraction,
                    start.longitude + (end.longitude - start.longitude) * segmentFraction
                )
            }
            traveled += segmentDistance
        }

        // Fallback to the last point if something goes wrong
        return points.last()
    }

    /**
     * Finds the index of the current navigation step based on the vehicle's position.
     */
    private fun findStepIndexForPosition(steps: List<com.doublezero.data.network.StepDto>?, allPathPoints: List<LatLng>, currentPosition: LatLng): Int {
        if (steps.isNullOrEmpty()) return 0

        // Find the point on the overall path that is closest to our current simulated position
        val closestPointIndexOnPath = allPathPoints.indices.minByOrNull {
            distanceBetweenMeters(currentPosition, allPathPoints[it])
        } ?: 0

        // Figure out which step that closest point belongs to
        var pointCounter = 0
        for (stepIndex in steps.indices) {
            val stepPoints = try {
                PolyUtil.decode(steps[stepIndex].polyline ?: "")
            } catch (e: Exception) {
                emptyList()
            }
            pointCounter += stepPoints.size
            if (closestPointIndexOnPath < pointCounter) {
                return stepIndex
            }
        }
        return steps.size - 1 // Default to the last step
    }
}

/**
 * Helper to calculate distance in meters between two LatLng points.
 */
private fun distanceBetweenMeters(start: LatLng, end: LatLng): Double {
    val r = 6371000.0 // Earth radius in meters
    val dLat = Math.toRadians(end.latitude - start.latitude)
    val dLon = Math.toRadians(end.longitude - start.longitude)
    val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(start.latitude)) * cos(Math.toRadians(end.latitude)) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}
