package com.doublezero.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Places DTOs
data class PlaceSearchRequestDto(val query: String)

// Use the shared PlaceDto (defined in java source) for responses
// (avoid duplicate Response DTO class)

interface PlacesApi {
    @POST("/places/search")
    suspend fun searchPlaces(@Body dto: PlaceSearchRequestDto): Response<List<com.doublezero.data.network.PlaceDto>>

    @GET("/places/autocomplete")
    suspend fun autocomplete(
        @Query("input") input: String,
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null
    ): Response<List<com.doublezero.data.network.PlaceSuggestionDto>>

    @GET("/places/{placeId}")
    suspend fun getPlaceDetails(@Path("placeId") placeId: String): Response<com.doublezero.data.network.PlaceDto>
}
