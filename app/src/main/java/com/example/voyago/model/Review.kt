package com.example.voyago.model

import java.util.Calendar
import android.net.Uri

data class Review(
    val reviewId: Int,
    val isTripReview: Boolean,
    var reviewerId: Int,
    var reviewedId: Int,
    var title: String,
    var comment: String,
    var score: Int,
    var photos: List<Uri>,
    val date: Calendar
)
