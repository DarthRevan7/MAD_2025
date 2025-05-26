package com.example.voyago.model

class ReviewModel {
    private var _reviews = privateReviews
    var reviews = _reviews

    private var nextReviewId = 31

    //Create a new trip
    fun createNewReview(newReview: Review): Review {
        val reviewWithId = newReview.copy(
            reviewId = nextReviewId++,
        )
        _reviews.value = _reviews.value + reviewWithId
        return reviewWithId
    }

    // Get reviews list of a trip
    fun getTripReviews(tripId: Int): List<Review> {
        return _reviews.value.filter { it.isTripReview && it.tripId == tripId }
    }

    //Tells if the logged in user reviewed the trip
    fun isReviewed(userId: Int, tripId: Int) :Boolean {
        return _reviews.value.any {
            it.isTripReview == true && it.reviewerId == userId
                    && it.tripId == tripId
        }
    }

    //Review of a trip made by a user
    fun getTripReview(userId: Int, tripId: Int): Review {
        if(isReviewed(userId, tripId )) {
            return _reviews.value.find { it.isTripReview && it.reviewerId == userId
                    && it.tripId == tripId}!!
        }
        return Review()
    }

    //Reviews of users that had taken part to a specific trip made by a user
    fun getUsersReviewsTrip(userId: Int, tripId: Int) :List<Review> {
        return _reviews.value.filter { !it.isTripReview && it.tripId == tripId && it.reviewerId == userId }
    }

    // Get reviews list of a user
    fun getUserReviews(id: Int): List<Review> {
        return _reviews.value.filter { !it.isTripReview && it.reviewedUserId == id }
    }


    // Calculate average user rating by id
    fun calculateRatingById(id:Int): Float {
        val reviewsForUser = _reviews.value.filter { !it.isTripReview && it.reviewedUserId == id }

        return if (reviewsForUser.isNotEmpty()) {
            val averageScore = reviewsForUser.map { it.score }.average().toFloat()
            val scaled = averageScore / 2f  // Convert from 0–10 to 0–5
            (scaled * 10).toInt() / 10f     // Truncate to 1 decimal place
        } else {
            5.0f // Default rating for users with no reviews
        }
    }

}