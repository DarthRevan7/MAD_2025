package com.example.voyago.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.voyago.model.Model
import com.example.voyago.model.Review


class ReviewViewModel(val model:Model): ViewModel() {

    fun addNewReview(newReview: Review): Review {
        val createdReview = model.createNewReview(newReview)
        println("Review: ${createdReview.reviewId} | ${createdReview.score} - ${createdReview.title} - ${createdReview.comment}")
        return createdReview
    }

    fun addAllTripReviews(reviews: List<Review>) {
        for (review in reviews) {
            addNewReview(review)

        }
    }

}

object ReviewFactory : ViewModelProvider.Factory{
    private val model:Model = Model()

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return when{
            modelClass.isAssignableFrom(ReviewViewModel::class.java)->
                ReviewViewModel(model) as T
            else -> throw IllegalArgumentException("Unknown ViewModel")
        }
    }
}