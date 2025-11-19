package com.doublezero.feature_mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doublezero.data.model.Trip
import com.doublezero.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val trips: List<Trip> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = tripRepository.observeTrips()
        .map { trips ->
            HistoryUiState(trips = trips)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState(isLoading = true)
        )

    fun addSampleTrip() {
        viewModelScope.launch {
            val newTrip = Trip(
                id = (uiState.value.trips.maxOfOrNull { it.id } ?: 0) + 1,
                date = "2025-01-${(10..30).random()}",
                time = "${(8..20).random()}:${(0..59).random().toString().padStart(2, '0')}",
                origin = "테스트 출발지",
                destination = "테스트 도착지",
                distance = "${(5..50).random()}.${(0..9).random()}km",
                duration = "${(10..90).random()}분",
                risk = listOf("safe", "caution", "risk").random(),
                riskDetails = "샘플 테스트 데이터"
            )
            tripRepository.addTrip(newTrip)
        }
    }

    fun deleteTrip(id: Int) {
        viewModelScope.launch {
            tripRepository.deleteTrip(id)
        }
    }
}

