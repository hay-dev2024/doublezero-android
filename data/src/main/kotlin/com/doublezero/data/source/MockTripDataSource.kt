package com.doublezero.data.source

import com.doublezero.data.model.Trip

object MockTripDataSource {

    fun getMockTrips(): List<Trip> = listOf(
        Trip(
            id = 1,
            date = "2025-10-22",
            time = "14:30",
            origin = "Seoul Station",
            destination = "Gangnam Office",
            distance = "12.5 km",
            duration = "25 min",
            risk = "safe",
            riskDetails = "Clear weather, low traffic. No accidents reported on route."
        ),
        Trip(
            id = 2,
            date = "2025-10-21",
            time = "09:15",
            origin = "Home",
            destination = "Yeouido Park",
            distance = "8.3 km",
            duration = "18 min",
            risk = "caution",
            riskDetails = "Light rain conditions. Moderate traffic at intersections."
        ),
        Trip(
            id = 3,
            date = "2025-10-20",
            time = "18:45",
            origin = "Hongdae",
            destination = "Incheon Airport",
            distance = "45.2 km",
            duration = "1 hr 5 min",
            risk = "safe",
            riskDetails = "Highway route. Clear conditions throughout journey."
        ),
        Trip(
            id = 4,
            date = "2025-10-19",
            time = "22:00",
            origin = "Itaewon",
            destination = "Bundang",
            distance = "28.7 km",
            duration = "42 min",
            risk = "risk",
            riskDetails = "Night driving. Heavy rain and reduced visibility reported."
        )
    )
}

