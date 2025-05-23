package com.example.voyago.model

import android.util.Log
import com.example.voyago.model.Trip.Activity
import com.example.voyago.model.Trip.TripStatus
import com.example.voyago.view.SelectableItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar
import kotlin.collections.contains
import kotlin.collections.forEach

class TripModel {

    private var _tripList = privateTripList
    var tripList = _tripList

    //SUBSET OF THE TRIP LIST

    //Trips published by the logged in user
    private val _publishedTrips = MutableStateFlow<List<Trip>>(emptyList())
    val publishedTrips: StateFlow<List<Trip>> = _publishedTrips

    fun filterPublishedByCreator(id: Int): List<Trip> {
        _publishedTrips.value = _tripList.value.filter { it.creatorId == id && it.published }
        return _publishedTrips.value
    }

    //Trips created by the user but not published
    private val _privateTrips = MutableStateFlow<List<Trip>>(emptyList())
    val privateTrips: StateFlow<List<Trip>> = _privateTrips

    fun filterPrivateByCreator(id: Int): List<Trip> {
        _privateTrips.value = _tripList.value.filter { it.creatorId == id && !it.published }
        return _privateTrips.value
    }

    //List of all the published trips
    private val _allPublishedTrips = MutableStateFlow<List<Trip>>(emptyList())
    val allPublishedTrips: StateFlow<List<Trip>> = _allPublishedTrips

    fun getAllPublishedTrips(): List<Trip> {
        val published = _tripList.value.filter { it.published }
        _allPublishedTrips.value = published
        return published
    }

    fun updateTripStatus(trip: Trip, status: TripStatus) {
        _tripList.value = _tripList.value.map {
            if (it.id == trip.id) it.copy(status = status) else it
        }
    }


    //List of trips the logged in user (id=1) joined
    private val _joinedTrips = MutableStateFlow<List<Trip>>(emptyList())
    val joinedTrips: StateFlow<List<Trip>> = _joinedTrips

    fun getJoinedTrips(userId :Int): List<Trip> {
        val joined = _tripList.value.filter { it.participants.containsKey(userId) && it.creatorId != userId }
        _joinedTrips.value = joined
        return joined
    }

    //List of trips after the application of the filters
    private val _filteredList = MutableStateFlow<List<Trip>>(emptyList())
    val filteredList: StateFlow<List<Trip>> = _filteredList

    fun filterFunction(list: List<Trip>, filterDestination: String, filterMinPrice: Double,
                       filterMaxPrice: Double, filterDuration: Pair<Int,Int>,
                       filterGroupSize: Pair<Int,Int>, filtersTripType: List<SelectableItem>,
                       filterUpcomingTrips: Boolean, filterCompletedTrips: Boolean,
                       filterBySeats: Int) {
        Log.d("FilterFunction", "Filtering with destination: $filterDestination, minPrice: $filterMinPrice, maxPrice: $filterMaxPrice, filterDuration: $filterDuration, filterGroupSize: $filterGroupSize, filterTripType: $filtersTripType")
        var filtered = list.filter { trip ->
            Log.d("FilterFunction", "Checking trip: ${trip.title}")
            val destination = filterDestination.isBlank() || trip.destination.contains(filterDestination, ignoreCase = true)
            //val price = (filterMinPrice == Double.MAX_VALUE && filterMaxPrice == Double.MIN_VALUE) || trip.estimatedPrice in filterMinPrice..filterMaxPrice

            val duration = (filterDuration.first == -1 && filterDuration.second == -1) ||
                    (trip.tripDuration() in filterDuration.first..filterDuration.second)

            val groupSize = (filterGroupSize.first == -1 && filterGroupSize.second == -1) ||
                    (trip.groupSize in filterGroupSize.first..filterGroupSize.second)

            // 0-0 => No filter for price
            val price = if (filterMinPrice == 0.0 && filterMaxPrice == 0.0) {
                true
            } else {
                trip.estimatedPrice in filterMinPrice..filterMaxPrice // Aplly filter with given interval
            }
            Log.d("FilterFunction", "Checking price for trip ${trip.title}: estimatedPrice=${trip.estimatedPrice}, filterMinPrice=$filterMinPrice, filterMaxPrice=$filterMaxPrice, priceCondition=$price")


            val completed = !filterCompletedTrips || trip.status == TripStatus.COMPLETED//|| !trip.canJoin()
            val canJoin = !filterUpcomingTrips || trip.loggedInUserCanJoin(1)
            val spots = trip.availableSpots() >= filterBySeats

            Log.d("FilterFunction", "Conditions: destination=$destination, price=$price, duration=$duration, groupSize=$groupSize, completed=$completed, spots=$spots")

            trip.published && destination && price && duration && groupSize && canJoin && completed
                    && spots
        }
        Log.d("FilterFunction", "Filtered trips: $filtered")

        if(!filtersTripType.any{ it.isSelected }) {
            _filteredList.value = filtered
            return
        } else {
            var filteredAgain = filtered.filter { trip ->
                filtersTripType.any {
                    trip.typeTravel.contains(it.typeTravel) && it.isSelected
                }

            }
            _filteredList.value = filteredAgain
            return
        }
        _filteredList.value = filtered
    }

