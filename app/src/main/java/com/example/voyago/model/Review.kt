package com.example.voyago.model

import java.util.Calendar

data class Review(
    val reviewId: Int = 0,
    val tripId: Int = 0,
    val isTripReview: Boolean = false,
    var reviewerId: Int = 0,
    var reviewedUserId: Int = 0,
    var title: String = "",
    var comment: String = "",
    var score: Int = 0,
    var photos: List<String> = emptyList(),
    val date: Long = 0L
) {


    //A valid review has filled fields
    fun isValidReview(): Boolean {
        return reviewId > 0 && reviewerId > 0 && (reviewedUserId > 0 || tripId > 0)  && score > 0
                && title != "" && comment != ""
    }
}
