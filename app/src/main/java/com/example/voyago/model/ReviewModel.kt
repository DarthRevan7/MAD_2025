package com.example.voyago.model

import android.annotation.SuppressLint
import android.util.Log
import com.example.voyago.Collections
import com.example.voyago.view.isUriString
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.Locale

// Review data structure
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

    //Review constructor
    constructor() : this(
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

    //Function that returns true if the review is valid
    //A valid review has filled fields
    fun isValidReview(): Boolean {
        return reviewId > 0 && reviewerId > 0 && (reviewedUserId > 0 || tripId > 0) && score > 0
                && title != "" && comment != "" && date > Timestamp(Date(0))
    }

    // Adds a method to get the URL of a single photo based on its path
    suspend fun getPhotoUrl(photoPath: String): String? {
        return try {
            when {
                photoPath.isUriString() -> {
                    // If the photoPath is already a URI string (e.g., starts with http/https),
                    // return it directly without modification
                    photoPath
                }

                photoPath.contains("/") -> {
                    // If the photoPath contains a slash, it is treated as a Firebase Storage path,
                    // so retrieve the download URL from Firebase Storage asynchronously
                    val storageRef = Firebase.storage.reference.child(photoPath)
                    storageRef.downloadUrl.await().toString()
                }

                else -> {
                    // Otherwise, assume photoPath is the name of a drawable resource,
                    // so just return it as is (likely handled elsewhere)
                    photoPath
                }
            }
        } catch (e: Exception) {
            // Log error if retrieving URL fails and return null
            Log.e("Review", "Failed to get photo URL for $photoPath", e)
            null
        }
    }
}

class ReviewModel {

    @SuppressLint("DefaultLocale")
    fun calculateRatingById(id: Int): Flow<Float> = callbackFlow {
        // Build a Firestore query to get reviews for a specific user, excluding trip reviews
        val query = Collections.reviews
            .whereEqualTo("reviewedUserId", id)
            .whereEqualTo("isTripReview", false)

        // Attach a real-time listener to the query
        val listener = query.addSnapshotListener { snapshot, error ->
            //If there is an error
            if (error != null) {
                // Log Firestore query error and send default rating (5.0f)
                Log.e("calculateRatingById", "Firestore error: ${error.message}")
                trySend(5.0f)
                return@addSnapshotListener
            }

            // Convert the query snapshot into a list of Review objects
            val reviews = snapshot?.toObjects(Review::class.java).orEmpty()

            // If reviews are present, calculate the average score (out of 10), then convert to 5-point scale
            val rating = if (reviews.isNotEmpty()) {
                val avg = reviews.map { it.score }.average().toFloat()
                // Format to one decimal place and normalize to a 5-star scale
                String.format(Locale.US, "%.1f", avg / 2f).toFloat()
            } else {
                // Default to 5.0f if there are no reviews
                5.0f
            }

            // Update the user's rating field in Firestore with the new rating
            Collections.users.document(id.toString())
                .update("rating", rating)
                .addOnFailureListener { e ->
                    // Log failure to update user rating
                    Log.e("calculateRatingById", "Failed to update user rating: ${e.message}")
                }
            // Send the calculated rating to the flow
            trySend(rating)
        }
        // Remove the Firestore listener when the flow collector is cancelled
        awaitClose { listener.remove() }
    }

    //Reviews of a trip
    private val _tripReviews = MutableStateFlow<List<Review>>(emptyList())
    val tripReviews: StateFlow<List<Review>> = _tripReviews

