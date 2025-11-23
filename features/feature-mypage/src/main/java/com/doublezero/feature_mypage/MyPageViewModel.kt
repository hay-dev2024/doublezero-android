package com.doublezero.feature_mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doublezero.data.repository.AuthRepository
import com.doublezero.feature_mypage.uistate.MyPageUiState
import com.doublezero.feature_mypage.uistate.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val authRepository: AuthRepository
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
}