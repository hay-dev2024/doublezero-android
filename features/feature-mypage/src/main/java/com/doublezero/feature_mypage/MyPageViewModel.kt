package com.doublezero.feature_mypage

import android.app.Application
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doublezero.data.repository.AuthRepository
import com.doublezero.feature_mypage.uistate.MyPageUiState
import com.doublezero.feature_mypage.uistate.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val historyRepository: com.doublezero.data.repository.HistoryRepository,
    private val application: Application
) : ViewModel() {

    private val TAG = "MyPageViewModel"

    val uiState: StateFlow<MyPageUiState> = combine(
        authRepository.observeAuthState(),
        authRepository.observeUserProfile()
    ) { isLoggedIn, profile ->
        MyPageUiState(
            isLoggedIn = isLoggedIn,
            userProfile = if (isLoggedIn) {
                UserProfile(
                    name = profile.name,
                    email = profile.email,
                    photoUrl = profile.photoUrl
                )
            } else {
                UserProfile()
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MyPageUiState()
    )

    private val _historyList = kotlinx.coroutines.flow.MutableStateFlow<List<com.doublezero.data.model.HistoryItem>>(emptyList())
    val historyList: StateFlow<List<com.doublezero.data.model.HistoryItem>> = _historyList

    private val _isLoadingHistory = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isLoadingHistory: StateFlow<Boolean> = _isLoadingHistory

    fun onGoogleLoginSuccess(idToken: String) {
        Log.d(TAG, "onGoogleLoginSuccess: received idToken length=${idToken.length}")
        viewModelScope.launch {
            try {
                authRepository.loginWithGoogle(idToken)
                Log.d(TAG, "onGoogleLoginSuccess: loginWithGoogle returned")
            } catch (e: Exception) {
                Log.e(TAG, "onGoogleLoginSuccess: login failed", e)
                e.printStackTrace()
            }
        }
    }

    fun onDevLogin() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "onDevLogin: invoking DEV_TEST login")
                authRepository.loginWithGoogle("DEV_TEST")
                Log.d(TAG, "onDevLogin: DEV_TEST login completed")
            } catch (e: Exception) {
                Log.e(TAG, "onDevLogin: DEV_TEST failed", e)
            }
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun loadHistory(limit: Int = 20) {
        viewModelScope.launch {
            _isLoadingHistory.value = true
            try {
                Log.d(TAG, "loadHistory: fetching history with limit=$limit")
                val result = historyRepository.getHistory(limit)
                result.onSuccess { items ->
                    Log.d(TAG, "loadHistory: success, ${items.size} items")

                    // Convert lat/lon to location names using Geocoder
                    val itemsWithNames = items.map { item ->
                        item.apply {
                            originName = getLocationName(originLat, originLon)
                            destinationName = getLocationName(destinationLat, destinationLon)
                        }
                    }

                    _historyList.value = itemsWithNames
                }.onFailure { error ->
                    Log.e(TAG, "loadHistory: failed", error)
                    _historyList.value = emptyList()
                }
            } finally {
                _isLoadingHistory.value = false
            }
        }
    }

    private suspend fun getLocationName(lat: Double, lon: Double): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(application, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                // Try to get the most specific location name
                address.locality ?: // City name (e.g., "Manhattan")
                address.subAdminArea ?: // County/District
                address.adminArea ?: // State/Province
                address.countryName ?: // Country
                formatLatLon(lat, lon) // Fallback to coordinates
            } else {
                formatLatLon(lat, lon)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getLocationName: failed for ($lat, $lon)", e)
            formatLatLon(lat, lon)
        }
    }

    private fun formatLatLon(lat: Double, lon: Double): String {
        return "%.4f, %.4f".format(lat, lon)
    }
}