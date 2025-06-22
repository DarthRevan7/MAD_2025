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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voyago.model.Trip
import com.example.voyago.model.deepCopy
import com.example.voyago.toCalendar
import com.example.voyago.viewmodel.TripViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesList(navController: NavController, vm: TripViewModel) {

    // Observe the currently selected trip from the ViewModel
    val selectedTrip by vm.selectedTrip

    // State to control whether to show the incomplete activities warning dialog
    var showIncompleteDialog by rememberSaveable { mutableStateOf(false) }

    // Create a deep copy snapshot of the trip when entering this screen based on the user action
    // This snapshot will be used to restore state if user cancels/back navigates
    val entryTripState = remember {
        when (vm.userAction) {
            // If user is editing a trip, copy the current editTrip data
            TripViewModel.UserAction.EDIT_TRIP -> vm.editTrip.deepCopy()
            // If user is creating a new trip, copy the newTrip data
            TripViewModel.UserAction.CREATE_TRIP -> vm.newTrip.deepCopy()
            // For any other action, no snapshot needed
            else -> null
        }
    }

    // Root container with background color and full screen size
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3EDF7))
    ) {
        // Keep track of the scrolling state for the LazyColumn
        val listState = rememberLazyListState()

        // LazyColumn to efficiently display the list of activities and UI elements
        LazyColumn(
            state = listState,      // Attach the scroll state
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Main content: the list of activities for the selected trip
            item {
                ActivitiesListContent(selectedTrip, vm, navController)
            }

            // Spacer between activities list and the "New Activity" button
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Button to add a new activity
            item {
                Button(
                    onClick = {
                        // Navigates to "new_activity" screen
                        navController.navigate("new_activity")
                        // Reset dialog visibility when navigating
                        showIncompleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp)
                ) {
                    Text(
                        text = "+",
                        fontSize = 30.sp
                    )
                }
            }

            // Spacer between button and finish/back section
            item {
                Spacer(modifier = Modifier.height(50.dp))
            }

            // Column container for warning dialog and action buttons (Back, Finish)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {

                    // Show a warning message if the user tries to finish the trip
                    // but not every day has at least one activity
                    if (showIncompleteDialog) {
                        Text(
                            text = "Each day of the trip must have at least one activity.",
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(top = 8.dp)
                        )
                    }

                    // Row containing Back and Finish buttons side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Back button to discard changes and go back to the previous screen
                        Button(
                            onClick = {
                                // When back is pressed, revert trip data to the original snapshot taken when entering
                                entryTripState?.let {
                                    when (vm.userAction) {
                                        // Revert edited trip data
                                        TripViewModel.UserAction.EDIT_TRIP -> vm.editTrip = it
                                        // Revert new trip data
                                        TripViewModel.UserAction.CREATE_TRIP -> vm.newTrip = it
                                        // No action otherwise
                                        else -> {}
                                    }
                                    // Sync ViewModel's selected trip state to the snapshot
                                    vm.setSelectedTrip(it)
                                }
                                // Navigate back in the stack
                                navController.popBackStack()
                            },

                            modifier = Modifier
                                .width(160.dp)
                                .height(60.dp)
                                .padding(top = 16.dp)
                        ) {
                            Text("Back")
                        }

                        // Spacer to push finish button to the right
                        Spacer(modifier = Modifier.weight(1f))

                        // Finish button to save or update the trip
                        Button(
                            onClick = {
                                // First, verify if all days have at least one activity
                                if (selectedTrip.hasActivityForEachDay()) {
                                    // If user is creating a new trip
                                    if (vm.userAction == TripViewModel.UserAction.CREATE_TRIP) {
                                        // Create an updated copy of the new trip with published = false and draft = false
                                        val updatedTrip = vm.newTrip.copy(
                                            activities = vm.newTrip.activities,
                                            published = false,
                                            isDraft = false
                                        )

                                        // Create a join request for the trip creator to mark them as participant
                                        val creatorJoinRequest = Trip.JoinRequest(
                                            userId = updatedTrip.creatorId,
                                            requestedSpots = 1,
                                            unregisteredParticipants = emptyList(),
                                            registeredParticipants = listOf(updatedTrip.creatorId)
                                        )

                                        // Assign the creator as the only participant initially
                                        updatedTrip.participants =
                                            mapOf(updatedTrip.creatorId.toString() to creatorJoinRequest)

                                        // Call the ViewModel method to save the new trip and navigate to trips list on success
                                        vm.editTrip(updatedTrip) { success ->
                                            if (success) {
                                                navController.navigate("my_trips_main") {
                                                    popUpTo("my_trips_main") {
                                                        // Do NOT remove the "my_trips_main" destination itself from the back stack.
                                                        // Only remove all screens above it.
                                                        // This ensures we navigate cleanly *back to* "my_trips_main" without duplicating it.
                                                        inclusive = false
                                                    }
                                                    // Prevent multiple instances of "my_trips_main" from being created.
                                                    // If "my_trips_main" is already at the top of the back stack,
                                                    // it will reuse the existing instance instead of creating a new one.
                                                    launchSingleTop = true
                                                }
                                            }
                                        }

                                        // If user is editing an existing trip or editing an activity
                                    } else if (vm.userAction == TripViewModel.UserAction.EDIT_TRIP
                                        ||
                                        vm.userAction == TripViewModel.UserAction.EDIT_ACTIVITY
                                    ) {
                                        // Prepare an updated copy of the edited trip marking draft as false
                                        val updatedTrip = vm.editTrip.copy(
                                            activities = vm.editTrip.activities,
                                            isDraft = false
                                        )

                                        Log.d("T1","updatedTrip in ActList = ${updatedTrip.activities.values}")
                                        Log.d("T1","selectedTrip in ActList = ${vm.selectedTrip.value.activities.values}")
                                        Log.d("T1","editTrip in ActList = ${vm.editTrip.activities.values}")
                                        Log.d("T1","newTrip in ActList = ${vm.newTrip.activities.values}")

                                        //Correction here: I have passed the selectedTrip.value for update the DB
                                        // Call the ViewModel method to update the trip and navigate on success
                                        vm.editTrip(vm.selectedTrip.value) { success ->
                                            if (success) {
                                                navController.navigate("my_trips_main") {
                                                    popUpTo("my_trips_main") {
                                                        // Do NOT remove the "my_trips_main" destination itself from the back stack.
                                                        // Only remove all screens above it.
                                                        // This ensures we navigate cleanly *back to* "my_trips_main" without duplicating it.
                                                        inclusive = false
                                                    }
                                                    // Prevent multiple instances of "my_trips_main" from being created.
                                                    // If "my_trips_main" is already at the top of the back stack,
                                                    // it will reuse the existing instance instead of creating a new one.
                                                    launchSingleTop = true
                                                }
                                            }
                                        }
                                    }

                                } else {
                                    // If some day has no activities, show the warning dialog
                                    showIncompleteDialog = true
                                }
                            },
                            modifier = Modifier
                                .width(160.dp)
                                .height(60.dp)
                                .padding(top = 16.dp)
                        ) {
                            Text("Finish")
                        }
                    }
                }
            }
        }
    }
}

