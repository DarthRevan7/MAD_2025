package com.example.voyago

import android.media.Image
import java.util.Calendar

data class Review(
    val reviewId: Int,
    var reviewerId: Int,
    var tripId: Int,
    var title: String,
    var comment: String,
    var score: Float,
    var photos: List<Image>?,
    var userId: Int?,
    var date: Calendar
)
