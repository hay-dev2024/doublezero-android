package com.doublezero.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.doublezero.data.model.UserProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AuthRepository {

    private val sharedPreferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secret_shared_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _userProfile = MutableStateFlow<UserProfile?>(null)

    init {
        val savedToken = sharedPreferences.getString("access_token", null)
        if (savedToken != null) {
            _userProfile.value = UserProfile(name = "Saved User", photoUrl = "")
        }
    }

    override fun observeAuthState(): Flow<Boolean> = _userProfile.map { it != null }

    override fun observeUserProfile(): Flow<UserProfile> = _userProfile.map { it ?: UserProfile() }

    override suspend fun loginWithGoogle(idToken: String) {
        delay(1000)

        sharedPreferences.edit()
            .putString("access_token", "mock_token_$idToken")
            .apply()

        _userProfile.value = UserProfile(
            name = "Google User",
            photoUrl = "https://lh3.googleusercontent.com/a/default-user"
        )
    }

    override suspend fun logout() {
        sharedPreferences.edit().clear().apply()
        _userProfile.value = null
    }
}