    //FUNCTION FOR THE APPLICATION OF THE FILTERS

    //Get the list of all the destinations present in the database for the search bar
    fun getDestinations(): List<String> {
        return _tripList.value.map { it.destination }.distinct()
    }

    //MaxPrice and min Price of the database
    private val _minPrice = Double.MAX_VALUE
    var minPrice: Double = _minPrice

    private val _maxPrice = Double.MIN_VALUE
    var maxPrice: Double = _maxPrice

    fun setMaxMinPrice() {
        _tripList.value.forEach { trip ->
            if (trip.estimatedPrice < minPrice) {
                minPrice = trip.estimatedPrice
            }
            if (trip.estimatedPrice > maxPrice) {
                maxPrice = trip.estimatedPrice
            }
        }
    }

    //Range of the Price slider
    fun setRange(list: List<SelectableItem>): Pair<Int, Int> {
        var min = Int.MAX_VALUE
        var max = Int.MIN_VALUE

        list.forEach { item ->
            if (item.isSelected) {
                if (item.min < min) {
                    min = item.min
                }
                if (item.max > max) {
                    max = item.max
                }
            }
        }

        if(min == Int.MAX_VALUE && max == Int.MIN_VALUE) {
            min = -1
            max = -1
        }

        println("SetRange min = $min")
        println("SetRange max = $max")

        return Pair(min, max)
    }

    //Get list of completed trips
    fun getCompletedTrips(): List<Trip> {
        return _tripList.value.filter { it.status == TripStatus.COMPLETED }
    }

    //Get list of upcoming trips
    fun getUpcomingTrips(): List<Trip> {
        return _tripList.value.filter { it.status == TripStatus.NOT_STARTED }
    }

    //CREATE A TRIP

    private var nextId = 9

    //Create a new trip
    fun createNewTrip(newTrip: Trip): Trip {
        val tripWithId = newTrip.copy(
            id = nextId++,
        )
        _tripList.value = _tripList.value + tripWithId
        return tripWithId
    }

    //A copy of the trip gets created in "My Trips" section as a private trip
    fun importTrip(
        photo: String, title: String, destination: String, startDate: Calendar,
        endDate: Calendar, estimatedPrice: Double, groupSize: Int,
        activities: Map<Calendar, List<Activity>>,
        typeTravel: List<TypeTravel>, creatorId: Int,
        published: Boolean
    ): List<Trip> {
        val newTrip = Trip(
            id = nextId++,
            photo = photo,
            title = title,
            destination = destination,
            startDate = startDate,
            endDate = endDate,
            estimatedPrice = estimatedPrice,
            groupSize = groupSize,
            participants = mapOf(creatorId to 1),
            activities = activities,
            status = TripStatus.NOT_STARTED,
            typeTravel = typeTravel,
            creatorId = creatorId,
            appliedUsers = emptyMap(),
            rejectedUsers = emptyMap(),
            published = published
        )
        _tripList.value = _tripList.value + newTrip

        return _tripList.value
    }

    //EDIT TRIP

