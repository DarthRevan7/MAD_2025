package com.example.voyago

import android.media.Image
import java.util.*

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
    var publicTrips: List<Int>?,         //Trip id
    var articles: List<Int>?,           //Article id
    var reviews: List<Int>?,             //Reviews id
    var privateTrips: List<Int>?,         //Trip id
    var tripsAppliedTo: List<Int>?       //Trip id
)
{
    fun Age(): String
    {
        val calendarToday = Calendar.getInstance()

        return (calendarToday.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR)).toString()

    }
}