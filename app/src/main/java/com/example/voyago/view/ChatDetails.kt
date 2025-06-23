package com.example.voyago.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.voyago.model.Trip
import com.example.voyago.model.User
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel

@Composable
fun ChatDetails(tripId: String, tripViewModel: TripViewModel, uvm: UserViewModel) {
    val tripState = produceState<Trip?>(initialValue = null, tripId) {
        tripViewModel.fetchTripById(tripId) { trip ->
            value = trip
        }
    }

    val trip = tripState.value


    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Trip: ${trip?.title}", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Participants:")
        val participantIds = trip?.participants?.keys

        participantIds?.forEach { userIdString ->
            val userId = userIdString.toIntOrNull()
            if (userId != null) {
                val userState = produceState<User?>(initialValue = null, userId) {
                    uvm.getUserData(userId).collect { user ->
                        value = user
                    }
                }

                val user = userState.value

                if (user != null) {
                    Text("- ${user.firstname} ${user.surname}")
                } else {
                    Text("- Loading user $userId...")
                }
            } else {
                Text("- Invalid user ID: $userIdString")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { /* TODO */ }) {
            Text("See trip details")
        }

        Button(
            onClick = { /* TODO */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Leave trip", color = Color.White)
        }
    }
}

