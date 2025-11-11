package com.doublezero.core.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    fun login(name: String = "John Doe", photoUrl: String = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=400&h=400&fit=crop") {
        _isLoggedIn.value = true
        _userProfile.value = UserProfile(name = name, photoUrl = photoUrl)
    }

    fun logout() {
        _isLoggedIn.value = false
        _userProfile.value = UserProfile()
    }
}

data class UserProfile(
    val name: String = "",
    val photoUrl: String = ""
)

