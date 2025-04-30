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

    //Editing viewModel

    /*
    OLD
    var filterMinPrice:Double by mutableDoubleStateOf(getMinPrice())
        private set
    var filterMaxPrice:Double by mutableDoubleStateOf(getMaxPrice())
        private set


     */

    var filterMinPrice:Double by mutableDoubleStateOf(0.0)
        private set
    var filterMaxPrice:Double by mutableDoubleStateOf(0.0)
        private set

    var durationItems: List<SelectableItem> by mutableStateOf(
        listOf(
            SelectableItem("1-3 days", 1, 3),
            SelectableItem("3-5 days", 3, 5),
            SelectableItem("5-7 days", 5, 7),
            SelectableItem("7-10 days", 7, 10),
            SelectableItem("10-15 days", 10, 15),
            SelectableItem("15-20 days", 15, 20),
            SelectableItem("> 20 days", 20, Int.MAX_VALUE)
        )
    )
    var groupSizeItems: List<SelectableItem> by mutableStateOf(
        listOf(
            SelectableItem("2-3 people", 2, 3),
            SelectableItem("3-5 people", 3, 5),
            SelectableItem("5-7 people", 5, 7),
            SelectableItem("7-10 people", 7, 10),
            SelectableItem("10-15 people", 10, 15),
            SelectableItem("> 15 people", 15, Int.MAX_VALUE)
        )
    )
    /*
    OLD
    var filtersTripType: List<SelectableItem> by mutableStateOf(emptyList())
        private set


     */
    var filtersTripType: List<SelectableItem> by mutableStateOf(
        listOf(
            SelectableItem("Adventure", -1, -1, typeTravel = TypeTravel.ADVENTURE),
            SelectableItem("Culture", -1, -1, typeTravel = TypeTravel.CULTURE),
            SelectableItem("Party", -1, -1, typeTravel = TypeTravel.PARTY),
            SelectableItem("Relax", -1, -1, typeTravel = TypeTravel.RELAX)
        )
    )
        private set

    /*
    OLD
    fun updateFilterDuration(list: List<SelectableItem>) {
        filterDuration = model.setRange(list)
        println("FilterDuration Pair: (${filterDuration.first}, ${filterDuration.second})")
    }

    fun updateFilterGroupSize(list: List<SelectableItem>) {
        filterGroupSize = model.setRange(list)
    }

    fun updateFiltersTripType(list: List<SelectableItem>) {
        filtersTripType = list
    }
    */

    fun updateFilterDuration(list: List<SelectableItem>) {
        durationItems = list
        filterDuration = model.setRange(list)
        println("FilterDuration Pair: (${filterDuration.first}, ${filterDuration.second})")
    }

    fun updateFilterGroupSize(list: List<SelectableItem>) {
        groupSizeItems = list
        filterGroupSize = model.setRange(list)
    }

    fun updateFiltersTripType(list: List<SelectableItem>) {
        filtersTripType = list
    }

    fun setMaxMinPrice() {
        model.setMaxMinPrice()
        filterMinPrice = model.minPrice
        filterMaxPrice = model.maxPrice
    }


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



    fun updateFilterPriceRange(minPrice:Double, maxPrice:Double) {
        filterMaxPrice = maxPrice
        filterMinPrice = minPrice
    }

    var filterDuration: Pair<Int,Int> by mutableStateOf(Pair(-1,-1))
        private set


    var filterGroupSize: Pair<Int,Int> by mutableStateOf(Pair(-1,-1))
        private set


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
    //fun setMaxMinPrice() = model.setMaxMinPrice()

    fun addActivityToSelectedTrip(activity: Activity) {
        model.addActivityToTrip(activity, currentTrip)?.let { updatedTrip ->
            currentTrip = updatedTrip
        }
    }

    fun deleteActivity(activity: Activity) {
        currentTrip = model.removeActivityFromTrip(activity, currentTrip)
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