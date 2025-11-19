package com.doublezero.data.model

data class Trip(
    val id: Int,
    val date: String,
    val time: String,
    val origin: String,
    val destination: String,
    val distance: String,
    val duration: String,
    val risk: String, // "safe", "caution", "risk"
    val riskDetails: String
)

