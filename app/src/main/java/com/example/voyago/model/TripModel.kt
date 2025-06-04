package com.example.voyago.model

import android.util.Log
import com.example.voyago.Collections
import com.example.voyago.model.Trip.Activity
import com.example.voyago.model.Trip.Participant
import com.example.voyago.model.Trip.TripStatus
import com.example.voyago.view.SelectableItem
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.collections.forEach
import kotlin.toString


//Function that converts a Long in a Calendar
fun toCalendar(timeDate : Timestamp) : Calendar {
    var calendarDate = Calendar.getInstance()
    calendarDate.time = timeDate.toDate()
    return calendarDate
}

//Trip data structure
data class Trip(
    val id: Int = 0,
    var photo: String = "",
    var title: String = "",
    var destination: String = "",
    var startDate: Timestamp = Timestamp(Date(0)),
    var endDate: Timestamp = Timestamp(Date(0)),
    var estimatedPrice: Double = 0.0,
    var groupSize: Int = 0,
    var participants: Map<String, JoinRequest> = emptyMap(),                   // userId, id JoinedRequest
    var activities: Map<String, List<Activity>> = emptyMap(),     // Map<Date, Activity>
    var status: String = "",
    var typeTravel: List<String> = emptyList(),
    var creatorId: Int = 0,
    var appliedUsers: Map<String, JoinRequest> = emptyMap(),                   // userId, id JoinedRequest
    var rejectedUsers: Map<String, JoinRequest> = emptyMap(),                  // userId, number of spots
    var published: Boolean = false
) {

    fun startDateAsCalendar(): Calendar = toCalendar(startDate)
    fun endDateAsCalendar(): Calendar = toCalendar(endDate)
    fun startDateAsLong(): Long = startDate.toDate().time
    fun endDateAsLong(): Long = endDate.toDate().time

    data class Activity(
        val id: Int = 0,
        var date: Timestamp = Timestamp(Date(0)),         // yyyy-mm-dd
        var time: String = "",           // hh:mm
        var isGroupActivity: Boolean = false,
        var description: String = "",
    ){
        fun dateAsCalendar(): Calendar = toCalendar(date)
    }

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
        startDate = Timestamp(Date(0)),
        endDate = Timestamp(Date(0)),
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
        updateStatusBasedOnDate()
    }

    //Function that updates the status of the trip based on the current date
    fun updateStatusBasedOnDate(): TripStatus {
        val today = Timestamp(Calendar.getInstance().time)

        return when {
            endDate < today -> TripStatus.COMPLETED
            startDate > today -> TripStatus.NOT_STARTED
            else -> TripStatus.IN_PROGRESS
        }
    }

    //Function that returns true if the Trip is valid
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

    //Function that returns true if a user can join the trip
    fun canJoin():Boolean {
        return this.status == TripStatus.NOT_STARTED.toString() && hasAvailableSpots()
    }

    //Function that returns true if the logged in user can join the trip
    fun loggedInUserCanJoin(id: Int): Boolean {
        return this.status == TripStatus.NOT_STARTED.toString() && hasAvailableSpots() && creatorId != id
                && !participants.containsKey(id.toString()) && !appliedUsers.containsKey(id.toString())
    }

    //Function that returns true if the Trip has available spots
    fun hasAvailableSpots():Boolean {
        return availableSpots() > 0
    }

    //Function that returns the number of available spots that the Trip has
    fun availableSpots(): Int {
        return this.groupSize - this.participants.values.sumOf { it.requestedSpots }
    }

    //Function that returns the duration of the trip in days
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

    //Function that prints the Trip
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

    //Function that returns true if the Trip has at least an activity for each day
    fun hasActivityForEachDay(): Boolean {
        val current = startDateAsCalendar()
        val end = endDateAsCalendar()

        while (!current.after(end)) {
            val hasActivity = activities.any { (activityDate, _) ->
                stringToCalendar(activityDate).get(Calendar.YEAR) == current.get(Calendar.YEAR) &&
                        stringToCalendar(activityDate).get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR)

            }

            if (!hasActivity) return false
            current.add(Calendar.DATE, 1)
        }

        return true
    }

    fun updateApplicationStatus(
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("trips")
            .document(this.id.toString())
            .update(
                mapOf(
                    "participants" to participants,
                    "appliedUsers" to appliedUsers,
                    "rejectedUsers" to rejectedUsers
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}

fun stringToCalendar(string: String): Calendar {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val date = formatter.parse(string)
    return Calendar.getInstance().apply {
        if (date != null) {
            date.month += 1
            time = date
        }
    }
}

//Possible type of travel
enum class TypeTravel {
    CULTURE,
    PARTY,
    ADVENTURE,
    RELAX
}


class TripModel {

    //SUBSET OF THE TRIP LIST

    //Function that returns the Trips created and published by the logged in user
    private val _publishedTrips = MutableStateFlow<List<Trip>>(emptyList())
    val publishedTrips: StateFlow<List<Trip>> = _publishedTrips

    fun filterPublishedByCreator(id: Int, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            callbackFlow {
                val listener = Collections.trips
                    .whereEqualTo("creatorId", id.toLong())
                    .whereEqualTo("published", true)
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null) {
                            trySend(snapshot.toObjects(Trip::class.java))
                        } else {
                            Log.e("Error", error.toString())
                            trySend(emptyList())
                        }
                    }
                awaitClose { listener.remove() }
            }.collect { trips ->
                _publishedTrips.value = trips
            }
        }
    }


    //Function that returns the Trips created, but not published by the logged in user
    private val _privateTrips = MutableStateFlow<List<Trip>>(emptyList())
    val privateTrips: StateFlow<List<Trip>> = _privateTrips

    fun filterPrivateByCreator(id: Int, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            callbackFlow {
                val listener = Collections.trips
                    .whereEqualTo("creatorId", id.toLong())
                    .whereEqualTo("published", false)
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null) {
                            trySend(snapshot.toObjects(Trip::class.java))
                        } else {
                            Log.e("Error", error.toString())
                            trySend(emptyList())
                        }
                    }
                awaitClose { listener.remove() }
            }.collect { trips ->
                _privateTrips.value = trips
            }
        }
    }

    //Function that returns all the published Trips
    private val _allPublishedTrips = MutableStateFlow<List<Trip>>(emptyList())
    val allPublishedTrips: StateFlow<List<Trip>> = _allPublishedTrips

    fun getAllPublishedTrips(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            callbackFlow<List<Trip>> {
                val listener = Collections.trips
                    .whereEqualTo("published", true)
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null) {
                            trySend(snapshot.toObjects(Trip::class.java))
                        } else {
                            Log.e("Firestore", "Error fetching published trips", error)
                            trySend(emptyList())
                        }
                    }

                awaitClose { listener.remove() }
            }.collect { trips ->
                _allPublishedTrips.value = trips
            }
        }
    }


    //Function that updates the status of a specific Trip
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

    //Function that return the Trips a user has joined
    private val _joinedTrips = MutableStateFlow<List<Trip>>(emptyList())
    val joinedTrips: StateFlow<List<Trip>> = _joinedTrips

    fun getJoinedTrips(userId: Int, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            callbackFlow<List<Trip>> {
                val listener = Collections.trips
                    .whereNotEqualTo("creatorId", userId)
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null) {
                            val joinedTrips = snapshot.toObjects(Trip::class.java)
                                .filter { it.participants.containsKey(userId.toString()) }

                            trySend(joinedTrips).onFailure {
                                Log.e("Firestore", "Failed to send joined trips", it)
                            }
                        } else {
                            Log.e("Firestore", "Error fetching joined trips", error)
                            trySend(emptyList())
                        }
                    }
                awaitClose { listener.remove() }
            }.collect { trips ->
                _joinedTrips.value = trips
            }
        }
    }


    //List of filtered Trips
    private val _filteredList = MutableStateFlow<List<Trip>>(emptyList())
    val filteredList: StateFlow<List<Trip>> = _filteredList

    //Function that return the list of Trips after the application of the selected filters
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

    //FUNCTION FOR THE APPLICATION OF THE FILTERS

    //Function that returns the list of all the destinations present in the database
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

    //Function that set the Max and Min Price of the database
    internal var minPrice = Double.MAX_VALUE
    internal var maxPrice = Double.MIN_VALUE

    suspend fun setMaxMinPrice() {
        try {
            val snapshot = Collections.trips.get().await()
            val trips = snapshot.toObjects(Trip::class.java)

            minPrice = trips.minOfOrNull { it.estimatedPrice } ?: Double.MAX_VALUE
            maxPrice = trips.maxOfOrNull { it.estimatedPrice } ?: Double.MIN_VALUE
        } catch (e: Exception) {
            Log.e("Firestore", "Failed to fetch trips for min/max price", e)
            minPrice = Double.MAX_VALUE
            maxPrice = Double.MIN_VALUE
        }
    }

    //Function that sets the Range of the Price slider
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

    //Function that returns the list of completed Trips
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

    //Function that return the list of Upcoming Trips
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

    //CREATE A TRIP

    //Function that creates a new Trip
    fun createNewTrip(newTrip: Trip, onResult: (Boolean, Trip?) -> Unit) {
        val firestore = Firebase.firestore
        val counterRef = firestore.collection("metadata").document("tripCounter")

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(counterRef)
            val lastTripId = snapshot.getLong("lastTripId") ?: 0
            val newTripId = lastTripId + 1

            // Set the new ID back to the counter document
            transaction.update(counterRef, "lastTripId", newTripId)

            // Create the trip with the new ID
            val tripWithId = newTrip.copy(id = newTripId.toInt())

            // Create a new document in the trips collection
            val tripDocRef = firestore.collection("trips").document(newTripId.toString())
            transaction.set(tripDocRef, tripWithId)

            tripWithId
        }.addOnSuccessListener { trip ->
            onResult(true, trip)
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Failed to create trip", e)
            onResult(false, null)
        }
    }


    //Function that imports a Trip in the "My Trip" section of the logged in user as private
    fun importTrip(photo: String, title: String, destination: String, startDate: Calendar,
                   endDate: Calendar, estimatedPrice: Double, groupSize: Int,
                   activities: Map<String, List<Activity>>, typeTravel: List<String>, creatorId: Int,
                   published: Boolean, onResult: (Boolean, Trip?) -> Unit) {
        val firestore = Firebase.firestore
        val counterRef = firestore.collection("metadata").document("tripCounter")

        firestore.runTransaction { transaction ->
            //Get and increment trip ID counter
            val snapshot = transaction.get(counterRef)
            val lastTripId = snapshot.getLong("lastTripId") ?: 0
            val newTripId = lastTripId + 1
            transaction.update(counterRef, "lastTripId", newTripId)

            //Create new Firestore doc reference
            val docRef = firestore.collection("trips").document(newTripId.toString())

            //Build Trip object with new ID
            val newTrip = Trip(
                id = newTripId.toInt(),
                photo = photo,
                title = title,
                destination = destination,
                startDate = Timestamp(startDate.time),
                endDate = Timestamp(endDate.time),
                estimatedPrice = estimatedPrice,
                groupSize = groupSize,
                participants = mapOf(
                    creatorId.toString() to Trip.JoinRequest(
                        userId = creatorId,
                        requestedSpots = 1,
                        unregisteredParticipants = emptyList(),
                        registeredParticipants = emptyList()
                    )
                ),
                activities = activities,
                status = TripStatus.NOT_STARTED.toString(),
                typeTravel = typeTravel,
                creatorId = creatorId,
                appliedUsers = emptyMap(),
                rejectedUsers = emptyMap(),
                published = published
            )

            //Save to Firestore
            transaction.set(docRef, newTrip)
            newTrip
        }.addOnSuccessListener { trip ->
            onResult(true, trip)
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Failed to import trip", e)
            onResult(false, null)
        }
    }


    //EDIT TRIP

    //Functions that edits the information of a specific Trip
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

    //Function that changes the Published status of a specific Trip
    fun changePublishedStatus(id: Int, onResult: (Boolean) -> Unit) {
        val docId = id.toString()

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

    //ACTIVITY MANAGEMENT

    //Function that add an Activity to a specific Trip
    fun addActivityToTrip(activity: Activity, trip: Trip?): Trip {
        val currentTrip = trip ?: Trip()

        val updatedActivities = currentTrip.activities.toMutableMap().apply {
            val dateKey: String = activity.dateAsCalendar().timeInMillis.toString()
            val updatedList: List<Activity> = getOrDefault(dateKey, emptyList()) + activity
            put(dateKey, updatedList)
        }

        val updatedTrip = currentTrip.copy(activities = updatedActivities)

        Collections.trips.document(updatedTrip.id.toString())
            .set(updatedTrip)

        return updatedTrip
    }

    //Function that edits an Activity
    fun editActivityInSelectedTrip(activityId: Int, updatedActivity: Activity, trip: Trip,
                                   onResult: (Boolean, Trip?) -> Unit) {
        val docId = trip.id.toString()
        val tripRef = Collections.trips.document(docId)

        tripRef.get()
            .addOnSuccessListener { snapshot ->
                val currentTrip = snapshot.toObject(Trip::class.java)
                if (currentTrip == null) {
                    onResult(false, null)
                    return@addOnSuccessListener
                }

                val originalActivities = currentTrip.activities.toMutableMap()
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

                if (!found) {
                    onResult(false, trip)
                    return@addOnSuccessListener
                }

                // Add updated activity to its new date
                val newDateKey = updatedActivity.dateAsCalendar().timeInMillis.toString()
                val updatedList = originalActivities.getOrDefault(newDateKey, emptyList<Activity>()) + updatedActivity
                originalActivities[newDateKey] = updatedList

                val updatedTrip = currentTrip.copy(activities = originalActivities)

                tripRef.set(updatedTrip)
                    .addOnSuccessListener {
                        onResult(true, updatedTrip)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Failed to update activity", e)
                        onResult(false, null)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch trip", e)
                onResult(false, null)
            }
    }

    //Delete an activity
    fun removeActivityFromTrip(activity: Activity, trip: Trip?, onResult: (Boolean, Trip?) -> Unit) {
        if (trip == null) {
            onResult(false, null)
            return
        }

        val dateKey = activity.dateAsCalendar().timeInMillis.toString()
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

        val docId = trip.id.toString()
        Collections.trips.document(docId)
            .set(updatedTrip)
            .addOnSuccessListener {
                onResult(true, updatedTrip)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to remove activity from trip", e)
                onResult(false, null)
            }
    }

    //DELETE A TRIP

    //Delete a trip
    fun deleteTrip(id: Int, onResult: (Boolean) -> Unit) {
        val docId = id.toString()

        Collections.trips
            .document(docId)
            .delete()
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to delete trip with ID: $id", e)
                onResult(false)
            }
    }

    //MANAGEMENT OF APPLICATIONS TO TRIPS

    //Function that adds a request to join to a specific Trip
    private val _askedTrips = MutableStateFlow<Map<String, Int>>(emptyMap())
    val askedTrips: StateFlow<Map<String, Int>> = _askedTrips

    fun requestToJoin(trip: Trip, userId: Int, spots: Int,
                      unregisteredParticipants: List<Participant>,
                      registeredParticipants: List<Int>, onResult: (Boolean) -> Unit) {
        val docId = trip.id.toString()
        val tripDocRef = Collections.trips.document(docId)

        val joinRequest = Trip.JoinRequest(
            userId = userId,
            requestedSpots = spots,
            unregisteredParticipants = unregisteredParticipants,
            registeredParticipants = registeredParticipants
        )

        val joinRequestMap = mapOf("appliedUsers.$userId" to joinRequest)

        tripDocRef.update(joinRequestMap)
            .addOnSuccessListener {
                _askedTrips.value = _askedTrips.value + (trip.id.toString() to spots)
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to apply to trip ${trip.id}", e)
                onResult(false)
            }
    }

    //Function that removes a request to join to a specific Trip
    fun cancelRequestToJoin(trip: Trip, userId: Int, onResult: (Boolean) -> Unit) {
        val docRef = Collections.trips.document(trip.id.toString())

        // Remove the userId from appliedUsers map locally
        val updatedAppliedUsers = trip.appliedUsers.toMutableMap().apply {
            remove(userId.toString())
        }

        // Update Firestore document's appliedUsers field
        docRef.update("appliedUsers", updatedAppliedUsers)
            .addOnSuccessListener {
                // Optionally update local state if needed
                _askedTrips.value = _askedTrips.value - trip.id.toString()
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to cancel join request", e)
                onResult(false)
            }
    }

    //Function that syncs the join request of a user with the AppliedUser of the trips
    fun syncAskedTripsWithAppliedUsers(userId: Int, onResult: (Boolean) -> Unit) {
        Collections.trips
            .get()
            .addOnSuccessListener { querySnapshot ->
                val askedMap = querySnapshot.documents.mapNotNull { doc ->
                    val trip = doc.toObject(Trip::class.java)
                    val requestedSpots = trip?.appliedUsers?.get(userId.toString())?.requestedSpots ?: 0
                    if (requestedSpots > 0 && trip != null) trip.id.toString() to requestedSpots else null
                }.toMap()
                _askedTrips.value = askedMap
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to sync asked trips", e)
                onResult(false)
            }
    }

}