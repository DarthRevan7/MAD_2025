package com.example.voyago.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.voyago.model.Model
import com.example.voyago.model.Trip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TripListViewModel(private val model: Model) : ViewModel() {

    private val _tripList = MutableStateFlow<List<Trip>>(model.tripList)
    val tripList: StateFlow<List<Trip>> = _tripList.asStateFlow()

    fun creatorPublishedFilter(id: Int) {
        _tripList.value = model.filterPublishedByCreator(id)
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