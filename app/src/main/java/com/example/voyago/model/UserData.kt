package com.example.voyago.model


import android.media.Image
import com.example.voyago.model.Article
import com.example.voyago.model.Review
import java.util.Calendar

data class UserData(
    val id: Int,
    var firstname: String,
    var surname: String,
    var username: String,
    var country: String,
    var email: String,
    var userDescription: String,
    var dateOfBirth: Calendar,
    var password: String,
    var profilePicture: String,
    var typeTravel: List<TypeTravel>,
    var desiredDestination: List<String>,
    var rating: Float,
    var reliability: Int

) {
    fun age(): String
    {
        val calendarToday = Calendar.getInstance()
        return (calendarToday.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR)).toString()

    }
    fun changeUserData(dataToChange:List<String>)
    {
        this.firstname = dataToChange[0]
        this.surname = dataToChange[1]
        this.username = dataToChange[2]
        this.email = dataToChange[3]
        this.country = dataToChange[4]
    }
}