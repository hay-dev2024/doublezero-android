package com.doublezero.data.network

import kotlinx.serialization.Serializable

@Serializable
data class PlaceDto(
    val placeId: String,
    val name: String,
    val formattedAddress: String,
    val lat: Double,
    val lon: Double,
    val types: List<String>
)

