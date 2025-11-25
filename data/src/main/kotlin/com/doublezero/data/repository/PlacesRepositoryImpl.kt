package com.doublezero.data.repository

import com.doublezero.data.network.PlaceSuggestionDto
import com.doublezero.data.network.PlaceDto
import com.doublezero.data.network.PlacesApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlacesRepositoryImpl @Inject constructor(): PlacesRepository {

    private val retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val api: PlacesApi by lazy { retrofit.create(PlacesApi::class.java) }

    override suspend fun searchPlaces(query: String): List<PlaceDto> {
        return try {
            val resp = api.searchPlaces(com.doublezero.data.network.PlaceSearchRequestDto(query))
            if (resp.isSuccessful) resp.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun autocomplete(input: String, lat: Double?, lon: Double?): List<PlaceSuggestionDto> {
        return try {
            val resp = api.autocomplete(input, lat, lon)
            if (resp.isSuccessful) resp.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getPlaceDetails(placeId: String): PlaceDto? {
        return try {
            val resp = api.getPlaceDetails(placeId)
            if (resp.isSuccessful) resp.body() else null
        } catch (e: Exception) {
            null
        }
    }
}
