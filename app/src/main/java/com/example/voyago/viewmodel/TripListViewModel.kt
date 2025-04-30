package com.example.voyago.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.voyago.view.SelectableItem
import java.util.Calendar


class TripListViewModel(val model: Model) : ViewModel() {
    val publishedTrips = model.publishedTrips
    val privateTrips = model.privateTrips
    val allPublishedTrips = model.allPublishedTrips
    val askedTrips = model.askedTrips
    val filteredList = model.filteredList

    var selectedTrip: Trip? by mutableStateOf(null)
        private set

    var currentTrip: Trip? by mutableStateOf(null)
        private set

    fun selectTrip(trip: Trip) {
        currentTrip = trip
        selectedTrip = trip
    }

//    fun resetCurrentTrip() {
//        currentTrip = null
//    }

    var filterDestination: String by mutableStateOf("")
        private set

    fun updateFilterDestination(str:String) {
        filterDestination = str
    }

    var filterMinPrice:Double by mutableDoubleStateOf(getMinPrice())
        private set
    var filterMaxPrice:Double by mutableDoubleStateOf(getMaxPrice())
        private set

    fun updateFilterPriceRange(minPrice:Double, maxPrice:Double) {
        filterMaxPrice = maxPrice
        filterMinPrice = minPrice
    }

    var filterDuration: Pair<Int,Int> by mutableStateOf(Pair(-1,-1))
        private set

    fun updateFilterDuration(list: List<SelectableItem>) {
        filterDuration = model.setRange(list)
        println("FilterDuration Pair: (${filterDuration.first}, ${filterDuration.second})")
    }

    var filterGroupSize: Pair<Int,Int> by mutableStateOf(Pair(-1,-1))
        private set

    fun updateFilterGroupSize(list: List<SelectableItem>) {
        filterGroupSize = model.setRange(list)
    }

    var filtersTripType: List<SelectableItem> by mutableStateOf(emptyList())
        private set

    fun updateFiltersTripType(list: List<SelectableItem>) {
        filtersTripType = list
    }

    var filterCompletedTrips: Boolean by mutableStateOf(false)
        private set

    fun updateCompletedTripsFilter(isSelected: Boolean) {
        filterCompletedTrips = isSelected
    }

    var filterBySeats: Int by mutableIntStateOf(0)
        private set

    fun updateFilterBySeats(seats: Int) {
        filterBySeats = seats
    }

    fun creatorPublicFilter() = model.filterPublishedByCreator(1)
    fun creatorPrivateFilter() = model.filterPrivateByCreator(1)

    var tripList: List<Trip> = emptyList()
        private set

    fun updatePublishedTrip() {
        tripList = model.getAllPublishedTrips()
        applyFilters()
    }

    fun changePublishedStatus(id: Int) = model.changePublishedStatus(id)

    fun deleteTrip(id: Int) = model.deleteTrip(id)

    fun getTripParticipants(trip: Trip): List<LazyUser> = model.getUsers(trip.participants)
    fun getTripApplicants(trip: Trip): List<LazyUser> = model.getUsers(trip.appliedUsers)

    fun addImportedTrip(photo: String, title: String, destination: String, startDate: Calendar,
                      endDate: Calendar, estimatedPrice: Double, groupSize: Int,
                      activities: Map<Calendar, List<Activity>>,
                      typeTravel: List<TypeTravel>, creatorId: Int,
                      published: Boolean): List<Trip> =
        model.importTrip(photo, title, destination, startDate, endDate, estimatedPrice,
            groupSize, activities, typeTravel, creatorId, published)

    fun addNewTrip(newTrip: Trip): Trip {
        val createdTrip = model.createNewTrip(newTrip)
        currentTrip = createdTrip
        selectedTrip = createdTrip
        return createdTrip
    }

    fun editNewTrip(newTrip: Trip): List<Trip> {
        val updatedList = model.editTrip(newTrip)
        currentTrip = updatedList.find { it.id == newTrip.id }
        selectedTrip = currentTrip
        return updatedList
    }


    fun toggleAskToJoin(tripId: Int) = model.toggleAskToJoin(tripId)

    fun allDestinations() = model.getDestinations()

    fun getActivities(trip: Trip): List<Activity> {
        return trip.activities.values.flatten()
    }

    fun getMinPrice() = model.minPrice
    fun getMaxPrice() = model.maxPrice
    fun setMaxMinPrice() = model.setMaxMinPrice()

    fun addActivityToSelectedTrip(activity: Trip.Activity) {
        model.addActivityToTrip(activity, currentTrip)?.let { updatedTrip ->
            currentTrip = updatedTrip
        }
    }



    fun applyFilters() = model.filterFunction(tripList, filterDestination, filterMinPrice, filterMaxPrice,
        filterDuration, filterGroupSize, filtersTripType, filterCompletedTrips, filterBySeats)
}

object Factory : ViewModelProvider.Factory{
    private val model:Model = Model()

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return when{
            modelClass.isAssignableFrom(TripListViewModel::class.java)->
                TripListViewModel(model) as T
            else -> throw IllegalArgumentException("Unknown ViewModel")
        }
    }
}