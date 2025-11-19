package com.doublezero.shared.data

import com.doublezero.data.model.UserProfile
import com.doublezero.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor() : AuthRepository {

    private val _isLoggedIn = MutableStateFlow(false)
    private val _userProfile = MutableStateFlow(UserProfile())

    override fun observeAuthState(): Flow<Boolean> = _isLoggedIn.asStateFlow()

    override fun observeUserProfile(): Flow<UserProfile> = _userProfile.asStateFlow()

    override suspend fun login(name: String, photoUrl: String) {
        _isLoggedIn.value = true
        _userProfile.value = UserProfile(name = name, photoUrl = photoUrl)
    }

    override suspend fun logout() {
        _isLoggedIn.value = false
        _userProfile.value = UserProfile()
    }
}

