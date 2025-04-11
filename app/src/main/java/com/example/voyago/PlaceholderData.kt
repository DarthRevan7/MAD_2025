package com.example.voyago

import android.media.Image
import com.example.voyago.model.TypeTravel
import java.util.*

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
    publicTrips = emptyList(),
    articles = emptyList(),
    reviews = emptyList(),
    privateTrips = emptyList(),
    tripsAppliedTo = emptyList()

)
data class UserProfileInfo(
    val id: Int,
    var firstname: String,
    var surname: String,
    var username: String,
    var dateOfBirth: Calendar,
    var country: String,
    var email: String,
    var password: String,
    var profilePicture: Image?,
    var typeTravel: List<TypeTravel>,
    var desiredDestination: List<String>,
    var rating: Float,
    var reliability: Int,
    var publicTrips: List<Int>,         //Trip id
    var articles: List<Int>,           //Article id
    var reviews: List<Int>,             //Reviews id
    var privateTrips: List<Int>,         //Trip id
    var tripsAppliedTo: List<Int>       //Trip id
)