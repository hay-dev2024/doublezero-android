package com.doublezero.feature_mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doublezero.core.data.auth.AuthRepository
import com.doublezero.feature_mypage.uistate.MyPageUiState
import com.doublezero.feature_mypage.uistate.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val uiState: StateFlow<MyPageUiState> = combine(
        authRepository.isLoggedIn,
        authRepository.userProfile
    ) { isLoggedIn, profile ->
        MyPageUiState(
            isLoggedIn = isLoggedIn,
            userProfile = UserProfile(
                name = profile.name,
                photoUrl = profile.photoUrl
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MyPageUiState()
    )

    fun onLogin() {
        authRepository.login()
    }

    fun onLogout() {
        authRepository.logout()
    }
}