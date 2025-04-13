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


data class LazySeriousTrip(
    val tripTitle:String,
    val destination:String,
    val startDate:String,
    val endDate:String,
    val esteemedPrice:Float,
    val sizeGroup:Int,
    val acceptedPeople:Int,
    val tripPhoto:Int,                      //Use the R.drawable which returns an integer
    val activities:Map<String,LazyActivity>
)
{
    //Calculates the remaining spots
    fun remainingSpots():Int
    {
        return this.sizeGroup-this.acceptedPeople
    }
}

data class LazyActivity(
    val activityDate:String,
    val activityTime:String,
    val isGroupActivity:Boolean,
    val description:String
)

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
{
    fun applyStrChanges(name:String, surname:String, username:String, email:String, country:String, userDescription:String)
    {
        this.name = name
        this.surname = surname
        this.username = username
        this.userDescription = userDescription
        this.email = email
        this.country = country
    }
    fun applyTypeTravelChanges(typeTravels:List<TypeTravel>)
    {
        if(typeTravels.isNotEmpty())
            this.typeTravelPreferences = typeTravels
    }
    fun applyDestinations(destinations:List<String>)
    {
        if(destinations.isNotEmpty())
            this.desiredDestinations = destinations
    }
}

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

/*
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

 */
