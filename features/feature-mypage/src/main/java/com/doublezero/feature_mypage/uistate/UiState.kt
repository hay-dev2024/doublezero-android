package com.doublezero.feature_mypage.uistate

data class UserProfile(
    val name: String = "",
    val photoUrl: String = ""
)

data class MyPageUiState(
    val isLoggedIn: Boolean = false,
    val userProfile: UserProfile = UserProfile()
)