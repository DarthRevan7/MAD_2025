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


//Function that converts a Long in a Calendar
fun toCalendar(timeDate: Timestamp): Calendar {
    var calendarDate = Calendar.getInstance()
    calendarDate.time = timeDate.toDate()
    return calendarDate
}

//Trip data structure
data class Trip(
    val id: Int = 0,
    var photo: String? = null,
    var title: String = "",
    var destination: String = "",
    var startDate: Timestamp = Timestamp(Date(0)),
    var endDate: Timestamp = Timestamp(Date(0)),
    var estimatedPrice: Double = 0.0,
    var groupSize: Int = 0,
    var participants: Map<String, JoinRequest> = emptyMap(),                   // userId, id JoinedRequest
    var activities: Map<String, List<Activity>> = emptyMap(),                  // Map<Date, Activity>
    var status: String = "",
    var typeTravel: List<String> = emptyList(),
    var creatorId: Int = 0,
    var appliedUsers: Map<String, JoinRequest> = emptyMap(),                   // userId, id JoinedRequest
    var rejectedUsers: Map<String, JoinRequest> = emptyMap(),                  // userId, number of spots
    var published: Boolean = false,
    var isDraft: Boolean = false
) {

    // Holds a cached photo URL to avoid repeated fetches or downloads.
    // This variable stores the last successfully retrieved photo URL, or null if none is cached.
    private var cachedPhotoUrl: String? = null

    // Suspended function to get the URL of the main photo for a trip.
    suspend fun getPhoto(): String {
        if (photo == null || photo == "") {
            // Return placeholder image URL if no photo is specified
            return com.example.voyago.StorageHelper.getImageDownloadUrl("trips/placeholder_photo.png")
                ?: ""
        }

        // Try to get the URL for the specified photo
        var url = com.example.voyago.StorageHelper.getImageDownloadUrl(photo!!)

        // If URL is null, fall back to the placeholder image URL
        if (url == null) {
            return com.example.voyago.StorageHelper.getImageDownloadUrl("trips/placeholder_photo.png")
                ?: ""
        }

        // Return the photo URL
        return url
    }

    // Suspended function to upload a new photo and update the trip's photo reference
    suspend fun setPhoto(newPhotoUri: android.net.Uri): Boolean {
        // Extract file extension from URI
        val extension = getFileExtension(newPhotoUri)
        // Build new storage path for the photo
        val newPath = "trips/${id}.$extension"

        // Upload image to Firebase Storage and get success status and URL
        val (success, url) = com.example.voyago.StorageHelper.uploadImageToStorage(
            newPhotoUri,
            newPath
        )
        return if (success && url != null) {
            photo = newPath             // Update photo path with new storage location
            cachedPhotoUrl = url        // Cache the downloadable URL of the uploaded photo
            true                        // Indicate successful upload and update
        } else {
            false                       // Upload failed, return false
        }
    }

    // Extracts the file extension from the given Uri's path
    private fun getFileExtension(uri: android.net.Uri): String {
        // Use "jpg" if path is null
        val path = uri.path ?: return "jpg"
        // Find last '.' in path
        val dot = path.lastIndexOf('.')

        return if (dot != -1 && dot < path.length - 1)      // Check if '.' exists and not last char
            path.substring(dot + 1)                         // Return extension after '.'
        else
            "jpg"                                           // Default to "jpg" if no valid extension
    }

    // Function that translate the startDate in a Calendar
    fun startDateAsCalendar(): Calendar = toCalendar(startDate)

    // Function that translate the endDate in a Calendar
    fun endDateAsCalendar(): Calendar = toCalendar(endDate)

    // Function that translate the startDate in a Long
    fun startDateAsLong(): Long = startDate.toDate().time

    //Activity data structure
    data class Activity(
        val id: Int = 0,
        var date: Timestamp = Timestamp(Date(0)),         // yyyy-mm-dd
        var time: String = "",                            // hh:mm
        var isGroupActivity: Boolean = false,
        var description: String = "",
    ) {
        // Function that transforms a date in a calendar
        fun dateAsCalendar(): Calendar = toCalendar(date)
    }

    // JoinRequest data structure
    data class JoinRequest(
        val userId: Int = 0,
        val requestedSpots: Int = 0,
        val unregisteredParticipants: List<Participant> = emptyList(), // excludes the requesting user
        val registeredParticipants: List<Int> = emptyList()           //users' Ids
    )

    // Participant data structure
    data class Participant(
        val name: String = "",
        val surname: String = "",
        val email: String = ""
    )

    // Enum class with the possible status of the trip
    enum class TripStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }

    //Trip constructor
    constructor() : this(
        id = -1,
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
        published = false,
        isDraft = false
    ) {
        //Function that updates the status of the trip based on the current date
        updateStatusBasedOnDate()
    }

    //Function that updates the status of the trip based on the current date
    fun updateStatusBasedOnDate(): TripStatus {
        // Get current date and time as Timestamp
        val today = Timestamp(Calendar.getInstance().time)

        return when {
            endDate < today -> TripStatus.COMPLETED         // Trip ended before today
            startDate > today -> TripStatus.NOT_STARTED     // Trip starts in the future
            else -> TripStatus.IN_PROGRESS                  // Trip is ongoing
        }
    }

    //Function that returns true if the Trip is valid
    fun isValid(): Boolean {
        var condition = true
        var yesterday = toCalendar(this.startDate)

        // Calculate the date one day before the start date
        yesterday.add(Calendar.DATE, -1)

        var startDate = toCalendar(this.startDate)
        var endDate = toCalendar(this.endDate)

        // Check that title and destination are not empty
        condition = title != "" && destination != ""

        // Ensure startDate and endDate are not equal to yesterday
        condition = condition && startDate != yesterday && endDate != yesterday

        // Check estimatedPrice and groupSize are positive
        condition = condition && estimatedPrice > 0.0 && groupSize > 0

        // Ensure activities list and travel type are not empty
        condition = condition && activities.isNotEmpty() && typeTravel.isNotEmpty()

        return condition
    }

    // Checks if the trip can be joined by the user.
    // Returns true only if the trip status is "NOT_STARTED" and there are available spots left.
    fun canJoin(): Boolean {
        return this.status == TripStatus.NOT_STARTED.toString() && hasAvailableSpots()
    }

    // Determines if a logged-in user, identified by their ID, is allowed to join the trip.
    // Returns true only if all the conditions are met
    fun loggedInUserCanJoin(id: Int): Boolean {
        return this.status == TripStatus.NOT_STARTED.toString()     //The trip isn't started yet
                && hasAvailableSpots()                              //The trip has available spots
                && creatorId != id                                  //The logged in user isn't the creator of the trip
                && !participants.containsKey(id.toString())         //The logged in user isn't already a participant
                && !appliedUsers.containsKey(id.toString())         //The logged in user didn't already ask to join to that trip
    }

    //Function that returns true if the Trip has available spots
    fun hasAvailableSpots(): Boolean {
        return availableSpots() > 0
    }

    //Function that returns the number of available spots that the Trip has
    fun availableSpots(): Int {
        return this.groupSize - this.participants.values.sumOf { it.requestedSpots }
    }

    //Function that returns the duration of the trip in days
    fun tripDuration(): Int {
        // Converts the start and end dates to Calendar objects
        val start = toCalendar(startDate)
        val end = toCalendar(endDate)

        var days = 0

        // Then counts how many days are between the start date (inclusive) and end date (exclusive)
        while (start.before(end)) {         // Loop until start date reaches end date
            days++                          // Increment day count
            start.add(Calendar.DATE, 1)     // Move to the next day
        }
        return days
    }

    // Checks whether there is at least one activity scheduled for every day of the trip.
    fun hasActivityForEachDay(): Boolean {
        // Start from the trip's start date
        val current = startDateAsCalendar()
        // Trip's end date
        val end = endDateAsCalendar()

        // Iterate through each day of the trip
        while (!current.after(end)) {
            // Check if there is any activity scheduled for the current day
            val hasActivity = activities.any { (activityDate, _) ->
                if (isTimestampLong(activityDate.toString())) {
                    // If the date is stored as a Timestamp, convert it and compare year & day
                    timestampToCalendar(activityDate).get(Calendar.YEAR) == current.get(Calendar.YEAR) &&
                            timestampToCalendar(activityDate).get(Calendar.DAY_OF_YEAR) == current.get(
                        Calendar.DAY_OF_YEAR
                    )
                } else {
                    // Otherwise, assume it's a String format date and compare
                    stringToCalendar(activityDate).get(Calendar.YEAR) == current.get(Calendar.YEAR) &&
                            stringToCalendar(activityDate).get(Calendar.DAY_OF_YEAR) == current.get(
                        Calendar.DAY_OF_YEAR
                    )

                }

            }
            // If no activity found for a day, return false
            if (!hasActivity) return false

            // Move to the next day
            current.add(Calendar.DATE, 1)
        }
        // If all days have at least one activity, return true
        return true
    }

    // Updates the participant, applied, and rejected users lists in Firestore for the current trip
    fun updateApplicationStatus(onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        // Get Firestore instance
        val db = FirebaseFirestore.getInstance()

        // Update the trip document with the current state of participants, appliedUsers, and rejectedUsers
        db.collection("trips")
            // Access the specific trip by ID
            .document(this.id.toString())
            .update(
                mapOf(
                    "participants" to participants,     // Update participants map
                    "appliedUsers" to appliedUsers,     // Update applied users map
                    "rejectedUsers" to rejectedUsers    // Update rejected users map
                )
            )
            .addOnSuccessListener { onSuccess() }       // Call success callback if update succeeds
            .addOnFailureListener { onFailure(it) }     // Call failure callback with the exception
    }
}

