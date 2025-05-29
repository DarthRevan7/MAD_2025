package com.example.voyago.model

import android.util.Log
import androidx.compose.runtime.collectAsState
import com.example.voyago.Collections
import com.example.voyago.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.tasks.await

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

    constructor() : this (
        reviewId = 0,
        tripId = 0,
        isTripReview = false,
        reviewerId = 0,
        reviewedUserId = 0,
        title = "",
        comment = "",
        score = 0,
        photos = emptyList(),
        date = 0L
    )


    //A valid review has filled fields
    fun isValidReview(): Boolean {
        return reviewId > 0 && reviewerId > 0 && (reviewedUserId > 0 || tripId > 0)  && score > 0
                && title != "" && comment != "" && date > 0L
    }
}

class ReviewModel {


    suspend fun createReview(reviewToCreate: Review): Review? {
        return try {
            val dataToAdd = reviewToCreate.copy(reviewId = 0) // Invalid ID for object creation

            val documentReference = Collections.reviews.add(dataToAdd).await()
            val newReviewId = documentReference.id

            Log.d("FirestoreHelper", "Recensione creata con successo con ID: $newReviewId")
            // Returns original object with new ID
            reviewToCreate.copy(reviewId = newReviewId.toInt())
        } catch (e: Exception) {
            Log.e("FirestoreHelper", "Errore durante la creazione della recensione", e)
            null
        }
    }

    //We will maybe not use this. Consider editing for soft delete.
    suspend fun deleteReview(reviewId: Int): Boolean {
        return try {
            Collections.reviews.document(reviewId.toString()).delete().await()
            Log.d("FirestoreHelper", "Recensione eliminata con successo: $reviewId")
            true
        } catch (e: Exception) {
            Log.e("FirestoreHelper", "Errore durante l'eliminazione della recensione: $reviewId", e)
            false
        }
    }

    //We will not use this to update reviews
    suspend fun updateReview(review: Review): Boolean {
        if (review.reviewId == 0) {
            Log.e("FirestoreHelper", "ID della recensione mancante. Impossibile aggiornare.")
            return false
        }
        return try {
            Collections.reviews.document(review.reviewId.toString()).set(review).await()
            Log.d("FirestoreHelper", "Recensione aggiornata con successo: ${review.reviewId}")
            true
        } catch (e: Exception) {
            Log.e("FirestoreHelper", "Errore durante l'aggiornamento della recensione: ${review.reviewId}", e)
            false
        }
    }



    // --------------------------------------------------------------

    /*
    //Create a new review
    fun createNewReview(newReview: Review): Review {
        val reviewWithId = newReview.copy(
            reviewId = nextReviewId++,
        )
        _reviews.value = _reviews.value + reviewWithId
        return reviewWithId
    }

     */

