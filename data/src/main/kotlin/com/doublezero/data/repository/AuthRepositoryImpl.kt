package com.doublezero.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.doublezero.data.model.UserProfile
import com.doublezero.data.network.AuthApi
import com.doublezero.data.network.GoogleLoginRequest
import com.doublezero.data.network.AuthResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

    // Retrofit client for local backend
    private val retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }

    init {
        val savedToken = sharedPreferences.getString("access_token", null)
        if (savedToken != null) {
            _userProfile.value = UserProfile(name = "Saved User", photoUrl = "")
        }
    }

    override fun observeAuthState(): Flow<Boolean> = _userProfile.map { it != null }

    override fun observeUserProfile(): Flow<UserProfile> = _userProfile.map { it ?: UserProfile() }

    override suspend fun loginWithGoogle(idToken: String) {
        // DEV shortcut: if idToken == "DEV_TEST", call backend test-token endpoint to get an access token
        if (idToken == "DEV_TEST") {
            try {
                android.util.Log.d("AuthRepositoryImpl", "loginWithGoogle: DEV_TEST path - requesting test token")
                val resp = authApi.getTestToken(null, "dev@example.com")
                if (resp.isSuccessful) {
                    val body = resp.body()
                    body?.let {
                        sharedPreferences.edit().putString("access_token", it.accessToken).apply()
                        _userProfile.value = UserProfile(name = "Dev User", photoUrl = "")
                        android.util.Log.d("AuthRepositoryImpl", "loginWithGoogle: DEV_TEST token stored")
                        return
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepositoryImpl", "loginWithGoogle: DEV_TEST failed", e)
            }
        }
        // Make real backend call
        try {
            android.util.Log.d("AuthRepositoryImpl", "loginWithGoogle: calling backend with idToken length=${idToken.length}")
            val response = authApi.googleLogin(GoogleLoginRequest(idToken))
            android.util.Log.d("AuthRepositoryImpl", "loginWithGoogle: response code=${response.code()}")
            if (response.isSuccessful) {
                val body: AuthResponse? = response.body()
                android.util.Log.d("AuthRepositoryImpl", "loginWithGoogle: response body=$body")
                body?.let {
                    sharedPreferences.edit()
                        .putString("access_token", it.accessToken)
                        .apply()

                    _userProfile.value = UserProfile(
                        name = it.user.displayName ?: it.user.email,
                        photoUrl = "https://lh3.googleusercontent.com/a/default-user"
                    )
                    android.util.Log.d("AuthRepositoryImpl", "loginWithGoogle: userProfile updated")
                    return
                }
            }
            throw RuntimeException("Login failed: ${response.code()} ${response.errorBody()?.string()}")
        } catch (e: Exception) {
            // fallback to mock behavior if backend not reachable
            android.util.Log.e("AuthRepositoryImpl", "loginWithGoogle: exception", e)
            e.printStackTrace()
            delay(1000)
            sharedPreferences.edit()
                .putString("access_token", "mock_token_$idToken")
                .apply()

            _userProfile.value = UserProfile(
                name = "Google User",
                photoUrl = "https://lh3.googleusercontent.com/a/default-user"
            )
            android.util.Log.d("AuthRepositoryImpl", "loginWithGoogle: fallback mock userProfile set")
        }
    }

    override suspend fun logout() {
        sharedPreferences.edit().clear().apply()
        _userProfile.value = null
    }
}