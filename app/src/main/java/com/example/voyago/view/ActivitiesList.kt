package com.example.voyago.view

import android.annotation.SuppressLint
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
    var showIncompleteDialog by remember { mutableStateOf(false) }

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

// Function that showcase the activities list content with fixed date-based grouping
@Composable
fun ActivitiesListContent(trip: Trip?, vm: TripViewModel, navController: NavController) {

    if (trip == null) {
        Log.w("ActivitiesListContent", "No trip provided, showing 'No trip selected' message")
        Text("No trip selected", modifier = Modifier.padding(16.dp))
        return
    }

    // Generate complete date range from trip start to end date
    val tripStartCal = trip.startDateAsCalendar()
    val tripEndCal = trip.endDateAsCalendar()

    val dateRange = generateDateRange(tripStartCal, tripEndCal)


    // State for delete confirmation dialog
    var activityToDelete by rememberSaveable { mutableStateOf<Trip.Activity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {

        // Check if there are any activities
        val hasNoActivities = trip.activities.values.all { it.isEmpty() }

        if (hasNoActivities) {
            Text(
                text = "No activities for trip to ${trip.destination}.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        } else {

            // Iterate through each day in the date range and display activities
            dateRange.forEachIndexed { index, dateKey ->
                val dayIndex = index + 1

                // Find all activities for this specific date
                val activitiesForDay = findActivitiesForDate(dateKey, trip.activities)

                // Only display the day if it has activities
                if (activitiesForDay.isNotEmpty()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Day title
                        Text(
                            text = "Day $dayIndex",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF555555)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Display each activity for this day (already sorted by time)
                        activitiesForDay.forEachIndexed { activityIndex, activity ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .fillMaxWidth()
                            ) {
                                // Edit icon
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Activity",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable {
                                            Log.d(
                                                "ActivitiesListContent",
                                                "Edit clicked for activity ID: ${activity.id}"
                                            )
                                            vm.userAction = TripViewModel.UserAction.EDIT_ACTIVITY
                                            navController.navigate("edit_Activity/${activity.id}")
                                        }
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Activity details
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${activity.time} - ${activity.description}" +
                                                if (activity.isGroupActivity) " (group activity)" else "",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                // Delete button
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
                    }
                } else {
                    Log.v(
                        "ActivitiesListContent",
                        "Day $dayIndex has no activities, skipping render"
                    )
                }
            }
        }

        // Delete confirmation dialog
        activityToDelete?.let { activity ->
            AlertDialog(
                onDismissRequest = {
                    Log.d("ActivitiesListContent", "Delete dialog dismissed")
                    activityToDelete = null
                },
                title = { Text("Delete Activity") },
                text = { Text("Are you sure you want to delete this activity?") },
                confirmButton = {
                    TextButton(onClick = {
                        Log.i(
                            "ActivitiesListContent",
                            "Confirming deletion of activity ID: ${activity.id}"
                        )
                        vm.deleteActivity(activity)
                        activityToDelete = null
                        Log.d("ActivitiesListContent", "Activity deletion completed")
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        Log.d("ActivitiesListContent", "Delete dialog cancelled")
                        activityToDelete = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// Helper function: Generate date range from start to end date
@SuppressLint("DefaultLocale")
private fun generateDateRange(startCal: Calendar, endCal: Calendar): List<String> {
    val dateList = mutableListOf<String>()
    val current = Calendar.getInstance().apply {
        timeInMillis = startCal.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val end = Calendar.getInstance().apply {
        timeInMillis = endCal.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    while (!current.after(end)) {
        val dateString = String.format(
            "%d/%d/%d",
            current.get(Calendar.DAY_OF_MONTH),
            current.get(Calendar.MONTH) + 1,
            current.get(Calendar.YEAR)
        )
        dateList.add(dateString)
        current.add(Calendar.DAY_OF_MONTH, 1)
    }

    Log.d("DateRangeDebug", "Generated date list: $dateList")
    return dateList
}

// Helper function: Parse activity date from string key
private fun findActivitiesForDate(
    targetDateKey: String,
    allActivities: Map<String, List<Trip.Activity>>
): List<Trip.Activity> {
    // First try direct match (DD/MM/YYYY format)
    var activities = allActivities[targetDateKey] ?: emptyList()

    // If no direct match, try converting DD/MM/YYYY to YYYY-MM-DD format
    if (activities.isEmpty() && targetDateKey.contains("/")) {
        try {
            val parts = targetDateKey.split("/")
            if (parts.size == 3) {
                val day = parts[0].padStart(2, '0')
                val month = parts[1].padStart(2, '0')
                val year = parts[2]
                val alternativeKey = "$year-$month-$day"
                activities = allActivities[alternativeKey] ?: emptyList()
            }
        } catch (_: Exception) {
            // Silently handle conversion errors
        }
    }

    return activities.sortedBy { activity ->
        parseTimeToMinutes(activity.time)
    }
}

private fun parseTimeToMinutes(timeString: String): Int {
    return try {

        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

        val time = LocalTime.parse(timeString, formatter)
        time.hour * 60 + time.minute
    } catch (e: Exception) {
        // If parsing fails, return a default value
        Log.e("TimeParsing", "Error parsing time: $timeString", e)
        0
    }
}

