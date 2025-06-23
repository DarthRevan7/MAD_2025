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
import com.example.voyago.viewmodel.TripViewModel

@Composable
fun ChatDetails(tripId: String, tripViewModel: TripViewModel) {
    val tripState = produceState<Trip?>(initialValue = null, tripId) {
        tripViewModel.fetchTripById(tripId) { trip ->
            value = trip
        }
    }

    val trip = tripState.value


    // Actual UI when trip is loaded
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Trip: ${trip?.title}", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        /* TODO */
        Text("Participants:")
//        trip.participants.forEach {
//            Text("- ${it.name}")
//        }

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

