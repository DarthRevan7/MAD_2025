package com.example.voyago

import java.util.*

//val calendar = Calendar.getInstance()
//calendar.set(1992,Calendar.MARCH,12)
//User info
val user = UserProfileInfo(
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