    suspend fun getTripReviews(tripId: Int): List<Review> {
        return try {
            val querySnapshot = Collections.reviews
                .whereEqualTo("tripId", tripId)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Review::class.java)?.takeIf { it.isTripReview }
                } catch (e: Exception) {
                    Log.e("Firestore", "Failed to deserialize review ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Failed to fetch reviews for trip $tripId", e)
            emptyList()
        }
    }


    /*
    // Get reviews list of a trip
    fun getTripReviews(tripId: Int): List<Review> {
        return _reviews.value.filter { it.isTripReview && it.tripId == tripId }
    }


     */
    suspend fun isReviewed(userId: Int, tripId: Int): Boolean {
        return try {
            // Query Firestore for a review matching user, trip, and type.
            val querySnapshot = Collections.reviews
                .whereEqualTo("isTripReview", true)
                .whereEqualTo("reviewerId", userId)
                .whereEqualTo("tripId", tripId)
                .limit(1) // Only need to know if one exists.
                .get()
                .await()

            // Check if any matching review was found.
            val hasReviewed = !querySnapshot.isEmpty

            if (hasReviewed) {
                Log.d("FirestoreHelper", "User $userId has already reviewed trip $tripId.")
            } else {
                Log.d("FirestoreHelper", "User $userId has not reviewed trip $tripId.")
            }

            hasReviewed
        } catch (e: Exception) {
            // Log error on failure and return false as a safe default.
            Log.e("FirestoreHelper", "Error checking review status for user $userId, trip $tripId", e)
            false
        }
    }

    /*
    //Tells if the logged in user reviewed the trip
    fun isReviewed(userId: Int, tripId: Int) :Boolean {
        return _reviews.value.any {
            it.isTripReview == true && it.reviewerId == userId
                    && it.tripId == tripId
        }
    }

     */
    suspend fun getTripReview(userId: Int, tripId: Int): Review? {
        return try {
            // Query Firestore for a specific trip review by user and trip ID.
            val querySnapshot = Collections.reviews
                .whereEqualTo("isTripReview", true)
                .whereEqualTo("reviewerId", userId)
                .whereEqualTo("tripId", tripId)
                .limit(1)
                .get()
                .await()

            // If found, map document to Review object.
            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents.first()
                val review = document.toObject(Review::class.java)?.copy(reviewId = document.id.toInt())
                Log.d("FirestoreHelper", "Found review for user $userId on trip $tripId: ${review?.reviewId}")
                review
            } else {
                Log.d("FirestoreHelper", "No review found for user $userId on trip $tripId.")
                null // No matching review.
            }
        } catch (e: Exception) {
            // Log error and return null on failure.
            Log.e("FirestoreHelper", "Error getting review for user $userId on trip $tripId", e)
            null
        }
    }
    /*
    //Review of a trip made by a user
    fun getTripReview(userId: Int, tripId: Int): Review {
        if(isReviewed(userId, tripId )) {
            return _reviews.value.find { it.isTripReview && it.reviewerId == userId
                    && it.tripId == tripId}!!
        }
        return Review()
    }

     */

    /*
    //Reviews of users that had taken part to a specific trip made by a user
    fun getUsersReviewsTrip(userId: Int, tripId: Int) :List<Review> {
        return _reviews.value.filter { !it.isTripReview && it.tripId == tripId && it.reviewerId == userId }
    }

     */

    suspend fun getUsersReviewsTrip(userId: Int, tripId: Int): List<Review>? {
        return try {
            // Query Firestore for user-specific reviews on a trip.
            val querySnapshot = Collections.reviews
                .whereEqualTo("isTripReview", false) // User review of a trip.
                .whereEqualTo("tripId", tripId)
                .whereEqualTo("reviewerId", userId)
                .get()
                .await()

            // Map documents to Review objects.
            val reviews = querySnapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Review::class.java)?.copy(reviewId = document.id.toInt())
                } catch (e: Exception) {
                    Log.e("FirestoreHelper", "Error parsing review ${document.id}", e)
                    null
                }
            }

            Log.d("FirestoreHelper", "Found ${reviews.size} user reviews for trip $tripId by user $userId.")
            reviews // Returns list (can be empty).
        } catch (e: Exception) {
            // Log error and return null on failure.
            Log.e("FirestoreHelper", "Error getting user reviews for trip $tripId by user $userId", e)
            null
        }
    }


    /*
    // Get reviews list of a user
    fun getUserReviews(id: Int): List<Review> {
        return _reviews.value.filter { !it.isTripReview && it.reviewedUserId == id }
    }

     */

    suspend fun getUserReviews(id: Int): List<Review>? {
        return try {
            // Query Firestore for reviews about a specific user (reviewedUserId).
            val querySnapshot = Collections.reviews
                .whereEqualTo("reviewedUserId", id)
                .whereEqualTo("isTripReview", false) // Exclude trip-specific reviews.
                .get()
                .await()

            // Map documents to Review objects.
            val reviews = querySnapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Review::class.java)?.copy(reviewId = document.id.toInt())
                } catch (e: Exception) {
                    Log.e("FirestoreHelper", "Error parsing review ${document.id}", e)
                    null
                }
            }

            Log.d("FirestoreHelper", "Found ${reviews.size} reviews for user $id.")
            reviews // Return list (can be empty).
        } catch (e: Exception) {
            // Log error; return null on failure.
            Log.e("FirestoreHelper", "Error getting reviews for user $id", e)
            null
        }
    }

    suspend fun calculateRatingById(id: Int): Float? {
        return try {
            // Fetch reviews for the specific user (excluding trip reviews).
            val querySnapshot = Collections.reviews
                .whereEqualTo("reviewedUserId", id)
                .whereEqualTo("isTripReview", false)
                .get()
                .await()

            // Map documents to Review objects.
            val reviewsForUser = querySnapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Review::class.java)?.copy(reviewId = document.id.toInt())
                } catch (e: Exception) {
                    Log.e("FirestoreHelper", "Error parsing review ${document.id}", e)
                    null
                }
            }

            if (reviewsForUser.isNotEmpty()) {
                val averageScore = reviewsForUser.map { it.score }.average().toFloat()
                val scaled = averageScore / 2f // Convert 0-10 to 0-5.
                val finalRating = (scaled * 10).toInt() / 10f // Truncate to 1 decimal place.

                Log.d("FirestoreHelper", "Calculated rating for user $id: $finalRating")
                finalRating
            } else {
                Log.d("FirestoreHelper", "No reviews found for user $id. Returning default rating.")
                5.0f // Default rating for users with no reviews.
            }
        } catch (e: Exception) {
            // Log error and return null on failure.
            Log.e("FirestoreHelper", "Error calculating rating for user $id", e)
            null
        }
    }

    /*
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

     */




}