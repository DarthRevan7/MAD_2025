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

    //Min price and Max price of the database (still useful?)
    fun getMinPrice() = model.minPrice
    fun getMaxPrice() = model.maxPrice
    fun setMaxMinPrice() = model.setMaxMinPrice()

    //Use in the new Trip interface
    var newTrip:Trip = Trip()

    //Use in the edit trip interface
    var editTrip:Trip = Trip()

    //Use in the select trip interface (trip detail)
    private val _selectedTrip = mutableStateOf<Trip?>(null)
    val selectedTrip: State<Trip?> = _selectedTrip

    fun setSelectedTrip(trip: Trip) {
        _selectedTrip.value = trip
    }


    //Identify what the user is doing
    var userAction:UserAction = UserAction.NOTHING

    public enum class UserAction {
        EDIT_TRIP, CREATE_TRIP, VIEW_TRIP, NOTHING, SEARCHING, FILTER_SELECTION
    }

    //List of all the published trips
    val publishedTrips = model.publishedTrips

    //List of all the private trips
    val privateTrips = model.privateTrips

    //List of the trips the logged in user (id=1) asked to join
    val askedTrips = model.askedTrips

    //Filtered list of trips
    val filteredList = model.filteredList

    //Destination filter
    var filterDestination: String by mutableStateOf("")
        private set

    fun updateFilterDestination(str: String) {
        filterDestination = str
    }

    //List of destination of the trip present in the database
    fun allDestinations() = model.getDestinations()

    //Price Range filter
    private val _priceBounds = mutableStateOf(0f..1000f)
    val priceBounds: State<ClosedFloatingPointRange<Float>> = _priceBounds

    private val _selectedPriceRange = mutableStateOf(0f..1000f)
    val selectedPriceRange: State<ClosedFloatingPointRange<Float>> = _selectedPriceRange

    var filterMinPrice: Double by mutableDoubleStateOf(0.0)
        private set
    var filterMaxPrice: Double by mutableDoubleStateOf(0.0)
        private set

    fun updateUserSelection(newRange: ClosedFloatingPointRange<Float>) {
        _selectedPriceRange.value = newRange
    }

    fun updateFilterPriceRange(minPrice: Double, maxPrice: Double) {
        filterMaxPrice = maxPrice
        filterMinPrice = minPrice
    }

    //Duration filter
    var filterDuration: Pair<Int, Int> by mutableStateOf(Pair(-1, -1))
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
        private set

    fun updateFilterDuration(list: List<SelectableItem>) {
        durationItems = list
        filterDuration = model.setRange(list)
    }

    //Group size filter
    var filterGroupSize: Pair<Int, Int> by mutableStateOf(Pair(-1, -1))
        private set

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
        private set

    fun updateFilterGroupSize(list: List<SelectableItem>) {
        groupSizeItems = list
        filterGroupSize = model.setRange(list)
    }

    //Trip Type filter
    var filtersTripType: List<SelectableItem> by mutableStateOf(
        listOf(
            SelectableItem("Adventure", -1, -1, typeTravel = TypeTravel.ADVENTURE),
            SelectableItem("Culture", -1, -1, typeTravel = TypeTravel.CULTURE),
            SelectableItem("Party", -1, -1, typeTravel = TypeTravel.PARTY),
            SelectableItem("Relax", -1, -1, typeTravel = TypeTravel.RELAX)
        )
    )
        private set

    fun updateFiltersTripType(list: List<SelectableItem>) {
        filtersTripType = list
    }

    //Trips you can no longer join filter
    var filterCompletedTrips: Boolean by mutableStateOf(false)
        private set

    fun updateCompletedTripsFilter(isSelected: Boolean) {
        filterCompletedTrips = isSelected
    }

    //Seats filter
    var filterBySeats: Int by mutableIntStateOf(0)
        private set

    fun updateFilterBySeats(seats: Int) {
        filterBySeats = seats
    }

    //Apply selected filters
    fun applyFilters() = model.filterFunction(tripList, filterDestination, filterMinPrice, filterMaxPrice,
        filterDuration, filterGroupSize, filtersTripType, filterCompletedTrips, filterBySeats)

    //Reset filters
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

    //Update list of published trips after filter application
    var tripList: List<Trip> = emptyList()
        private set

    fun updatePublishedTrip() {
        tripList = model.getAllPublishedTrips()
        applyFilters()
    }

    //List of trips created and published by the logged in user (id=1)
    fun creatorPublicFilter() = model.filterPublishedByCreator(1)

    //List of trips created, but not published by the logged in user (id=1)
    fun creatorPrivateFilter() = model.filterPrivateByCreator(1)

    //Import an already published trip as a private trip of the logged in user (id=1)
    fun addImportedTrip(photo: String, title: String, destination: String, startDate: Calendar,
                        endDate: Calendar, estimatedPrice: Double, groupSize: Int,
                        activities: Map<Calendar, List<Activity>>,
                        typeTravel: List<TypeTravel>, creatorId: Int,
                        published: Boolean): List<Trip> =
        model.importTrip(photo, title, destination, startDate, endDate, estimatedPrice,
            groupSize, activities, typeTravel, creatorId, published)

    //Make published or private a trip
    fun changePublishedStatus(id: Int) = model.changePublishedStatus(id)

    //Delete a trip from the database
    fun deleteTrip(id: Int) = model.deleteTrip(id)

    //List of user that are taking part to the trip
    fun getTripParticipants(trip: Trip): List<LazyUser> = model.getUsers(trip.participants)

    //List of user that asked to join the trip
    fun getTripApplicants(trip: Trip): List<LazyUser> = model.getUsers(trip.appliedUsers)

    //Add new trip to the database
    fun addNewTrip(newTrip: Trip): Trip {
        val createdTrip = model.createNewTrip(newTrip)
        _selectedTrip.value = createdTrip
        return createdTrip
    }

    //Edit an already existing trip in the database
    fun editNewTrip(newTrip: Trip): List<Trip> {
        val updatedList = model.editTrip(newTrip)
        _selectedTrip.value = updatedList.find { it.id == newTrip.id }!!
        return updatedList
    }

    //Ask to join a trip or cancel application
    fun toggleAskToJoin(tripId: Int) = model.toggleAskToJoin(tripId)

    //List of activities of a specific trip
    fun getActivities(trip: Trip): List<Activity> {
        return trip.activities.values.flatten()
    }

    //Add an activity to a specific trip
    fun addActivityToSelectedTrip(activity: Trip.Activity) {
        model.addActivityToTrip(activity, _selectedTrip.value)?.let { updatedTrip ->
            _selectedTrip.value = updatedTrip
        }
    }

    fun addActivityToTrip(activity: Trip.Activity) {
        //Creating a new trip
        if(userAction == UserAction.CREATE_TRIP) {
            model.addActivityToTrip(activity, newTrip)?.let { updatedTrip ->
                newTrip = updatedTrip
                _selectedTrip.value = newTrip
            }
        } else if(userAction == UserAction.EDIT_TRIP) {
            //I am editing an existing trip
            model.addActivityToTrip(activity, editTrip)?.let { updatedTrip ->
                editTrip = updatedTrip
                _selectedTrip.value = editTrip
            }
        }
    }

    //Delete activity from a specific trip
    fun deleteActivity(activity: Trip.Activity) {
        val trip =  model.removeActivityFromTrip(activity, _selectedTrip.value)!!

        _selectedTrip.value = trip

        if (userAction == UserAction.CREATE_TRIP) {
            newTrip = trip
        } else if (userAction == UserAction.EDIT_TRIP) {
            editTrip = trip
        }

    }

    //Edit a specific activity from a specific trip
    fun editActivity(activityId: Int, updatedActivity: Trip.Activity) {
        val trip = model.editActivityInSelectedTrip(activityId, updatedActivity, _selectedTrip.value)!!

        _selectedTrip.value = trip

        if (userAction == UserAction.CREATE_TRIP) {
            newTrip = trip
        } else if (userAction == UserAction.EDIT_TRIP) {
            editTrip = trip
        }

    }
}

object Factory : ViewModelProvider.Factory{
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

