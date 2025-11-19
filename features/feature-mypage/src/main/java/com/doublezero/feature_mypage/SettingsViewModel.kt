package com.doublezero.feature_mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doublezero.shared.model.SpeedUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


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
        viewModelScope.launch {
            _uiState.update { it.copy(showSavedConfirmation = true) }
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(showSavedConfirmation = false) }
        }
    }
}