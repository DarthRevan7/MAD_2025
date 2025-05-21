package com.example.voyago.viewmodel

import android.net.Uri
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
import com.example.voyago.view.*
import kotlinx.coroutines.launch
import java.util.Calendar

class TripViewModel(val model:Model): ViewModel() {
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

    enum class UserAction {
        EDIT_TRIP, CREATE_TRIP, VIEW_TRIP, NOTHING, SEARCHING, FILTER_SELECTION
    }

    //EXPLORE

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

    //Min price and Max price of the database
    fun getMinPrice() = model.minPrice
    fun getMaxPrice() = model.maxPrice
    fun setMaxMinPrice() = model.setMaxMinPrice()

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

    //Trips the logged in user (id=1) can join filter
    var filterUpcomingTrips: Boolean by mutableStateOf(false)
        private set

    fun getUpcomingTripsList(): List<Trip>{
        return model.getUpcomingTrips()
    }

    fun updateUpcomingTripsFilter(isSelected: Boolean) {
        filterUpcomingTrips = isSelected
    }

    //Trips that already happened filter
    var filterCompletedTrips: Boolean by mutableStateOf(false)
        private set

    fun getCompletedTripsList(): List<Trip>{
        return model.getCompletedTrips()
    }

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
        filterDuration, filterGroupSize, filtersTripType, filterUpcomingTrips, filterCompletedTrips, filterBySeats)

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

    //Ask to join a trip or cancel application
    val askedTrips = model.askedTrips
    //fun toggleAskToJoin(tripId: Int) = model.toggleAskToJoin(tripId)
    fun askToJoin(trip: Trip, userId: Int, spots: Int) = model.requestToJoin(trip, userId, spots)
    fun cancelAskToJoin(trip: Trip, userId: Int) = model.cancelRequestToJoin(trip, userId)
    fun syncAskedTrips() = model.syncAskedTripsWithAppliedUsers(1)

    //MY TRIPS

    //List of trips created and published by the logged in user
    val publishedTrips = model.publishedTrips
    fun creatorPublicFilter(id: Int) = model.filterPublishedByCreator(id)

    //List of trips created, but not published by the logged in user
    val privateTrips = model.privateTrips
    fun creatorPrivateFilter(id: Int) = model.filterPrivateByCreator(id)

    //List of trips the logged in user joined
    val joinedTrips = model.joinedTrips
    fun tripUserJoined(userId: Int) = model.getJoinedTrips(userId)

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

    //Mutable list of applications
    var applications = mutableStateOf(emptyMap<UserData, Int>())

    // Participants with spots taken
    fun getTripParticipants(trip: Trip): Map<UserData, Int> {
        return trip.participants.mapNotNull { (userId, spots) ->
            model.getUsers(listOf(userId)).firstOrNull()?.let { user -> user to spots }
        }.toMap()
    }

    // Applicants with requested spots
    fun getTripApplicants(trip: Trip): Map<UserData, Int> {
        return trip.appliedUsers.mapNotNull { (userId, spots) ->
            model.getUsers(listOf(userId)).firstOrNull()?.let { user -> user to spots }
        }.toMap()
    }

    // Rejected users with requested spots
    fun getTripRejectedUsers(trip: Trip): Map<UserData, Int> {
        return trip.rejectedUsers.mapNotNull { (userId, spots) ->
            model.getUsers(listOf(userId)).firstOrNull()?.let { user -> user to spots }
        }.toMap()
    }

    //Approve an application
    fun acceptApplication(trip: Trip?, userId: Int) {
        if (trip == null || userId !in trip.appliedUsers) return

        val requestedSpots = trip.appliedUsers[userId] ?: return

        val usedSpots = trip.participants.values.sum()

        if (usedSpots + requestedSpots > trip.groupSize) return

        // Accept applicant
        trip.participants = trip.participants + (userId to requestedSpots)
        trip.appliedUsers = trip.appliedUsers - userId

        // Recalculate used spots
        val updatedUsedSpots = trip.participants.values.sum()
        val remainingSpots = trip.groupSize - updatedUsedSpots

        // Reject remaining applicants who can't fit
        val remainingApplicants = trip.appliedUsers.toMap()
        for ((id, spots) in remainingApplicants) {
            if (spots > remainingSpots) {
                trip.appliedUsers = trip.appliedUsers - id
                trip.rejectedUsers = trip.rejectedUsers + (id to spots)
            }
        }

        // Update your applications value accordingly
        applications.value = getTripApplicants(trip)
    }

    //Reject an application
    fun rejectApplication(trip: Trip?, userId: Int) {
        if (trip != null && userId in trip.appliedUsers) {
            val requestedSpots = trip.appliedUsers[userId] ?: return

            trip.appliedUsers = trip.appliedUsers - userId
            trip.rejectedUsers = trip.rejectedUsers + (userId to requestedSpots)

            // Assign just the list of users, ignoring requestedSpots here
            applications.value = getTripApplicants(trip)
        }
    }

    //Add new trip to the database
    fun addNewTrip(newTrip: Trip): Trip {
        val createdTrip = model.createNewTrip(newTrip)
        _selectedTrip.value = createdTrip
        return createdTrip
    }

    //Edit an already existing trip in the database
    fun editExistingTrip(trip: Trip): List<Trip> {
        val updatedList = model.editTrip(trip)
        _selectedTrip.value = updatedList.find { it.id == trip.id }!!
        return updatedList
    }

    //List of activities of a specific trip
    fun getActivities(trip: Trip): List<Activity> {
        return trip.activities.values.flatten()
    }

    //Add an activity to a specific trip
    fun addActivityToSelectedTrip(activity: Activity) {
        model.addActivityToTrip(activity, _selectedTrip.value)?.let { updatedTrip ->
            _selectedTrip.value = updatedTrip
        }
    }

    fun addActivityToTrip(activity: Activity) {
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
    fun deleteActivity(activity: Activity) {
        val trip =  model.removeActivityFromTrip(activity, _selectedTrip.value)!!

        _selectedTrip.value = trip

        if (userAction == UserAction.CREATE_TRIP) {
            newTrip = trip
        } else if (userAction == UserAction.EDIT_TRIP) {
            editTrip = trip
        }

    }

    //Edit a specific activity from a specific trip
    fun editActivity(activityId: Int, updatedActivity: Activity) {
        val trip = model.editActivityInSelectedTrip(activityId, updatedActivity, _selectedTrip.value)!!

        _selectedTrip.value = trip

        if (userAction == UserAction.CREATE_TRIP) {
            newTrip = trip
        } else if (userAction == UserAction.EDIT_TRIP) {
            editTrip = trip
        }

    }

    //Get Trip reviews
    fun getTripReviews(id: Int): List<Review> = model.getTripReviews(id)

    //See if a user reviewed a trip
    fun isReviewed(userId: Int, tripId: Int) = model.isReviewed(userId,tripId)

    //Get review of a trip created by the logged in user
    fun tripReview(userId: Int, tripId :Int) = model.getTripReview(model.getUserDataById(userId).id,
        tripId)

    //Get reviews that a user did for the other participant to a certain trip
    fun getUsersReviewsTrip(userId: Int, tripId: Int) = model.getUsersReviewsTrip(userId, tripId)


    // -------------- PROFILE VIEWMODEL ------------

    //PROFILE

    //Edit user profile
    fun editUserData(updatedUserData: UserData): List<UserData> {
        val updatedList = model.editUserData(updatedUserData)
        return updatedList
    }

    //Get user information
    fun getUserData(id: Int): UserData {
        return model.getUserDataById(id)
    }

    //Get user reviews
    fun getUserReviews(id: Int): List<Review> = model.getUserReviews(id)

    // Camera
    private val _profileImageUri = mutableStateOf<Uri?>(null)
    var profileImageUri: State<Uri?> = _profileImageUri

    fun setProfileImageUri(uri: Uri?) {
        _profileImageUri.value = uri
    }



    //INITIALIZE VIEWMODEL
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

