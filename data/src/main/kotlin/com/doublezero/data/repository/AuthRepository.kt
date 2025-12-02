package com.doublezero.data.repository

import com.doublezero.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<Boolean>
    fun observeUserProfile(): Flow<UserProfile>

    suspend fun loginWithGoogle(idToken: String)

    suspend fun logout()

    /**
     * Get current access token (for backend API calls like navigation session)
     * Returns null if no token available
     */
    suspend fun getAccessToken(): String?
}