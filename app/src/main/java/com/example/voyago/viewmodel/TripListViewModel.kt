package com.example.voyago.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.voyago.model.LazyUser
import com.example.voyago.model.Model
import com.example.voyago.model.Trip


class TripListViewModel(val model: Model) : ViewModel() {
    val publishedTrips = model.publishedTrips
    val privateTrips = model.privateTrips

    var selectedTrip: Trip? by mutableStateOf(null)
        private set

    fun selectTrip(trip: Trip) {
        selectedTrip = trip
    }

    fun creatorPublicFilter(id: Int) = model.filterPublishedByCreator(id)
    fun creatorPrivateFilter(id:Int) = model.filterPrivateByCreator(id)

    fun changePublishedStatus(id: Int) {
        model.changePublishedStatus(id)
    }

    fun addNewTrip(newTrip: Trip) {
        model.addTrip(newTrip)
    }

    fun deleteTrip(id: Int) {
        model.deleteTrip(id)
    }

    fun getUser(id: Int): LazyUser? {
        return model.getUserById(id)
    }
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