    //Edit trip information
    fun editTrip(updatedTrip: Trip): List<Trip> {
        _tripList.value = _tripList.value.map {
            if (it.id == updatedTrip.id) updatedTrip else it
        }
        return _tripList.value
    }

    //Change the published status of a trip
    fun changePublishedStatus(id: Int) {
        _tripList.value = _tripList.value.map {
            if (it.id == id) {
                it.copy(published = !it.published)  // Toggle the published status
            } else {
                it
            }
        }
    }

    //ACTIVITY MANAGEMENT

    //Add an activity
    fun addActivityToTrip(activity: Activity, trip: Trip?): Trip? {
        if (trip == null) return null

        val updatedTrip = trip.copy(
            activities = trip.activities
                .toMutableMap()
                .apply {
                    val dateKey = activity.date
                    val updatedList = getOrDefault(dateKey, emptyList()) + activity
                    put(dateKey, updatedList)
                }
        )

        _tripList.value = _tripList.value.map {
            if (it.id == updatedTrip.id) updatedTrip else it
        }

        return updatedTrip
    }

    //Edit an Activity
    fun editActivityInSelectedTrip(
        activityId: Int,
        updatedActivity: Activity,
        trip: Trip?
    ): Trip? {
        if (trip == null) return null

        val originalActivities = trip.activities.toMutableMap()

        //Find and remove the old activity
        var found = false
        for ((date, activities) in originalActivities) {
            if (activities.any { it.id == activityId }) {
                val newList = activities.filter { it.id != activityId }
                if (newList.isEmpty()) {
                    originalActivities.remove(date)
                } else {
                    originalActivities[date] = newList
                }
                found = true
                break
            }
        }

        if (!found) return trip // nothing changed

        //Add the updated activity to the new date key
        val newDateKey = updatedActivity.date
        val updatedList = originalActivities.getOrDefault(newDateKey, emptyList()) + updatedActivity
        originalActivities[newDateKey] = updatedList

        val updatedTrip = trip.copy(activities = originalActivities)

        _tripList.value = _tripList.value.map {
            if (it.id == updatedTrip.id) updatedTrip else it
        }

        return updatedTrip
    }

    //Delete an activity
    fun removeActivityFromTrip(activity: Activity, trip: Trip?): Trip? {
        if (trip == null) return null

        val dateKey = activity.date
        val updatedActivities = trip.activities
            .toMutableMap()
            .apply {
                val updatedList = getOrDefault(dateKey, emptyList()) - activity
                if (updatedList.isEmpty()) {
                    remove(dateKey)
                } else {
                    put(dateKey, updatedList)
                }
            }
            .toMap()

        val updatedTrip = trip.copy(activities = updatedActivities)

        _tripList.value = _tripList.value.map {
            if (it.id == updatedTrip.id) updatedTrip else it
        }

        return updatedTrip
    }

    //DELETE A TRIP

    //Delete a trip
    fun deleteTrip(id: Int) {
        _tripList.value = _tripList.value.filter { it.id != id }
    }

    //MANAGEMENT OF APPLICATIONS TO TRIPS

    private val _askedTrips = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val askedTrips: StateFlow<Map<Int, Int>> = _askedTrips

    fun requestToJoin(trip: Trip, userId :Int, spots: Int) {
        val updatedAppliedUsers = trip.appliedUsers.toMutableMap()
        updatedAppliedUsers[userId] = spots

        trip.appliedUsers = updatedAppliedUsers
        _askedTrips.value = _askedTrips.value + (trip.id to spots)
    }

    fun cancelRequestToJoin(trip: Trip, userId: Int) {
        val updatedAppliedUsers = trip.appliedUsers.toMutableMap()
        updatedAppliedUsers.remove(userId)

        trip.appliedUsers = updatedAppliedUsers
        _askedTrips.value = _askedTrips.value - trip.id
    }

    fun syncAskedTripsWithAppliedUsers(userId: Int) {
        val askedMap = _tripList.value
            .filter { trip -> trip.appliedUsers.containsKey(userId) }
            .associate { trip -> trip.id to (trip.appliedUsers[userId] ?: 0) }
        _askedTrips.value = askedMap
    }
}