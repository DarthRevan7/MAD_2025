package com.example.voyago.viewmodel

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.voyago.model.domain.Trip
import com.example.voyago.model.domain.Trip.TripStatus
import com.example.voyago.model.domain.TypeTravel
import com.example.voyago.model.domain.UserData
import com.example.voyago.model.data.TripRepository
import com.example.voyago.model.data.UserRepository
import com.example.voyago.model.domain.Review
import com.example.voyago.model.domain.Trip.Activity
import com.example.voyago.view.SelectableItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import java.util.Calendar


class TripViewModel(
    private val tripRepo: TripRepository,
    private val userRepo: UserRepository
) : ViewModel() {
    private var _users = MutableStateFlow<List<UserData>>(emptyList())
    var users: StateFlow<List<UserData>> = _users

    private var _tripList = MutableStateFlow<List<Trip>>(emptyList())
    var tripList: StateFlow<List<Trip>> = _tripList


    private var _reviews = MutableStateFlow<List<Review>>(emptyList())
    var reviews: StateFlow<List<Review>> = _reviews

    private var nextId = 9

    var newTrip:Trip = Trip()

    //Use in the edit trip interface
    var editTrip:Trip = Trip()

    private val _publishedTrips = MutableStateFlow<List<Trip>>(emptyList())
    val publishedTrips: StateFlow<List<Trip>> = _publishedTrips

    private val _privateTrips = MutableStateFlow<List<Trip>>(emptyList())
    val privateTrips: StateFlow<List<Trip>> = _privateTrips

    private val _allPublishedTrips = MutableStateFlow<List<Trip>>(emptyList())
    val allPublishedTrips: StateFlow<List<Trip>> = _allPublishedTrips

    private val _askedTrips = MutableStateFlow<Set<Int>>(emptySet())
    val askedTrips: StateFlow<Set<Int>> = _askedTrips

    private val _minPrice = Double.MAX_VALUE
    var minPrice: Double = _minPrice

    private val _maxPrice = Double.MIN_VALUE
    var maxPrice: Double = _maxPrice

    private val _filteredList = MutableStateFlow<List<Trip>>(emptyList())
    val filteredList: StateFlow<List<Trip>>  = _filteredList


    //User Business Logic
    fun getUsers(ids: List<Int>): List<UserData> {
        return _users.value.filter { it.id in ids }
    }



    fun filterPublishedByCreator(id: Int): List<Trip> {
        _publishedTrips.value = _tripList.value.filter { it.creatorId == id && it.published }
        return _publishedTrips.value
    }

    fun filterPrivateByCreator(id: Int): List<Trip> {
        _privateTrips.value = _tripList.value.filter { it.creatorId == id && !it.published }
        return _privateTrips.value
    }

    fun changePublishedStatus(id: Int) {
        _tripList.value = _tripList.value.map {
            if (it.id == id) {
                it.copy(published = !it.published)  // Toggle the published status
            } else {
                it
            }
        }
    }

    //Edit a specific activity from a specific trip
    fun editActivity(activityId: Int, updatedActivity: Trip.Activity) {
        val trip = editActivityInSelectedTrip(activityId, updatedActivity, _selectedTrip.value)!!

        _selectedTrip.value = trip

        if (userAction == UserAction.CREATE_TRIP) {
            newTrip = trip
        } else if (userAction == UserAction.EDIT_TRIP) {
            editTrip = trip
        }

    }

    fun deleteActivity(activity: Trip.Activity) {
        val trip =  removeActivityFromTrip(activity, _selectedTrip.value)!!

        _selectedTrip.value = trip

        if (userAction == UserAction.CREATE_TRIP) {
            newTrip = trip
        } else if (userAction == UserAction.EDIT_TRIP) {
            editTrip = trip
        }

    }

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
            participants = listOf(creatorId),
            activities = activities,
            status = TripStatus.NOT_STARTED,
            typeTravel = typeTravel,
            creatorId = creatorId,
            appliedUsers = emptyList(),
            rejectedUsers = emptyList(),
            reviews = emptyList(),
            published = published
        )
        _tripList.value = _tripList.value + newTrip

        return _tripList.value
    }

    fun createNewTrip(newTrip: Trip): Trip {
        val tripWithId = newTrip.copy(
            id = nextId++,
        )
        _tripList.value = _tripList.value + tripWithId
        return tripWithId
    }

    fun editTrip(updatedTrip: Trip): List<Trip> {
        _tripList.value = _tripList.value.map {
            if (it.id == updatedTrip.id) updatedTrip else it
        }
        return _tripList.value
    }

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

    fun toggleAskToJoin(tripId: Int) {
        _askedTrips.value = if (_askedTrips.value.contains(tripId)) {
            _askedTrips.value - tripId
        } else {
            _askedTrips.value + tripId
        }
    }

    fun getDestinations(): List<String> {
        return _tripList.value.map { it.destination }.distinct()
    }

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

    fun filterFunction(list: List<Trip>, filterDestination: String, filterMinPrice: Double,
                       filterMaxPrice: Double, filterDuration: Pair<Int,Int>,
                       filterGroupSize: Pair<Int,Int>, filtersTripType: List<SelectableItem>,
                       filterCompletedTrips: Boolean, filterBySeats: Int) {
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


            val completed = !filterCompletedTrips || !trip.canJoin()
            val spots = trip.availableSpots() >= filterBySeats

            Log.d("FilterFunction", "Conditions: destination=$destination, price=$price, duration=$duration, groupSize=$groupSize, completed=$completed, spots=$spots")

            trip.published && destination && price && duration && groupSize && completed
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




    //Get list of completed trips
    fun getCompletedTrips(): List<Trip> {
        return _tripList.value.filter { it.status == TripStatus.COMPLETED }
    }

    //Get list of upcoming trips
    fun getUpcomingTrips(): List<Trip> {
        return _tripList.value.filter { it.status == TripStatus.NOT_STARTED }
    }

    var filterCompletedTrips: Boolean by mutableStateOf(false)
        private set

    fun applyFilters() = filterFunction(tripList.value, filterDestination, filterMinPrice, filterMaxPrice,
        filterDuration, filterGroupSize, filtersTripType, filterCompletedTrips, filterBySeats)

    fun updatePublishedTrip() {
        // 直接 new 一个 MutableStateFlow
        tripList = MutableStateFlow(getAllPublishedTrips())
        applyFilters()
    }

    fun getAllPublishedTrips(): List<Trip> {
        val published = _tripList.value.filter { it.published }
        _allPublishedTrips.value = published
        return published
    }

    var filterBySeats: Int by mutableIntStateOf(0)
        private set

    //Reset filters
    fun resetFilters() {
        filterDestination = ""

        updateFilterPriceRange(0.0,0.0)
        setMaxMinPrice()
        val minPrice = minPrice.toFloat()
        val maxPrice = maxPrice.toFloat()
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


    // ———————— 公共 State ————————

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips

    private val _selectedTrip = mutableStateOf<Trip?>(null)
    val selectedTrip: State<Trip?> = _selectedTrip

    private val _participants = MutableStateFlow<List<Int>>(emptyList())
    val participants: StateFlow<List<Int>> = _participants

    private val _applicants = MutableStateFlow<List<Int>>(emptyList())
    val applicants: StateFlow<List<Int>> = _applicants

    private val _rejected = MutableStateFlow<List<Int>>(emptyList())
    val rejected: StateFlow<List<Int>> = _rejected


    // now map/filter works:
    val completedTrips: StateFlow<List<Trip>> = _trips
        .map { list -> list.filter { it.status == TripStatus.COMPLETED } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val upcomingTrips: StateFlow<List<Trip>> = _trips
        .map { list -> list.filter { it.status == TripStatus.NOT_STARTED } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            _trips.value = tripRepo.fetchAll()
        }
    }





    /** 手动刷新一次所有 Trip */
    fun refreshAllTrips() {
        viewModelScope.launch {
            _loading.value = true
            _trips.value = tripRepo.fetchAll()
            _loading.value = false
        }
    }

    /** 新建 Trip 并选中它 */
    fun addNewTrip(trip: Trip) = viewModelScope.launch {
        // 1) 调用 save，注意它返回 Unit
        tripRepo.save(trip)

        // 2) 直接把原始 trip 赋给 selected
        _selectedTrip.value = trip

        // 3) 刷新列表
        refreshAllTrips()
    }


    /** 编辑已存在的 Trip 并选中它 */
    fun editExistingTrip(trip: Trip) = viewModelScope.launch {
        tripRepo.save(trip)
        _selectedTrip.value = trip
        refreshAllTrips()
    }

    /** 删除一个 Trip */
    fun deleteTrip(id: Int) = viewModelScope.launch {
        tripRepo.delete(id)
        refreshAllTrips()
    }

    /** 切换 published 状态 */
    fun togglePublished(id: Int) = viewModelScope.launch {
        val current = _trips.value.find { it.id == id } ?: return@launch
        val updated = current.copy(published = !current.published)
        tripRepo.save(updated)
        refreshAllTrips()
    }

    // ———————— 选中 Trip & 用户列表 ————————

    /** 选中一个 Trip 并同时刷新它的参与者/申请者/被拒列表 */
    fun setSelectedTrip(trip: Trip) {
        _selectedTrip.value = trip
        loadParticipants(trip)
        loadApplicants(trip)
        loadRejected(trip)
    }


    private fun loadParticipants(trip: Trip) {
        // 直接把 Trip 中的 participants ID 列表给它
        _participants.value = trip.participants
    }

    private fun loadApplicants(trip: Trip) {
        _applicants.value = trip.appliedUsers
    }

    private fun loadRejected(trip: Trip) {
        _rejected.value = trip.rejectedUsers
    }

    var userAction:UserAction = UserAction.NOTHING

    public enum class UserAction {
        EDIT_TRIP, CREATE_TRIP, VIEW_TRIP, NOTHING, SEARCHING, FILTER_SELECTION
    }





    // ———————— 处理报名申请 ————————

    fun acceptApplication(trip: Trip, userId: Int) = viewModelScope.launch {
        // 1) 检查合法性
        if (userId !in trip.appliedUsers) return@launch
        val applicant = userRepo.fetchUserFull(userId) ?: return@launch
        val spots = applicant.requestedSpots
        val used = trip.participants.size
        if (used + spots > trip.groupSize) return@launch

        // 2) 更新内存模型
        val newApplied = trip.appliedUsers - userId
        val newParticipants = trip.participants + List(spots) { userId }
        val remainingSpots = trip.groupSize - (used + spots)

        // 3) 超额申请者拒绝
        val toReject = userRepo
            .fetchByIds(newApplied)
            .filter { it.requestedSpots > remainingSpots }
            .map { it.id }

        val updated = trip.copy(
            appliedUsers    = newApplied - toReject.toSet(),
            participants    = newParticipants,
            rejectedUsers   = trip.rejectedUsers + toReject
        )

        // 4) 持久化 & 刷新 UI
        tripRepo.save(updated)
        setSelectedTrip(updated)
        refreshAllTrips()
    }

    fun rejectApplication(trip: Trip, userId: Int) = viewModelScope.launch {
        if (userId !in trip.appliedUsers) return@launch
        val updated = trip.copy(
            appliedUsers  = trip.appliedUsers - userId,
            rejectedUsers = trip.rejectedUsers + userId
        )
        tripRepo.save(updated)
        setSelectedTrip(updated)
        refreshAllTrips()
    }

    //filter
    private data class BundleA(
        val trips: List<Trip> = emptyList(),
        val dest: String = "",
        val price: ClosedFloatingPointRange<Float> = 0f..Float.MAX_VALUE,
        val dur: Pair<Int,Int> = -1 to -1,
        val group: Pair<Int,Int> = -1 to -1
    )



    //Destination filter
    var filterDestination: String by mutableStateOf("")
        private set

    fun updateFilterDestination(str: String) {
        filterDestination = str
    }

    /** List all distinct destinations from our in-memory trips */
    fun allDestinations(): List<String> =
        _trips.value.map { it.destination }.distinct()

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

    /** Compute the current min price across all trips */
    fun getMinPrice(): Double =
        _trips.value.minOfOrNull { it.estimatedPrice } ?: 0.0

    /** Compute the current max price across all trips */
    fun getMaxPrice(): Double =
        _trips.value.maxOfOrNull { it.estimatedPrice } ?: 0.0



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
        filterDuration = setRange(list)
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
        filterGroupSize = setRange(list)
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

}

/** 为 Compose navGraph 提供同一个 factory 实例 */
class TripViewModelFactory(
    private val tripRepo: TripRepository,
    private val userRepo: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripViewModel(tripRepo, userRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }

    }

