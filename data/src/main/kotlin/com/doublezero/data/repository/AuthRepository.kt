package com.doublezero.data.repository

import com.doublezero.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<Boolean>
    fun observeUserProfile(): Flow<UserProfile>
    suspend fun login(name: String, photoUrl: String)
    suspend fun logout()
}

