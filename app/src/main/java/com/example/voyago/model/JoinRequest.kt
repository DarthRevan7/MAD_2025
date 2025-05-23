package com.example.voyago.model

data class JoinRequest (
    val id: Long,
    val userId: Int,
    val tripId: Int,
    val requestedSpots: Int,
    val participants: List<Participant>
) {
    data class Participant (
        val id: Long,
        val name: String,
        val surname: String
    )
}
