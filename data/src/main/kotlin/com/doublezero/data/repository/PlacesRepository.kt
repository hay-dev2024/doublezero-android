package com.doublezero.data.repository

import com.doublezero.data.network.PlaceSuggestionDto
import com.doublezero.data.network.PlaceDto

interface PlacesRepository {
    suspend fun searchPlaces(query: String): List<PlaceDto>
    suspend fun autocomplete(input: String, lat: Double? = null, lon: Double? = null): List<PlaceSuggestionDto>
    suspend fun getPlaceDetails(placeId: String): PlaceDto?
}
