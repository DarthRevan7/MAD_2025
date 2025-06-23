package com.example.voyago.view

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.net.Uri
import android.util.Log
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.voyago.model.Trip
import com.example.voyago.toStringDate
import com.example.voyago.viewmodel.TripViewModel
import com.google.firebase.Timestamp
import java.util.Calendar

@Composable
fun EditTrip(navController: NavController, vm: TripViewModel) {
    // Get the trip to be edited from the ViewModel
    val trip = vm.editTrip

    // Set the current user action in the ViewModel to editing mode
    vm.userAction = TripViewModel.UserAction.EDIT_TRIP

    // Remember a deep copy of the original trip for rollback/cancel purposes
    val originalTripState = remember {
        vm.editTrip.copy(
            // Deep copy of activities map (keys and list of activities)
            activities = vm.editTrip.activities
                .mapValues { (_, acts) -> acts.map { it.copy() } },

            // Copy of lists and maps to avoid mutability issues
            typeTravel = vm.editTrip.typeTravel.toList(),
            participants = vm.editTrip.participants.toMap(),
            appliedUsers = vm.editTrip.appliedUsers.toMap(),
            rejectedUsers = vm.editTrip.rejectedUsers.toMap()
        )
    }

    // State to track if there was an error related to trip image upload/selection
    var tripImageError by rememberSaveable { mutableStateOf(false) }
    // State holding the local image URI selected by user
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    // State holding the remote image URL (e.g. from a server)
    var remoteImageUrl by remember { mutableStateOf<String?>(null) }

    // Load the remote image URL when trip ID changes, only if no local image is selected
    LaunchedEffect(trip.id) {
        remoteImageUrl = trip.getPhoto()
    }

    // Holds the editable text field values (title, destination, estimated price, group size)
    val fieldValues = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )) {
        mutableStateListOf(
            trip.title,
            trip.destination,
            trip.estimatedPrice.toString(),
            trip.groupSize.toString(),
        )
    }

    // Names of fields for labels and validation messages
    val fieldNames = listOf("Title", "Destination", "Price Estimated", "Group Size")
    // Tracks which fields currently have validation errors
    val fieldErrors = remember { mutableStateListOf(false, false, false, false) }
    // Tracks if fields have been touched/modified (for showing errors only after interaction)
    val fieldTouched = remember { mutableStateListOf(false, false, false, false) }

    // List of possible trip types the user can select from
    val typeTravel = listOf("party", "adventure", "culture", "relax")

    // Holds currently selected trip types as a mutable state list
    val selected = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) {
        // Initialize from the trip's current types, lowercase for consistency
        trip.typeTravel.map { it.toString().lowercase() }.toMutableStateList()
    }

    // Tracks if there is a validation error for the trip type selection
    var typeTravelError by rememberSaveable { mutableStateOf(false) }

    // Date Handling: Start Date and End Date fields and related Calendar objects
    var startDate by rememberSaveable { mutableStateOf(trip.startDateAsCalendar().toStringDate()) }
    var startCalendar by rememberSaveable { mutableStateOf<Calendar?>(trip.startDateAsCalendar()) }
    var endDate by rememberSaveable { mutableStateOf(trip.endDateAsCalendar().toStringDate()) }
    var endCalendar by rememberSaveable { mutableStateOf<Calendar?>(trip.endDateAsCalendar()) }

    // Holds error message related to dates
    var dateError by rememberSaveable { mutableStateOf("") }

    // States for managing the confirmation dialog for activity reallocation when dates change
    var showReallocationDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var onConfirmReallocation by remember { mutableStateOf<(() -> Unit)?>(null) }
    var onCancelReallocation by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Keep the original start and end dates for comparison to detect changes
    val originalStartDate = remember { vm.editTrip.startDate }
    val originalEndDate = remember { vm.editTrip.endDate }

    // Function to validate input fields by index
    fun validateField(index: Int, value: String) {
        when (index) {
            0, 1 -> { // Title and Destination validation
                // Error if blank or doesn't contain any letter
                fieldErrors[index] = value.isBlank() || !value.any { it.isLetter() }
            }

            2 -> { // Price validation
                // Error if blank, non-positive, or not a valid decimal number with up to two decimals
                fieldErrors[index] = value.isBlank() ||
                        value.toDoubleOrNull()?.let { it <= 0.0 } != false ||
                        !value.matches(Regex("^\\d+(\\.\\d{1,2})?$"))
            }

            3 -> { // Group Size validation
                // Error if blank or less than or equal to 1
                fieldErrors[index] = value.isBlank() ||
                        value.toIntOrNull()?.let { it <= 1 } != false
            }
        }
    }

    // Main UI container with background color
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3EDF7))
    ) {
        // LazyListState to control scrolling behavior
        val listState = rememberLazyListState()

        // LazyColumn for efficient vertical scrolling list of form items
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Trip image editing UI item
            item {
                TripImageEdit(
                    trip = trip,
                    imageUri = imageUri,
                    onUriSelected = { uri -> imageUri = uri }
                )
            }

            // Show error message if trip image upload failed
            if (tripImageError) {
                item {
                    Text(
                        text = "Upload Trip Photo",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Spacer to add vertical space
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Form fields for Title, Destination, Price, and Group Size
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Iterate over each field for rendering input and validation
                    fieldValues.forEachIndexed { index, item ->
                        //Title and Destination Fields
                        if (index == 0 || index == 1) {
                            val textHasErrors = item.toString().isBlank() || // Check if it is empty
                                    !item.toString()
                                        .any { it.isLetter() } // Check if it contains letters

                            fieldErrors[index] = textHasErrors // Set error status

                            ValidatingInputTextField(
                                item.toString(),
                                { newValue ->
                                    fieldValues[index] = newValue
                                    // Set touch state
                                    fieldTouched[index] = true
                                    // Real-time verification
                                    validateField(index, newValue)
                                },
                                // Error is shown only after touching
                                fieldTouched[index] && fieldErrors[index],
                                fieldNames[index]
                            )
                        } else if (index == 2) { //Price Estimated Field
                            // Setting two decimal places to price
                            val priceText = item.toString() // Get price text
                            val floatHasErrors = priceText.isBlank() || // Check if it is empty
                                    priceText.toDoubleOrNull()
                                        ?.let { it <= 0.0 } != false || // Check if it is greater than 0
                                    !priceText.matches(Regex("^\\d+(\\.\\d{1,2})?$")) // Regular expression with two decimal places

                            fieldErrors[index] = floatHasErrors // Set error status

                            ValidatingInputFloatField( // Validate input floating point field
                                item.toString(), // current value
                                { newValue ->
                                    // Real-time validation and formatting when processing input
                                    val filteredValue =
                                        newValue.filter { it.isDigit() || it == '.' } // Only numbers and decimal points are allowed

                                    // Check the position and number of decimal points
                                    val decimalIndex = filteredValue.indexOf('.')
                                    val processedValue = if (decimalIndex != -1) {
                                        val beforeDecimal =
                                            filteredValue.substring(
                                                0,
                                                decimalIndex
                                            ) // The part before the decimal point
                                        val afterDecimal =
                                            filteredValue.substring(decimalIndex + 1) // The part after the decimal point

                                        // Limit the number of digits after the decimal point to two
                                        if (afterDecimal.length <= 2) {
                                            filteredValue
                                        } else {
                                            "$beforeDecimal.${afterDecimal.take(2)}" // Truncate the first two decimal places
                                        }
                                    } else {
                                        filteredValue // No decimal point, use directly
                                    }

                                    fieldValues[index] =
                                        processedValue // Update the processed value
                                },
                                floatHasErrors, // Are there any errors?
                                fieldNames[index] // Field name
                            )
                        } else { //Group Size Field
                            val intHasErrors =
                                (item.toString().isBlank() || item.toString().toIntOrNull()
                                    ?.let { it <= 1 } != false)

                            fieldErrors[index] = intHasErrors // Set error status

                            ValidatingInputIntField( // Validate input integer field
                                item.toString(), // current value
                                {
                                    fieldValues[index] = it // callback to update value
                                },
                                intHasErrors, // Are there any errors?
                                fieldNames[index] // Field name
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Trip Type label centered
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()

                ) {
                    Text(
                        text = "Trip type",
                        modifier = Modifier
                            .align(Alignment.Center),
                        fontSize = 17.sp
                    )
                }
            }

            // Subtitle and error message for trip type selection
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 3.dp)
                ) {
                    Text(
                        text = "Select one or more options",
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .align(Alignment.Center),
                        fontStyle = FontStyle.Italic
                    )
                }

                if (typeTravelError && selected.isEmpty()) {
                    Text(
                        text = "Select at least one travel type",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

            }

            // Row of chips to select trip types
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                ) {
                    typeTravel.forEach { type ->
                        FilterChip(
                            selected = type in selected,
                            onClick = {
                                if (type in selected) {
                                    selected.remove(type)
                                } else {
                                    selected.add(type)
                                }
                            },
                            label = { Text(type.lowercase()) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Date selection UI
            item {
                val context = LocalContext.current
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                // Get today's date as the minimum selectable date
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // Modify the date picker logic - process directly without showing a dialog box
                val startDatePickerDialog = remember {
                    DatePickerDialog(
                        context,
                        { _: DatePicker, y: Int, m: Int, d: Int ->
                            startDate = "$d/${m + 1}/$y"
                            val newStartCalendar = Calendar.getInstance().apply {
                                set(y, m, d, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            startCalendar = newStartCalendar

                        }, year, month, day
                    ).apply {
                        datePicker.minDate = today.timeInMillis
                    }
                }

                val endDatePickerDialog = remember {
                    DatePickerDialog(
                        context,
                        { _: DatePicker, y: Int, m: Int, d: Int ->
                            endDate = "$d/${m + 1}/$y"
                            val newEndCalendar = Calendar.getInstance().apply {
                                set(y, m, d, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            endCalendar = newEndCalendar

                        }, year, month, day
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 35.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp)
                    ) {
                        // Configure minimum selectable date as today and show the start date picker
                        OutlinedButton(onClick = {
                            startDatePickerDialog.datePicker.minDate = today.timeInMillis
                            startDatePickerDialog.show()
                        }) {
                            Text("Start Date")
                        }

                        // Show selected start date below the button if one exists
                        if (startDate.isNotEmpty()) {
                            Text(
                                "Start: $startDate",
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp)
                    ) {
                        // If a start date exists, set minimum end date to same day or later
                        OutlinedButton(onClick = {
                            if (startCalendar != null) {
                                val startDateMin = Calendar.getInstance().apply {
                                    timeInMillis = startCalendar!!.timeInMillis
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                endDatePickerDialog.datePicker.minDate = startDateMin.timeInMillis
                            } else {
                                // If no start date selected, fallback to today as minimum
                                endDatePickerDialog.datePicker.minDate = today.timeInMillis
                            }
                            endDatePickerDialog.show()
                        }) {
                            Text("End Date")
                        }

                        // Show selected end date below the button if one exists
                        if (endDate.isNotEmpty()) {
                            Text("End: $endDate", modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }

                // If there's a validation error with dates, show it below the row
                if (dateError.isNotEmpty()) {
                    Text(
                        text = dateError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(50.dp))
            }

            //Cancel Button and Next Button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                ) {
                    //Cancel Button
                    Button(
                        onClick = {
                            // Revert trip to its original state and return to previous screen
                            vm.editTrip = originalTripState
                            vm.setSelectedTrip(originalTripState)
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .width(160.dp)
                            .height(60.dp)
                            .padding(top = 16.dp)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    //Next Button
                    Button(
                        onClick = {
                            vm.userAction = TripViewModel.UserAction.EDIT_TRIP

                            // Validate trip type
                            typeTravelError = selected.isEmpty()

                            // Validate dates
                            val isDateValid = validateDateOrder(startCalendar, endCalendar)
                            dateError = if (!isDateValid) {
                                "Start Date and End Date cannot be empty.\n End Date must be after Start Date"
                            } else {
                                ""
                            }

                            // Check if any form field has validation errors
                            val hasFieldErrors = fieldErrors.any { it }

                            if (!typeTravelError && dateError.isEmpty() && !hasFieldErrors) {
                                // Compare original and current dates to detect change
                                val originalStartCal = Calendar.getInstance()
                                    .apply { time = originalStartDate.toDate() }
                                val originalEndCal =
                                    Calendar.getInstance().apply { time = originalEndDate.toDate() }

                                val hasDateChanged =
                                    startCalendar?.timeInMillis != originalStartCal.timeInMillis ||
                                            endCalendar?.timeInMillis != originalEndCal.timeInMillis

                                if (hasDateChanged && startCalendar != null && endCalendar != null) {

                                    //Automatically reallocate activities without asking
                                    smartReallocateActivitiesDirectly(
                                        vm = vm,
                                        oldStartCal = originalStartCal,
                                        oldEndCal = originalEndCal,
                                        newStartCal = startCalendar!!,
                                        newEndCal = endCalendar!!
                                    )

                                    // Update trip and navigate forward
                                    updateTripAndNavigate(
                                        vm, startCalendar!!, endCalendar!!, navController,
                                        selected, fieldValues[0], fieldValues[1],
                                        fieldValues[3].toIntOrNull() ?: 2, imageUri
                                    )
                                } else {
                                    // No date changes – just update trip directly
                                    updateTripAndNavigate(
                                        vm, startCalendar!!, endCalendar!!, navController,
                                        selected, fieldValues[0], fieldValues[1],
                                        fieldValues[3].toIntOrNull() ?: 2, imageUri
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .width(160.dp)
                            .height(60.dp)
                            .padding(top = 16.dp)
                    ) {
                        Text("Next")
                    }
                }
            }
        }
    }

    // Reassign confirmation dialog
    if (showReallocationDialog) {
        AlertDialog(
            onDismissRequest = { showReallocationDialog = false },
            title = { Text("Activity Reallocation") },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReallocationDialog = false
                        onConfirmReallocation?.invoke()
                    }
                ) {
                    Text("Move to Last Day")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showReallocationDialog = false
                        onCancelReallocation?.invoke()
                    }
                ) {
                    Text("Delete Activities")
                }
            }
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@SuppressLint("DiscouragedApi")
@Composable
fun TripImageEdit(trip: Trip, imageUri: Uri?, onUriSelected: (Uri?) -> Unit) {

    // This lets us launch the photo picker and receive the selected image URI
    val pickMedia = rememberLauncherForActivityResult(
        // Use Android's new photo picker contract
        contract = PickVisualMedia()
    ) { uri ->
        // When the user picks an image, invoke the callback with the new URI
        onUriSelected(uri)
    }

    // State to hold remote trip image URL (from the trip object in case no new image is selected)
    var remoteImageUrl by remember { mutableStateOf<String?>(null) }

    // This ensures we always show the trip’s existing image if there’s no local change
    LaunchedEffect(trip.photo) {
        if (imageUri == null) {
            remoteImageUrl = trip.getPhoto()
        }
    }

    // Outer container for image preview + image picker icon
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {

        //Conditional rendering: Choose what image to display
        when {
            // Case 1: If a new image URI is selected, show it
            imageUri != null -> {
                GlideImage(
                    model = imageUri,   // URI of newly selected image
                    contentDescription = "Selected Trip Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Case 2: Otherwise show the existing image from the trip (if loaded)
            remoteImageUrl != null -> {
                GlideImage(
                    model = remoteImageUrl,     // URL of image fetched from trip object
                    contentDescription = "Trip Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Floating image picker button (bottom right corner)
        IconButton(
            onClick = {
                // Launch the image picker to select an image
                pickMedia.launch(
                    // Only allow image selection
                    PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = CircleShape
                )
        ) {
            // Icon inside the button
            Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = "Select photo from gallery",
                tint = Color.White,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

// Function: Smartly reallocates activities in a trip when the date range changes
fun smartReallocateActivities(
    vm: TripViewModel,
    oldStartCal: Calendar,
    oldEndCal: Calendar,
    newStartCal: Calendar,
    newEndCal: Calendar
) {
    // Get the trip currently being edited from the ViewModel
    val currentTrip = vm.editTrip


    // Calculate the number of days in the old and new date ranges
    val oldIntervalDays = calculateDaysBetween(oldStartCal, oldEndCal)
    val newIntervalDays = calculateDaysBetween(newStartCal, newEndCal)

    // This will hold the reallocated activities after processing
    val updatedActivities = mutableMapOf<String, List<Trip.Activity>>()

    // Decide what kind of reallocation logic to apply based on interval change
    when {
        // Case 1: Same duration — activities can be shifted directly to match the new date range
        oldIntervalDays == newIntervalDays -> {
            reallocateWithSameInterval(
                currentTrip.activities,     // Original activity map: date -> list of activities
                oldStartCal,                // Original start date
                newStartCal,                // New start date to shift from
                updatedActivities           // Output: shifted activities in new date range
            )
        }

        // Case 2: New duration is longer — distribute activities into new range,
        // and leave extra new days empty (activities won't be moved arbitrarily)
        newIntervalDays > oldIntervalDays -> {
            reallocateWithLongerInterval(
                currentTrip.activities,     // Original activities
                oldStartCal,                // Reference original start date
                newStartCal,                // New base date for offset
                updatedActivities           // Output: moved activities
            )
        }

        // Case 3: New duration is shorter — need to resolve activities that don't fit
        newIntervalDays < oldIntervalDays -> {
            reallocateWithShorterInterval(
                currentTrip.activities,     // All original activities
                oldStartCal,                // Original start
                newStartCal,                // New start
                newEndCal,                  // New end to limit allocation within range
                updatedActivities           // Output
            )
        }
    }

    // Apply the updated activities and new date range to the trip
    vm.editTrip = currentTrip.copy(
        activities = updatedActivities,
        startDate = Timestamp(newStartCal.time),
        endDate = Timestamp(newEndCal.time)
    )

    //Update ViewModel state to reflect new trip data
    vm.setSelectedTrip(vm.editTrip)
}

// Calculates the number of days between two Calendar dates
fun calculateDaysBetween(startCal: Calendar, endCal: Calendar): Int {

    // Create a clean copy of the start date and normalize it to midnight
    val startDate = Calendar.getInstance().apply {
        timeInMillis = startCal.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // Do the same for the end date: normalize to midnight
    val endDate = Calendar.getInstance().apply {
        timeInMillis = endCal.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // Calculate the difference in milliseconds between the two normalized dates
    val diffInMillis = endDate.timeInMillis - startDate.timeInMillis

    // Convert milliseconds to days by dividing by the number of milliseconds in a day
    return (diffInMillis / (24 * 60 * 60 * 1000)).toInt() + 1
}

// Case 1: When the trip duration (number of days) is unchanged,
// this function repositions all activities to the same relative day,
// just offset by the number of days between the old and new start dates.
private fun reallocateWithSameInterval(
    originalActivities: Map<String, List<Trip.Activity>>,
    oldStartCal: Calendar,
    newStartCal: Calendar,
    updatedActivities: MutableMap<String, List<Trip.Activity>>
) {

    // Calculate how many days the start date has shifted by
    // Subtract 1 to get the offset (e.g., if shifted by 3 full days, offset = 2)
    val dayOffset = calculateDaysBetween(oldStartCal, newStartCal) - 1

    // Iterate over each original activity date and its corresponding list of activities
    originalActivities.forEach { (oldDateKey, activities) ->
        try {
            // Parse the original date key (String) into a Calendar object
            val oldActivityDate = parseActivityDate(oldDateKey)

            // Create a new Calendar instance based on the old date and shift it by the offset
            val newActivityDate = Calendar.getInstance().apply {
                timeInMillis = oldActivityDate.timeInMillis     // Start from old activity's date
                add(
                    Calendar.DAY_OF_MONTH,
                    dayOffset
                )           // Move it by the offset to the new position
            }

            // Convert the new Calendar object back to a String key to use in the updated map
            val newDateKey = newActivityDate.toStringDate()

            // Update all activities to reflect the new date (copying each one with a new timestamp)
            val updatedActivityList = activities.map { activity ->
                activity.copy(date = Timestamp(newActivityDate.time))
            }

            // Add the updated activities to the output map under the new date key
            updatedActivities[newDateKey] = updatedActivityList

        } catch (e: Exception) {
            Log.e("SmartReallocation", "Error processing date $oldDateKey", e)
        }
    }
}

// Case 2: When the new trip duration is longer than the original
private fun reallocateWithLongerInterval(
    originalActivities: Map<String, List<Trip.Activity>>,
    oldStartCal: Calendar,
    newStartCal: Calendar,
    updatedActivities: MutableMap<String, List<Trip.Activity>>
) {
    // Loop through each original date and the associated activities
    originalActivities.forEach { (oldDateKey, activities) ->
        try {

            // Convert the old string date key into a Calendar object
            val oldActivityDate = parseActivityDate(oldDateKey)

            // Calculate the position of this date in the original trip (relative to old start date)
            val relativeDay = calculateDaysBetween(oldStartCal, oldActivityDate) - 1

            // Create the new date by adding the same relative day to the new trip's start date
            val newActivityDate = Calendar.getInstance().apply {
                timeInMillis = newStartCal.timeInMillis     // Start from the new start date
                add(Calendar.DAY_OF_MONTH, relativeDay)     // Maintain the same relative day
            }

            // Convert the calculated Calendar date back into a string key
            val newDateKey = newActivityDate.toStringDate()

            val updatedActivityList = activities.map { activity ->
                activity.copy(date = Timestamp(newActivityDate.time))
            }

            // Update each activity to have a new date
            updatedActivities[newDateKey] = updatedActivityList

        } catch (e: Exception) {
            Log.e("SmartReallocation", "Error processing date $oldDateKey", e)
        }
    }
}

// Date Validation Function: Ensures that the end date is not earlier than the start date.
fun validateDateOrder(startCalendar: Calendar?, endCalendar: Calendar?): Boolean {

    // Ensure startCalendar is not null
    if (startCalendar == null) {
        return false
    }

    // Ensure endCalendar is not null
    if (endCalendar == null) {
        return false
    }

    // Normalize the start date
    val startDate = Calendar.getInstance().apply {
        timeInMillis = startCalendar.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // Normalize the end date
    val endDate = Calendar.getInstance().apply {
        timeInMillis = endCalendar.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // Ensure that the end date is the same as or after the start date
    val isValid = endDate.timeInMillis >= startDate.timeInMillis

    // Return true if the end date is valid, otherwise false
    return isValid
}

// Updates the trip information in the ViewModel and navigates to the activities list screen
private fun updateTripAndNavigate(
    vm: TripViewModel,
    startCalendar: Calendar,
    endCalendar: Calendar,
    navController: NavController,
    selected: MutableList<String>,
    title: String,
    destination: String,
    groupSize: Int,
    imageUri: Uri?
) {
    // Update the editable trip object with the latest user input
    if(imageUri != null) {
        vm.editTrip = vm.editTrip.copy(
            typeTravel = selected.toList(),
            title = title,
            destination = destination,
            groupSize = groupSize,
            photo = imageUri.toString(),
            startDate = Timestamp(startCalendar.time),
            endDate = Timestamp(endCalendar.time)
        )
    }
    else {
        vm.editTrip = vm.editTrip.copy(
            typeTravel = selected.toList(),
            title = title,
            destination = destination,
            groupSize = groupSize,
            photo = vm.editTrip.photo,
            startDate = Timestamp(startCalendar.time),
            endDate = Timestamp(endCalendar.time)
        )
    }

    // Update selectedTrip to reflect changes made in editTrip
    vm.setSelectedTrip(vm.editTrip)

    // Explicitly mark this as an EDIT_TRIP action
    vm.userAction = TripViewModel.UserAction.EDIT_TRIP


    // Update the DB with the new trip
    vm.editTrip(vm.selectedTrip.value) { success -> Log.d("DB2", "$success") }


    // Navigate to the activities list screen where the trip details will be shown
    navController.navigate("activities_list")
}

// Automatically reallocates activities for a trip when the start or end date is changed.
fun smartReallocateActivitiesDirectly(
    vm: TripViewModel,
    oldStartCal: Calendar,
    oldEndCal: Calendar,
    newStartCal: Calendar,
    newEndCal: Calendar
) {
    // Apply reallocation immediately based on date changes
    smartReallocateActivities(vm, oldStartCal, oldEndCal, newStartCal, newEndCal)
}

// Parses a given dateKey string into a Calendar object
fun parseActivityDate(dateKey: String): Calendar {
    return try {
        when {
            // If the string can be parsed as a long number and looks like a timestamp
            dateKey.toLongOrNull() != null && dateKey.length > 10 -> {
                // Parse it as a timestamp (milliseconds since epoch)
                Calendar.getInstance().apply {
                    timeInMillis = dateKey.toLong()
                }
            }

            else -> {
                // Fallback — treat the string as a manually formatted date
                parseDateManually(dateKey)
            }
        }
    } catch (e: Exception) {
        Log.e("DateParsing", "Errore nel parsing della data $dateKey: ${e.message}")
        try {
            // Retry using a known fallback date format
            parseDateManually(dateKey, "d/M/yyyy")
        } catch (_: Exception) {
            // If all parsing attempts fail, return current date as a fallback
            Calendar.getInstance()
        }
    }
}

// Manually parses a date string into a Calendar object, based on the provided format
private fun parseDateManually(dateString: String, format: String = "YYYY-MM-DD"): Calendar {
    // Split the input string into parts using either "-" or "/" as separators
    val parts = dateString.split("-", "/")

    // Validate that the date string has exactly three components (day, month, year)
    if (parts.size != 3) {
        throw IllegalArgumentException("Invalid date format: $dateString")
    }

    return when (format) {
        // Format: "YYYY-MM-DD"
        "YYYY-MM-DD" -> {
            val year = parts[0].toInt()         // Extract year from first part
            val month = parts[1].toInt() - 1     // Month is zero-based in Calendar
            val day = parts[2].toInt()       // Extract day from third part

            // Construct a Calendar instance with parsed values
            Calendar.getInstance().apply {
                set(year, month, day)
            }
        }

        // Format: "d/M/yyyy"
        "d/M/yyyy" -> {
            val day = parts[0].toInt()          // Day comes first
            val month = parts[1].toInt() - 1    // Day comes first
            val year = parts[2].toInt()         // Year comes last

            // Construct a Calendar instance with parsed values
            Calendar.getInstance().apply {
                set(year, month, day)
            }
        }
        // Unsupported or unrecognized format
        else -> {
            throw IllegalArgumentException("Unsupported date parsing format: $format")
        }
    }
}

// Reallocates trip activities when the new trip duration is shorter than the original
private fun reallocateWithShorterInterval(
    originalActivities: Map<String, List<Trip.Activity>>,
    oldStartCal: Calendar,
    newStartCal: Calendar,
    newEndCal: Calendar,
    updatedActivities: MutableMap<String, List<Trip.Activity>>
) {
    // Normalize original start date (remove time fields for accurate day comparison
    val oldStart = Calendar.getInstance().apply {
        timeInMillis = oldStartCal.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // Normalize new start date
    val newStart = Calendar.getInstance().apply {
        timeInMillis = newStartCal.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // Normalize new end date
    val newEnd = Calendar.getInstance().apply {
        timeInMillis = newEndCal.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // Calculate the number of days in the new trip duration
    val newTripDays = calculateDaysBetween(newStart, newEnd)

    // List to collect any activities that fall outside the new date range
    val overflowActivities = mutableListOf<Trip.Activity>()

    // Map to store valid activities assigned to each valid day in the new date range
    val dayToActivitiesMap = mutableMapOf<Int, MutableList<Trip.Activity>>()

    // Loop through each date key in the original activity map
    originalActivities.forEach { (oldDateKey, activities) ->
        try {
            // Parse the original date key to a Calendar object
            val activityDate = parseActivityDate(oldDateKey)

            // Normalize the activity date (remove time components)
            val normalizedActivityDate = Calendar.getInstance().apply {
                timeInMillis = activityDate.timeInMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Calculate the day index relative to the original start date (1-based)
            val dayNumber = calculateDaysBetween(oldStart, normalizedActivityDate)

            if (dayNumber <= newTripDays) {
                // If activity fits within the new trip duration, add it to the correct day
                if (!dayToActivitiesMap.containsKey(dayNumber)) {
                    dayToActivitiesMap[dayNumber] = mutableListOf()
                }
                dayToActivitiesMap[dayNumber]?.addAll(activities)

            } else {
                // Otherwise, mark it as overflow
                overflowActivities.addAll(activities)
            }

        } catch (e: Exception) {
            Log.e("SmartReallocation", "Error processing date $oldDateKey: ${e.message}")
            // If date parsing or logic fails, treat the activities as overflow
            overflowActivities.addAll(activities)
        }
    }

    // Reassign valid activities to the corresponding new dates
    dayToActivitiesMap.forEach { (dayNumber, activities) ->
        val newDate = Calendar.getInstance().apply {
            timeInMillis = newStart.timeInMillis
            add(Calendar.DAY_OF_MONTH, dayNumber - 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val newDateKey = newDate.toStringDate()

        // Copy activities and assign them the new timestamp
        val updatedActivityList = activities.map { activity ->
            activity.copy(date = Timestamp(newDate.time))
        }

        updatedActivities[newDateKey] = updatedActivityList
    }

    // Assign all overflow activities to the last day of the new trip
    if (overflowActivities.isNotEmpty()) {
        val lastDayKey = newEnd.toStringDate()

        val overflowWithNewDate = overflowActivities.map { activity ->
            activity.copy(date = Timestamp(newEnd.time))
        }

        // Append overflow activities to any existing activities on the last day
        updatedActivities[lastDayKey] =
            (updatedActivities[lastDayKey] ?: emptyList()) + overflowWithNewDate
    }
}