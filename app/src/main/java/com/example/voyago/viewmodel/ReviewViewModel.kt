package com.example.voyago.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.voyago.model.*
import com.google.android.play.core.integrity.v
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class ReviewViewModel(val reviewModel:ReviewModel): ViewModel() {


    private suspend fun addNewReview(newReview: Review): Review? {
        val createdReview = reviewModel.createReview(newReview)
        return createdReview
    }

    fun addAllTripReviews(reviews: List<Review>) {
        // Get the coroutine scope
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            Log.d(
                "AddReviews",
                "Avvio dell'elaborazione di ${reviews.size} recensioni in una singola coroutine."
            )
            for (review in reviews) {
                val createdReview = addNewReview(review) // Call addNewReview

                if (createdReview != null) {
                    // Success for this review
                    Log.d(
                        "AddReviews",
                        "Recensione ${createdReview.reviewId} aggiunta con successo."
                    )
                } else {
                    // Errors for this review
                    Log.e(
                        "AddReviews",
                        "Errore: la recensione non è stata creata per input: $review"
                    )
                }
            }
            Log.d("AddReviews", "Completata l'elaborazione di tutte le recensioni.")
        }
    }

    /*
    fun addAllTripReviews(reviews: List<Review>) {
        for (review in reviews) {
            addNewReview(review)

        }
    }
     */

    val tripReviews = reviewModel.tripReviews
    fun getTripReviews(tripId:Int) = reviewModel.getTripReviews(tripId, viewModelScope)



    

    // Select photos
    private val _selectedUris = MutableStateFlow<List<Uri>>(emptyList())
    val selectedUris: StateFlow<List<Uri>> = _selectedUris

    fun updateSelectedUris(uris: List<Uri>) {
        _selectedUris.value = uris
    }

    fun calculateRatingById(id: Int): Flow<Float> = reviewModel.calculateRatingById(id)

}

object ReviewFactory : ViewModelProvider.Factory{
    private val model:ReviewModel = ReviewModel()

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return when{
            modelClass.isAssignableFrom(ReviewViewModel::class.java)->
                ReviewViewModel(model) as T
            else -> throw IllegalArgumentException("Unknown ViewModel")
        }
    }
}