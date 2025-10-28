package com.doublezero.feature_mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doublezero.feature_mypage.uistate.MyPageUiState
import com.doublezero.feature_mypage.uistate.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



class MyPageViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Google 로그인 성공 시 호출 (현재는 Mock 데이터)
     */
    fun onLogin() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    userProfile = UserProfile(
                        name = "John Doe",
                        photoUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=400&h=400&fit=crop"
                    )
                )
            }
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            _uiState.update {
                // 로그인 상태와 유저 정보를 초기화
                it.copy(
                    isLoggedIn = false,
                    userProfile = UserProfile()
                )
            }
        }
    }
}