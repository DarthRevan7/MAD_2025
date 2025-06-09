package com.example.voyago.view

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.voyago.viewmodel.NotificationViewModel
import com.example.voyago.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.example.voyago.viewmodel.TripViewModel

@Composable
fun NotificationView(navController: NavController, nvm: NotificationViewModel, uvm: UserViewModel, vm: TripViewModel) {

    val user by uvm.loggedUser.collectAsState()
    val userId = user.id.toString()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        nvm.loadNotificationsForUser(context, userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (nvm.notifications.isEmpty()) {
            Text("No notifications yet.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(nvm.notifications) { notification ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (notification.type == "TRIP") {
                                    val tripId = notification.idLink.toString()
                                    vm.fetchTripById(tripId) { trip ->
                                        if (trip != null) {
                                            vm.setOtherTrip(trip)
                                            vm.userAction = TripViewModel.UserAction.VIEW_OTHER_TRIP

                                            val notificationTrip = TripNotification(
                                                trip.id,
                                                trip.photo,
                                                trip.title,
                                                trip.destination,
                                                trip.startDate,
                                                trip.endDate,
                                                trip.estimatedPrice,
                                                trip.groupSize,
                                                trip.participants,
                                                trip.activities,
                                                trip.status,
                                                trip.typeTravel,
                                                trip.creatorId,
                                                trip.appliedUsers,
                                                trip.rejectedUsers,
                                                trip.published,
                                                trip.isDraft
                                            )
                                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                                "notificationValues",
                                                notificationTrip
                                            )


                                            navController.navigate("trip_details")
                                            Log.e("Notification", "Found: ${vm.otherTrip.value.id}")
                                        } else {
                                            Log.e("Notification", "Trip not found for ID: $tripId")
                                        }
                                    }
                                } else if (notification.type == "REVIEW") {
                                    navController.navigate("profile_overview")
                                } else if (notification.type == "APPROVED") {

                                    val tripId = notification.idLink.toString()
                                    vm.fetchTripById(tripId) { trip ->
                                        if (trip != null) {
                                            vm.setOtherTrip(trip)
                                            vm.userAction = TripViewModel.UserAction.VIEW_OTHER_TRIP

                                            val notificationTrip = TripNotification(
                                                trip.id,
                                                trip.photo,
                                                trip.title,
                                                trip.destination,
                                                trip.startDate,
                                                trip.endDate,
                                                trip.estimatedPrice,
                                                trip.groupSize,
                                                trip.participants,
                                                trip.activities,
                                                trip.status,
                                                trip.typeTravel,
                                                trip.creatorId,
                                                trip.appliedUsers,
                                                trip.rejectedUsers,
                                                trip.published,
                                                trip.isDraft
                                            )
                                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                                "notificationValues",
                                                notificationTrip
                                            )


                                            navController.navigate("trip_details")
                                            Log.e("Notification", "Found: ${vm.otherTrip.value.id}")
                                        } else {
                                            Log.e("Notification", "Trip not found for ID: $tripId")
                                        }
                                    }

                                } else if (notification.type == "REJECTED") {
                                    navController.navigate("explore_main")
                                } else if (notification.type == "NEW_APPLICATION") {
                                    val tripId = notification.idLink.toString()
                                    vm.fetchTripById(tripId) { trip ->
                                        if (trip != null) {
                                            vm.setOtherTrip(trip)
                                            vm.userAction = TripViewModel.UserAction.VIEW_OTHER_TRIP

                                            val notificationTrip = TripNotification(
                                                trip.id,
                                                trip.photo,
                                                trip.title,
                                                trip.destination,
                                                trip.startDate,
                                                trip.endDate,
                                                trip.estimatedPrice,
                                                trip.groupSize,
                                                trip.participants,
                                                trip.activities,
                                                trip.status,
                                                trip.typeTravel,
                                                trip.creatorId,
                                                trip.appliedUsers,
                                                trip.rejectedUsers,
                                                trip.published,
                                                trip.isDraft
                                            )
                                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                                "notificationValues",
                                                notificationTrip
                                            )


                                            navController.navigate("trip_details")
                                            Log.e("Notification", "Found: ${vm.otherTrip.value.id}")
                                        } else {
                                            Log.e("Notification", "Trip not found for ID: $tripId")
                                        }
                                    }
                                }
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notification Icon",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(notification.title, style = MaterialTheme.typography.bodyLarge)
                            Text(notification.body, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}


