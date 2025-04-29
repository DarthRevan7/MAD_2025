package com.example.voyago.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.voyago.model.LazyUser
import com.example.voyago.model.Model
import com.example.voyago.model.Trip
import com.example.voyago.model.Trip.Activity
import com.example.voyago.model.TypeTravel
import com.example.voyago.view.SelectableItem
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Calendar
import kotlin.math.max


class TripListViewModel(val model: Model) : ViewModel() {
    val publishedTrips = model.publishedTrips
    val privateTrips = model.privateTrips
    val allPublishedTrips = model.allPublishedTrips
    val askedTrips = model.askedTrips

    val tripList = model.tripList

    val filteredTrips = MutableStateFlow<List<Trip>>(emptyList())

    fun filterByCompletionVM() {
        filteredTrips.value = model.filterByCompletion(updatePublishedTrip())
        filteredTrips.value.forEach { it.printTrip() }
        println("Size of filtered trips: " + filteredTrips.value.size.toString())
    }

    var selectedTrip: Trip? by mutableStateOf(null)
        private set

    var currentTrip: Trip? by mutableStateOf(null)
        private set

    fun selectTrip(trip: Trip) {
        currentTrip = trip
        selectedTrip = trip
    }

    var filterDestination: String by mutableStateOf("")
        private set

    fun updateFilterDestination(str:String) {
        filterDestination = str
    }

    var filterMinPrice:Double by mutableDoubleStateOf(getMinPrice())
        private set
    var filterMaxPrice:Double by mutableDoubleStateOf(getMaxPrice())
        private set

    fun updateFilterPriceRange(minPrice:Double, maxPrice:Double) {
        filterMaxPrice = maxPrice
        filterMinPrice = minPrice
    }

    var filterDuration: Pair<Int,Int> by mutableStateOf(Pair(-1,-1))
        private set

    var filterGroupSize: Pair<Int,Int> by mutableStateOf(Pair(-1,-1))
        private set

    fun updateFilterDuration(list: List<SelectableItem>) {
        filterDuration = model.setRange(list)
    }

    fun updateFilterGroupSize(list: List<SelectableItem>) {
        filterGroupSize = model.setRange(list)
    }

    var filtersTripType: List<SelectableItem> by mutableStateOf(emptyList())
        private set

    fun updateFiltersTripType(list: List<SelectableItem>) {
        filtersTripType = list
    }

    var filterCompletedTrips: Boolean by mutableStateOf(false)
        private set

    fun updateCompletedTripsFilter(isSelected: Boolean) {
        filterCompletedTrips = isSelected
    }

    var filterBySeats: Int by mutableIntStateOf(1)
        private set

    fun updateFilterBySeats(seats: Int) {
        filterBySeats = seats
    }

    fun getAllTrips():List<Trip> {
        model.tripList.value.forEach { it.printTrip() }
        println("Size of all trips: ${model.tripList.value.size} ")
        return model.tripList.value
    }

    fun creatorPublicFilter() = model.filterPublishedByCreator(1)
    fun creatorPrivateFilter() = model.filterPrivateByCreator(1)
    fun updatePublishedTrip() = model.getAllPublishedTrips()

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

    fun addNewTrip(newTrip: Trip): List<Trip> = model.createNewTrip(newTrip)

    fun toggleAskToJoin(tripId: Int) = model.toggleAskToJoin(tripId)

    fun allDestinations() = model.getDestinations()

    fun getActivities(trip: Trip): List<Activity> {
        return trip.activities.values.flatten()
    }

    fun getMinPrice() = model.minPrice
    fun getMaxPrice() = model.maxPrice
    fun setMaxMinPrice() = model.setMaxMinPrice()

    fun addActivityToSelectedTrip(activity: Trip.Activity) {
        currentTrip = currentTrip?.copy(
            activities = currentTrip?.activities.orEmpty()
                .toMutableMap()
                .apply {
                    val dateKey = activity.date
                    val updatedList = getOrDefault(dateKey, emptyList()) + activity
                    put(dateKey, updatedList)
                }
        )
    }

    fun searchWithFilter(dbList: List<Trip>, destination: String, minPrice:Float, maxPrice:Float,
                         minDays:Int, maxDays:Int, minSize:Int, maxSize:Int,
                         searchForCompleted:Boolean, minAvailableSeats:Int,
                         vararg typesTravel:TypeTravel):List<Trip> =
        model.tripFilter(dbList, destination, minPrice, maxPrice, minDays, maxDays, minSize,
            maxSize, searchForCompleted, minAvailableSeats, *typesTravel)
}

object Factory : ViewModelProvider.Factory{
    private val model:Model = Model()

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return when{
            modelClass.isAssignableFrom(TripListViewModel::class.java)->
                TripListViewModel(model) as T
            else -> throw IllegalArgumentException("Unknown ViewModel")
        }
    }
}