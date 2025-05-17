package com.example.voyago.model.domain

<<<<<<< Updated upstream:app/src/main/java/com/example/voyago/model/Review.kt
import android.media.Image
=======
import android.net.Uri
>>>>>>> Stashed changes:app/src/main/java/com/example/voyago/model/domain/Review.kt
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