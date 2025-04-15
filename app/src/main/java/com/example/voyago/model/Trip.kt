package com.example.voyago.model

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
    var participants: List<Int>,               //user id
    var activities: Map<Date,Int>,            //Map<Date,Activity> to filter by day
    var status: TripStatus,
    var typeTravel: List<TypeTravel>,
    var creatorId: Int,
    var appliedUsers: List<Int>,
    var published: Boolean
)
{
    data class Activity(
        val id: Int,
        var date: Date,           //yyyy-mm-gg
        var time: String,           //hh:mm
        var description: String
    )

    enum class TripStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }
}


enum class TypeTravel {
    CULTURE,
    PARTY,
    ADVENTURE,
    RELAX
}
