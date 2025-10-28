package com.doublezero.feature_mypage.uistate

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