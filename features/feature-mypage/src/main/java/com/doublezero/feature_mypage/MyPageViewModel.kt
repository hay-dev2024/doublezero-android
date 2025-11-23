package com.doublezero.feature_mypage

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
        viewModelScope.launch {
            try {
                authRepository.loginWithGoogle(idToken)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}