package com.example.voyago.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.voyago.model.ReviewModel
import com.example.voyago.model.Trip
import com.example.voyago.model.Trip.Activity
import com.example.voyago.model.Trip.Participant
import com.example.voyago.model.TripModel
import com.example.voyago.model.TypeTravel
import com.example.voyago.model.User
import com.example.voyago.model.UserModel
import com.example.voyago.view.SelectableItem
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class TripViewModel(
    val tripModel: TripModel,
    val userModel: UserModel,
    val reviewModel: ReviewModel,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUserId(): Int {
        return auth.currentUser?.uid?.hashCode()?.let {
            if (it < 0) -it else it
        } ?: -1
    }

    //Use in the new Trip interface
    var newTrip: Trip = Trip()

    //Use in the edit trip interface
    var editTrip: Trip = Trip()

    //Used to see the details of a trip that isn't the selected trip
    private val _otherTrip = mutableStateOf<Trip>(Trip())
    val otherTrip: State<Trip> = _otherTrip

    fun setOtherTrip(trip: Trip) {
        _otherTrip.value = trip
    }

    //Used in the select trip interface (trip detail)
    private val _selectedTrip = mutableStateOf<Trip>(Trip())
    val selectedTrip: State<Trip> = _selectedTrip

    fun setSelectedTrip(trip: Trip) {
        _selectedTrip.value = trip
    }

    //Identify what the user is doing
    var userAction by mutableStateOf(UserAction.NOTHING)
//    var userAction by mutableStateOf(savedStateHandle["userAction"] ?: UserAction.NOTHING)
//        private set

//    fun setUserAction(action: UserAction) {
//        userAction = action
//        savedStateHandle["userAction"] = action
//    }

    enum class UserAction {
        EDIT_TRIP, CREATE_TRIP, VIEW_TRIP, NOTHING, SEARCHING, FILTER_SELECTION, VIEW_OTHER_TRIP, EDIT_ACTIVITY
    }

    //EXPLORE

    //Filtered list of trips
    val filteredList = tripModel.filteredList

    //Destination filter
    var filterDestination: String by mutableStateOf("")
        private set

    fun updateFilterDestination(str: String) {
        filterDestination = str
    }

    //List of destination of the trip present in the database
    fun allDestinations() = tripModel.getDestinations()

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
    suspend fun setMaxMinPrice() = tripModel.setMaxMinPrice()

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
        filterDuration = tripModel.setRange(list)
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
        filterGroupSize = tripModel.setRange(list)
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

    fun getUpcomingTripsList(): Flow<List<Trip>> {
        return tripModel.getUpcomingTrips()
    }

    fun updateUpcomingTripsFilter(isSelected: Boolean) {
        filterUpcomingTrips = isSelected
    }

    //Trips that already happened filter
    var filterCompletedTrips: Boolean by mutableStateOf(false)
        private set

    fun getCompletedTripsList(): Flow<List<Trip>> {
        return tripModel.getCompletedTrips()
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
    fun applyFilters() = tripModel.filterFunction(
        allPublishedList,
        filterDestination,
        filterMinPrice,
        filterMaxPrice,
        filterDuration,
        filterGroupSize,
        filtersTripType,
        filterUpcomingTrips,
        filterCompletedTrips,
        filterBySeats,
        viewModelScope
    )

    //Reset filters
    fun resetFilters() {
        filterDestination = ""

        updateFilterPriceRange(0.0, 0.0)
        viewModelScope.launch {
            tripModel.setMaxMinPrice()
        }
        val minPrice = tripModel.minPrice.toFloat()
        val maxPrice = tripModel.maxPrice.toFloat()
        val bounds = minPrice..maxPrice
        _selectedPriceRange.value = bounds

        filterDuration = Pair(-1, -1)
        filterGroupSize = Pair(-1, -1)
        durationItems.forEach { it.isSelected = false }
        groupSizeItems.forEach { it.isSelected = false }
        filtersTripType.forEach { it.isSelected = false }
        filterCompletedTrips = false
        filterBySeats = 0
    }

    //Update list of published trips after filter application
    val allPublishedList = tripModel.allPublishedTrips

    fun updatePublishedTrip() {
        tripModel.getAllPublishedTrips(viewModelScope)
        applyFilters()
    }


    //Ask to join a trip or cancel application
    val askedTrips = tripModel.askedTrips
    fun askToJoin(
        trip: Trip, userId: Int, spots: Int, unregisteredParticipants: List<Participant>,
        registeredParticipants: List<Int>
    ) {
        tripModel.requestToJoin(
            trip = trip, userId = userId, spots = spots,
            unregisteredParticipants = unregisteredParticipants,
            registeredParticipants = registeredParticipants
        ) { success ->
            if (success) {
                Log.d("ViewModel", "Successfully requested to join trip ${trip.id}")
            } else {
                Log.e("ViewModel", "Failed to request to join trip ${trip.id}")
            }
        }
    }

    fun cancelAskToJoin(trip: Trip, userId: Int) {
        tripModel.cancelRequestToJoin(trip, userId) { success ->
            if (success) {
                Log.d("ViewModel", "Request canceled successfully")
            } else {
                Log.e("ViewModel", "Failed to cancel request")
            }
        }
    }

    fun syncAskedTrips(userId: Int, onResult: (Boolean) -> Unit) {
        tripModel.syncAskedTripsWithAppliedUsers(userId, onResult)
    }

    //MY TRIPS
    fun editTrip(trip: Trip, onResult: (Boolean) -> Unit) {
        editExistingTrip(trip, onResult)
    }

    //List of trips created and published by the logged in user
    val publishedTrips = tripModel.publishedTrips
    fun creatorPublicFilter(id: Int) = tripModel.filterPublishedByCreator(id, viewModelScope)

    //List of trips created, but not published by the logged in user
    val privateTrips = tripModel.privateTrips
    fun creatorPrivateFilter(id: Int) = tripModel.filterPrivateByCreator(id, viewModelScope)

    //List of trips a user joined
    val joinedTrips = tripModel.joinedTrips
    fun tripUserJoined(userId: Int) = tripModel.getJoinedTrips(userId, viewModelScope)

    //Import an already published trip as a private trip of the logged in user (id=1)
    fun addImportedTrip(
        photo: String?, title: String, destination: String, startDate: Calendar,
        endDate: Calendar, estimatedPrice: Double, groupSize: Int,
        activities: Map<String, List<Trip.Activity>>, typeTravel: List<String>, creatorId: Int,
        published: Boolean, onResult: (Boolean, Trip?) -> Unit
    ) {
        tripModel.importTrip(
            photo,
            title,
            destination,
            startDate,
            endDate,
            estimatedPrice,
            groupSize,
            activities,
            typeTravel,
            creatorId,
            published,
            onResult
        )
    }


    //Make published or private a trip
    fun changePublishedStatus(id: Int) {
        tripModel.changePublishedStatus(id) { success ->
            if (success) {
                Log.d("TripViewModel", "Trip publish status changed successfully")
            } else {
                Log.e("TripViewModel", "Failed to change publish status")
            }
        }
    }

    //Update creator of a trip
    fun updateTripCreator(tripId: Int, newCreatorId: Int, oldCreatorId: Int) =
        tripModel.updateCreatorId(tripId, newCreatorId, oldCreatorId)

    //Update trip participants
    fun updateTripParticipants(tripId: Int, participantId: Int) =
        tripModel.removeParticipantFromTrip(tripId, participantId.toString()) { success ->
            if (success) {
                Log.d("TripViewModel", "Trip participants updated successfully")
            } else {
                Log.e("TripViewModel", "Failed to update trip participants")
            }
        }

    //Delete a trip from the database
    fun deleteTrip(id: Int) {
        tripModel.deleteTrip(id) { success ->
            if (success) {
                Log.d("ViewModel", "Trip successfully deleted from Firebase.")
            } else {
                Log.e("ViewModel", "Trip deletion failed.")
            }
        }
    }

    //Mutable list of applications
    private val _applications = MutableStateFlow<Map<User, Trip.JoinRequest>>(emptyMap())
    val applications: StateFlow<Map<User, Trip.JoinRequest>> = _applications

    // Participants with spots taken
    private val _tripParticipants = MutableStateFlow<Map<User, Trip.JoinRequest>>(emptyMap())
    val tripParticipants: StateFlow<Map<User, Trip.JoinRequest>> = _tripParticipants

    fun getTripParticipants(trip: Trip) {
        val userIds = trip.participants.keys.toList()

        if (userIds.isEmpty()) {
            _tripParticipants.value = emptyMap()
            return
        }

        viewModelScope.launch {
            userModel.getUsers(userIds.map { it -> it.toInt() }).collect {
                try {
                    val userMap = it.associateBy { it.id }
                    val mapped = trip.participants.mapNotNull { (userId, joinRequest) ->
                        userMap[userId.toInt()]?.let { user ->
                            user to joinRequest
                        }
                    }.toMap()
                    _tripParticipants.value = mapped
                } catch (e: Exception) {
                    Log.e("TripViewModel", "Errore recupero utenti", e)
                    _tripParticipants.value = emptyMap()
                }
            }
        }
    }


    //Applicants with Requested Spots
    private val _tripApplicants = MutableStateFlow<Map<User, Trip.JoinRequest>>(emptyMap())
    val tripApplicants: StateFlow<Map<User, Trip.JoinRequest>> = _tripApplicants

    fun getTripApplicants(trip: Trip) {
        val userIds = trip.appliedUsers.keys.toList()

        if (userIds.isEmpty()) {
            _tripApplicants.value = emptyMap()
            return
        }

        viewModelScope.launch {
            userModel.getUsers(userIds.map { it -> it.toInt() }).collect {
                try {
                    val userMap = it.associateBy { it.id }
                    val mapped = trip.appliedUsers.mapNotNull { (userId, joinRequest) ->
                        userMap[userId.toInt()]?.let { user ->
                            user to joinRequest
                        }
                    }.toMap()
                    _tripApplicants.value = mapped
                } catch (e: Exception) {
                    Log.e("TripViewModel", "Errore recupero utenti", e)
                    _tripApplicants.value = emptyMap()
                }
            }
        }
    }

    //Rejected users with requested spots
    private val _tripRejectedUsers = MutableStateFlow<Map<User, Trip.JoinRequest>>(emptyMap())
    val tripRejectedUsers: StateFlow<Map<User, Trip.JoinRequest>> = _tripRejectedUsers

    fun getTripRejectedUsers(trip: Trip) {
        val userIds = trip.rejectedUsers.keys.toList()

        if (userIds.isEmpty()) {
            _tripRejectedUsers.value = emptyMap()
            return
        }

        viewModelScope.launch {
            userModel.getUsers(userIds.map { it -> it.toInt() }).collect {
                try {
                    val userMap = it.associateBy { it.id }
                    val mapped = trip.rejectedUsers.mapNotNull { (userId, joinRequest) ->
                        userMap[userId.toInt()]?.let { user ->
                            user to joinRequest
                        }
                    }.toMap()
                    _tripRejectedUsers.value = mapped
                } catch (e: Exception) {
                    Log.e("TripViewModel", "Errore recupero utenti", e)
                    _tripRejectedUsers.value = emptyMap()
                }
            }
        }
    }


    //Approve an application
    fun acceptApplication(trip: Trip?, userId: Int) {
        if (trip == null || userId.toString() !in trip.appliedUsers) return

        val requestedSpots = trip.appliedUsers[userId.toString()]?.requestedSpots ?: return

        val usedSpots = trip.participants.values.sumOf { it.requestedSpots }

        val joinRequest = trip.appliedUsers[userId.toString()] ?: return

        if (usedSpots + requestedSpots > trip.groupSize) return

        // Accept applicant
        trip.participants = trip.participants + (userId.toString() to joinRequest)
        trip.appliedUsers = trip.appliedUsers - userId.toString()

        // Recalculate used spots
        val updatedUsedSpots = trip.participants.values.sumOf { it.requestedSpots }
        val remainingSpots = trip.groupSize - updatedUsedSpots

        // Reject remaining applicants who can't fit
        val remainingApplicants = trip.appliedUsers.toMap()
        for ((id, joinRequest) in remainingApplicants) {
            if (joinRequest.requestedSpots > remainingSpots) {
                trip.appliedUsers = trip.appliedUsers - id
                trip.rejectedUsers = trip.rejectedUsers + (id to joinRequest)
            }
        }

        trip.updateApplicationStatus(
            onSuccess = {
                Log.d("TripViewModel", "Accepted application and updated Firestore")
            },
            onFailure = {
                Log.e("TripViewModel", "Failed to update trip in Firestore", it)
            }
        )

        // Update your applications value accordingly
        _applications.value = tripApplicants.value
    }

    //Reject an application
    fun rejectApplication(trip: Trip?, userId: Int) {
        if (trip != null && userId.toString() in trip.appliedUsers) {
            val joinRequest = trip.appliedUsers[userId.toString()] ?: return

            trip.appliedUsers = trip.appliedUsers - userId.toString()
            trip.rejectedUsers = trip.rejectedUsers + (userId.toString() to joinRequest)

            trip.updateApplicationStatus(
                onSuccess = {
                    Log.d("TripViewModel", "Accepted application and updated Firestore")
                },
                onFailure = {
                    Log.e("TripViewModel", "Failed to update trip in Firestore", it)
                }
            )

            // Assign just the list of users, ignoring requestedSpots here
            _applications.value = tripRejectedUsers.value
        }
    }

    //Add new trip to the database
    fun addNewTrip(newTrip: Trip, onResult: (Boolean, Trip?) -> Unit) {
        tripModel.createNewTrip(newTrip) { success, createdTrip ->
            if (success && createdTrip != null) {
                _selectedTrip.value = createdTrip
            }
            onResult(success, createdTrip)
        }
    }


    //Edit an already existing trip in the database
    fun editExistingTrip(trip: Trip, onResult: (Boolean) -> Unit) {
        tripModel.editTrip(trip) { success ->
            if (success) {
                _selectedTrip.value = trip
            }
            onResult(success)
        }
    }


    //Delete activity from a specific trip
    //Delete activity from a specific trip
    fun deleteActivity(activity: Trip.Activity) {
        Log.d("TripViewModel", "Deleting activity: ${activity.id}")

        when (userAction) {
            UserAction.CREATE_TRIP -> {
                // 对于新创建的行程，只在本地删除
                val updatedActivities = newTrip.activities.toMutableMap()
                updatedActivities.forEach { (day, activities) ->
                    val filteredActivities = activities.filter { it.id != activity.id }
                    if (filteredActivities.isEmpty()) {
                        updatedActivities.remove(day)
                    } else {
                        updatedActivities[day] = filteredActivities
                    }
                }
                newTrip = newTrip.copy(activities = updatedActivities)
                _selectedTrip.value = newTrip
            }

            UserAction.EDIT_TRIP -> {
                // 对于编辑现有行程，调用 TripModel 删除并同步到数据库
                tripModel.removeActivityFromTrip(activity.id, editTrip) { success, updatedTrip ->
                    if (success && updatedTrip != null) {
                        editTrip = updatedTrip
                        _selectedTrip.value = updatedTrip
                        Log.d(
                            "TripViewModel",
                            "Activity deleted and synced to Firebase successfully"
                        )
                    } else {
                        Log.e("TripViewModel", "Failed to delete activity from Firebase")
                    }
                }
            }

            else -> {
                // 对于其他情况，如果是已保存的行程，也调用 TripModel
                val currentTrip = _selectedTrip.value
                if (currentTrip.id != -1) {
                    tripModel.removeActivityFromTrip(
                        activity.id,
                        currentTrip
                    ) { success, updatedTrip ->
                        if (success && updatedTrip != null) {
                            _selectedTrip.value = updatedTrip
                            Log.d(
                                "TripViewModel",
                                "Activity deleted and synced to Firebase successfully"
                            )
                        } else {
                            Log.e("TripViewModel", "Failed to delete activity from Firebase")
                        }
                    }
                }
            }
        }
    }

    //Edit a specific activity from a specific trip
    fun editActivity(activityId: Int, updatedActivity: Activity) {
        val trip = _selectedTrip.value

        tripModel.editActivityInSelectedTrip(
            activityId,
            updatedActivity,
            trip
        ) { success, updatedTrip ->
            if (success && updatedTrip != null) {
                _selectedTrip.value = updatedTrip

                when (userAction) {
                    UserAction.CREATE_TRIP -> newTrip = updatedTrip
                    UserAction.EDIT_TRIP -> editTrip = updatedTrip
                    else -> {}
                }

            }
        }
    }


    private fun updateAllTripStatuses() {
        val trips = tripModel.allPublishedTrips.value

        for (trip in trips) {
            val oldStatus = trip.status
            val newStatus = trip.updateStatusBasedOnDate()

            if (newStatus.toString() != oldStatus) {
                tripModel.updateTripStatus(trip.id, newStatus.toString())
            }
        }
    }

    fun fetchTripById(tripId: String, onResult: (Trip?) -> Unit) {
        tripModel.getTripById(tripId) { trip ->
            if (trip != null) {
                _otherTrip.value = trip
            }
            onResult(trip)
        }
    }

    fun createTripWithImageUpload(
        imageUri: Uri?,
        title: String,
        destination: String,
        startDate: Calendar,
        endDate: Calendar,
        estimatedPrice: Double,
        groupSize: Int,
        typeTravel: List<String>,
        creatorId: Int,
        onResult: (Boolean, Trip?, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 首先创建一个临时Trip来获取ID
                val tempTrip = Trip(
                    title = title,
                    destination = destination,
                    startDate = Timestamp(startDate.time),
                    endDate = Timestamp(endDate.time),
                    estimatedPrice = estimatedPrice,
                    groupSize = groupSize,
                    activities = mutableMapOf(),
                    typeTravel = typeTravel,
                    creatorId = creatorId,
                    published = false,
                    id = -1,
                    participants = emptyMap(),
                    status = Trip.TripStatus.NOT_STARTED.toString(),
                    appliedUsers = emptyMap(),
                    rejectedUsers = emptyMap(),
                    photo = null // 暂时为null
                )

                // 创建Trip获取ID
                tripModel.createNewTrip(tempTrip) { success, createdTrip ->
                    if (success && createdTrip != null) {
                        // Trip创建成功，现在上传图片
                        if (imageUri != null) {
                            viewModelScope.launch {
                                val uploadSuccess = createdTrip.setPhoto(imageUri)
                                if (uploadSuccess) {
                                    // 图片上传成功，更新Trip
                                    tripModel.editTrip(createdTrip) { updateSuccess ->
                                        if (updateSuccess) {
                                            onResult(true, createdTrip, null)
                                        } else {
                                            onResult(false, null, "Failed to save image path")
                                        }
                                    }
                                } else {
                                    // 图片上传失败，但Trip已创建，使用默认图片
                                    Log.w(
                                        "TripViewModel",
                                        "Image upload failed, using default image"
                                    )
                                    onResult(true, createdTrip, null)
                                }
                            }
                        } else {
                            // 没有图片，直接返回成功
                            onResult(true, createdTrip, null)
                        }
                    } else {
                        onResult(false, null, "Failed to create trip")
                    }
                }
            } catch (e: Exception) {
                Log.e("TripViewModel", "Error creating trip with image", e)
                onResult(false, null, "Error: ${e.message}")
            }
        }
    }

    fun refreshSelectedTrip() {
        when (userAction) {
            UserAction.CREATE_TRIP -> {
                _selectedTrip.value = newTrip
                Log.d("TripViewModel", "Refreshed selected trip to newTrip: ${newTrip.id}")
            }

            UserAction.EDIT_TRIP -> {
                _selectedTrip.value = editTrip
                Log.d("TripViewModel", "Refreshed selected trip to editTrip: ${editTrip.id}")
            }

            else -> {
                Log.d("TripViewModel", "Keeping current selected trip: ${_selectedTrip.value.id}")
            }
        }
    }

    // 🔴 修复点18: 改进addActivityToTrip方法，确保状态同步
    fun addActivityToTrip(activity: Trip.Activity) {
        Log.d("TripViewModel", "Adding activity: $activity")
        Log.d("TripViewModel", "Current user action: $userAction")

        when (userAction) {
            UserAction.CREATE_TRIP -> {
                Log.d("TripViewModel", "Adding activity to new trip")
                val updatedTrip = tripModel.addActivityToTrip(activity, newTrip)
                newTrip = updatedTrip
                _selectedTrip.value = newTrip
                Log.d("TripViewModel", "Updated newTrip activities: ${newTrip.activities}")
            }

            UserAction.EDIT_TRIP -> {
                Log.d("TripViewModel", "Adding activity to edit trip")
                val updatedTrip = tripModel.addActivityToTrip(activity, editTrip)
                editTrip = updatedTrip
                _selectedTrip.value = editTrip
                Log.d("TripViewModel", "Updated editTrip activities: ${editTrip.activities}")
            }

            else -> {
                Log.d("TripViewModel", "Adding activity to selected trip")
                val currentTrip = _selectedTrip.value
                val updatedTrip = tripModel.addActivityToTrip(activity, currentTrip)
                _selectedTrip.value = updatedTrip
                Log.d("TripViewModel", "Updated selectedTrip activities: ${updatedTrip.activities}")
            }
        }
    }

    //INITIALIZE VIEWMODEL
    init {
        viewModelScope.launch {
            updateAllTripStatuses()

            tripModel.setMaxMinPrice()

            val minPrice = tripModel.minPrice.toFloat()
            val maxPrice = tripModel.maxPrice.toFloat()

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

object Factory : ViewModelProvider.Factory {
    private val tripModel: TripModel = TripModel()
    private val userModel: UserModel = UserModel()
    private val reviewModel: ReviewModel = ReviewModel()

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val savedStateHandle = extras.createSavedStateHandle()

        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(TripViewModel::class.java) ->
                TripViewModel(tripModel, userModel, reviewModel, savedStateHandle) as T

            modelClass.isAssignableFrom(UserViewModel::class.java) ->
                UserViewModel(userModel) as T

            modelClass.isAssignableFrom(ReviewViewModel::class.java) ->
                ReviewViewModel(reviewModel) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}. Please add it to the Factory.")
        }
    }
}

