package com.example.voyago

data class UserProfileInfo(
    var firstname: String,
    var surname: String,
    var username: String,
    var typeTravel: List<String>,
    var desiredDestination: List<String>,
    var userDescription: String,
    var rating: Float,
    var reliability: Int,
    var trips: List<Trip>?,
    var articles: List<Article>?,
    var reviews: List<Review>?
)
