package com.example.voyago.model

data class JoinRequest (
    val id: Long,                           //Join Request ID
    val userId: Int,                        //Requesting User ID
    val tripId: Int,                        //Join Request's Trip ID
    val requestedSpots: Int,                //Number of reserved Spots
    val participants: List<Participant>,    //List of Unregistered Participants
    val registeredParticipants: List<Int>   //List of Registered Users' IDs
) {
    data class Participant (
        val id: Long,                       //Participant ID
        val name: String,
        val surname: String,
        val email:String
    ) {

        override fun hashCode(): Int {
            var tempHash:Int = super.hashCode()
            tempHash += name.hashCode() + surname.hashCode() + email.hashCode()
            return tempHash
        }

        override fun equals(other: Any?): Boolean {
            if(other == null) return false
            if (this === other) return true
            if (javaClass != other.javaClass) return false

            other as Participant

            if (id != other.id) return false
            if (name != other.name) return false
            if (surname != other.surname) return false
            if (email != other.email) return false

            return true
        }
    }
}
