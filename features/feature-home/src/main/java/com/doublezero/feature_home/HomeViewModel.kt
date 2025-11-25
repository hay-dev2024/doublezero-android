package com.doublezero.feature_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doublezero.data.network.PlaceAutocompleteSuggestionDto
import com.doublezero.data.network.PlaceResponseDto
import com.doublezero.data.network.RouteDto
import com.doublezero.data.repository.NavigationRepository
import com.doublezero.data.repository.PlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = false,
    val suggestions: List<PlaceAutocompleteSuggestionDto> = emptyList(),
    val selectedOrigin: PlaceResponseDto? = null,
    val selectedDestination: PlaceResponseDto? = null,
    val routes: List<RouteDto> = emptyList(),
    val selectedRouteIndex: Int = -1,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val placesRepository: PlacesRepository,
    private val navigationRepository: NavigationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private var autocompleteJob: Job? = null

    fun onQueryChanged(input: String) {
        // debounce logic: only when >=2 chars
        autocompleteJob?.cancel()
        if (input.length < 2) {
            _uiState.value = _uiState.value.copy(suggestions = emptyList())
            return
        }
        autocompleteJob = viewModelScope.launch {
            delay(300)
            try {
                val suggestions = placesRepository.autocomplete(input)
                // take top 5
                _uiState.value = _uiState.value.copy(suggestions = suggestions.take(5))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to fetch suggestions")
            }
        }
    }

    fun selectSuggestionAsOrigin(placeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            val place = placesRepository.getPlaceDetails(placeId)
            if (place != null) {
                _uiState.value = _uiState.value.copy(selectedOrigin = place, suggestions = emptyList())
            } else {
                _uiState.value = _uiState.value.copy(error = "Failed to load place details")
            }
            _uiState.value = _uiState.value.copy(loading = false)
        }
    }

    fun selectSuggestionAsDestination(placeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            val place = placesRepository.getPlaceDetails(placeId)
            if (place != null) {
                _uiState.value = _uiState.value.copy(selectedDestination = place, suggestions = emptyList())
            } else {
                _uiState.value = _uiState.value.copy(error = "Failed to load place details")
            }
            _uiState.value = _uiState.value.copy(loading = false)
        }
    }

    fun findRoute() {
        val origin = _uiState.value.selectedOrigin
        val dest = _uiState.value.selectedDestination
        if (origin == null || dest == null) {
            _uiState.value = _uiState.value.copy(error = "Select origin and destination")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            try {
                val routes = navigationRepository.computeRoute(
                    origin.lat, origin.lon, dest.lat, dest.lon, token = null
                )
                if (routes.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(routes = routes, selectedRouteIndex = 0)
                } else {
                    _uiState.value = _uiState.value.copy(error = "No route found")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Route request failed")
            } finally {
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }

    fun selectRoute(index: Int) {
        if (index < 0 || index >= _uiState.value.routes.size) return
        _uiState.value = _uiState.value.copy(selectedRouteIndex = index)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
