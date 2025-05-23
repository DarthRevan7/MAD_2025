package com.example.voyago.model

data class JoinRequest (
    val id: Int,                           //Join Request ID
    val userId: Int,                        //Requesting User ID
    val tripId: Int,                        //Join Request's Trip ID
    val requestedSpots: Int,                //Number of reserved Spots
    val participants: List<Participant>,    //List of Unregistered Participants
    val registeredParticipants: List<Int>   //List of Registered Users' IDs
) {
    data class Participant (
        val name: String,
        val surname: String,
        val email:String
    )
}