    // Fetches reviews related to a specific trip using Firestore real-time updates.
    fun getTripReviews(tripId: Int, coroutineScope: CoroutineScope) {
        // Launch the coroutine in the provided scope
        coroutineScope.launch {
            // Create a flow that listens to Firestore snapshot changes
            callbackFlow {
                val listener = Collections.reviews
                    .whereEqualTo("tripId", tripId)             // Filter by trip ID
                    .whereEqualTo("isTripReview", true)         // Only include trip reviews
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null) {
                            // Convert the snapshot into a list of Review objects
                            val reviews = snapshot.toObjects(Review::class.java)

                            // Emit the review list into the flow
                            trySend(reviews)
                        } else {
                            // Emit an empty list if snapshot is null
                            trySend(emptyList())
                        }
                    }

                // Clean up the listener when the flow is closed or cancelled
                awaitClose {
                    listener.remove()
                }
            }.collect { reviews ->
                // Collect values emitted by the flow and update the StateFlow
                _tripReviews.value = reviews
            }
        }
    }

    // Reviews about a user (non-trip-specific)
    private val _userReviews = MutableStateFlow<List<Review>>(emptyList())
    val userReviews: StateFlow<List<Review>> = _userReviews

    // Fetches non-trip reviews written about a specific user and updates the `_userReviews` StateFlow
    fun getUserReviews(userId: Int, coroutineScope: CoroutineScope) {
        // Launch a coroutine in the provided scope
        coroutineScope.launch {
            // Use callbackFlow to convert Firestore's listener into a Kotlin Flow
            callbackFlow {
                // Build the Firestore query: fetch reviews for the user that are not trip-related
                val listener = Collections.reviews
                    .whereEqualTo("reviewedUserId", userId)
                    .whereEqualTo("isTripReview", false)
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null) {
                            // Safely map Firestore documents to Review objects, logging any failures
                            val reviews = snapshot.documents.mapNotNull { document ->
                                try {
                                    // Convert document to Review and assign the document ID as `reviewId`
                                    document.toObject(Review::class.java)
                                        ?.copy(reviewId = document.id.toInt())
                                } catch (e: Exception) {
                                    Log.e(
                                        "FirestoreHelper",
                                        "Error parsing review ${document.id}",
                                        e
                                    )
                                    null
                                }
                            }
                            // Emit the parsed list of reviews into the flow
                            trySend(reviews)
                        } else {
                            // If an error occurs, log it and emit an empty list
                            Log.e("FirestoreHelper", "Error fetching user reviews", error)
                            trySend(emptyList())
                        }
                    }

                // Remove the Firestore listener when the flow collection ends
                awaitClose { listener.remove() }
            }.collect { reviews ->
                // Update the backing StateFlow with the latest list of user reviews
                _userReviews.value = reviews
            }
        }
    }

    // Boolean that tells if a user has reviewed a trip
    private val _isReviewed = MutableStateFlow(false)
    val isReviewed: StateFlow<Boolean> = _isReviewed

    // Checks if a specific user has already submitted a trip review for a given trip.
    // Updates the `_isReviewed` StateFlow with the result (true or false).
    fun isReviewed(userId: Int, tripId: Int, coroutineScope: CoroutineScope) {
        // Launch a coroutine in the provided scope
        coroutineScope.launch {
            // Use callbackFlow to bridge Firestore's real-time listener with Kotlin Flow
            callbackFlow {
                // Create a Firestore query
                val listener = Collections.reviews
                    .whereEqualTo("isTripReview", true)     //Only trip reviews
                    .whereEqualTo(
                        "reviewerId",
                        userId
                    )     //Written by the specific user (reviewerId)
                    .whereEqualTo("tripId", tripId)         //Related to the given trip (tripId)
                    .limit(1)                               //Limit to 1 result for efficiency
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            // Send false (not reviewed)
                            trySend(false)
                        } else {
                            // Consider reviewed if the snapshot exists and is not empty
                            val reviewed = snapshot != null && !snapshot.isEmpty
                            trySend(reviewed)
                        }
                    }
                // Remove listener when the flow is closed/cancelled
                awaitClose { listener.remove() }
            }.collect { reviewed ->
                // Update StateFlow to reflect whether the user has reviewed the trip
                _isReviewed.value = reviewed
            }
        }
    }

    //Trip review made by a user
    private val _tripReview = MutableStateFlow(Review()) // Use a default constructor
    val tripReview: StateFlow<Review> = _tripReview

    // Fetches a specific trip review written by a user for a given trip.
    // The result (if any) is collected into the `_tripReview` StateFlow
    fun getTripReview(userId: Int, tripId: Int, coroutineScope: CoroutineScope) {
        // Launch a coroutine in the provided CoroutineScope
        coroutineScope.launch {
            // Convert Firestore's snapshot listener into a Kotlin Flow
            callbackFlow {
                // Firestore query to find a single trip review matching user and trip ID
                val listener = Collections.reviews
                    .whereEqualTo("isTripReview", true)     // Only trip-related reviews
                    .whereEqualTo("reviewerId", userId)     // Review written by this user
                    .whereEqualTo("tripId", tripId)         // Related to this trip
                    .limit(1)                               // Only one review expected per user-trip pair
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            // On error, log it and emit a default (empty) Review object
                            Log.e("FirestoreHelper", "Error getting trip review", error)
                            trySend(Review()) // Emit default Review on error
                        } else {
                            // Get the first (and only) document if it exists
                            val document = snapshot?.documents?.firstOrNull()
                            // Convert it to a Review object, or fallback to default Review
                            val review = document?.toObject(Review::class.java)
                                ?.copy(reviewId = document.id.toInt())
                                ?: Review() // Emit default if not found
                            // Emit the review into the flow
                            trySend(review)
                        }
                    }
                // Clean up Firestore listener when the flow is closed
                awaitClose { listener.remove() }
            }.collect { review ->
                // Update backing StateFlow to notify UI of the fetched review
                _tripReview.value = review
            }
        }
    }

    //Get reviews of a user about another user on a trip
    private val _usersTripReviews = MutableStateFlow<List<Review>>(emptyList())
    val usersTripReviews: StateFlow<List<Review>> = _usersTripReviews

    // Fetches all non-trip reviews written by a specific user (`userId`) related to a specific trip (`tripId`)
    // These are typically peer-to-peer user reviews written after a trip
    // The results are stored in the `_usersTripReviews` StateFlow
    fun getUsersReviewsTrip(userId: Int, tripId: Int, coroutineScope: CoroutineScope) {
        // Launch a coroutine in the provided scope to perform asynchronous operations
        coroutineScope.launch {
            // Convert Firestore's real-time snapshot listener to a Kotlin Flow
            callbackFlow {
                // Firestore query
                val listener = Collections.reviews
                    .whereEqualTo(
                        "isTripReview",
                        false
                    )    // Fetch reviews that are NOT trip reviews (user-to-user reviews)
                    .whereEqualTo("tripId", tripId)         // Filter by the specific trip
                    .whereEqualTo("reviewerId", userId)     // Filter by the specific user
                    .addSnapshotListener { snapshot, error ->

                        if (error != null) {
                            // Log error and emit an empty list if Firestore fails
                            Log.e("FirestoreHelper", "Error getting user reviews for trip", error)
                            trySend(emptyList()) // Send empty list on error
                            return@addSnapshotListener
                        }

                        // Map Firestore documents to Review objects, safely
                        val reviews = snapshot?.documents?.mapNotNull { document ->
                            try {
                                // Parse document to Review and set the reviewId from the document ID
                                document.toObject(Review::class.java)
                                    ?.copy(reviewId = document.id.toInt())
                            } catch (e: Exception) {
                                Log.e("FirestoreHelper", "Error parsing review ${document.id}", e)
                                null
                            }
                        } ?: emptyList()  // Default to empty list if snapshot is null

                        // Emit the list of parsed reviews
                        trySend(reviews)
                    }

                // Remove Firestore listener when the flow collection is cancelled or completed
                awaitClose { listener.remove() }
            }.collect { reviews ->
                // Update backing StateFlow to notify of the latest user-to-user reviews
                _usersTripReviews.value = reviews
            }
        }
    }

    // Creates a new review in Firestore with a unique incrementing ID.
    fun createReview(review: Review, onResult: (Boolean, Review?) -> Unit) {
        // Get Firestore instance and reference to the review counter document
        val firestore = Firebase.firestore
        val counterRef = firestore.collection("metadata").document("reviewCounter")

        // Start a Firestore transaction to ensure atomic read/write of the ID counter and review creation
        firestore.runTransaction { transaction ->
            // Get the current counter value for the lastReviewId
            val snapshot = transaction.get(counterRef)
            val lastReviewId = snapshot.getLong("lastReviewId") ?: 0

            // Increment the counter to create a new unique review ID
            val newReviewId = lastReviewId + 1

            // Create a new Review object with the generated ID
            transaction.update(counterRef, "lastReviewId", newReviewId)

            // Create the trip with the new ID
            val reviewWithId = review.copy(reviewId = newReviewId.toInt())

            // Reference to the new review document using the generated ID as the document ID
            val tripDocRef = firestore.collection("reviews").document(newReviewId.toString())

            // Add the review to Firestore as part of the same transaction
            transaction.set(tripDocRef, reviewWithId)

            // Return the created review to the success listener
            reviewWithId
        }.addOnSuccessListener { review ->
            // Notify caller that creation succeeded and return the created review
            onResult(true, review)
        }.addOnFailureListener { e ->
            // Log the error and notify the caller that creation failed
            Log.e("Firestore", "Failed to create review", e)
            onResult(false, null)
        }
    }

}