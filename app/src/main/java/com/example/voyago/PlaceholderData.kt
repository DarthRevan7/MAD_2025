package com.example.voyago

import android.media.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.voyago.activities.ProfilePhoto
import com.example.voyago.model.*
import java.util.*
import kotlin.Nothing

/*
val sampleTrips = listOf(
    Trip(
        id = 1,
        photo = R.drawable.paris,
        title = "Romantic Getaway in Paris",
        destination = "Paris, France",
        startDate = Calendar.getInstance().apply { set(2025, 5, 10) },
        endDate = Calendar.getInstance().apply { set(2025, 5, 17) },
        estimatedPrice = 1200.0,
        groupSize = 4,
        participants = listOf(1, 2),
        activities = mapOf(
            Date(125, 5, 10) to 1,
            Date(125, 5, 12) to 2
        ),
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.CULTURE, TypeTravel.RELAX),
        creatorId = 1,
        appliedUsers = listOf(3, 4),
        published = true
    ),
    Trip(
        id = 2,
        photo = R.drawable.tokyo,
        title = "Tokyo Urban Adventure",
        destination = "Tokyo, Japan",
        startDate = Calendar.getInstance().apply { set(2025, 7, 5) },
        endDate = Calendar.getInstance().apply { set(2025, 7, 15) },
        estimatedPrice = 2200.0,
        groupSize = 5,
        participants = listOf(5, 6, 7),
        activities = mapOf(
            Date(125, 7, 6) to 1
        ),
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.ADVENTURE, TypeTravel.CULTURE),
        creatorId = 2,
        appliedUsers = listOf(8, 9),
        published = true
    ),
    Trip(
        id = 3,
        photo = R.drawable.bali,
        title = "Bali Beach Escape",
        destination = "Bali, Indonesia",
        startDate = Calendar.getInstance().apply { set(2025, 8, 1) },
        endDate = Calendar.getInstance().apply { set(2025, 8, 10) },
        estimatedPrice = 1500.0,
        groupSize = 6,
        participants = listOf(10, 11),
        activities = mapOf(
            Date(125, 8, 2) to 3
        ),
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.RELAX),
        creatorId = 3,
        appliedUsers = listOf(12, 13),
        published = true
    ),
    Trip(
        id = 4,
        photo = R.drawable.rome_photo,
        title = "Historic Rome Tour",
        destination = "Rome, Italy",
        startDate = Calendar.getInstance().apply { set(2025, 9, 15) },
        endDate = Calendar.getInstance().apply { set(2025, 9, 22) },
        estimatedPrice = 1300.0,
        groupSize = 8,
        participants = listOf(14, 15, 16),
        activities = mapOf(
            Date(125, 9, 16) to 2
        ),
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.CULTURE),
        creatorId = 4,
        appliedUsers = listOf(),
        published = true
    ),
    Trip(
        id = 5,
        photo = R.drawable.sydney,
        title = "Sydney Down Under Party",
        destination = "Sydney, Australia",
        startDate = Calendar.getInstance().apply { set(2025, 11, 20) },
        endDate = Calendar.getInstance().apply { set(2025, 11, 30) },
        estimatedPrice = 2000.0,
        groupSize = 10,
        participants = listOf(17, 18),
        activities = mapOf(
            Date(125, 11, 21) to 1
        ),
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.PARTY, TypeTravel.ADVENTURE),
        creatorId = 5,
        appliedUsers = listOf(19),
        published = true
    ),
    Trip(
        id = 6,
        photo = R.drawable.new_york,
        title = "New York City Lights",
        destination = "New York, USA",
        startDate = Calendar.getInstance().apply { set(2025, 12, 10) },
        endDate = Calendar.getInstance().apply { set(2025, 12, 20) },
        estimatedPrice = 1800.0,
        groupSize = 6,
        participants = listOf(20, 21, 22),
        activities = mapOf(
            Date(125, 12, 11) to 1
        ),
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.CULTURE, TypeTravel.PARTY),
        creatorId = 6,
        appliedUsers = listOf(23, 24),
        published = true
    ),
    Trip(
        id = 7,
        photo = R.drawable.reykjavik,
        title = "Icelandic Adventure",
        destination = "Reykjavik, Iceland",
        startDate = Calendar.getInstance().apply { set(2025, 10, 1) },
        endDate = Calendar.getInstance().apply { set(2025, 10, 8) },
        estimatedPrice = 1600.0,
        groupSize = 5,
        participants = listOf(25, 26),
        activities = mapOf(
            Date(125, 10, 2) to 2
        ),
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.ADVENTURE),
        creatorId = 7,
        appliedUsers = listOf(27),
        published = true
    ),
    Trip(
        id = 8,
        photo = R.drawable.rio,
        title = "Carnival in Rio",
        destination = "Rio de Janeiro, Brazil",
        startDate = Calendar.getInstance().apply { set(2025, 2, 20) },
        endDate = Calendar.getInstance().apply { set(2025, 2, 28) },
        estimatedPrice = 1700.0,
        groupSize = 12,
        participants = listOf(28, 29, 30),
        activities = mapOf(
            Date(125, 2, 21) to 3
        ),
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.PARTY),
        creatorId = 8,
        appliedUsers = listOf(31, 32),
        published = true
    )
)


*/

