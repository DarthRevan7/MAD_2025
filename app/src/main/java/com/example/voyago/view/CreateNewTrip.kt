package com.example.voyago.view

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.voyago.model.TypeTravel
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNewTrip(
    navController: NavController, vm: TripViewModel,
    uvm: UserViewModel
) {

    // Set the current user action to CREATE_TRIP
    vm.userAction = TripViewModel.UserAction.CREATE_TRIP

    // Observe the currently logged-in user from the UserViewModel
    val currentUser by uvm.loggedUser.collectAsState()

    // State to hold the selected trip image URI
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    // Flag to indicate if the photo input has been interacted with
    var photoTouched = rememberSaveable { mutableStateOf(false) }

    // Flag to show image upload error if no image is selected
    var tripImageError by rememberSaveable { mutableStateOf(false) }

    // Flags for image upload status
    var isUploadingImage by rememberSaveable { mutableStateOf(false) }
    var uploadError by rememberSaveable { mutableStateOf<String?>(null) }

    // Holds values for the text fields: title, destination, price, group size
    val fieldValues = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )) {
        mutableStateListOf(
            "",
            "",
            "",
            "",
        )
    }

    // Names of each field for reference and labels
    val fieldNames = listOf("Title", "Destination", "Price Estimated", "Group Size")

    // Flags to track if each field has been touched
    var fieldTouched = rememberSaveable { mutableStateListOf(false, false, false, false) }

    // Array to hold error status for each field
    var fieldErrors = arrayOf(false, false, false, false)

    // Available trip types to choose from
    val typeTravel = listOf("Party", "Adventure", "Culture", "Relax")

    // Stores selected trip types
    val selected = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) {
        mutableStateListOf<String>()
    }

    // Flag for type selection error
    var typeTravelError by rememberSaveable { mutableStateOf(false) }

    // States for start and end date pickers
    var startDate by rememberSaveable { mutableStateOf("") }
    var startCalendar by rememberSaveable { mutableStateOf<Calendar?>(null) }

    var endDate by rememberSaveable { mutableStateOf("") }
    var endCalendar by rememberSaveable { mutableStateOf<Calendar?>(null) }

    // Holds error message for date validation
    var dateError by rememberSaveable { mutableStateOf("") }

    // Outer container for the entire screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3EDF7))  // Light background color
    ) {
        // Scroll state for the LazyColumn
        val listState = rememberLazyListState()

        // Scrollable vertical list containing all form elements
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Image selector component with validation
            item {
                TripImage(
                    imageUri = imageUri,
                    onUriSelected = { uri -> imageUri = uri },
                    photoTouched
                )
            }

            // Error message if no image is uploaded
            if (tripImageError && !photoTouched.value) {
                item {
                    Text(
                        text = "Upload Trip Photo",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Input fields for title, destination, price, and group size
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    //TextFields with various info
                    fieldValues.forEachIndexed { index, item ->
                        val fieldIsBeenTouched = fieldTouched[index]
                        // Title must contain at least a letter and cannot be black
                        if (index == 0) {
                            val textHasErrors = fieldIsBeenTouched && (item.toString().isBlank() ||
                                    !item.toString().any { it.isLetter() })
                            fieldErrors[index] = textHasErrors

                            ValidatingInputTextField(
                                item,
                                {
                                    fieldValues[index] = it
                                    if (!fieldIsBeenTouched) {
                                        fieldTouched[index] = true
                                    }
                                },
                                textHasErrors,
                                fieldNames[index]
                            )

                        }
                        // Destination
                        else if (index == 1) {
                            val textHasErrors = fieldIsBeenTouched && (
                                    item.toString().isBlank() ||
                                            !item.toString()
                                                .all { it.isLetter() || it.isWhitespace() } ||
                                            !item.toString().any { it.isLetter() }
                                    )

                            fieldErrors[index] = textHasErrors

                            ValidatingInputTextField(
                                item,
                                {
                                    fieldValues[index] = it
                                    if (!fieldIsBeenTouched) {
                                        fieldTouched[index] = true
                                    }
                                },
                                textHasErrors,
                                fieldNames[index]
                            )
                        }
                        // Price must be a valid positive decimal number
                        else if (index == 2) {
                            val floatHasErrors = fieldIsBeenTouched && (item.toString().isBlank() ||
                                    item.toString().toDoubleOrNull()?.let { it <= 0.0 } != false ||
                                    !item.toString().matches(Regex("^\\d+(\\.\\d+)?$")))

                            fieldErrors[index] = floatHasErrors

                            ValidatingInputFloatField(
                                item,
                                {
                                    fieldValues[index] = it
                                    if (!fieldIsBeenTouched) {
                                        fieldTouched[index] = true
                                    }
                                },
                                floatHasErrors,
                                fieldNames[index]
                            )
                            // Group size must be an integer greater than 1
                        } else {
                            val intHasErrors = fieldIsBeenTouched &&
                                    (item.isBlank() || item.toIntOrNull()?.let { it <= 1 } != false)

                            fieldErrors[index] = intHasErrors

                            ValidatingInputIntField(
                                item,
                                {
                                    fieldValues[index] = it
                                    if (!fieldIsBeenTouched) {
                                        fieldTouched[index] = true
                                    }
                                },
                                intHasErrors,
                                fieldNames[index]
                            )
                        }
                    }
                }
            }

            // Section title for trip type
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

            // Subtitle for trip type instructions
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

                // Error message if no type selected
                if (typeTravelError && selected.isEmpty()) {
                    Text(
                        text = "Select at least one travel type",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

            }

            // Type selection chips (selectable buttons)
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

            // Date pickers for start and end date
            item {
                val context = LocalContext.current
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                // Dialog for selecting start date
                val startDatePickerDialog = remember {
                    DatePickerDialog(
                        context,
                        { _: DatePicker, y: Int, m: Int, d: Int ->
                            startDate = "$d/${m + 1}/$y"
                            startCalendar = Calendar.getInstance().apply {
                                set(y, m, d, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }

                            // Reset end date if it is before new start date
                            if (endCalendar != null && endCalendar!!.before(startCalendar)) {
                                endDate = ""
                                endCalendar = null
                            }
                        }, year, month, day
                    ).apply {
                        // Set min date to today (no past dates allowed for start date)
                        datePicker.minDate =
                            System.currentTimeMillis() - 1000 // minus 1s to avoid edge issues
                    }
                }

                // Dialog for selecting end date
                val endDatePickerDialog = remember {
                    DatePickerDialog(
                        context,
                        { _: DatePicker, y: Int, m: Int, d: Int ->
                            endDate = "$d/${m + 1}/$y"
                            endCalendar = Calendar.getInstance().apply {
                                set(y, m, d, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                        }, year, month, day
                    )
                }

                // Before showing the end date picker, update its minDate based on startCalendar
                LaunchedEffect(startCalendar) {
                    if (startCalendar != null) {
                        endDatePickerDialog.datePicker.minDate = startCalendar!!.timeInMillis
                    } else {
                        // If no start date selected, disable past dates, or you can disable button instead
                        endDatePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
                    }
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
                        OutlinedButton(onClick = { startDatePickerDialog.show() }) {
                            Text("Start Date")
                        }

                        if (startDate.isNotEmpty()) {
                            Text("Start: $startDate", modifier = Modifier.padding(top = 8.dp))
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { endDatePickerDialog.show() },
                            enabled = startCalendar != null // disable button if startDate not picked
                        ) {
                            Text("End Date")
                        }

                        if (endDate.isNotEmpty()) {
                            Text("End: $endDate", modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }

                // Show error if dates are missing or invalid
                if (dateError.isNotEmpty() && !validateDateOrder(startCalendar, endCalendar)) {
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
                    // Cancel Button resets action and returns to previous screen
                    Button(
                        onClick = {
                            vm.userAction = TripViewModel.UserAction.NOTHING
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

                    // Next Button validates inputs and initiates trip creation
                    Button(
                        onClick = {
                            tripImageError = !imageUri.toString().isUriString()

                            fieldTouched.forEachIndexed { index, _ ->
                                fieldTouched[index] = true
                            }

                            typeTravelError = selected.isEmpty()

                            dateError = if (!validateDateOrder(startCalendar, endCalendar)) {
                                "Start Date and End Date cannot be empty.\n End Date must be after Start Date"
                            } else {
                                ""
                            }

                            if (!tripImageError && !fieldErrors.any { it } && !typeTravelError &&
                                validateDateOrder(
                                    startCalendar,
                                    endCalendar
                                ) && !isUploadingImage) {

                                isUploadingImage = true
                                uploadError = null

                                // Launch async image upload + trip creation
                                vm.createTripWithImageUpload(
                                    imageUri = imageUri,
                                    title = fieldValues[0],
                                    destination = fieldValues[1],
                                    startDate = startCalendar!!,
                                    endDate = endCalendar!!,
                                    estimatedPrice = fieldValues[2].toDouble(),
                                    groupSize = fieldValues[3].toInt(),
                                    typeTravel = selected.map {
                                        TypeTravel.valueOf(it.uppercase()).toString()
                                    },
                                    creatorId = currentUser.id
                                ) { success, trip, error ->
                                    isUploadingImage = false
                                    if (success && trip != null) {
                                        vm.newTrip = trip
                                        vm.setSelectedTrip(trip)
                                        vm.userAction = TripViewModel.UserAction.CREATE_TRIP
                                        navController.navigate("activities_list")
                                    } else {
                                        uploadError = error ?: "Failed to create trip"
                                    }
                                }
                            }
                        },
                        enabled = !isUploadingImage, // 上传时禁用按钮
                        modifier = Modifier
                            .width(160.dp)
                            .height(60.dp)
                            .padding(top = 16.dp)
                    ) {
                        if (isUploadingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Next")
                        }
                    }

                    // Show upload error message if any
                    uploadError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                }

            }

        }
    }
}


@Composable
fun TripImage(imageUri: Uri?, onUriSelected: (Uri?) -> Unit, photoTouched: MutableState<Boolean>) {
    // Get the current context for image loading
    val context = LocalContext.current

    // This is a launcher that lets the user pick an image from the gallery
    val pickMedia = rememberLauncherForActivityResult(
        // Uses system's visual media picker for images
        contract = PickVisualMedia()
    ) { uri ->
        // Updates the selected image URI when user picks an image
        onUriSelected(uri)
    }

    // Container box for the image or placeholder
    Box(
        modifier = Modifier
            .fillMaxWidth()     // Full screen width
            .height(250.dp),    // Fixed height for the image area
        contentAlignment = Alignment.Center     // Center content within the box
    ) {

        if (imageUri != null) {
            // If an image has been selected, load and display it asynchronously
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUri)     // Load the image from URI
                    .crossfade(true)    // Enable crossfade animation
                    .build(),
                contentDescription = "Selected Trip Photo",     // Accessibility description
                contentScale = ContentScale.Crop,               // Crop the image to fill the box
                modifier = Modifier.fillMaxSize()               // Fill the entire Box space
            )
            // Mark that user has touched the image area
            photoTouched.value = true
        } else {
            // If no image is selected, show a placeholder UI with an icon and text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,      // Icon for photo upload
                    contentDescription = "Placeholder Add Photo Icon",
                    modifier = Modifier.size(80.dp),     // Size of the icon
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)   // Faded color
                )

                // Space between icon and text
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap icon to add photo",     // Instructional text
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)  // Faded color
                )
            }
            // Mark that image area hasn't been touched yet
            photoTouched.value = false
        }

        // Floating action button at the bottom-right to open the image picker
        IconButton(
            onClick = {
                // Launch the media picker, restricting it to image files only
                pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)     // Position the button in bottom-right
                .padding(16.dp)                 // Add some spacing from the edges
                .background(
                    color = Color.Black.copy(alpha = 0.3f),     // Semi-transparent background
                    shape = CircleShape     // Circular shape for the button
                )
        ) {
            Icon(
                imageVector = Icons.Default.AddPhotoAlternate,      // Icon shown inside the FAB
                contentDescription = "Select photo from gallery",   // Accessibility description
                tint = Color.White,                                 // White icon color
                modifier = Modifier.padding(4.dp)                   // Inner padding
            )
        }
    }
}

