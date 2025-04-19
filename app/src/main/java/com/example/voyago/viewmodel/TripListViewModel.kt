package com.example.voyago.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.voyago.model.Model

class TripListViewModel(val model: Model) : ViewModel() {
    val publishedTrips = model.publishedTrips
    val privateTrips = model.privateTrips
    fun creatorPublicFilter(id: Int) = model.filterPublishedByCreator(id)
    fun creatorPrivateFilter(id:Int) = model.filterPrivateByCreator(id)
}

object Factory : ViewModelProvider.Factory{
    private val model:Model = Model()

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when{
            modelClass.isAssignableFrom(TripListViewModel::class.java)->
                TripListViewModel(model) as T
            else -> throw IllegalArgumentException("Unknown ViewModel")
        }
    }
}