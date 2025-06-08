package com.example.voyago.model

import android.util.Log
import com.example.voyago.Collections
import com.example.voyago.view.isUriString
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

data class Review(
    val reviewId: Int = 0,
    val tripId: Int = 0,
    @get:PropertyName("isTripReview")
    @set:PropertyName("isTripReview")
    var isTripReview: Boolean = false,
    var reviewerId: Int = 0,
    var reviewedUserId: Int = 0,
    var title: String = "",
    var comment: String = "",
    var score: Int = 0,
    var photos: List<String> = emptyList(),
    val date: Timestamp = Timestamp(Date(0))
) {

    fun dateAsCalendar(): Calendar = toCalendar(date)
    fun dateAsLong(): Long = date.toDate().time

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
        date = Timestamp(Date(0))
    )


    //A valid review has filled fields
    fun isValidReview(): Boolean {
        return reviewId > 0 && reviewerId > 0 && (reviewedUserId > 0 || tripId > 0)  && score > 0
                && title != "" && comment != "" && date > Timestamp(Date(0))
    }

    // 添加获取单个照片URL的方法
    suspend fun getPhotoUrl(photoPath: String): String? {
        return try {
            when {
                photoPath.isUriString() -> {
                    // 如果是 URI 格式，直接返回
                    photoPath
                }
                photoPath.contains("/") -> {
                    // 如果包含路径分隔符，说明是 Firebase Storage 路径
                    val storageRef = Firebase.storage.reference.child(photoPath)
                    storageRef.downloadUrl.await().toString()
                }
                else -> {
                    // 否则是 drawable 资源名称
                    photoPath
                }
            }
        } catch (e: Exception) {
            Log.e("Review", "Failed to get photo URL for $photoPath", e)
            null
        }
    }
    // 获取所有照片URL的方法
    suspend fun getPhotoUrls(): List<String?> {
        return photos.map { photoPath ->
            getPhotoUrl(photoPath)
        }
    }
}

class ReviewModel {


    /*suspend fun createReview(reviewToCreate: Review): Review? {
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
    }*/

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

    /*
    // Get reviews list of a trip
    fun getTripReviews(tripId: Int): List<Review> {
        return _reviews.value.filter { it.isTripReview && it.tripId == tripId }
    }



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

     */

    /*
    //Tells if the logged in user reviewed the trip
    fun isReviewed(userId: Int, tripId: Int) :Boolean {
        return _reviews.value.any {
            it.isTripReview == true && it.reviewerId == userId
                    && it.tripId == tripId
        }
    }


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

     */
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

//    suspend fun getUsersReviewsTrip(userId: Int, tripId: Int): List<Review>? {
//        return try {
//            // Query Firestore for user-specific reviews on a trip.
//            val querySnapshot = Collections.reviews
//                .whereEqualTo("isTripReview", false) // User review of a trip.
//                .whereEqualTo("tripId", tripId)
//                .whereEqualTo("reviewerId", userId)
//                .get()
//                .await()
//
//            // Map documents to Review objects.
//            val reviews = querySnapshot.documents.mapNotNull { document ->
//                try {
//                    document.toObject(Review::class.java)?.copy(reviewId = document.id.toInt())
//                } catch (e: Exception) {
//                    Log.e("FirestoreHelper", "Error parsing review ${document.id}", e)
//                    null
//                }
//            }
//
//            Log.d("FirestoreHelper", "Found ${reviews.size} user reviews for trip $tripId by user $userId.")
//            reviews // Returns list (can be empty).
//        } catch (e: Exception) {
//            // Log error and return null on failure.
//            Log.e("FirestoreHelper", "Error getting user reviews for trip $tripId by user $userId", e)
//            null
//        }
//    }


    /*
    // Get reviews list of a user
    fun getUserReviews(id: Int): List<Review> {
        return _reviews.value.filter { !it.isTripReview && it.reviewedUserId == id }
    }

     */



    /*
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

     */

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

