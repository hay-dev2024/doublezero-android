package com.doublezero.shared.data

import com.doublezero.data.model.Trip
import com.doublezero.data.repository.TripRepository
import com.doublezero.data.source.MockTripDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepositoryImpl @Inject constructor() : TripRepository {

    private val _trips = MutableStateFlow(MockTripDataSource.getMockTrips())

    override fun observeTrips(): Flow<List<Trip>> = _trips.asStateFlow()

    override fun getTripById(id: Int): Flow<Trip?> = _trips.map { trips ->
        trips.firstOrNull { it.id == id }
    }

    override suspend fun addTrip(trip: Trip) {
        val currentTrips = _trips.value.toMutableList()
        currentTrips.add(trip)
        _trips.value = currentTrips
    }

    override suspend fun deleteTrip(id: Int) {
        val currentTrips = _trips.value.toMutableList()
        currentTrips.removeAll { it.id == id }
        _trips.value = currentTrips
    }
}

