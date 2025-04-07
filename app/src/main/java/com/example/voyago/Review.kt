package com.example.voyago

data class Review(
    var user: UserProfileInfo,
    var title: String,
    var text: String,
    var rating: Float
)
