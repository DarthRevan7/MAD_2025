package com.example.voyago.model

import java.util.Calendar
/*
data class Trip(
    val id: Int,
    var photo: String,
    var title: String,
    var destination: String,
    var startDate: Calendar,
    var endDate: Calendar,
    var estimatedPrice: Double,
    var groupSize: Int,
    var participants: Map<String, JoinRequest>,                   // userId, id JoinedRequest
    var activities: Map<String, List<Activity>>,     // Map<Date, Activity>
    var status: TripStatus,
    var typeTravel: List<TypeTravel>,
    var creatorId: Int,
    var appliedUsers: Map<String, JoinRequest>,                   // userId, id JoinedRequest
    var rejectedUsers: Map<String, JoinRequest>,                  // userId, number of spots
    var published: Boolean
) {


    data class Activity(
        val id: Int,
        var date: Calendar,         // yyyy-mm-dd
        var time: String,           // hh:mm
        var isGroupActivity: Boolean,
        var description: String
    )

    data class JoinRequest(
        val userId: Int,
        val requestedSpots: Int,
        val unregisteredParticipants: List<Participant>, // excludes the requesting user
        val registeredParticipants: List<Int>            //users' Ids
    )

    data class Participant(
        val name: String,
        val surname: String,
        val email: String
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
        participants = emptyMap(),
        activities = emptyMap(),
        status = TripStatus.NOT_STARTED,
        typeTravel = emptyList(),
        creatorId = -1,
        appliedUsers = emptyMap(),
        rejectedUsers = emptyMap(),
        published = false
    ) {
        var yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -1)

        startDate = yesterday
        endDate = yesterday

        updateStatusBasedOnDate()

    }

    fun updateStatusBasedOnDate(): TripStatus {
        val today = Calendar.getInstance()
        return when {
            endDate.before(today) -> TripStatus.COMPLETED
            startDate.after(today) -> TripStatus.NOT_STARTED
            else -> TripStatus.IN_PROGRESS
        }
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

    fun loggedInUserCanJoin(id: Int): Boolean {
        return this.status == TripStatus.NOT_STARTED && hasAvailableSpots() && creatorId != id
                && !participants.containsKey(id) && !appliedUsers.containsKey(id)
    }

    fun hasAvailableSpots():Boolean {
        return availableSpots() > 0
    }

    fun availableSpots(): Int {
        return this.groupSize - this.participants.values.sumOf { it.requestedSpots }
    }

    fun tripDuration(): Int {
        val start = startDate.clone() as Calendar
        val end = endDate.clone() as Calendar
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

 */