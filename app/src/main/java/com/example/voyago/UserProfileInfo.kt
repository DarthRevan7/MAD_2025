package com.example.voyago

data class UserProfileInfo(
    val firstame: String,
    val surname: String,
    val username: String,
    val typeTravel: List<String>,
    val desiredDestination: String,
    val userDescription: String,
    val rating: Float,
    val relaiability: Int
)
