package com.doublezero.feature_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.doublezero.data.repository.NavigationRepository
import com.doublezero.data.repository.PlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

data class HomeUiState(
    // Use the actual DTO types returned by the data module APIs
    val suggestions: List<com.doublezero.data.network.PlaceAutocompleteSuggestionDto> = emptyList(),
    val selectedOrigin: com.doublezero.data.network.PlaceResponseDto? = null,
    val selectedDestination: com.doublezero.data.network.PlaceResponseDto? = null,
    val routes: List<com.doublezero.data.network.RouteDto> = emptyList(),
    val selectedRouteIndex: Int = -1,
    val error: String? = null,
    // Simulation State
    val isSimulating: Boolean = false,
    val simPosition: LatLng? = null,
    val simStepIndex: Int = -1
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val placesRepository: PlacesRepository,
    private val navigationRepository: NavigationRepository
) : ViewModel() {

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

    fun startSimulation() {
        simulationJob?.cancel() // Cancel any previous simulation
        val route = _uiState.value.routes.getOrNull(_uiState.value.selectedRouteIndex)
        if (route == null) return

        // handle nullable polyline safely
        val polyline = route.polyline ?: return
        if (polyline.isBlank()) return

        val pathPoints = try {
            PolyUtil.decode(polyline)
        } catch (e: Exception) {
            emptyList()
        }
        if (pathPoints.isEmpty()) return

        _uiState.update { it.copy(isSimulating = true, simStepIndex = 0) }

        simulationJob = viewModelScope.launch {
            // The total duration is taken from the route data, with a fallback (minutes -> ms)
            val totalDurationMinutes = route.duration?.split(" ")?.firstOrNull()?.toLongOrNull() ?: 60L
            val totalDurationMs = totalDurationMinutes * 60 * 1000
            val startTime = System.currentTimeMillis()

            while (_uiState.value.isSimulating) {
                val elapsedTime = System.currentTimeMillis() - startTime
                val fraction = (elapsedTime.toFloat() / totalDurationMs).coerceIn(0f, 1f)

                if (fraction >= 1f) {
                    _uiState.update { it.copy(simPosition = pathPoints.last()) }
                    stopSimulation()
                    break
                }

                // Get the precise point on the path for the current fraction of time
                val currentPos = getPointAtFraction(pathPoints, fraction)
                // Find which step of the directions corresponds to the current position
                val currentStepIndex = findStepIndexForPosition(route.steps, pathPoints, currentPos)

                _uiState.update {
                    it.copy(
                        simPosition = currentPos,
                        simStepIndex = currentStepIndex
                    )
                }
                delay(100) // Update every 100ms for smooth animation
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
