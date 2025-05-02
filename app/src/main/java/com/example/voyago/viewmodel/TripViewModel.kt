package com.example.voyago.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.voyago.model.*
import com.example.voyago.model.Trip.Activity
import com.example.voyago.viewmodel.*
import com.example.voyago.view.*
import kotlinx.coroutines.launch
import java.util.Calendar

class TripViewModel(val model:Model): ViewModel() {

    //Use in the new Trip interface
    var newTrip:Trip = Trip()
    //Use in the edit trip interface
    var editTrip:Trip = Trip()
    //Use in the select trip interface (trip detail)
    var selectedTrip:Trip = Trip()

    //Identify what the user is doing
    var userAction:UserAction = UserAction.NOTHING


    public enum class UserAction {
        EDIT_TRIP, CREATE_TRIP, VIEW_TRIP, NOTHING, SEARCHING, FILTER_SELECTION
    }

    private val _priceBounds = mutableStateOf(0f..1000f)
    val priceBounds: State<ClosedFloatingPointRange<Float>> = _priceBounds

    private val _selectedPriceRange = mutableStateOf(0f..1000f)
    val selectedPriceRange: State<ClosedFloatingPointRange<Float>> = _selectedPriceRange

    // Other properties and functions
    var filterMinPrice: Double by mutableDoubleStateOf(0.0)
        private set
    var filterMaxPrice: Double by mutableDoubleStateOf(0.0)
        private set

    val publishedTrips = model.publishedTrips
    val privateTrips = model.privateTrips
    val allPublishedTrips = model.allPublishedTrips
    val askedTrips = model.askedTrips
    val filteredList = model.filteredList

    var filterDestination: String by mutableStateOf("")
        private set

    var filterDuration: Pair<Int, Int> by mutableStateOf(Pair(-1, -1))
        private set

    var filterGroupSize: Pair<Int, Int> by mutableStateOf(Pair(-1, -1))
        private set

    var filterCompletedTrips: Boolean by mutableStateOf(false)
        private set








    fun updateFilterDestination(str: String) {
        filterDestination = str
    }

    fun updateFilterPriceRange(minPrice: Double, maxPrice: Double) {
        filterMaxPrice = maxPrice
        filterMinPrice = minPrice
    }




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
        selectedTrip = createdTrip
        return createdTrip
    }

    fun editNewTrip(newTrip: Trip): List<Trip> {
        val updatedList = model.editTrip(newTrip)
        selectedTrip = updatedList.find { it.id == newTrip.id }!!
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
        model.addActivityToTrip(activity, selectedTrip)?.let { updatedTrip ->
            selectedTrip = updatedTrip
        }
    }

    fun deleteActivity(activity: Trip.Activity) {
        selectedTrip = model.removeActivityFromTrip(activity, selectedTrip)!!
    }

    fun editActivity(activityId: Int, updatedActivity: Trip.Activity) {
        selectedTrip = model.editActivityInSelectedTrip(activityId, updatedActivity, selectedTrip)!!
    }

    fun applyFilters() = model.filterFunction(tripList, filterDestination, filterMinPrice, filterMaxPrice,
        filterDuration, filterGroupSize, filtersTripType, filterCompletedTrips, filterBySeats)

    fun resetFilters() {
        filterDestination = ""

        updateFilterPriceRange(0.0,0.0)
        model.setMaxMinPrice()
        val minPrice = model.minPrice.toFloat()
        val maxPrice = model.maxPrice.toFloat()
        val bounds = minPrice..maxPrice
        _selectedPriceRange.value = bounds

        filterDuration = Pair(-1,-1)
        filterGroupSize = Pair(-1,-1)
        durationItems.forEach { it.isSelected = false }
        groupSizeItems.forEach { it.isSelected = false }
        filtersTripType.forEach { it.isSelected = false }
        filterCompletedTrips = false
        filterBySeats = 0
    }



    // Initialize ViewModel, fetching min and max price from model
    init {
        viewModelScope.launch {
            model.setMaxMinPrice()

            val minPrice = model.minPrice.toFloat()
            val maxPrice = model.maxPrice.toFloat()

            if (minPrice.isFinite() && maxPrice.isFinite() && maxPrice >= minPrice) {
                val bounds = minPrice..maxPrice
                _priceBounds.value = bounds
                _selectedPriceRange.value = bounds
            } else {
                _priceBounds.value = 0f..1000f
                _selectedPriceRange.value = 0f..1000f
            }
        }
    }



    //Stuff for UI
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

    var filtersTripType: List<SelectableItem> by mutableStateOf(
        listOf(
            SelectableItem("Adventure", -1, -1, typeTravel = TypeTravel.ADVENTURE),
            SelectableItem("Culture", -1, -1, typeTravel = TypeTravel.CULTURE),
            SelectableItem("Party", -1, -1, typeTravel = TypeTravel.PARTY),
            SelectableItem("Relax", -1, -1, typeTravel = TypeTravel.RELAX)
        )
    )
        private set


}

object NewFactory : ViewModelProvider.Factory{
    private val model:Model = Model()

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return when{
            modelClass.isAssignableFrom(TripViewModel::class.java)->
                TripViewModel(model) as T
            else -> throw IllegalArgumentException("Unknown ViewModel")
        }
    }
}