// Function that showcase the activities list content
@Composable
fun ActivitiesListContent(trip: Trip?, vm: TripViewModel, navController: NavController) {
    // Handle case when no trip is selected
    if (trip == null) {
        Text("No trip selected", modifier = Modifier.padding(16.dp))
        return      // Exit early
    }

    // Sort the trip days by calendar date (ensures chronological order)
    val sortedDays = trip.activities.keys.sortedBy { key ->
        val calendar = key.toCalendar()
        // Normalize time to 00:00 to avoid issues in comparison/sorting
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        // Return calendar for sorting
        calendar
    }

    // Check if the trip contains any activities at all
    val hasNoActivities = trip.activities.values.all { it.isEmpty() }

    // State to hold which activity is being deleted (if any), persistent across recompositions
    var activityToDelete by rememberSaveable { mutableStateOf<Trip.Activity?>(null) }

    // Main container
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        // Display a message if the trip has no activities
        if (hasNoActivities) {
            Text(
                text = "No activities for trip to ${trip.destination}.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        } else {
            // Iterate through each sorted day with activities
            sortedDays.forEach { day ->

                // Convert string day key to Calendar
                val activityCalendar = day.toCalendar()

                // Get the calendar instance of the trip’s start date
                val currentTripStartCalendar = trip.startDateAsCalendar()

                // Calculate which "day number" of the trip this date represents (e.g., Day 1, Day 2, etc.)
                val dayIndex = calculateDayIndex(activityCalendar, currentTripStartCalendar)

                // Prepare formatter to sort activities by time of day
                val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

                // Get the list of activities for this day, sorted by time
                val activitiesForDay = (trip.activities[day] ?: emptyList())
                    .sortedBy { LocalTime.parse(it.time, formatter) }

                // Render section for the day
                Column(modifier = Modifier.padding(16.dp)) {

                    // Day header (e.g., "Day 2")
                    Text(
                        text = "Day $dayIndex",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF555555)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Display each activity for the current day
                    activitiesForDay.forEach { activity ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth()
                        ) {
                            // Edit icon, navigates to edit screen for the activity
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Activity",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        vm.userAction = TripViewModel.UserAction.EDIT_ACTIVITY
                                        navController.navigate("edit_Activity/${activity.id}")
                                    }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Activity details (time and description)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${activity.time} - ${activity.description}" +
                                            if (activity.isGroupActivity) " (group activity)" else "",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // Delete button (sets state to trigger confirmation dialog)
                            OutlinedButton(
                                onClick = {
                                    activityToDelete = activity
                                },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Delete", color = Color.Red)
                            }
                        }
                    }

                    // Show confirmation dialog when a delete is initiated
                    activityToDelete?.let { activity ->
                        AlertDialog(
                            onDismissRequest = {
                                activityToDelete = null     // Cancel delete
                            },
                            title = { Text("Delete Activity") },
                            text = { Text("Are you sure you want to delete this activity?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    vm.deleteActivity(activity)     // Actual delete call to ViewModel
                                    activityToDelete = null     // Clear dialog state
                                }) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    activityToDelete = null     // Clear dialog state
                                }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}


// Calculates the day index (starting from Day 1) of an activity relative to the trip's start date
fun calculateDayIndex(activityCalendar: Calendar, tripStartCalendar: Calendar): Int {
    // Normalize the activity date by resetting time fields to midnight (00:00)
    val activityDate = Calendar.getInstance().apply {
        timeInMillis = activityCalendar.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)    // Set hour to 0
        set(Calendar.MINUTE, 0)         // Set minute to 0
        set(Calendar.SECOND, 0)         // Set second to 0
        set(Calendar.MILLISECOND, 0)    // Set millisecond to 0
    }

    // Normalize the trip start date to midnight for consistent comparison
    val tripStartDate = Calendar.getInstance().apply {
        timeInMillis = tripStartCalendar.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // Calculate the difference in time (milliseconds) between activity and trip start
    val diffInMillis = activityDate.timeInMillis - tripStartDate.timeInMillis

    // Convert the time difference from milliseconds to days
    val diffInDays = diffInMillis / (24 * 60 * 60 * 1000) // 1 day = 86400000 ms

    // Add 1 so day count starts from 1 (not 0). E.g., same day = Day 1
    return (diffInDays + 1).toInt()
}

