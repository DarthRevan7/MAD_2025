package com.example.voyago.model

data class LazyUser(
    val id: Int,
    var name: String,
    var surname: String,
    var rating: Float,
    var requestedSpots: Int
)