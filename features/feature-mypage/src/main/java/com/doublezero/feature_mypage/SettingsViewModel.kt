package com.doublezero.feature_mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// speedUnit 상태 ('kmh' | 'mph')
enum class SpeedUnit(val displayName: String) {
    KMH("Kilometers per hour (km/h)"),
    MPH("Miles per hour (mph)")
}

// SettingsScreen의 전체 UI 상태
data class SettingsUiState(
    val ttsEnabled: Boolean = true,
    val riskDisplayEnabled: Boolean = true,
    val selectedSpeedUnit: SpeedUnit = SpeedUnit.KMH,
    val showSavedConfirmation: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    fun setTtsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(ttsEnabled = enabled) }
    }

    fun setRiskDisplayEnabled(enabled: Boolean) {
        _uiState.update { it.copy(riskDisplayEnabled = enabled) }
    }

    fun setSelectedSpeedUnit(unit: SpeedUnit) {
        _uiState.update { it.copy(selectedSpeedUnit = unit) }
    }

    fun saveSettings() {
        // TODO: 실제 저장 로직 구현 (e.g., DataStore, SharedPreferences)
        // 저장이 완료되면 확인 메시지 표시
        viewModelScope.launch {
            _uiState.update { it.copy(showSavedConfirmation = true) }
            // 일정 시간 후 메시지 숨김
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(showSavedConfirmation = false) }
        }
    }
}