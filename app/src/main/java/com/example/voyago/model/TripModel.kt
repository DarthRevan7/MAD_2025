package com.example.voyago.model

import android.util.Log
import com.example.voyago.Collections
import com.example.voyago.model.Trip.Activity
import com.example.voyago.model.Trip.Participant
import com.example.voyago.model.Trip.TripStatus
import com.example.voyago.view.SelectableItem
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import kotlin.collections.contains
import kotlin.collections.forEach

fun toCalendar(timeDate : Long) : Calendar {
    var calendarDate = Calendar.getInstance()
    calendarDate.timeInMillis = timeDate
    return calendarDate
}

data class Trip(
    val id: Int = 0,
    var photo: String = "",
    var title: String = "",
    var destination: String = "",
    var startDate: Long = 0L,
    var endDate: Long = 0L,
    var estimatedPrice: Double = 0.0,
    var groupSize: Int = 0,
    var participants: Map<Int, JoinRequest> = emptyMap(),                   // userId, id JoinedRequest
    var activities: Map<Calendar, List<Activity>> = emptyMap(),     // Map<Date, Activity>
    var status: String = "",
    var typeTravel: List<String> = emptyList(),
    var creatorId: Int = 0,
    var appliedUsers: Map<Int, JoinRequest> = emptyMap(),                   // userId, id JoinedRequest
    var rejectedUsers: Map<Int, JoinRequest> = emptyMap(),                  // userId, number of spots
    var published: Boolean = false
) {

    data class Activity(
        val id: Int = 0,
        var date: Long = 0L,         // yyyy-mm-dd
        var time: String = "",           // hh:mm
        var isGroupActivity: Boolean = false,
        var description: String = ""
    )

    data class JoinRequest(
        val userId: Int = 0,
        val requestedSpots: Int = 0,
        val unregisteredParticipants: List<Participant> = emptyList(), // excludes the requesting user
        val registeredParticipants: List<Int> = emptyList()           //users' Ids
    )

    data class Participant(
        val name: String = "",
        val surname: String = "",
        val email: String = ""
    )

    enum class TripStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }
    constructor() : this (
        id = -1,
        photo = "",
        title = "",
        destination = "",
        startDate = 0L,
        endDate = 0L,
        estimatedPrice = -1.0,
        groupSize = -1,
        participants = emptyMap(),
        activities = emptyMap(),
        status = "",
        typeTravel = emptyList(),
        creatorId = -1,
        appliedUsers = emptyMap(),
        rejectedUsers = emptyMap(),
        published = false
    ) {

        /*
        var yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -1)

        startDate = yesterday
        endDate = yesterday

         */

        updateStatusBasedOnDate()

    }

    fun updateStatusBasedOnDate(): TripStatus {
        val today = Calendar.getInstance().timeInMillis

        return when {
            endDate < today -> TripStatus.COMPLETED
            startDate > today -> TripStatus.NOT_STARTED
            else -> TripStatus.IN_PROGRESS
        }
    }

    fun isValid():Boolean {
        var condition = true
        var yesterday = toCalendar(this.startDate)
        yesterday.add(Calendar.DATE, -1)

        var startDate = toCalendar(this.startDate)

        var endDate = toCalendar(this.endDate)

        condition = photo != "" && title != "" && destination != ""

        condition = condition && startDate != yesterday && endDate != yesterday

        condition = condition && estimatedPrice > 0.0 && groupSize > 0

        condition = condition && activities.isNotEmpty() && typeTravel.isNotEmpty()

        return condition
    }

    fun canJoin():Boolean {
        return this.status == TripStatus.NOT_STARTED.toString() && hasAvailableSpots()
    }

    fun loggedInUserCanJoin(id: Int): Boolean {
        return this.status == TripStatus.NOT_STARTED.toString() && hasAvailableSpots() && creatorId != id
                && !participants.containsKey(id) && !appliedUsers.containsKey(id)
    }

    fun hasAvailableSpots():Boolean {
        return availableSpots() > 0
    }

    fun availableSpots(): Int {
        return this.groupSize - this.participants.values.sumOf { it.requestedSpots }
    }

    fun tripDuration(): Int {
        val start = toCalendar(startDate)
        val end = toCalendar(endDate)

        var days = 0
        while (start.before(end)) {
            days++
            start.add(Calendar.DATE, 1)
        }
        return days
    }

    fun printTrip()
    {
        println("Trip data: ")
        println(destination)
        println(id)
        println(title)
        println(estimatedPrice)
        println(groupSize)
        println("Status: $status")
        println("Published? $published")
        println("Available spots: " + availableSpots().toString())
        println("Can join? " + canJoin().toString())
        println("Has available spots? " + hasAvailableSpots().toString())


    }

    fun hasActivityForEachDay(): Boolean {
        val current = toCalendar(startDate)
        val end = toCalendar(endDate)

        while (!current.after(end)) {
            val hasActivity = activities.any { (activityDate, _) ->
                activityDate.get(Calendar.YEAR) == current.get(Calendar.YEAR) &&
                        activityDate.get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR)
            }

            if (!hasActivity) return false
            current.add(Calendar.DATE, 1)
        }

        return true
    }

}

