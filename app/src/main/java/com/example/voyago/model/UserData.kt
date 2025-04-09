package com.example.voyago.model

import android.media.Image
import com.example.voyago.Article
import com.example.voyago.Review
import com.example.voyago.Trip
import com.example.voyago.TypeTravel
import java.util.Calendar

data class UserData(
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
    var publicTrips: List<Trip>?,         //Trip id
    var articles: List<Article>?,           //Article id
    var reviews: List<Review>?,             //Reviews id
    var privateTrips: List<Trip>?,         //Trip id
    var tripsAppliedTo: List<Trip>?       //Trip id
) {
    fun age(): String
    {
        val calendarToday = Calendar.getInstance()
        return (calendarToday.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR)).toString()

    }
}
