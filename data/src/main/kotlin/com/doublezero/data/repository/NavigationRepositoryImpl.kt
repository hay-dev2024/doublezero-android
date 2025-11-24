package com.doublezero.data.repository

import com.doublezero.data.network.LatLngDto
import com.doublezero.data.network.NavigationApi
import com.doublezero.data.network.RouteRequestDto
import com.doublezero.data.network.RouteDto
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationRepositoryImpl @Inject constructor() : NavigationRepository {

    // Create a Retrofit instance for local backend (matching AuthRepositoryImpl pattern)
    private val retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val navigationApi: NavigationApi by lazy { retrofit.create(NavigationApi::class.java) }

    override suspend fun computeRoute(originLat: Double, originLng: Double, destLat: Double, destLng: Double, token: String?): RouteDto? {
        val req = RouteRequestDto(
            origin = LatLngDto(originLat, originLng),
            destination = LatLngDto(destLat, destLng)
        )

        return try {
            val authHeader = token?.let { "Bearer $it" }
            val resp = navigationApi.computeRoute(req, authHeader)
            if (resp.isSuccessful) {
                resp.body()?.routes?.firstOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            // network error or parsing error -> return null so caller can handle fallback
            null
        }
    }
}