enum class TypeTravel {
    CULTURE,
    PARTY,
    ADVENTURE,
    RELAX
}

class TripModel {

    //SUBSET OF THE TRIP LIST

    //Trips published by the logged in user
    fun filterPublishedByCreator(id: Int): Flow<List<Trip>> = callbackFlow {
        val listener = Collections.trips
            .whereEqualTo("id", id)
            .whereEqualTo("published", true)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Trip::class.java))
                } else {
                    Log.e("Error", error.toString())
                    trySend(emptyList())
                }
            }
        awaitClose {
            listener.remove()
        }
    }

    fun filterPrivateByCreator(id: Int): Flow<List<Trip>> = callbackFlow {
        val listener = Collections.trips
            .whereEqualTo("id", id)
            .whereEqualTo("published", false)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Trip::class.java))
                } else {
                    Log.e("Error", error.toString())
                    trySend(emptyList())
                }
            }
        awaitClose {
            listener.remove()
        }
    }

    fun getAllPublishedTrips(): Flow<List<Trip>> = callbackFlow {
        val listener = Collections.trips
            .whereEqualTo("published", true)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Trip::class.java))
                } else {
                    Log.e("Error", error.toString())
                    trySend(emptyList())
                }
            }
        awaitClose {
            listener.remove()
        }
    }

    fun updateTripStatus(tripId: Int, newStatus: String) {
        Collections.trips
            .whereEqualTo("id", tripId)
            .get()
            .addOnSuccessListener { snapshot ->
                for (document in snapshot.documents) {
                    document.reference.update("status", newStatus)
                }
            }
            .addOnFailureListener { error ->
                Log.e("Firestore", "Failed to update trip status", error)
            }
    }

    fun getJoinedTrips(userId: Int): Flow<List<Trip>> = callbackFlow {
        val listener = Collections.trips
            .whereNotEqualTo("creatorId", userId)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    val joinedTrips = snapshot.toObjects(Trip::class.java)
                        .filter { it.participants.containsKey(userId) }

                    trySend(joinedTrips).onFailure {
                        Log.e("Firestore", "Failed to send joined trips", it)
                    }
                } else {
                    Log.e("Firestore", "Error fetching joined trips", error)
                    trySend(emptyList())
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    private val _filteredList = MutableStateFlow<List<Trip>>(emptyList())
    val filteredList: StateFlow<List<Trip>> = _filteredList

    // Call this function to start filtering trips reactively
    fun filterFunction(tripsFlow: Flow<List<Trip>>, filterDestination: String,
                       filterMinPrice: Double, filterMaxPrice: Double,
                       filterDuration: Pair<Int, Int>, filterGroupSize: Pair<Int, Int>,
                       filtersTripType: List<SelectableItem>, filterUpcomingTrips: Boolean,
                       filterCompletedTrips: Boolean, filterBySeats: Int,
                       coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            tripsFlow.collect { list ->
                val filtered = list.filter { trip ->
                    val destination = filterDestination.isBlank() || trip.destination.contains(filterDestination, ignoreCase = true)

                    val duration = (filterDuration.first == -1 && filterDuration.second == -1) ||
                            (trip.tripDuration() in filterDuration.first..filterDuration.second)

                    val groupSize = (filterGroupSize.first == -1 && filterGroupSize.second == -1) ||
                            (trip.groupSize in filterGroupSize.first..filterGroupSize.second)

                    val price = if (filterMinPrice == 0.0 && filterMaxPrice == 0.0) {
                        true
                    } else {
                        trip.estimatedPrice in filterMinPrice..filterMaxPrice
                    }

                    val completed = !filterCompletedTrips || trip.status == TripStatus.COMPLETED.toString()
                    val canJoin = !filterUpcomingTrips || trip.loggedInUserCanJoin(1)
                    val spots = trip.availableSpots() >= filterBySeats

                    trip.published && destination && price && duration && groupSize && canJoin && completed && spots
                }

                val finalFiltered = if (!filtersTripType.any { it.isSelected }) {
                    filtered
                } else {
                    filtered.filter { trip ->
                        filtersTripType.any { it.isSelected && trip.typeTravel.contains(it.typeTravel.toString()) }
                    }
                }

                _filteredList.value = finalFiltered
            }
        }
    }


    fun getDestinations(): Flow<List<String>> = callbackFlow {
        val listener = Collections.trips
            .whereEqualTo("published", true)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    val destinations = snapshot.documents
                        .mapNotNull { it.getString("destination") }
                        .distinct()

                    trySend(destinations).onFailure {
                        Log.e("Firestore", "Failed to send destinations", it)
                    }
                } else {
                    Log.e("Firestore", "Error fetching destinations", error)
                    trySend(emptyList())
                }
            }

        awaitClose { listener.remove() }
    }

    internal var minPrice = Double.MAX_VALUE
    internal var maxPrice = Double.MIN_VALUE

    suspend fun setMaxMinPrice() {
        try {
            val snapshot = Collections.trips.get().await()  // Using kotlinx-coroutines-play-services for .await()
            val trips = snapshot.toObjects(Trip::class.java)

            minPrice = trips.minOfOrNull { it.estimatedPrice } ?: Double.MAX_VALUE
            maxPrice = trips.maxOfOrNull { it.estimatedPrice } ?: Double.MIN_VALUE
        } catch (e: Exception) {
            Log.e("Firestore", "Failed to fetch trips for min/max price", e)
            minPrice = Double.MAX_VALUE
            maxPrice = Double.MIN_VALUE
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
        return Pair(min, max)
    }

    fun getCompletedTrips(): Flow<List<Trip>> = callbackFlow {
        val listener = Collections.trips
            .whereEqualTo("status", "COMPLETED")
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Trip::class.java)).onFailure {
                        Log.e("Firestore", "Send failed", it)
                    }
                } else {
                    Log.e("Firestore", "Listener error", error)
                    trySend(emptyList())
                }
            }

        awaitClose { listener.remove() }
    }

    fun getUpcomingTrips(): Flow<List<Trip>> = callbackFlow {
        val listener = Collections.trips
            .whereEqualTo("status", "NOT_STARTED")
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Trip::class.java)).onFailure {
                        Log.e("Firestore", "Send failed", it)
                    }
                } else {
                    Log.e("Firestore", "Listener error", error)
                    trySend(emptyList())
                }
            }

        awaitClose { listener.remove() }
    }

    fun createNewTrip(newTrip: Trip, onResult: (Boolean, Trip?) -> Unit) {
        val docRef = Collections.trips.document() // Let Firestore generate a unique document ID
        val generatedId = docRef.id.hashCode()     // Use a hash of the string ID as an Int

        val tripWithId = newTrip.copy(id = generatedId)

        docRef.set(tripWithId)
            .addOnSuccessListener {
                onResult(true, tripWithId)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to create trip", e)
                onResult(false, null)
            }
    }

    fun importTrip(
        photo: String,
        title: String,
        destination: String,
        startDate: Calendar,
        endDate: Calendar,
        estimatedPrice: Double,
        groupSize: Int,
        activities: Map<Calendar, List<Activity>>,
        typeTravel: List<TypeTravel>,
        creatorId: Int,
        published: Boolean,
        onResult: (Boolean, Trip?) -> Unit
    ) {
        val docRef = Collections.trips.document() // Firestore-generated ID
        val tripId = docRef.id.hashCode()

        val newTrip = Trip(
            id = tripId,
            photo = photo,
            title = title,
            destination = destination,
            startDate = startDate.timeInMillis,
            endDate = endDate.timeInMillis,
            estimatedPrice = estimatedPrice,
            groupSize = groupSize,
            participants = mapOf(
                creatorId to Trip.JoinRequest(
                    userId = creatorId,
                    requestedSpots = 1,
                    unregisteredParticipants = emptyList(),
                    registeredParticipants = emptyList()
                )
            ),
            activities = activities,
            status = TripStatus.NOT_STARTED.toString(),
            typeTravel = listOf(typeTravel.toString()),
            creatorId = creatorId,
            appliedUsers = emptyMap(),
            rejectedUsers = emptyMap(),
            published = published
        )

        docRef.set(newTrip)
            .addOnSuccessListener {
                onResult(true, newTrip)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to import trip", e)
                onResult(false, null)
            }
    }

    fun editTrip(updatedTrip: Trip, onResult: (Boolean) -> Unit) {
        val docId = updatedTrip.id.toString() // Assumes Firestore document ID

        Collections.trips
            .document(docId)
            .set(updatedTrip)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to update trip", e)
                onResult(false)
            }
    }

    fun changePublishedStatus(id: Int, onResult: (Boolean) -> Unit) {
        val docId = id.toString()  // Assuming document ID is stringified trip ID

        val tripDocRef = Collections.trips.document(docId)

        // Get current trip to toggle the published status
        tripDocRef.get()
            .addOnSuccessListener { snapshot ->
                val trip = snapshot.toObject(Trip::class.java)
                if (trip != null) {
                    val updatedPublished = !trip.published
                    tripDocRef.update("published", updatedPublished)
                        .addOnSuccessListener { onResult(true) }
                        .addOnFailureListener {
                            Log.e("Firestore", "Failed to update published status", it)
                            onResult(false)
                        }
                } else {
                    Log.e("Firestore", "Trip not found for ID: $id")
                    onResult(false)
                }
            }
            .addOnFailureListener {
                Log.e("Firestore", "Failed to fetch trip for ID: $id", it)
                onResult(false)
            }
    }


    fun addActivityToTrip(activity: Activity, trip: Trip?): Trip {
        val currentTrip = trip ?: Trip()

        val updatedActivities = currentTrip.activities.toMutableMap().apply {
            val dateKey: Calendar = toCalendar(activity.date)
            val updatedList: List<Activity> = getOrDefault(dateKey, emptyList()) + activity
            put(dateKey, updatedList)
        }

        val updatedTrip = currentTrip.copy(activities = updatedActivities)

        Collections.trips.document(updatedTrip.id.toString())
            .set(updatedTrip)

        return updatedTrip
    }

    

    //--------------------------------------------------------------------

    /*
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

    fun updateTripStatus(trip: Trip, status: String) {
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
        var filtered = list.filter { trip ->
            val destination = filterDestination.isBlank() || trip.destination.contains(filterDestination, ignoreCase = true)

            val duration = (filterDuration.first == -1 && filterDuration.second == -1) ||
                    (trip.tripDuration() in filterDuration.first..filterDuration.second)

            val groupSize = (filterGroupSize.first == -1 && filterGroupSize.second == -1) ||
                    (trip.groupSize in filterGroupSize.first..filterGroupSize.second)

            // 0-0 => No filter for price
            val price = if (filterMinPrice == 0.0 && filterMaxPrice == 0.0) {
                true
            } else {
                trip.estimatedPrice in filterMinPrice..filterMaxPrice // Apply filter with given interval
            }

            val completed = !filterCompletedTrips || trip.status == TripStatus.COMPLETED//|| !trip.canJoin()
            val canJoin = !filterUpcomingTrips || trip.loggedInUserCanJoin(1)
            val spots = trip.availableSpots() >= filterBySeats

            trip.published && destination && price && duration && groupSize && canJoin && completed
                    && spots
        }

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
            participants = mapOf(creatorId to Trip.JoinRequest(userId = creatorId, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList())),
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

    fun requestToJoin(trip: Trip, userId :Int, spots: Int, unregisteredParticipants: List<Participant>, registeredParticipants: List<Int>) {
        val updatedAppliedUsers = trip.appliedUsers.toMutableMap()
        val joinRequest = Trip.JoinRequest(
            userId = userId,
            requestedSpots = spots,
            unregisteredParticipants = unregisteredParticipants,
            registeredParticipants = registeredParticipants
        )

        updatedAppliedUsers[userId] = joinRequest

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
            .associate { trip ->
                val requestedSpots = trip.appliedUsers[userId]?.requestedSpots ?: 0
                trip.id to requestedSpots
            }
        _askedTrips.value = askedMap
    }
    
     */
}