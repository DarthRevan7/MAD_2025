package com.example.voyago

data class UserProfileInfo(
    val firstname: String,
    val surname: String,
    val username: String,
    val typeTravel: List<String>,
    val desiredDestination: List<String>,
    val userDescription: String,
    val rating: Float,
    val reliability: Int
)