data class LazyUser(
    var name:String,
    var surname:String,
    var username:String,
    var userDescription:String,
    var email:String,
    var country:String,
    var approvalRate:Float,
    var reliability:Int,
    var typeTravelPreferences:List<TypeTravel>,
    var desiredDestinations:List<String>,
    var trips:List<LazyTrip>,
    var articles:List<LazyArticle>,
    var reviews:List<LazyReview>,
    var profileImage:Image?
)

//LAZY DATA
data class LazyTrip(
    val destination:String,
    val strDate:String,
    val photo: Int
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

val tripList2 = listOf(
    LazyTrip("Reykjavik", "October, 2024", R.drawable.reykjavik),
    LazyTrip("Rio", "February, 2025", R.drawable.rio),
    LazyTrip("Paris", "May, 2025", R.drawable.paris),
    LazyTrip("New York", "December, 2025", R.drawable.new_york),

)

val tripList1 = listOf(
    LazyTrip("Roma", "September, 2024", R.drawable.rome_photo),
    LazyTrip("Sydney", "November, 2024", R.drawable.sydney),
    LazyTrip("Tokyo", "July, 2004", R.drawable.tokyo),
    LazyTrip("Bali", "August, 2025", R.drawable.bali),
)

val articleList2 = listOf(
    LazyArticle("Chasing Iceland Lights", "October, 2024"),
    LazyArticle("Dancing Through Rio", "February, 2025"),
    LazyArticle("Fall in Love in Paris", "May, 2025"),
    LazyArticle("Discover New York City", "December, 2025")
)

val articleList1 = listOf(
    LazyArticle("Journey into Rome's History", "September, 2024"),
    LazyArticle("Sydney Nights & Surf Days", "November, 2024"),
    LazyArticle("Ultimate Tokyo Adventure", "July, 2004"),
    LazyArticle("Ocean Meets Culture", "August, 2025")
)

val reviewList1 = listOf(
    LazyReview("Alice", "Martin", 4.5f, "August, 2025"),
    LazyReview("Leo", "Nguyen", 5.0f, "July, 2025"),
    LazyReview("Maria", "Gonzalez", 4.2f, "June, 2025"),
    LazyReview("Thomas", "Anders", 3.8f, "September, 2025"),
    LazyReview("Lena", "Kowalski", 4.7f, "October, 2025"),
    LazyReview("David", "O'Connor", 4.9f, "May, 2025"),
    LazyReview("Sofia", "Chen", 4.0f, "November, 2025"),
    LazyReview("Max", "Dubois", 3.5f, "December, 2025")
)

val reviewList2 = listOf(
    LazyReview("Emily", "Smith", 4.6f, "April, 2025"),
    LazyReview("James", "Lee", 4.1f, "March, 2025"),
    LazyReview("Nina", "Petrova", 4.8f, "February, 2025"),
    LazyReview("Oscar", "Morales", 3.9f, "January, 2025"),
    LazyReview("Isabella", "Bianchi", 4.4f, "August, 2025"),
    LazyReview("Lukas", "Meier", 3.7f, "June, 2025"),
    LazyReview("Ava", "Johnson", 4.3f, "July, 2025"),
    LazyReview("Mateo", "Silva", 4.0f, "May, 2025")
)

val user1 = LazyUser(
    name = "James",
    surname = "Lee",
    username = "james.explorer",
    userDescription = "Adventure junkie. If it's off-road, underwater, or on a mountain, I’m in.",
    email = "jameslee@polito.it",
    country = "Liberia",
    approvalRate = 4.3f,
    reliability = 90,
    typeTravelPreferences = listOf( TypeTravel.ADVENTURE, TypeTravel.CULTURE ),
    desiredDestinations = listOf( "Australia", "Japan", "Italy"),
    trips = tripList1,
    articles = articleList1,
    reviews = reviewList1,
    profileImage = null
)

val user2 = LazyUser(
    name = "Sofia",
    surname = "Chen",
    username = "sofiatrips",
    userDescription = "Planner, packer, photographer. I bring color to every itinerary.",
    email = "sofiachen@alice.it",
    country = "Panama",
    approvalRate = 4.9f,
    reliability = 97,
    typeTravelPreferences = listOf( TypeTravel.PARTY, TypeTravel.RELAX ),
    desiredDestinations = listOf( "Iceland","France", "United States of America" ),
    trips = tripList2,
    articles = articleList2,
    reviews = reviewList2,
    profileImage = null
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
