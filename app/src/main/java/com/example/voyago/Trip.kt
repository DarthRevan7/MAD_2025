package com.example.voyago

import android.media.Image
import java.util.Date

data class Trip(
    var photo: Image,
    var title: String,
    var destination: String,
    var startDate: Date,
    var endDate: Date,
    var estimatedPrice: Double,
    var groupSize: Int,
    var participants: List<UserProfileInfo>,
    var activities: Map<String,Activity>,       //Map<Date,Activity> to filter by day
    var status: TripStatus,
    var availableSpots: Int = groupSize - participants.size
)

data class Activity(
    var date: String,           //yyyy-mm-gg
    var time: String,           //hh:mm
    var description: String
)

enum class TripStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}