package com.example.voyago.model

import java.util.Calendar
import android.net.Uri

data class Review(
    val reviewId: Int,
    var reviewerId: Int,
    var tripId: Int,
    var title: String,
    var comment: String,
    var score: Float,
    var photos: List<Uri>,
    var userId: Int?,
    var date: Calendar
)
