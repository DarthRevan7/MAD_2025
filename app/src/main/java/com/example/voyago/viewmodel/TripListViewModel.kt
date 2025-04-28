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
import com.example.voyago.model.Trip.Activity
import com.example.voyago.model.TypeTravel
import java.util.Calendar


class TripListViewModel(val model: Model) : ViewModel() {
    val publishedTrips = model.publishedTrips
    val privateTrips = model.privateTrips
    val allPublishedTrips = model.allPublishedTrips
    val askedTrips = model.askedTrips

    var selectedTrip: Trip? by mutableStateOf(null)
        private set

    fun selectTrip(trip: Trip) {
        selectedTrip = trip
    }


    fun creatorPublicFilter() = model.filterPublishedByCreator(1)
    fun creatorPrivateFilter() = model.filterPrivateByCreator(1)
    fun updatePublishedTrip() = model.getAllPublishedTrips()

    fun changePublishedStatus(id: Int) = model.changePublishedStatus(id)

    fun deleteTrip(id: Int) = model.deleteTrip(id)

    fun getTripParticipants(trip: Trip): List<LazyUser> = model.getUsers(trip.participants)
    fun getTripApplicants(trip: Trip): List<LazyUser> = model.getUsers(trip.appliedUsers)

    fun addNewTrip(photo: String, title: String, destination: String, startDate: Calendar,
                      endDate: Calendar, estimatedPrice: Double, groupSize: Int,
                      activities: Map<Calendar, List<Activity>>,
                      typeTravel: List<TypeTravel>, creatorId: Int,
                      published: Boolean): List<Trip> =
        model.createNewTrip(photo, title, destination, startDate, endDate, estimatedPrice,
            groupSize, activities, typeTravel, creatorId, published)

    fun toggleAskToJoin(tripId: Int) = model.toggleAskToJoin(tripId)

    fun allDestinations() = model.getDestinations()

    fun getActivities(trip: Trip): List<Activity> {
        return trip.activities.values.flatten()
    }

    fun addActivityToTrip(trip: Trip, activity: Activity) {
        val activitiesForDate = trip.activities[activity.date]?.toMutableList() ?: mutableListOf()
        activitiesForDate.add(activity)
        trip.activities = trip.activities.toMutableMap().apply {
            put(activity.date, activitiesForDate)
        }
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