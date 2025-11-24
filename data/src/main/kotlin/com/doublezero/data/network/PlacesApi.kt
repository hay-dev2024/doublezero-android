package com.doublezero.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Places DTOs
data class PlaceSearchRequestDto(val query: String)

data class PlaceResponseDto(
    val placeId: String,
    val name: String,
    val formattedAddress: String,
    val lat: Double,
    val lon: Double,
    val types: List<String>
)

data class PlaceAutocompleteSuggestionDto(
    val placeId: String,
    val description: String,
    val mainText: String,
    val secondaryText: String
)

interface PlacesApi {
    @POST("/places/search")
    suspend fun searchPlaces(@Body dto: PlaceSearchRequestDto): Response<List<PlaceResponseDto>>

    @GET("/places/autocomplete")
    suspend fun autocomplete(
        @Query("input") input: String,
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null
    ): Response<List<PlaceAutocompleteSuggestionDto>>

    @GET("/places/{placeId}")
    suspend fun getPlaceDetails(@Path("placeId") placeId: String): Response<PlaceResponseDto>
}

