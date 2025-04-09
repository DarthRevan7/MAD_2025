package com.example.voyago

import java.util.*

//val calendar = Calendar.getInstance()
//calendar.set(1992,Calendar.MARCH,12)
//User info

data class LazyTrip(
    val destination:String,
    val strDate:String
)

data class LazyArticle(
    val title:String,
    val strDate:String
)

data class LazyReview(
    val name:String,
    val surname:String,
    val rating:Float,
    val strDate:String
)

val tripList = listOf(
        LazyTrip("Marrakech", "April, 2024"),
        LazyTrip("Patagonia", "September, 2023"),
        LazyTrip("Banzai", "November, 1997"),
        LazyTrip("Turin", "April, 2025"),
        LazyTrip("Naples", "June, 1999"),
        LazyTrip("Chestnut", "February, 2005")
        )

val articleList = listOf(
    LazyArticle("Beautiful Marrakech", "April, 2024"),
    LazyArticle("Amazing Patagonia", "September, 2023"),
    LazyArticle("Cool Banzai", "November, 1997"),
    LazyArticle("Fascinating Turin", "April, 2025"),
    LazyArticle("Enchanted Naples", "June, 1999"),
    LazyArticle("Cute Chestnut", "February, 2005")
)

val reviewList = listOf(
    LazyReview("Jon", "Dan", 4.7f, "April, 2000"),
    LazyReview("John", "Snow", 4.5f, "September, 2023"),
    LazyReview("Lara", "Croft", 4.9f, "November, 1997"),
    LazyReview("Sally", "Martin", 4.6f, "April, 2025"),
    LazyReview("Harry", "Weasley", 3.5f, "June, 1999"),
    LazyReview("Rhys", "Black", 3.2f, "February, 2005"),
    LazyReview("Serious", "Black", 3.2f, "January, 2005"),
    LazyReview("Remus", "Scarface", 3.2f, "February, 2023")
)

val user = UserProfileInfo(
    id = 1,
    firstname = "Alice",
    surname = "Walkeraccia",
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