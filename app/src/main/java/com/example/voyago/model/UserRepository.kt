package com.example.voyago.model

import java.util.Calendar
import java.util.GregorianCalendar

class UserRepository {
    fun fetchUserData(myProfile: Boolean) : UserData
    {
        var loggedUser = UserData(
            id = 1,
            firstname = "Alice",
            surname = "Walker",
            username = "alice_w",
            dateOfBirth = GregorianCalendar(1995, Calendar.MARCH, 12),
            country = "USA",
            email = "alice@example.com",
            password = "securePassword123",
            userDescription = "hi",
            profilePicture = null,
            typeTravel = listOf(TypeTravel.CULTURE, TypeTravel.ADVENTURE),
            desiredDestination = listOf("Greece", "Italy", "Japan"),
            rating = 4.7f,
            reliability = 90,
            publicTrips = emptyList(),
            articles = emptyList(),
            reviews = emptyList(),
            privateTrips = emptyList(),
            tripsAppliedTo = emptyList(),
            tripsApplicationAccepted = emptyList()
        )

        var user = UserData(
            id = 1,
            firstname = "Bella",
            surname = "Estrange",
            username = "beauty_lest",
            dateOfBirth = GregorianCalendar(1985, Calendar.OCTOBER, 31),
            country = "UK",
            email = "bellalast@example.com",
            password = "securePassword987",
            userDescription = "hi",
            profilePicture = null,
            typeTravel = listOf(TypeTravel.RELAX, TypeTravel.PARTY),
            desiredDestination = listOf("Romania", "USA", "South Korea"),
            rating = 4.3f,
            reliability = 55,
            publicTrips = emptyList(),
            articles = emptyList(),
            reviews = emptyList(),
            privateTrips = emptyList(),
            tripsAppliedTo = emptyList(),
            tripsApplicationAccepted = emptyList()
        )

        return if(myProfile) {
            loggedUser
        } else {
            user
        }
    }
}