package com.example.voyago.view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voyago.model.Trip
import com.example.voyago.parseAndSetTime
import com.example.voyago.toCalendar
import com.example.voyago.toStringDate
import com.example.voyago.viewmodel.TripViewModel
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Composable function that renders a UI for adding a new activity to a selected trip
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewActivity(navController: NavController, vm: TripViewModel) {

    // Safely observe the selected trip from ViewModel
    // This ensures that any updates to the selectedTrip state are reflected here
    val selectedTrip by vm.selectedTrip

    // Checkbox state to mark if this is a group activity
    var isGroupActivityChecked by rememberSaveable { mutableStateOf(false) }

    // State to hold the description of the activity
    var activityDescription by rememberSaveable { mutableStateOf("") }

    // Tracks whether the user has interacted with the description field
    var descriptionTouched = rememberSaveable { mutableStateOf(false) }

    // Validation state: True if description is empty or doesn't contain letters
    val descriptionHasErrors by rememberSaveable {
        derivedStateOf {
            descriptionTouched.value && (activityDescription.isBlank() || !activityDescription.any { it.isLetter() })
        }
    }

    // Dynamically calculate default date based on trip's start date
    val defaultDate by rememberSaveable {
        derivedStateOf {
            try {
                when {
                    // If the start date is valid formats it in a Calendar and returns it
                    selectedTrip.startDate.seconds > 0 -> {
                        val formatted = toCalendar(selectedTrip.startDate).toStringDate()
                        formatted
                    }
                    // Otherwise gets the current date in Calendar format and returns it
                    else -> {
                        val currentDate = Calendar.getInstance().toStringDate()
                        currentDate
                    }
                }
            } catch (e: Exception) {
                // Handles the exception
                Log.e("NewActivity", "Error computing default date", e)
                Calendar.getInstance().toStringDate()
            }
        }
    }

    // Track user-selected activity date
    var activityDate by rememberSaveable { mutableStateOf("") }

    // Automatically set the activity date to the default if not already selected
    LaunchedEffect(defaultDate) {
        if (activityDate.isEmpty()) {
            activityDate = defaultDate
        }
    }

    // Time selected for the activity
    var selectedTime by rememberSaveable { mutableStateOf("09:00 AM") }

    // Error state for date validation
    var showDateError by rememberSaveable { mutableStateOf(false) }
    var dateErrorMessage by rememberSaveable { mutableStateOf("") }

    // Validate that the trip is valid before proceeding
    if (selectedTrip.id == 0 || selectedTrip.startDate.seconds <= 0) {
        // If the trip is not valid show an error message
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Invalid trip data. Please go back and try again.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Go Back button
            Button(onClick = { navController.popBackStack() }) {
                Text("Go Back")
            }
        }
        return
    }

    // Root container box with background color
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3EDF7))
    ) {
        // For maintaining scroll position
        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .align(Alignment.Center)
                .background(
                    color = Color(0xFFE6E0E9),
                    shape = RoundedCornerShape(24.dp)
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Group Activity selection
            item {
                // Row with the "Group Activity" text and relative checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth(.8f)
                        .background(Color(0xFFD6D0D9))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // "Group Activity" text
                    Text(text = "Group activity")

                    // Spacer to move the checkbox to the right
                    Spacer(modifier = Modifier.weight(1f))

                    // Checkbox for the "Group Activity"
                    Checkbox(
                        checked = isGroupActivityChecked,
                        onCheckedChange = { isGroupActivityChecked = it }
                    )
                }
            }

            // Select Date Section
            item {
                // Get current context
                val context = LocalContext.current

                // Initialize calendar based on selectedTrip's start date
                val initialCalendar = remember(selectedTrip.startDate.seconds) {
                    Calendar.getInstance().apply {
                        try {
                            // Set the start date as the initial selected date in the calendar if it is valid
                            time = if (selectedTrip.startDate.seconds > 0) {
                                selectedTrip.startDate.toDate()
                            } else {
                                // Otherwise fallback to the current date
                                Date(0)
                            }
                        } catch (e: Exception) {
                            // Error handling: return and empty date
                            Log.e("NewActivity", "Error setting initial calendar", e)
                            time = Date()
                        }
                    }
                }

                // Get year, month, day from the selected date
                val year = initialCalendar.get(Calendar.YEAR)
                val month = initialCalendar.get(Calendar.MONTH)
                val day = initialCalendar.get(Calendar.DAY_OF_MONTH)

                // DatePickerDialog should reflect latest selectedTrip state
                val startDatePickerDialog = remember(selectedTrip.id) {
                    DatePickerDialog(
                        context,
                        { _: DatePicker, y: Int, m: Int, d: Int ->
                            try {
                                // Get selected date
                                val pickedCalendar = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, y)
                                    set(Calendar.MONTH, m)
                                    set(Calendar.DAY_OF_MONTH, d)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }

                                // Trip boundaries for validation
                                val tripStartCal = Calendar.getInstance().apply {
                                    time = selectedTrip.startDate.toDate()
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }

                                val tripEndCal = Calendar.getInstance().apply {
                                    time = selectedTrip.endDate.toDate()
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                    set(Calendar.SECOND, 59)
                                    set(Calendar.MILLISECOND, 999)
                                }

                                // Validation for selected date
                                val isValid =
                                    !pickedCalendar.before(tripStartCal) && !pickedCalendar.after(
                                        tripEndCal
                                    )

                                // If the selected date is valid
                                if (isValid) {
                                    // Format the date
                                    activityDate = "$d/${m + 1}/$y"

                                    // Set the date error as False
                                    showDateError = false

                                    // Set the error message as empty
                                    dateErrorMessage = ""
                                } else {
                                    // Otherwise
                                    // Set the date error as True
                                    showDateError = true

                                    // Set the message error
                                    dateErrorMessage =
                                        "Activity date must be within the trip period \n(${tripStartCal.toStringDate()} - ${tripEndCal.toStringDate()})"
                                }
                            } catch (e: Exception) {
                                // Handle exception
                                Log.e("NewActivity", "Error handling date selection", e)

                                // Set the date error as True
                                showDateError = true

                                // Set the error message
                                dateErrorMessage = "Error selecting date. Please try again."
                            }
                        }, year, month, day // Get year, month and date of the selected date
                    ).apply {
                        try {
                            // If the start date and the selected date are valid
                            if (selectedTrip.startDate.seconds > 0 && selectedTrip.endDate.seconds > 0) {
                                // Set the correct date limit

                                // Start date limit
                                val tripStartCal = Calendar.getInstance().apply {
                                    time = selectedTrip.startDate.toDate()
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }

                                // End date limit
                                val tripEndCal = Calendar.getInstance().apply {
                                    time = selectedTrip.endDate.toDate()
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                    set(Calendar.SECOND, 59)
                                    set(Calendar.MILLISECOND, 999)
                                }

                                // Set the date limits
                                datePicker.minDate = tripStartCal.timeInMillis
                                datePicker.maxDate = tripEndCal.timeInMillis
                            }
                        } catch (e: Exception) {
                            // Handle exception
                            Log.e("NewActivity", "Error setting date picker limits", e)
                        }
                    }
                }

                // Button for the selection of the date
                Button(
                    onClick = { startDatePickerDialog.show() },     // Show the start date picker
                    modifier = Modifier
                        .fillMaxWidth(.8f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.Black,
                        containerColor = Color(0xFFD6D0D9)
                    )
                ) {
                    Text("Selected Date: $activityDate")    // Show the selected date
                }

                // If there is an error in the date, show the error message
                if (showDateError) {
                    Text(
                        text = dateErrorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Select Time Section
            item {
                // Get the local context
                val context = LocalContext.current

                // State for showing the time picker
                val showTimePicker = rememberSaveable { mutableStateOf(false) }

                // If the state of the picker is True, show the Time Picker
                if (showTimePicker.value) {
                    //
                    val (currentHour, currentMinute) = rememberSaveable {
                        try {
                            // Get the current time
                            val cal = Calendar.getInstance()
                            cal.time = parseAndSetTime(cal, selectedTime).time
                            Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                        } catch (e: Exception) {
                            // In case of exception log it and set the default time "9:00"
                            Log.e("NewActivity", "Error parsing time", e)
                            Pair(9, 0)
                        }
                    }

                    // Time Picker Component
                    TimePickerDialog(
                        context,
                        { _: TimePicker, hourOfDay: Int, minute: Int ->
                            // Get current hour and minute in a calendar object with the current date
                            val cal = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, hourOfDay)
                                set(Calendar.MINUTE, minute)
                            }

                            // Set the hour
                            val hour = cal.get(Calendar.HOUR)

                            // Establish if it is AM or PM
                            val amPm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"

                            // Format the selected time
                            selectedTime = String.format(
                                Locale.US,
                                "%02d:%02d %s",
                                if (hour == 0) 12 else hour,
                                minute,
                                amPm
                            )

                            // Close the Time Picker setting its state as false
                            showTimePicker.value = false
                        },
                        currentHour,
                        currentMinute,
                        false
                    ).show()
                }

                // Button for the selection of the Time
                Button(
                    onClick = { showTimePicker.value = true },  // Show the Time Picker
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.Black,
                        containerColor = Color(0xFFD6D0D9)
                    )
                ) {
                    Text("Select Time: $selectedTime")      // Show the selected time
                }
            }

            // Activity Description section
            item {
                // Validate real-time the activity description input field
                ValidatingInputTextField(
                    activityDescription,
                    {
                        activityDescription = it
                        descriptionTouched.value = true
                    },
                    descriptionHasErrors,
                    "Activity Description"
                )
            }

            // Cancel Button and Add Button
            item {
                // row that contains the Cancel and Add Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Cancel Button that send you back in the stack
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Cancel")
                    }

                    // Space between the buttons
                    Spacer(modifier = Modifier.width(16.dp))

                    // Add Button
                    Button(
                        onClick = {
                            // Final validation before adding activity
                            val parsedDate = try {
                                // Format the date to a Calendar
                                activityDate.toCalendar()
                            } catch (e: Exception) {
                                // Otherwise log the exception and return null
                                Log.e("NewActivity", "Date parsing failed: $activityDate", e)
                                null
                            }

                            // If the date is null
                            if (parsedDate == null) {
                                // Set the date error as True
                                showDateError = true

                                // Set the message error
                                dateErrorMessage = "Invalid date format. Please select a date."

                                // Return the button
                                return@Button
                            }

                            // Assign the date to ActivityCalendar
                            val activityCalendar = parsedDate

                            // Define limit for start date
                            val tripStartCal = Calendar.getInstance().apply {
                                time = selectedTrip.startDate.toDate()
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }

                            // Define limit for the end date
                            val tripEndCal = Calendar.getInstance().apply {
                                time = selectedTrip.endDate.toDate()
                                set(Calendar.HOUR_OF_DAY, 23)
                                set(Calendar.MINUTE, 59)
                                set(Calendar.SECOND, 59)
                                set(Calendar.MILLISECOND, 999)
                            }

                            // Define variable that validates the date
                            val isDateValid =
                                !activityCalendar.before(tripStartCal) && !activityCalendar.after(
                                    tripEndCal
                                )

                            // If the date is not valid
                            if (!isDateValid) {
                                // Set the date error as true
                                showDateError = true

                                // Set the date error message
                                dateErrorMessage = "Activity date must be within the trip period"

                                // Return the button
                                return@Button
                            }

                            // Set that the user has interacted with the description field
                            descriptionTouched.value = true

                            // if the date and the description of the activity don't have errors
                            if (!showDateError && !descriptionHasErrors) {
                                // Collect all existing activity IDs for the current trip
                                val existingIds =
                                    selectedTrip.activities.values.flatten().map { it.id }.toSet()

                                // Generate a new unique ID for the new activity
                                var newActivityId =
                                    if (existingIds.isEmpty()) 1 else (existingIds.maxOrNull()
                                        ?: 0) + 1

                                // Ensure the generated ID is truly unique
                                // It's unlikely but possible that the newActivityId already exists,
                                // so we increment it until we find an unused one
                                while (existingIds.contains(newActivityId)) {
                                    newActivityId++
                                }

                                // Create a new Trip.Activity object with all the entered data
                                val newActivity = Trip.Activity(
                                    id = newActivityId,                         // The unique ID we just computed
                                    date = Timestamp(activityCalendar.time),    // Convert the selected Calendar to a Timestamp
                                    time = selectedTime,                        // User-selected time string
                                    isGroupActivity = isGroupActivityChecked,   // Boolean flag from checkbox
                                    description = activityDescription           // Text description of the activity
                                )

                                // Add the new activity to the ViewModel
                                vm.addActivityToTrip(newActivity)

                                // Navigate back to the previous screen
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}


