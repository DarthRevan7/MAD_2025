package com.example.voyago.model

import java.util.Calendar

data class Trip(
    val id: Int,
    var photo: String,
    var title: String,
    var destination: String,
    var startDate: Calendar,
    var endDate: Calendar,
    var estimatedPrice: Double,
    var groupSize: Int,
    var participants: List<Int>,                            //user id
    var activities: Map<Calendar, List<Activity>>,          //Map<Date,Activity> to filter by day
    var status: TripStatus,
    var typeTravel: List<TypeTravel>,
    var creatorId: Int,
    var appliedUsers: List<Int>,
    var rejectedUsers: List<Int>,
    var published: Boolean
) {
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

    constructor() : this (
        id = -1,
        photo = "",
        title = "",
        destination = "",
        startDate = Calendar.getInstance(),
        endDate = Calendar.getInstance(),
        estimatedPrice = -1.0,
        groupSize = -1,
        participants = emptyList(),
        activities = emptyMap(),
        status = TripStatus.NOT_STARTED,
        typeTravel = emptyList(),
        creatorId = -1,
        appliedUsers = emptyList(),
        rejectedUsers = emptyList(),
        published = false
    ) {
        var yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -1)

        startDate = yesterday
        endDate = yesterday

    }

    fun isValid():Boolean {
        var condition = true
        var yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -1)

        condition = photo != "" && title != "" && destination != ""

        condition = condition && startDate != yesterday && endDate != yesterday

        condition = condition && estimatedPrice > 0.0 && groupSize > 0

        condition = condition && activities.isNotEmpty() && typeTravel.isNotEmpty()

        return condition
    }

    fun canJoin():Boolean {
        return this.status == TripStatus.NOT_STARTED && hasAvailableSpots()
    }

    fun hasAvailableSpots():Boolean {
        return availableSpots() > 0
    }

    fun availableSpots(): Int {
        return this.groupSize - this.participants.size
    }

    fun tripDuration():Int {
        return endDate.get(Calendar.DAY_OF_YEAR) - startDate.get(Calendar.DAY_OF_YEAR)
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
        val current = startDate.clone() as Calendar
        val end = endDate.clone() as Calendar

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
