package com.doublezero.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.doublezero.data.model.HistoryItem
import com.doublezero.data.network.CreateHistoryDto
import com.doublezero.data.network.HistoryApi
import com.doublezero.data.network.HistoryDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : HistoryRepository {

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

    private val TAG = "HistoryRepositoryImpl"

    private val retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .callTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val historyApi: HistoryApi by lazy { retrofit.create(HistoryApi::class.java) }

    override suspend fun saveHistory(request: CreateHistoryDto): Result<HistoryDto> =
        withContext(Dispatchers.IO) {
            try {
                val token = sharedPreferences.getString("access_token", null)
                if (token.isNullOrBlank()) {
                    Log.e(TAG, "saveHistory: No token available")
                    return@withContext Result.failure(Exception("No authentication token"))
                }

                Log.d(TAG, "saveHistory: calling backend API")
                val response = historyApi.saveHistory(request, "Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "saveHistory: success")
                    Result.success(response.body()!!)
                } else {
                    Log.e(TAG, "saveHistory: failed with code ${response.code()}")
                    Result.failure(Exception("Failed to save history: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "saveHistory: exception", e)
                Result.failure(e)
            }
        }

    override suspend fun getHistory(limit: Int): Result<List<HistoryItem>> =
        withContext(Dispatchers.IO) {
            try {
                val token = sharedPreferences.getString("access_token", null)
                if (token.isNullOrBlank()) {
                    Log.e(TAG, "getHistory: No token available")
                    return@withContext Result.failure(Exception("No authentication token"))
                }

                Log.d(TAG, "getHistory: calling backend API with limit=$limit")
                val response = historyApi.getHistory(limit, "Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    val historyList = response.body()!!.map { dto ->
                        HistoryItem(
                            id = dto.id,
                            originLat = dto.origin.lat,
                            originLon = dto.origin.lon,
                            destinationLat = dto.destination.lat,
                            destinationLon = dto.destination.lon,
                            distanceMeters = dto.distanceMeters,
                            durationSeconds = dto.durationSeconds,
                            summary = dto.summary,
                            riskLevel = dto.riskSummary?.level,
                            startedAt = dto.startedAt,
                            endedAt = dto.endedAt,
                            createdAt = dto.createdAt,
                            originName = dto.originName,  // ✅ Use place name from backend
                            destinationName = dto.destinationName  // ✅ Use place name from backend
                        )
                    }
                    Log.d(TAG, "getHistory: success, ${historyList.size} items")
                    Result.success(historyList)
                } else {
                    Log.e(TAG, "getHistory: failed with code ${response.code()}")
                    Result.failure(Exception("Failed to get history: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "getHistory: exception", e)
                Result.failure(e)
            }
        }
}