// FUNCTIONS THAT VALIDATES DIFFERENT KINDS OF INPUT FIELDS

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ValidatingInputTextField(
    text: String, updateState: (String) -> Unit,
    validatorHasErrors: Boolean, label: String
) {
    // Allows control of the on-screen keyboard
    val keyboardController = LocalSoftwareKeyboardController.current

    // Use a Column to hold the text field and potentially validation text
    Column(
        modifier = Modifier
            .wrapContentSize()      // Take only as much size as needed
            .pointerInput(Unit) {
                // Detect tap gestures inside this Column
                detectTapGestures(onTap = {
                    // Hide the keyboard when the Column is tapped anywhere
                    keyboardController?.hide()
                })
            }
    ) {
        // Text input field with validation feedback
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()     // Make the field stretch full width of parent
                .padding(10.dp),    // Outer spacing around the field
            value = text,           // The current text value
            onValueChange = updateState,    // Update the state when the user types
            label = { Text(label) },        // Label shown inside the text field
            isError = validatorHasErrors,   // Red border and error state if true
            supportingText = {
                // Optional helper/error text shown below the field
                if (validatorHasErrors) {
                    Text("This field cannot be empty and cannot contains only numbers")
                }
            },
            // Set input type as plain text
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
    }
}

@Composable
fun ValidatingInputFloatField(
    text: String,
    updateState: (String) -> Unit,
    validatorHasErrors: Boolean,
    label: String
) {
    // Access to the keyboard controller for showing/hiding the on-screen keyboard
    val keyboardController = LocalSoftwareKeyboardController.current

    // Layout container for the input field and optional error message
    Column(
        modifier = Modifier
            .wrapContentSize()      // Makes the component only as big as needed
            .pointerInput(Unit) {
                // Detect touch input on the whole column
                detectTapGestures(onTap = {
                    // Hides the keyboard when user taps outside the field
                    keyboardController?.hide()
                })
            }
    ) {
        // Main input field for float (decimal) values
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()     // Takes the full width of the parent
                .padding(10.dp),    // Adds padding around the field
            value = text,           // Binds the current value to the field
            onValueChange = updateState,    // Updates state on input change
            label = { Text(label) },        // Displays label inside the outlined field
            isError = validatorHasErrors,   // Visually indicates error if true
            supportingText = {
                // Shows error message below the field if validation fails
                if (validatorHasErrors) {
                    Text("This field cannot be empty and must be a number greater that 0.0")
                }
            },
            // Restricts input to numeric values
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
fun ValidatingInputIntField(
    text: String,
    updateState: (String) -> Unit,
    validatorHasErrors: Boolean,
    label: String
) {
    // Access to the keyboard controller for showing/hiding the on-screen keyboard
    val keyboardController = LocalSoftwareKeyboardController.current

    // Layout container for the input field and optional error message
    Column(
        modifier = Modifier
            .wrapContentSize()      // Makes the component only as big as needed
            .pointerInput(Unit) {
                // Detect touch input on the whole column
                detectTapGestures(onTap = {
                    // Hides the keyboard when user taps outside the field
                    keyboardController?.hide()
                })
            }
    ) {
        // Main input field for integer values
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()      // Takes the full width of the parent
                .padding(10.dp),     // Adds padding around the field
            value = text,            // Binds the current value to the field
            onValueChange = updateState,    // Updates state on input change
            label = { Text(label) },        // Displays label inside the outlined field
            isError = validatorHasErrors,   // Visually indicates error if true
            supportingText = {
                // Shows error message below the field if validation fails
                if (validatorHasErrors) {
                    Text("This field cannot be empty and must be an integer number greater than 1")
                }
            },
            // Restricts input to numeric values
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

