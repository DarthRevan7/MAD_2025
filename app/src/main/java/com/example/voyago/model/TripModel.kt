package com.example.voyago.model

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.example.voyago.Collections
import com.example.voyago.model.Trip.Activity
import com.example.voyago.model.Trip.Participant
import com.example.voyago.model.Trip.TripStatus
import com.example.voyago.toCalendar
import com.example.voyago.toStringDate
import com.example.voyago.view.SelectableItem
import com.example.voyago.view.isUriString
import com.example.voyago.view.parseActivityDate
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

//Function that converts a Long in a Calendar
//fun toCalendar(timeDate: Timestamp): Calendar {
//    var calendarDate = Calendar.getInstance()
//    calendarDate.time = timeDate.toDate()
//    return calendarDate
//}

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
                    activityDate.toCalendar().get(Calendar.YEAR) == current.get(Calendar.YEAR) &&
                            activityDate.toCalendar().get(Calendar.DAY_OF_YEAR) == current.get(
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
        // Set up a Firestore listener on the "trips" collection filtering published trips only
        val listener = Collections.trips
            .whereEqualTo("published", true)        // Filter for trips where published is true
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    // Extract the "destination" field from each document and remove duplicates
                    val destinations = snapshot.documents
                        .mapNotNull { it.getString("destination") }
                        .distinct()

                    // Send the list of destinations downstream through the flow
                    trySend(destinations).onFailure {
                        Log.e("Firestore", "Failed to send destinations", it)
                    }
                } else {
                    // Emits an empty list if an error occurs while fetching data.
                    Log.e("Firestore", "Error fetching destinations", error)
                    trySend(emptyList())
                }
            }
        // Clean up listener when the flow is closed or cancelled
        awaitClose { listener.remove() }
    }

    //Function that set the Max and Min Price of the database
    internal var minPrice = Double.MAX_VALUE
    internal var maxPrice = Double.MIN_VALUE

    suspend fun setMaxMinPrice() {
        try {
            // Fetch all documents from the "trips" collection in Firestore
            val snapshot = Collections.trips.get().await()

            // Convert Firestore documents to a list of Trip objects
            val trips = snapshot.toObjects(Trip::class.java)

            // Find the minimum estimated price among all trips or use Double.MAX_VALUE if the list is empty
            minPrice = trips.minOfOrNull { it.estimatedPrice } ?: Double.MAX_VALUE
            // Find the maximum estimated price among all trips or use Double.MIN_VALUE if the list is empty
            maxPrice = trips.maxOfOrNull { it.estimatedPrice } ?: Double.MIN_VALUE
        } catch (e: Exception) {
            // If an error occurs while fetching or processing the data, log it and set fallback values
            Log.e("Firestore", "Failed to fetch trips for min/max price", e)
            minPrice = Double.MAX_VALUE
            maxPrice = Double.MIN_VALUE
        }
    }

    //Function that sets the Range of the Price slider
    fun setRange(list: List<SelectableItem>): Pair<Int, Int> {
        // Initialize min and max with extreme values
        var min = Int.MAX_VALUE
        var max = Int.MIN_VALUE

        // Iterate through the list of SelectableItem
        list.forEach { item ->
            if (item.isSelected) {
                // Update min if the item's min is smaller than current min
                if (item.min < min) {
                    min = item.min
                }
                // Update max if the item's max is larger than current max
                if (item.max > max) {
                    max = item.max
                }
            }
        }

        // If no items were selected, return (-1, -1) as a default "not set" value
        if (min == Int.MAX_VALUE && max == Int.MIN_VALUE) {
            min = -1
            max = -1
        }

        // Return the calculated min and max range as a Pair
        return Pair(min, max)
    }

    //Function that returns the list of completed Trips
    fun getCompletedTrips(): Flow<List<Trip>> = callbackFlow {
        // Set up a Firestore listener for trips with status "COMPLETED"
        val listener = Collections.trips
            .whereEqualTo("status", "COMPLETED")
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    // If the snapshot is valid, convert documents to Trip objects and send them through the flow
                    trySend(snapshot.toObjects(Trip::class.java)).onFailure {
                        Log.e("Firestore", "Send failed", it)
                    }
                } else {
                    // If there's an error with the listener, log it and send an empty list
                    Log.e("Firestore", "Listener error", error)
                    trySend(emptyList())
                }
            }

        // Clean up the listener when the flow collector is cancelled
        awaitClose { listener.remove() }
    }

    //Function that return the list of Upcoming Trips
    fun getUpcomingTrips(): Flow<List<Trip>> = callbackFlow {
        // Create a Firestore snapshot listener for trips with status "NOT_STARTED"
        val listener = Collections.trips
            .whereEqualTo("status", "NOT_STARTED")      // Filter trips that haven't started yet
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    // Convert the snapshot documents to a list of Trip objects and send it to the flow
                    trySend(snapshot.toObjects(Trip::class.java)).onFailure {
                        Log.e("Firestore", "Send failed", it)
                    }
                } else {
                    // If there's an error fetching the snapshot, log the error and send an empty list
                    Log.e("Firestore", "Listener error", error)
                    trySend(emptyList())
                }
            }

        // Automatically remove the listener when the flow is closed to prevent memory leaks
        awaitClose { listener.remove() }
    }

    //CREATE A TRIP

    //Function that creates a new Trip
    fun createNewTrip(newTrip: Trip, onResult: (Boolean, Trip?) -> Unit) {
        // Get Firestore instance and reference to the trip counter document
        val firestore = Firebase.firestore
        val counterRef = firestore.collection("metadata").document("tripCounter")

        // Use a Firestore transaction to safely increment the trip ID and create a new trip
        firestore.runTransaction { transaction ->
            // Retrieve the current trip counter
            val snapshot = transaction.get(counterRef)
            val lastTripId = snapshot.getLong("lastTripId") ?: 0
            val newTripId = lastTripId + 1

            // Update the counter document with the new trip ID
            transaction.update(counterRef, "lastTripId", newTripId)

            // Create a new Trip object with the generated ID
            val tripWithId = newTrip.copy(id = newTripId.toInt())

            // Create a new document in the "trips" collection using the new trip ID
            val tripDocRef = firestore.collection("trips").document(newTripId.toString())
            transaction.set(tripDocRef, tripWithId)

            // Return the created trip from the transaction
            tripWithId
        }.addOnSuccessListener { trip ->
            // If transaction succeeds, return the created trip via callback
            onResult(true, trip)
        }.addOnFailureListener { e ->
            // If transaction fails, log the error and notify the caller
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
        // Get Firestore instance and reference to the trip counter document
        val firestore = Firebase.firestore
        val counterRef = firestore.collection("metadata").document("tripCounter")

        firestore.runTransaction { transaction ->
            // Retrieve the current trip ID counter and increment it
            val snapshot = transaction.get(counterRef)
            val lastTripId = snapshot.getLong("lastTripId") ?: 0
            val newTripId = lastTripId + 1
            transaction.update(counterRef, "lastTripId", newTripId)

            // Create a new Firestore document reference using the new trip ID
            val docRef = firestore.collection("trips").document(newTripId.toString())

            // Construct the Trip object with all the provided and default fields
            val newTrip = Trip(
                id = newTripId.toInt(),
                photo = photo
                    ?: "",                        // If no photo is provided, use an empty string
                title = title,
                destination = destination,
                startDate = Timestamp(startDate.time),  // Convert Calendar to Timestamp
                endDate = Timestamp(endDate.time),      // Convert Calendar to Timestamp
                estimatedPrice = estimatedPrice,
                groupSize = groupSize,
                participants = mapOf(
                    // Initialize participants with the creator
                    creatorId.toString() to Trip.JoinRequest(
                        userId = creatorId,
                        requestedSpots = 1,
                        unregisteredParticipants = emptyList(),
                        registeredParticipants = emptyList()
                    )
                ),
                activities = activities,
                status = TripStatus.NOT_STARTED.toString(),     // Default status when importing
                typeTravel = typeTravel,
                creatorId = creatorId,
                appliedUsers = emptyMap(),
                rejectedUsers = emptyMap(),
                published = published,
                isDraft = true          // Mark as a draft (used for imported but unpublished trips)
            )

            // Save the new trip to Firestore in the transaction
            transaction.set(docRef, newTrip)
            newTrip
        }.addOnSuccessListener { trip ->
            // Call the result callback with success and the created trip
            onResult(true, trip)
        }.addOnFailureListener { e ->
            // Log error and return failure through the callback
            Log.e("Firestore", "Failed to import trip", e)
            onResult(false, null)
        }
    }


    //EDIT TRIP

    suspend fun uploadPhotoAndGetUrl(photoUri: Uri, tripId: String): String {
        return try {
            val storageUrl = "trips/${tripId}/${tripId + "_cover"}"
            val storageRef = Firebase.storage.reference.child(storageUrl)
            storageRef.putFile(photoUri).await()
            return storageUrl // Get download URL
        } catch (e: Exception) {
            Log.e("Firebase", "Error uploading photo", e)
            throw e // Re-throw the exception for handling in editTrip
        }
    }


    fun editTrip(updatedTrip: Trip,
                 originalTrip: Trip,
                 viewModelScope: CoroutineScope,
                 onResult: (Boolean) -> Unit) {
        val docId = updatedTrip.id.toString()

        viewModelScope.launch {
            val tripDocRef = Collections.trips.document(docId)

            try {
                var tripToUpdate = updatedTrip // Start with the updated trip

                // If a new photo is provided, upload it and get the URL
                if (updatedTrip.photo != null) {
                        if(updatedTrip.photo!!.isUriString())
                        {
                            Log.d("T2", "updatedTrip.photo=${updatedTrip.photo}")
                            val photoUrl = uploadPhotoAndGetUrl(updatedTrip.photo!!.toUri(), updatedTrip.id.toString())
                            tripToUpdate = tripToUpdate.copy(photo = photoUrl) // Update the trip with the new URL
                            Log.d("T2", "Photo update success: URL = $photoUrl")
                        }
                }


                // Overwrite the document with the (possibly) updated trip data
                tripDocRef.set(tripToUpdate).await() // await() here!
                Log.d("DB1", "Trip ${updatedTrip.id} updated successfully.")
                tripDocRef.update("draft", false).await()
                onResult(true) // Notify success

            } catch (e: Exception) {
                Log.e("DB1", "Error editing trip ${updatedTrip.id}: ${e.message}", e)
                onResult(false) // Notify failure
            }
        }
    }

    //Functions that edits the information of a specific Trip
//    fun editTrip(updatedTrip: Trip, viewModelScope: CoroutineScope, onResult: (Boolean) -> Unit) {
//        // Convert the trip ID to a string to match the Firestore document ID format
//        val docId = updatedTrip.id.toString()
//
//        // Reference the specific trip document
//        val tripDocRef = Collections.trips.document(docId)
//
//        viewModelScope.launch {
//            // Access the "trips" collection and update the document with the given ID
//            try {
//                var updateTrip: Trip? = null
//                var successPhoto: Boolean? = false
//                tripDocRef.get().addOnSuccessListener { result ->
//                    updateTrip = result.toObject(Trip::class.java)
//                }
//                if(updatedTrip.photo != null && updateTrip != null)
//                {
//                    Log.d("T2", "updatedTrip.photo.toUri=${updatedTrip.photo!!.toUri()}")
//                    successPhoto = updateTrip!!.setPhoto(updatedTrip.photo!!.toUri())
//                    Log.d("T2", "Photo update success")
//                }
//
//                // Overwrites the document with the new trip data
//                if(updateTrip != null)
//                {
//                    tripDocRef.set(updateTrip!!).await()
//                    onResult(true)
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }

    //Function that changes the Published status of a specific Trip
    fun changePublishedStatus(id: Int, onResult: (Boolean) -> Unit) {
        // Convert trip ID to string to match Firestore document ID
        val docId = id.toString()

        // Reference the specific trip document
        val tripDocRef = Collections.trips.document(docId)

        // Fetch the current trip data to toggle the published status
        tripDocRef.get()
            .addOnSuccessListener { snapshot ->
                // Convert snapshot to Trip object
                val trip = snapshot.toObject(Trip::class.java)
                if (trip != null) {
                    // Toggle the current published value
                    val updatedPublished = !trip.published

                    // Update the "published" field in Firestore
                    tripDocRef.update("published", updatedPublished)
                        .addOnSuccessListener { onResult(true) }    // Notify success
                        .addOnFailureListener {
                            Log.e("Firestore", "Failed to update published status", it)
                            onResult(false)     // Notify failure if update fails
                        }
                } else {
                    // Trip does not exist in the database
                    Log.e("Firestore", "Trip not found for ID: $id")
                    onResult(false)
                }
            }
            .addOnFailureListener {
                // Failed to retrieve trip document
                Log.e("Firestore", "Failed to fetch trip for ID: $id", it)
                onResult(false)
            }
    }

    //ACTIVITY MANAGEMENT

    //Function that add an Activity to a specific Trip
    fun addActivityToTrip(activity: Activity, trip: Trip?): Trip {
        val currentTrip = trip ?: Trip()

        // 创建活动的可变副本
        val updatedActivities = currentTrip.activities.toMutableMap()

        // 使用活动日期作为字符串键
        val dateKey: String = activity.dateAsCalendar().toStringDate()

        // 获取该日期现有的活动列表
        val existingActivities = updatedActivities.getOrDefault(dateKey, emptyList()).toMutableList()

        // 添加新活动
        existingActivities.add(activity)

        // 按时间排序活动
        val sortedActivities = existingActivities.sortedBy { act ->
            try {
                LocalTime.parse(act.time, DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
            } catch (e: Exception) {
                LocalTime.MIN // 如果解析失败，放在最前面
            }
        }

        // 更新活动映射
        updatedActivities[dateKey] = sortedActivities

        // 创建更新后的行程对象
        val updatedTrip = currentTrip.copy(activities = updatedActivities)

        // 如果行程已存在于 Firestore 中，保存更新
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


    //Function that edits an Activity
    fun editActivityInSelectedTrip(
        activityId: Int,
        updatedActivity: Activity,
        trip: Trip,
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

                // 创建活动的可变副本
                val originalActivities = currentTrip.activities.toMutableMap()
                var found = false

                // 第一步：从原位置移除活动
                for ((dateKey, activities) in originalActivities.toMap()) {
                    if (activities.any { it.id == activityId }) {
                        val newList = activities.filter { it.id != activityId }

                        if (newList.isEmpty()) {
                            // 如果该日期没有其他活动了，移除整个日期键
                            originalActivities.remove(dateKey)
                        } else {
                            // 否则更新活动列表
                            originalActivities[dateKey] = newList
                        }
                        found = true
                        break
                    }
                }

                if (!found) {
                    onResult(false, trip)
                    return@addOnSuccessListener
                }

                // 第二步：根据新的活动日期，找到正确的日期键来放置活动
                val newActivityDate = updatedActivity.dateAsCalendar()
                val newDateKey = findCorrectDateKeyForActivity(newActivityDate, originalActivities, currentTrip)

                // 将活动添加到正确的日期键下
                if (originalActivities.containsKey(newDateKey)) {
                    // 如果该日期已存在活动，添加到现有列表
                    val existingActivities = originalActivities[newDateKey]!!.toMutableList()
                    existingActivities.add(updatedActivity)

                    // 按时间排序
                    val sortedActivities = existingActivities.sortedBy { activity ->
                        parseTimeToMinutes(activity.time)
                    }

                    originalActivities[newDateKey] = sortedActivities
                } else {
                    // 如果该日期不存在活动，创建新列表
                    originalActivities[newDateKey] = listOf(updatedActivity)
                }

                // 创建更新后的行程对象
                val updatedTrip = currentTrip.copy(activities = originalActivities)

                // 保存到 Firestore
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

    // 新增函数：根据活动日期找到正确的日期键
    private fun findCorrectDateKeyForActivity(
        activityDate: Calendar,
        existingActivities: Map<String, List<Trip.Activity>>,
        trip: Trip
    ): String {
        // 标准化活动日期（只保留年月日，去掉时间）
        val normalizedActivityDate = Calendar.getInstance().apply {
            timeInMillis = activityDate.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 首先检查是否与现有的日期键匹配
        for (dateKey in existingActivities.keys) {
            try {
                val existingDate = parseActivityDate(dateKey)
                val normalizedExistingDate = Calendar.getInstance().apply {
                    timeInMillis = existingDate.timeInMillis
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // 如果日期匹配，使用现有的日期键
                if (normalizedActivityDate.timeInMillis == normalizedExistingDate.timeInMillis) {
                    return dateKey
                }
            } catch (e: Exception) {
                Log.e("DateMatching", "Error parsing existing date key: $dateKey", e)
            }
        }

        // 如果没有匹配的现有日期键，创建新的日期键
        return normalizedActivityDate.toStringDate()
    }

    private fun parseTimeToMinutes(timeString: String): Int {
        return try {
            val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
            val time = LocalTime.parse(timeString, formatter)
            time.hour * 60 + time.minute
        } catch (e: Exception) {
            // 如果解析失败，返回一个默认值
            Log.e("TimeParsing", "Error parsing time: $timeString", e)
            0
        }
    }

    //Delete an activity from a trip
    fun removeActivityFromTrip(activityId: Int, trip: Trip, onResult: (Boolean, Trip?) -> Unit) {
        // Create a mutable copy of the trip's activities map
        val updatedActivities = trip.activities.toMutableMap()
        // Flag to track if the activity to remove is found
        var found = false

        // Iterate over a copy of the map entries (date to activities list)
        for ((date, activities) in updatedActivities.toMap()) {
            // Check if any activity in the list matches the activityId to remove
            if (activities.any { it.id == activityId }) {
                // Filter out the activity to remove
                val filteredActivities = activities.filter { it.id != activityId }
                // If no activities remain for that date, remove the date key from the map
                if (filteredActivities.isEmpty()) {
                    updatedActivities.remove(date)
                } else {
                    // Otherwise, update the activities list for that date
                    updatedActivities[date] = filteredActivities
                }
                // Mark that the activity was found and removed
                found = true
                // Exit the loop since we found and removed the activity
                break
            }
        }

        // If the activity was not found, return failure with the original trip
        if (!found) {
            onResult(false, trip)
            return
        }

        // Create a new Trip instance with the updated activities map
        val updatedTrip = trip.copy(activities = updatedActivities)


        // Check if the trip has a valid Firestore document ID
        if (trip.id != -1) {
            val docId = trip.id.toString()
            // Update the trip document in Firestore with the new activities map
            Collections.trips.document(docId)
                .set(updatedTrip)
                .addOnSuccessListener {
                    onResult(true, updatedTrip)     // Notify success with updated trip
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Failed to delete activity", e)
                    onResult(false, null)       // Notify failure
                }
        } else {
            // If the trip ID is invalid (e.g., not saved in Firestore), just return success with the updated trip locally
            onResult(true, updatedTrip)
        }
    }

    //DELETE A TRIP

    //Delete a trip
    fun deleteTrip(id: Int, onResult: (Boolean) -> Unit) {
        // Convert the trip ID to a string to use as Firestore document ID
        val docId = id.toString()

        Collections.trips
            .document(docId)        // Reference the Firestore document with the given ID
            .delete()               // Attempt to delete the document from Firestore
            .addOnSuccessListener {
                onResult(true)      // Notify success if deletion is successful
            }
            .addOnFailureListener { e ->
                // Notify failure
                Log.e("Firestore", "Failed to delete trip with ID: $id", e)
                onResult(false)
            }
    }

    //Delete a trip from the "My Trips" section of the logged in user
    fun cancelTripForUser(userId: String, tripId: String, onResult: (Boolean) -> Unit) {
        // Get Firestore instance
        val firebase = FirebaseFirestore.getInstance()

        // Reference to the user's canceledTrips subcollection document for the given tripId
        val ref = firebase.collection("users")
            .document(userId)
            .collection("canceledTrips")
            .document(tripId)

        // Write an empty document to mark the trip as canceled for the user
        ref.set(emptyMap<String, Any>()) // Write an empty document
            .addOnSuccessListener { onResult(true) }        // Notify success if operation succeeds
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to cancel trip", e)
                onResult(false)     // Notify failure
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
        // Convert trip ID to string for Firestore document ID
        val docId = trip.id.toString()
        // Reference to the trip document
        val tripDocRef = Collections.trips.document(docId)

        // Create a JoinRequest object with user details and requested spots
        val joinRequest = Trip.JoinRequest(
            userId = userId,
            requestedSpots = spots,
            unregisteredParticipants = unregisteredParticipants,
            registeredParticipants = registeredParticipants
        )

        // Prepare the map to update the appliedUsers field with the new join request
        val joinRequestMap = mapOf("appliedUsers.$userId" to joinRequest)

        // Prepare the map to update the appliedUsers field with the new join request
        tripDocRef.update(joinRequestMap)
            .addOnSuccessListener {
                // On success, add the trip ID and spots to the askedTrips state
                _askedTrips.value = _askedTrips.value + (trip.id.toString() to spots)
                onResult(true)  // Notify success
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to apply to trip ${trip.id}", e)
                onResult(false)     // Notify failure
            }
    }

    //Function that removes a request to join to a specific Trip
    fun cancelRequestToJoin(trip: Trip, userId: Int, onResult: (Boolean) -> Unit) {
        // Reference to the trip document in Firestore
        val docRef = Collections.trips.document(trip.id.toString())

        // Create a mutable copy of the appliedUsers map and remove the userId from it locally
        val updatedAppliedUsers = trip.appliedUsers.toMutableMap().apply {
            remove(userId.toString())   // Remove the user's join request
        }

        // Update the appliedUsers field in Firestore with the updated map (without the cancelled user)
        docRef.update("appliedUsers", updatedAppliedUsers)
            .addOnSuccessListener {
                // Update local state by removing this trip from askedTrips
                _askedTrips.value = _askedTrips.value - trip.id.toString()
                onResult(true)      // Notify that cancellation succeeded
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to cancel join request", e)
                onResult(false)     // Notify that cancellation failed
            }
    }

    //Function that syncs the join request of a user with the AppliedUser of the trips
    fun syncAskedTripsWithAppliedUsers(userId: Int, onResult: (Boolean) -> Unit) {
        // Fetch all trips from the Firestore collection
        Collections.trips
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Create a map of tripId to requestedSpots for trips where the user has applied
                val askedMap = querySnapshot.documents.mapNotNull { doc ->
                    val trip = doc.toObject(Trip::class.java)
                    // Get the number of spots requested by the current user (if any)
                    val requestedSpots =
                        trip?.appliedUsers?.get(userId.toString())?.requestedSpots ?: 0
                    // Include in map only if the user has requested one or more spots
                    if (requestedSpots > 0 && trip != null) trip.id.toString() to requestedSpots else null
                }.toMap()
                // Update the local askedTrips state with the newly constructed map
                _askedTrips.value = askedMap
                onResult(true)
            }
            .addOnFailureListener { e ->
                // Log and report failure if the Firestore read operation fails
                Log.e("Firestore", "Failed to sync asked trips", e)
                onResult(false)
            }
    }

    // Firestore instance used throughout the class
    private val firestore = FirebaseFirestore.getInstance()

    // Retrieves a trip document by its ID and returns the result via a callback
    fun getTripById(tripId: String, onResult: (Trip?) -> Unit) {
        // Access the "trips" collection and fetch the document with the given tripId
        firestore.collection("trips").document(tripId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Convert the document to a Trip object if it exists
                    val trip = document.toObject(Trip::class.java)
                    onResult(trip)
                } else {
                    // If the document does not exist, return null
                    onResult(null)
                }
            }
            .addOnFailureListener {
                // Log any errors that occur while fetching the document
                Log.e("TripModel", "Failed to fetch trip by ID", it)
                // Return null in case of failure
                onResult(null)
            }
    }

    // Get trip by title
    suspend fun getTripByTitle(title: String): Trip? {
        val db = Firebase.firestore
        return try {
            val snapshot = db.collection("trips")
                .whereEqualTo("title", title)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot.documents.first().toObject(Trip::class.java)
            } else null
        } catch (e: Exception) {
            Log.e("TripModel", "Error fetching trip by title", e)
            null
        }
    }



}

// Creates a deep copy of a Trip instance to avoid shared references in mutable fields
fun Trip.deepCopy(): Trip =
    this.copy(
        // Deep copy of the activities map:
        // For each date key, create a new list of copied Activity instances
        activities = this.activities.mapValues { (_, list) -> list.map { it.copy() } },
        // Copy the typeTravel list to ensure it's a separate instance
        typeTravel = this.typeTravel.toList(),
        // Create shallow copies of these maps (sufficient if their contents are immutable or not shared)
        participants = this.participants.toMap(),
        appliedUsers = this.appliedUsers.toMap(),
        rejectedUsers = this.rejectedUsers.toMap()
    )