fun stringToCalendar(string: String): Calendar {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)   // Define the date format and locale
    val date =
        formatter.parse(string)                          // Parse the string into a Date object
    return Calendar.getInstance().apply {
        if (date != null) {
            time =
                date                                 // Set the Calendar's time to the parsed Date
        }
    }
}

// Checks if the input string can be safely converted to a Long
fun isTimestampLong(input: String): Boolean {
    return input.toLongOrNull() != null
}

// Converts a timestamp string to a Calendar instance
fun timestampToCalendar(timestamp: String): Calendar {
    val millis =
        timestamp.toLong()                 // Convert the timestamp string to a Long (milliseconds)
    return Calendar.getInstance().apply {
        timeInMillis = millis               // Set the Calendar time to the given milliseconds
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

    // Fetches and updates the list of trips published by a specific creator using Firestore real-time updates
    fun filterPublishedByCreator(id: Int, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            callbackFlow {
                // Set up Firestore query to filter trips by creatorId and published status
                val listener = Collections.trips
                    .whereEqualTo(
                        "creatorId",
                        id.toLong()
                    )     // Filter trips created by the given user ID
                    .whereEqualTo(
                        "published",
                        true
                    )            // Only include trips that are published
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null) {
                            // Send the list of Trip objects to the flow if data is available
                            trySend(snapshot.toObjects(Trip::class.java))
                        } else {
                            // Log error and send an empty list if query fails
                            Log.e("Error", error.toString())
                            trySend(emptyList())
                        }
                    }
                // Clean up listener when flow collection is finished or cancelled
                awaitClose { listener.remove() }
            }.collect { trips ->
                // Update the state flow with the latest list of published trips
                _publishedTrips.value = trips
            }
        }
    }


    //Function that returns the Trips created, but not published by the logged in user
    private val _privateTrips = MutableStateFlow<List<Trip>>(emptyList())
    val privateTrips: StateFlow<List<Trip>> = _privateTrips

    // Fetches and updates the list of private (unpublished) trips created by a specific user using Firestore real-time updates
    fun filterPrivateByCreator(id: Int, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            callbackFlow {
                // Firestore query to find trips where creatorId matches and published is false (private trips)
                val listener = Collections.trips
                    .whereEqualTo(
                        "creatorId",
                        id.toLong()
                    )     // Filter trips by the creator's user ID
                    .whereEqualTo(
                        "published",
                        false
                    )           // Only include trips that are not published (private)
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null) {
                            // Emit the list of Trip objects from the snapshot to the flow
                            trySend(snapshot.toObjects(Trip::class.java))
                        } else {
                            // Log error and emit an empty list if fetching trips failed
                            Log.e("Error", error.toString())
                            trySend(emptyList())
                        }
                    }
                // Remove Firestore listener when flow collection ends
                awaitClose { listener.remove() }
            }.collect { trips ->
                // Update the private trips state flow with the latest data
                _privateTrips.value = trips
            }
        }
    }

    //Function that returns all the published Trips
    private val _allPublishedTrips = MutableStateFlow<List<Trip>>(emptyList())
    val allPublishedTrips: StateFlow<List<Trip>> = _allPublishedTrips

    // Fetches and listens for all published trips from Firestore, updating the state flow with real-time data
    fun getAllPublishedTrips(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            callbackFlow<List<Trip>> {
                // Query Firestore for trips where "published" is true (public trips)
                val listener = Collections.trips
                    .whereEqualTo("published", true)
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null) {
                            // Emit the list of Trip objects retrieved from Firestore snapshot
                            trySend(snapshot.toObjects(Trip::class.java))
                        } else {
                            // Log an error and emit an empty list if fetching trips failed
                            Log.e("Firestore", "Error fetching published trips", error)
                            trySend(emptyList())
                        }
                    }
                // Clean up the Firestore listener when the flow collection ends
                awaitClose { listener.remove() }
            }.collect { trips ->
                // Update the state flow with the current list of published trips
                _allPublishedTrips.value = trips
            }
        }
    }

    //Function that updates the status of a specific Trip
    fun updateTripStatus(tripId: Int, newStatus: String) {
        // Query the "trips" collection for documents where the "id" matches the given tripId
        Collections.trips
            .whereEqualTo("id", tripId)
            .get()
            .addOnSuccessListener { snapshot ->
                // For each matching document, update its "status" field to the newStatus value
                for (document in snapshot.documents) {
                    document.reference.update("status", newStatus)
                }
            }
            .addOnFailureListener { error ->
                // Log an error message if the update operation fails
                Log.e("Firestore", "Failed to update trip status", error)
            }
    }

    //Function that updates the creatorId of a specific Trip and removes the old creator from participants
    fun updateCreatorId(tripId: Int, newCreatorId: Int, oldCreatorId: Int) {
        // Query the "trips" collection for documents where the "id" matches the given tripId
        Collections.trips
            .whereEqualTo("id", tripId)
            .get()
            .addOnSuccessListener { snapshot ->
                // Iterate over each matching trip document
                for (document in snapshot.documents) {
                    // Retrieve the current participants map from the document (if present)
                    val participants = document.get("participants") as? Map<*, *>

                    if (participants != null) {
                        // Create a mutable copy of participants and remove the old creator by their ID (as String)
                        val updatedParticipants = participants.toMutableMap().apply {
                            remove(oldCreatorId.toString())
                        }

                        // Update the document with the new creatorId and the updated participants map
                        document.reference.update(
                            mapOf(
                                "creatorId" to newCreatorId,
                                "participants" to updatedParticipants
                            )
                        ).addOnSuccessListener {
                            // Log success message on successful update
                            Log.d("Firestore", "Trip creator and participants updated successfully")
                        }.addOnFailureListener { error ->
                            // Log error message if update fails
                            Log.e("Firestore", "Failed to update trip data", error)
                        }
                    } else {
                        // Log an error if participants field is missing or not in expected format
                        Log.e("Firestore", "Participants field is missing or invalid")
                    }
                }
            }
            .addOnFailureListener { error ->
                // Log error if fetching the trip document fails
                Log.e("Firestore", "Failed to fetch trip document", error)
            }
    }

    //Function that removes a participant from a trip
    fun removeParticipantFromTrip(tripId: Int, participantId: String, onResult: (Boolean) -> Unit) {
        // Query the "trips" collection for the trip document with the given tripId
        Collections.trips
            .whereEqualTo("id", tripId)
            .get()
            .addOnSuccessListener { snapshot ->
                // Check if the trip document exists
                if (snapshot.documents.isEmpty()) {
                    Log.e("Firestore", "Trip not found")
                    onResult(false)     // Notify failure if no such trip exists
                    return@addOnSuccessListener
                }

                // Get the first (and presumably only) trip document
                val document = snapshot.documents[0]
                // Retrieve the current participants map (if present)
                val participants = document.get("participants") as? Map<*, *>

                // Create a mutable copy of participants and remove the specified participant by ID
                val updatedParticipants = participants?.toMutableMap()
                updatedParticipants?.remove(participantId)

                // Update the trip document with the updated participants map in Firestore
                document.reference.update("participants", updatedParticipants)
                    .addOnSuccessListener {
                        onResult(true)      // Notify success
                    }
                    .addOnFailureListener { error ->
                        Log.e("Firestore", "Failed to update participants", error)
                        onResult(false)     // Notify failure if update fails
                    }
            }
            .addOnFailureListener { error ->
                Log.e("Firestore", "Failed to fetch trip document", error)
                onResult(false)         // Notify failure if query fails
            }
    }

    //Function that return the Trips a user has joined
    private val _joinedTrips = MutableStateFlow<List<Trip>>(emptyList())
    val joinedTrips: StateFlow<List<Trip>> = _joinedTrips

    fun getJoinedTrips(userId: Int, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            callbackFlow<List<Trip>> {
                // Query trips where the creator is NOT the given user
                val listener = Collections.trips
                    .whereNotEqualTo("creatorId", userId)
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null) {
                            // Convert documents to Trip objects, then filter trips
                            // where the participants contain the userId as a key
                            val joinedTrips = snapshot.toObjects(Trip::class.java)
                                .filter { it.participants.containsKey(userId.toString()) }

                            // Send the filtered list to the flow, log error on failure
                            trySend(joinedTrips).onFailure {
                                Log.e("Firestore", "Failed to send joined trips", it)
                            }
                        } else {
                            // Log error and send empty list if snapshot is null or error occurs
                            Log.e("Firestore", "Error fetching joined trips", error)
                            trySend(emptyList())
                        }
                    }
                // Remove Firestore listener when the flow collector is closed
                awaitClose { listener.remove() }

                // Collect the emitted trips and update the _joinedTrips state flow
            }.collect { trips ->
                _joinedTrips.value = trips
            }
        }
    }

    //Function that return the completed Trips a user has canceled from its personal page
    private val _canceledTrips = MutableStateFlow<Set<String>>(emptySet())
    val canceledTrips: StateFlow<Set<String>> = _canceledTrips

    // Retrieves the set of canceled completed trip IDs for a specific user from Firestore
    fun getCanceledTrips(userId: String, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            callbackFlow<Set<String>> {
                // Listen to the "canceledTrips" subcollection under the user's document
                val listener = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("canceledTrips")
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null) {
                            // Extract the document IDs (trip IDs) into a set
                            val tripIds = snapshot.documents.map { it.id }.toSet()
                            // Emit the set of canceled trip IDs, log on failure
                            trySend(tripIds).onFailure {
                                Log.e("Firestore", "Failed to send canceledTrips", it)
                            }
                        } else {
                            // Log the error and emit an empty set if snapshot is null or error occurs
                            Log.e("Firestore", "Error fetching canceledTrips", error)
                            trySend(emptySet())
                        }
                    }
                // Remove Firestore listener when the flow collector is closed
                awaitClose { listener.remove() }

                // Collect emitted sets and update the _canceledTrips state flow
            }.collect { ids ->
                _canceledTrips.value = ids
            }
        }
    }


    //List of filtered Trips
    private val _filteredList = MutableStateFlow<List<Trip>>(emptyList())
    val filteredList: StateFlow<List<Trip>> = _filteredList

    //Function that return the list of Trips after the application of the selected filters
    fun filterFunction(
        tripsFlow: Flow<List<Trip>>, filterDestination: String,
        filterMinPrice: Double, filterMaxPrice: Double,
        filterDuration: Pair<Int, Int>, filterGroupSize: Pair<Int, Int>,
        filtersTripType: List<SelectableItem>, filterUpcomingTrips: Boolean,
        filterCompletedTrips: Boolean, filterBySeats: Int, loggedUserId: Int = 0,
        coroutineScope: CoroutineScope
    ) {
        coroutineScope.launch {
            // Collect trips emitted by the flow continuously
            tripsFlow.collect { list ->
                // Apply initial filtering on multiple criteria
                val filtered = list.filter { trip ->
                    // Destination filter: accept if blank or trip destination contains filter string
                    val destination = filterDestination.isBlank() || trip.destination.contains(
                        filterDestination,
                        ignoreCase = true
                    )

                    // Duration filter: disabled if (-1, -1); otherwise, trip duration within range
                    val duration = (filterDuration.first == -1 && filterDuration.second == -1) ||
                            (trip.tripDuration() in filterDuration.first..filterDuration.second)

                    // Group size filter: disabled if (-1, -1); otherwise, trip group size within range
                    val groupSize = (filterGroupSize.first == -1 && filterGroupSize.second == -1) ||
                            (trip.groupSize in filterGroupSize.first..filterGroupSize.second)

                    // Price filter: disabled if both min and max are 0.0; otherwise trip price within range
                    val price = if (filterMinPrice == 0.0 && filterMaxPrice == 0.0) {
                        true
                    } else {
                        trip.estimatedPrice in filterMinPrice..filterMaxPrice
                    }

                    // Completed trips filter: only accept if filterCompletedTrips is false or trip is completed
                    val completed =
                        !filterCompletedTrips || trip.status == TripStatus.COMPLETED.toString()

                    // Upcoming trips filter: only accept if filterUpcomingTrips is false or user can join trip
                    val canJoin = !filterUpcomingTrips || trip.loggedInUserCanJoin(loggedUserId)

                    // Seats filter: trip must have at least filterBySeats available spots
                    val spots = trip.availableSpots() >= filterBySeats

                    // Trip must be published and meet all filter conditions
                    trip.published && destination && price && duration && groupSize && canJoin && completed && spots
                }

                // Further filter by trip type if any trip types are selected
                val finalFiltered = if (!filtersTripType.any { it.isSelected }) {
                    filtered    // no type filtering applied
                } else {
                    filtered.filter { trip ->
                        // Keep trips that match at least one selected trip type
                        filtersTripType.any { it.isSelected && trip.typeTravel.contains(it.typeTravel.toString()) }
                    }
                }
                // Update the observable filtered list with the final filtered trips
                _filteredList.value = finalFiltered
            }
        }
    }

    //FUNCTION FOR THE APPLICATION OF THE FILTERS

    //Function that returns the list of all the destinations present in the database
    // Uses callbackFlow to bridge Firestore's snapshot listener with Kotlin Flow.
    fun getDestinations(): Flow<List<String>> = callbackFlow {
        // The function sets up a real-time listener on the "trips" collection
        val listener = Collections.trips
            .whereEqualTo("published", true)        // Filter for trips where published is true
            //Whenever data changes, it extracts the "destination" field from
            // each document, removes duplicates, and emits the list of destinations.
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    val destinations = snapshot.documents
                        .mapNotNull { it.getString("destination") }
                        .distinct()

                    trySend(destinations).onFailure {
                        Log.e("Firestore", "Failed to send destinations", it)
                    }
                } else {
                    // Emits an empty list if an error occurs while fetching data.
                    Log.e("Firestore", "Error fetching destinations", error)
                    trySend(emptyList())
                }
            }
        // Remove the listener when the flow is closed or cancelled
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

        if (min == Int.MAX_VALUE && max == Int.MIN_VALUE) {
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
    fun importTrip(
        photo: String?,
        title: String, destination: String, startDate: Calendar,
        endDate: Calendar, estimatedPrice: Double, groupSize: Int,
        activities: Map<String, List<Activity>>, typeTravel: List<String>, creatorId: Int,
        published: Boolean, onResult: (Boolean, Trip?) -> Unit
    ) {
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
                photo = photo ?: "",  // 添加 photo 字段
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
                published = published,
                isDraft = true  // 设置为草稿
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


    //Function that edits an Activity
    fun editActivityInSelectedTrip(
        activityId: Int, updatedActivity: Activity, trip: Trip,
        onResult: (Boolean, Trip?) -> Unit
    ) {
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
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val newDateKey: String = dateFormat.format(updatedActivity.dateAsCalendar().time)
                val updatedList = originalActivities.getOrDefault(
                    newDateKey,
                    emptyList<Activity>()
                ) + updatedActivity
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

    //Delete an activity from a trip
    fun removeActivityFromTrip(activityId: Int, trip: Trip, onResult: (Boolean, Trip?) -> Unit) {
        // 直接使用传入的trip对象，而不是从数据库重新获取
        val updatedActivities = trip.activities.toMutableMap()
        var found = false

        // 查找并删除指定ID的活动
        for ((date, activities) in updatedActivities.toMap()) {
            if (activities.any { it.id == activityId }) {
                val filteredActivities = activities.filter { it.id != activityId }
                if (filteredActivities.isEmpty()) {
                    updatedActivities.remove(date)
                } else {
                    updatedActivities[date] = filteredActivities
                }
                found = true
                break
            }
        }

        if (!found) {
            onResult(false, trip)
            return
        }

        // 创建更新后的trip对象，保持所有原有数据
        val updatedTrip = trip.copy(activities = updatedActivities)

        // 只有当trip已经保存到数据库时才更新数据库
        if (trip.id != -1) {
            val docId = trip.id.toString()
            Collections.trips.document(docId)
                .set(updatedTrip)
                .addOnSuccessListener {
                    onResult(true, updatedTrip)
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Failed to delete activity", e)
                    onResult(false, null)
                }
        } else {
            // 如果trip还没有保存到数据库，直接返回更新后的trip
            onResult(true, updatedTrip)
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

    //Delete a trip from the "My Trips" section of the logged in user
    fun cancelTripForUser(userId: String, tripId: String, onResult: (Boolean) -> Unit) {
        val firebase = FirebaseFirestore.getInstance()
        val ref = firebase.collection("users")
            .document(userId)
            .collection("canceledTrips")
            .document(tripId)

        ref.set(emptyMap<String, Any>()) // Write an empty document
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to cancel trip", e)
                onResult(false)
            }
    }


    //MANAGEMENT OF APPLICATIONS TO TRIPS

    //Function that adds a request to join to a specific Trip
    private val _askedTrips = MutableStateFlow<Map<String, Int>>(emptyMap())
    val askedTrips: StateFlow<Map<String, Int>> = _askedTrips

    fun requestToJoin(
        trip: Trip, userId: Int, spots: Int,
        unregisteredParticipants: List<Participant>,
        registeredParticipants: List<Int>, onResult: (Boolean) -> Unit
    ) {
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
                    val requestedSpots =
                        trip?.appliedUsers?.get(userId.toString())?.requestedSpots ?: 0
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

    private val firestore = FirebaseFirestore.getInstance()
    fun getTripById(tripId: String, onResult: (Trip?) -> Unit) {
        firestore.collection("trips").document(tripId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val trip = document.toObject(Trip::class.java)
                    onResult(trip)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                Log.e("TripModel", "Failed to fetch trip by ID", it)
                onResult(null)
            }
    }

    fun addActivityToTrip(activity: Activity, trip: Trip?): Trip {
        val currentTrip = trip ?: Trip()

        // 🔴 添加详细的调试日志
        Log.d("TripModel", "=== Adding Activity ===")
        Log.d("TripModel", "Activity: $activity")
        Log.d("TripModel", "Activity date: ${activity.date.toDate()}")
        Log.d("TripModel", "Current trip: ${currentTrip.id}")

        val updatedActivities = currentTrip.activities.toMutableMap().apply {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val dateKey: String = dateFormat.format(activity.date.toDate())
            Log.d("TripModel", "Date key: $dateKey")

            val existingActivities = getOrDefault(dateKey, emptyList())
            val updatedList: List<Activity> = existingActivities + activity
            put(dateKey, updatedList)

            Log.d("TripModel", "Updated activities for $dateKey: ${updatedList.size} activities")
        }

        val updatedTrip = currentTrip.copy(activities = updatedActivities)

        Log.d("TripModel", "Final trip activities: ${updatedTrip.activities}")

        // 🔴 修复点20: 使用正确的Firestore引用
        if (currentTrip.id > 0) {
            FirebaseFirestore.getInstance()
                .collection("trips")
                .document(updatedTrip.id.toString())
                .set(updatedTrip)
                .addOnSuccessListener {
                    Log.d("TripModel", "Activity saved to Firestore successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("TripModel", "Failed to save activity to Firestore", e)
                }
        }

        return updatedTrip
    }
}

fun Trip.deepCopy(): Trip =
    this.copy(
        activities = this.activities.mapValues { (_, list) -> list.map { it.copy() } },
        typeTravel = this.typeTravel.toList(),
        participants = this.participants.toMap(),
        appliedUsers = this.appliedUsers.toMap(),
        rejectedUsers = this.rejectedUsers.toMap()
    )