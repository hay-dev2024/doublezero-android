package com.doublezero.data.repository

import com.doublezero.data.model.Trip
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    fun observeTrips(): Flow<List<Trip>>
    fun getTripById(id: Int): Flow<Trip?>
    suspend fun addTrip(trip: Trip)
    suspend fun deleteTrip(id: Int)
}

