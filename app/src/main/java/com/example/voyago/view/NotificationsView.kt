package com.example.voyago.view

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voyago.viewmodel.ArticleViewModel
import com.example.voyago.viewmodel.NotificationViewModel
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel

@Composable
fun NotificationView(
    navController: NavController,
    nvm: NotificationViewModel,
    uvm: UserViewModel,
    vm: TripViewModel,
    avm: ArticleViewModel
) {

    // Get the currently logged-in user from the user view model
    val user by uvm.loggedUser.collectAsState()

    // Extract the user's ID for fetching notifications
    val userId = user.id.toString()

    // Android context
    val context = LocalContext.current

    // Load notifications once when the userId becomes available or changes
    LaunchedEffect(userId) {
        nvm.loadNotificationsForUser(context, userId)
    }

    // Main column layout to structure the screen content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Screen title
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.titleLarge
        )

        // Space below the title
        Spacer(modifier = Modifier.height(16.dp))

        // Show message if there are no notifications
        if (nvm.notifications.isEmpty()) {
            Text("No notifications yet.")
        } else {
            // Scrollable list of notifications
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Iterate through each notification
                items(nvm.notifications) { notification ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Handle navigation logic based on notification type
                                // 1. Trip Notification
                                if (notification.type == "TRIP") {
                                    val tripId = notification.idLink.toString()
                                    vm.fetchTripById(tripId) { trip ->
                                        if (trip != null) {
                                            // Save trip in viewmodel and set context for viewing another user's trip
                                            vm.setOtherTrip(trip)
                                            vm.userAction = TripViewModel.UserAction.VIEW_OTHER_TRIP

                                            // Create a TripNotification object to pass as arguments
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

                                            // Pass the data using navController's SavedStateHandle
                                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                                "notificationValues",
                                                notificationTrip
                                            )

                                            // Navigate to the trip details screen
                                            navController.navigate("trip_details")
                                        } else {
                                            Log.e("Notification", "Trip not found for ID: $tripId")
                                        }
                                    }

                                    // 2. Review notification
                                } else if (notification.type == "REVIEW") {
                                    // Leads to the profile screen with "Reviews" tab selected
                                    navController.navigate("profile_overview?tabIndex=2")
                                    // 3. Trip application approved
                                } else if (notification.type == "APPROVED") {

                                    val tripId = notification.idLink.toString()
                                    vm.fetchTripById(tripId) { trip ->
                                        if (trip != null) {
                                            // Save trip in viewmodel and set context for viewing another user's trip
                                            vm.setOtherTrip(trip)
                                            vm.userAction = TripViewModel.UserAction.VIEW_OTHER_TRIP

                                            // Create a TripNotification object to pass as arguments
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

                                            // Pass the data using navController's SavedStateHandle
                                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                                "notificationValues",
                                                notificationTrip
                                            )

                                            // Navigate to the trip details screen in the "My Trips" section
                                            navController.navigate("trip_details?owner=true")
                                        } else {
                                            Log.e("Notification", "Trip not found for ID: $tripId")
                                        }
                                    }
                                    // 4. Trip application rejected
                                } else if (notification.type == "REJECTED") {
                                    val tripId = notification.idLink.toString()
                                    vm.fetchTripById(tripId) { trip ->
                                        if (trip != null) {
                                            // Save trip in viewmodel and set context for viewing another user's trip
                                            vm.setOtherTrip(trip)
                                            vm.userAction = TripViewModel.UserAction.VIEW_OTHER_TRIP

                                            // Create a TripNotification object to pass as arguments
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

                                            // Pass the data using navController's SavedStateHandle
                                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                                "notificationValues",
                                                notificationTrip
                                            )

                                            // Navigate to the trip details screen
                                            navController.navigate("trip_details")
                                        } else {
                                            Log.e("Notification", "Trip not found for ID: $tripId")
                                        }
                                    }
                                    // 5. Someone applied to your trip
                                } else if (notification.type == "NEW_APPLICATION") {
                                    val tripId = notification.idLink.toString()
                                    vm.fetchTripById(tripId) { trip ->
                                        if (trip != null) {
                                            // Save trip in viewmodel and set context for viewing another user's trip
                                            vm.setOtherTrip(trip)
                                            vm.userAction = TripViewModel.UserAction.VIEW_OTHER_TRIP

                                            // Create a TripNotification object to pass as arguments
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

                                            // Pass the data using navController's SavedStateHandle
                                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                                "notificationValues",
                                                notificationTrip
                                            )

                                            // Navigate to the trip details screen in the "My Trips" section
                                            navController.navigate("trip_details?owner=true")
                                        } else {
                                            Log.e("Notification", "Trip not found for ID: $tripId")
                                        }
                                    }
                                    // 6. New article was published
                                } else if (notification.type == "ARTICLE") {
                                    val articleId = notification.idLink
                                    // Get article from the articleViewModel
                                    val article = avm.getArticleById(articleId)
                                    if (article != null) {
                                        // Navigate to article details
                                        navController.navigate("article_detail/${article.id}")
                                    } else {
                                        Log.e("Notification", "Article not found")
                                    }
                                }
                            }
                            // Padding inside each notification row
                            .padding(8.dp)
                    ) {
                        // Icon indicating it's a notification
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notification Icon",
                            tint = MaterialTheme.colorScheme.primary
                        )

                        // Space between icon and text
                        Spacer(modifier = Modifier.width(8.dp))

                        // Column with notification title and body
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


