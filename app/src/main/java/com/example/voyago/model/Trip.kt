package com.example.voyago.model

import android.net.*
import java.util.Calendar
import java.util.Date

data class Trip(
    val id: Int,
    var photo: Int,
    var title: String,
    var destination: String,
    var startDate: Calendar,
    var endDate: Calendar,
    var estimatedPrice: Double,
    var groupSize: Int,
    var participants: List<Int>,               //user id - is this useful?
    var activities: Map<Calendar, List<Activity>>,            //Map<Date,Activity> to filter by day
    var status: TripStatus,
    var typeTravel: List<TypeTravel>,
    var creatorId: Int,
    var appliedUsers: List<Int>,
    var published: Boolean,
    var isCompleted: Boolean
)
{
    data class Activity(
        val id: Int,
        var date: Calendar,           //yyyy-mm-gg
        var time: String,           //hh:mm
        var isGroupActivity:Boolean,
        var description: String
    )

    enum class TripStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }

    fun availableSpots(): Int {
        return this.groupSize - this.participants.size
    }

    fun tripDuration():Int
    {
        return endDate.get(Calendar.DAY_OF_YEAR) - startDate.get(Calendar.DAY_OF_YEAR)
    }

}

enum class TypeTravel {
    CULTURE,
    PARTY,
    ADVENTURE,
    RELAX
}