    fun calculateRatingById(id: Int): Flow<Float> = callbackFlow {
        val query = Collections.reviews
            .whereEqualTo("reviewedUserId", id)
            .whereEqualTo("isTripReview", false)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("calculateRatingById", "Firestore error: ${error.message}")
                trySend(5.0f)
                return@addSnapshotListener
            }

            val reviews = snapshot?.toObjects(Review::class.java).orEmpty()
            val rating = if (reviews.isNotEmpty()) {
                val avg = reviews.map { it.score }.average().toFloat()
                String.format("%.1f", avg / 2f).toFloat()
            } else {
                5.0f
            }

            // 🔄 Update user's rating field in Firestore
            Collections.users.document(id.toString())
                .update("rating", rating)
                .addOnFailureListener { e ->
                    Log.e("calculateRatingById", "Failed to update user rating: ${e.message}")
                }

            trySend(rating)
        }

        awaitClose { listener.remove() }
    }


    // -------------------------- THIS WORKS ---------------------------

    //Get the reviews of a trip
    private val _tripReviews = MutableStateFlow<List<Review>>(emptyList())
    val tripReviews: StateFlow<List<Review>> = _tripReviews

    // 在 ReviewModel 中替换 getTripReviews 方法：

    fun getTripReviews(tripId: Int, coroutineScope: CoroutineScope) {
        Log.d("ReviewModel", "=== getTripReviews called ===")
        Log.d("ReviewModel", "Requesting reviews for trip ID: $tripId")

        coroutineScope.launch {
            callbackFlow {
                val listener = Collections.reviews
                    .whereEqualTo("tripId", tripId)
                    .whereEqualTo("isTripReview", true)
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null) {
                            val reviews = snapshot.toObjects(Review::class.java)
                            Log.d("ReviewModel", " Firestore returned ${reviews.size} reviews for trip $tripId")

                            // 详细日志每个 review
                            reviews.forEachIndexed { index, review ->
                                Log.d("ReviewModel", "Review $index:")
                                Log.d("ReviewModel", "  - ID: ${review.reviewId}")
                                Log.d("ReviewModel", "  - Title: '${review.title}'")
                                Log.d("ReviewModel", "  - TripID: ${review.tripId}")
                                Log.d("ReviewModel", "  - IsTripReview: ${review.isTripReview}")
                                Log.d("ReviewModel", "  - ReviewerId: ${review.reviewerId}")
                                Log.d("ReviewModel", "  - Score: ${review.score}")
                                Log.d("ReviewModel", "  - Comment: '${review.comment}'")
                            }

                            trySend(reviews)
                        } else {
                            Log.e("ReviewModel", "❌ Error fetching reviews for trip $tripId", error)
                            trySend(emptyList())
                        }
                    }
                awaitClose {
                    Log.d("ReviewModel", "Closing listener for trip $tripId reviews")
                    listener.remove()
                }
            }.collect { reviews ->
                Log.d("ReviewModel", "📝 Updating _tripReviews with ${reviews.size} reviews")
                _tripReviews.value = reviews
            }
        }
    }

    // Get live updates of reviews about a user (non-trip-specific)
    private val _userReviews = MutableStateFlow<List<Review>>(emptyList())
    val userReviews: StateFlow<List<Review>> = _userReviews

    fun getUserReviews(userId: Int, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            callbackFlow {
                val listener = Collections.reviews
                    .whereEqualTo("reviewedUserId", userId)
                    .whereEqualTo("isTripReview", false)
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null) {
                            val reviews = snapshot.documents.mapNotNull { document ->
                                try {
                                    document.toObject(Review::class.java)?.copy(reviewId = document.id.toInt())
                                } catch (e: Exception) {
                                    Log.e("FirestoreHelper", "Error parsing review ${document.id}", e)
                                    null
                                }
                            }
                            trySend(reviews)
                        } else {
                            Log.e("FirestoreHelper", "Error fetching user reviews", error)
                            trySend(emptyList())
                        }
                    }
                awaitClose { listener.remove() }
            }.collect { reviews ->
                _userReviews.value = reviews
            }
        }
    }

    // Check if a user has reviewed a trip
    private val _isReviewed = MutableStateFlow(false)
    val isReviewed: StateFlow<Boolean> = _isReviewed

    fun isReviewed(userId: Int, tripId: Int, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            callbackFlow {
                val listener = Collections.reviews
                    .whereEqualTo("isTripReview", true)
                    .whereEqualTo("reviewerId", userId)
                    .whereEqualTo("tripId", tripId)
                    .limit(1)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("FirestoreHelper", "Error checking review status", error)
                            trySend(false)
                        } else {
                            val reviewed = snapshot != null && !snapshot.isEmpty
                            trySend(reviewed)
                            Log.d("FirestoreHelper", "isReviewed updated: $reviewed")
                        }
                    }
                awaitClose { listener.remove() }
            }.collect { reviewed ->
                _isReviewed.value = reviewed
            }
        }
    }

    //Get a trip review made by a user
    private val _tripReview = MutableStateFlow(Review()) // Use a default constructor
    val tripReview: StateFlow<Review> = _tripReview

    fun getTripReview(userId: Int, tripId: Int, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            callbackFlow {
                val listener = Collections.reviews
                    .whereEqualTo("isTripReview", true)
                    .whereEqualTo("reviewerId", userId)
                    .whereEqualTo("tripId", tripId)
                    .limit(1)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("FirestoreHelper", "Error getting trip review", error)
                            trySend(Review()) // Emit default Review on error
                        } else {
                            val document = snapshot?.documents?.firstOrNull()
                            val review = document?.toObject(Review::class.java)
                                ?.copy(reviewId = document.id.toInt()) ?: Review() // Emit default if not found
                            Log.d("FirestoreHelper", "Trip review status: ${if (document != null) "Found" else "Not found"}")
                            trySend(review)
                        }
                    }
                awaitClose { listener.remove() }
            }.collect { review ->
                _tripReview.value = review
            }
        }
    }

    //Get reviews of a user about another user on a trip
    private val _usersTripReviews = MutableStateFlow<List<Review>>(emptyList())
    val usersTripReviews: StateFlow<List<Review>> = _usersTripReviews

    fun getUsersReviewsTrip(userId: Int, tripId: Int, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            callbackFlow {
                val listener = Collections.reviews
                    .whereEqualTo("isTripReview", false) // User review about another user on a trip
                    .whereEqualTo("tripId", tripId)
                    .whereEqualTo("reviewerId", userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("FirestoreHelper", "Error getting user reviews for trip", error)
                            trySend(emptyList()) // Send empty list on error
                            return@addSnapshotListener
                        }

                        val reviews = snapshot?.documents?.mapNotNull { document ->
                            try {
                                document.toObject(Review::class.java)?.copy(reviewId = document.id.toInt())
                            } catch (e: Exception) {
                                Log.e("FirestoreHelper", "Error parsing review ${document.id}", e)
                                null
                            }
                        } ?: emptyList()

                        Log.d("FirestoreHelper", "Fetched ${reviews.size} user reviews for trip $tripId by user $userId")
                        trySend(reviews)
                    }

                awaitClose { listener.remove() }
            }.collect { reviews ->
                _usersTripReviews.value = reviews
            }
        }
    }

    fun createReview(review: Review, onResult: (Boolean, Review?) -> Unit) {
        val firestore = Firebase.firestore
        val counterRef = firestore.collection("metadata").document("reviewCounter")

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(counterRef)
            val lastReviewId = snapshot.getLong("lastReviewId") ?: 0
            val newReviewId = lastReviewId + 1

            // Set the new ID back to the counter document
            transaction.update(counterRef, "lastReviewId", newReviewId)

            // Create the trip with the new ID
            val reviewWithId = review.copy(reviewId = newReviewId.toInt())

            // Create a new document in the trips collection
            val tripDocRef = firestore.collection("reviews").document(newReviewId.toString())
            transaction.set(tripDocRef, reviewWithId)

            reviewWithId
        }.addOnSuccessListener { review ->
            onResult(true, review)
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Failed to create review", e)
            onResult(false, null)
        }
    }

}