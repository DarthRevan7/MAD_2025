package com.example.voyago.model

import java.util.Calendar
import android.net.Uri
import com.example.voyago.model.Trip
import com.example.voyago.model.Trip.TripStatus

data class Review(
    val reviewId: Int,
    val tripId: Int,
    val isTripReview: Boolean,
    var reviewerId: Int,
    var reviewedUserId: Int,
    var title: String,
    var comment: String,
    var score: Int,
    var photos: List<Uri>,
    val date: Calendar
) {
    constructor() : this (
        reviewId = -1,
        tripId = -1,
        isTripReview = false,
        reviewerId = -1,
        reviewedUserId = -1,
        title = "",
        comment = "",
        score = -1,
        photos = emptyList(),
        date = Calendar.getInstance()
    )

    //A valid review has filled fields!!
    fun isValidReview(): Boolean {
        return reviewId > 0 && reviewerId > 0 && (reviewedUserId > 0 || tripId > 0)  && score > 0
                && title != "" && comment != ""
    }
}
