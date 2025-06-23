package com.example.voyago.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.voyago.model.Trip
import com.example.voyago.model.User
import com.example.voyago.viewmodel.ChatViewModel
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel

@Composable
fun ChatDetails(navController: NavController,
                 tripId: String, 
                 tripViewModel: TripViewModel, 
                 uvm: UserViewModel,
                 chatViewModel: ChatViewModel,) {
    val tripState = produceState<Trip?>(initialValue = null, tripId) {
        tripViewModel.fetchTripById(tripId) { trip -> value = trip }
    }

    val trip = tripState.value
    val showDialog = remember { mutableStateOf(false) }
    val loggedUser by uvm.loggedUser.collectAsState()

    if (trip == null) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        return
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Trip: ${trip.title}", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // See Trip Details Button
        Button(onClick = {
            tripViewModel.setSelectedTrip(trip)
            navController.navigate("trip_details")
        }) {
            Text("See trip details")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Participants:", style = MaterialTheme.typography.titleMedium)

        val participantIds = trip.participants.keys
        participantIds.forEach { userIdString ->
            val userId = userIdString.toIntOrNull()
            if (userId != null) {
                val userState = produceState<User?>(initialValue = null, userId) {
                    uvm.getUserData(userId).collect { user -> value = user }
                }

                val user = userState.value
                if (user != null) {
                    MemberItem(
                        navController,
                        loggedUserId = uvm.loggedUser.value.id,
                        userId = user.id,
                        name = "${user.firstname} ${user.surname}",
                        rating = String.format("%.1f", user.rating),
                        isCreator = user.id == trip.creatorId,
                        isDarkBackground = false,
                        avatarUrl = user.profilePictureUrl
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Text("- Loading user $userId...")
                }
            } else {
                Text("- Invalid user ID: $userIdString")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (trip.status == Trip.TripStatus.COMPLETED.toString()) {
            Text(
                text = "Trip completed!",
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            Button(
                onClick = { showDialog.value = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Leave trip", color = Color.White)
            }
        }

    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Confirm Leave Trip") },
            text = {
                Text("Are you sure you want to leave this trip? This action will affect your reliability score.")
            },
            confirmButton = {
                Button(onClick = {
                    if (trip.status != Trip.TripStatus.COMPLETED.toString()) {
                        if (loggedUser.id == trip.creatorId) {
                            if (!trip.published || trip.participants.size == 1) {
                                // Reject all pending applications
                                trip.appliedUsers.forEach { userId, _ ->
                                    tripViewModel.rejectApplication(trip, userId.toInt())
                                }

                                tripViewModel.deleteTrip(trip.id)
                                tripViewModel.updatePublishedTrip(loggedUser.id)
                                chatViewModel.removeParticipantFromRoom(trip.title, loggedUser.id)
                            } else if (trip.participants.size > 1) {
                                // Transfer ownership to another participant
                                val newOwner = trip.participants.entries.firstOrNull { it.key.toIntOrNull() != trip.creatorId }
                                newOwner?.let {
                                    tripViewModel.updateTripCreator(
                                        trip.id,
                                        it.key.toInt(),
                                        trip.creatorId
                                    )
                                }
                                chatViewModel.removeParticipantFromRoom(trip.title, loggedUser.id)
                                uvm.updateUserReliability(loggedUser.id, -10) { success ->
                                    if (success) {
                                        Log.d("TripDetails", "Reliability updated")
                                    } else {
                                        Log.e("TripDetails", "Reliability update failed")
                                    }
                                }
                            }
                        } else {
                            // Regular user leaves the trip
                            tripViewModel.updateTripParticipants(trip.id, loggedUser.id)
                            chatViewModel.removeParticipantFromRoom(trip.title, loggedUser.id)
                            uvm.updateUserReliability(loggedUser.id, -5) { success ->
                                if (success) {
                                    Log.d("TripDetails", "Reliability updated")
                                } else {
                                    Log.e("TripDetails", "Reliability update failed")
                                }
                            }
                        }
                    } else {
                        // Trip completed — cancel only
                        tripViewModel.cancelTrip(trip.creatorId.toString(), trip.id.toString())
                    }


                    showDialog.value = false
                    navController.navigate("chats_list")
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}



@Composable
fun MemberItem(
    navController: NavController,
    userId: Int,
    loggedUserId: Int,
    name: String,
    rating: String,
    isCreator: Boolean,
    isDarkBackground: Boolean,
    avatarUrl: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clickable {
                if(userId != loggedUserId) {
                    navController.navigate("user_profile/${userId}")
                }
                else {
                    navController.navigate("profile_overview?tabIndex={tabIndex}")
                }
            },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "$name's Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkBackground) Color.White else Color.Black
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Star Rating",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = rating,
                    fontSize = 16.sp,
                    color = if (isDarkBackground) Color.White else Color.Black
                )
                if (isCreator) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Creator",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}



