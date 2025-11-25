package com.doublezero.data.network

import kotlinx.serialization.Serializable

@Serializable
data class PlaceSuggestionDto(
    val placeId: String,
    val description: String,
    val mainText: String,
    val secondaryText: String
)

