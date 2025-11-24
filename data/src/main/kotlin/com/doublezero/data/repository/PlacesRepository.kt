package com.doublezero.data.repository

import com.doublezero.data.network.PlaceAutocompleteSuggestionDto
import com.doublezero.data.network.PlaceResponseDto

interface PlacesRepository {
    suspend fun searchPlaces(query: String): List<PlaceResponseDto>
    suspend fun autocomplete(input: String, lat: Double? = null, lon: Double? = null): List<PlaceAutocompleteSuggestionDto>
    suspend fun getPlaceDetails(placeId: String): PlaceResponseDto?
}

