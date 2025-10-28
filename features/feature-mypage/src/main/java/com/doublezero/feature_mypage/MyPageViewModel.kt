package com.doublezero.feature_mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// data class
data class UserProfile(
    val name: String = "",
    val photoUrl: String = ""
)

// ViewModel이 관리할 전체 UI 상태
data class MyPageUiState(
    val isLoggedIn: Boolean = false,
    val userProfile: UserProfile = UserProfile()
)

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