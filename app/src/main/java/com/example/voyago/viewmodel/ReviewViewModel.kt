package com.example.voyago.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.voyago.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class ReviewViewModel(val reviewModel:ReviewModel): ViewModel() {

    fun addNewReview(newReview: Review): Review {
        val createdReview = reviewModel.createNewReview(newReview)
        println("Review: ${createdReview.reviewId} | ${createdReview.score} - ${createdReview.title} - ${createdReview.comment}")
        return createdReview
    }

    fun addAllTripReviews(reviews: List<Review>) {
        for (review in reviews) {
            addNewReview(review)

        }
    }

    // Select photos
    private val _selectedUris = MutableStateFlow<List<Uri>>(emptyList())
    val selectedUris: StateFlow<List<Uri>> = _selectedUris

    fun updateSelectedUris(uris: List<Uri>) {
        _selectedUris.value = uris
    }

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