package com.example.voyago.model

import com.example.voyago.TypeTravel
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.GregorianCalendar

class UserRepository {
    suspend fun fetchUserData() : UserData
    {
        delay(1000)
        return UserData(
            id = 1,
            firstname = "Alice",
            surname = "Walker",
            username = "alice_w",
            dateOfBirth = GregorianCalendar(1995, Calendar.MARCH, 12),
            country = "USA",
            email = "alice@example.com",
            password = "securePassword123",
            profilePicture = null,
            typeTravel = listOf(TypeTravel.CULTURE, TypeTravel.ADVENTURE),
            desiredDestination = listOf("Greece", "Italy", "Japan"),
            rating = 4.7f,
            reliability = 90,
            publicTrips = null,
            articles = null,
            reviews = null,
            privateTrips = null,
            tripsAppliedTo = null
        )
    }
}