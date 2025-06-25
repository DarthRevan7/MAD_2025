package com.example.voyago.view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import java.util.Locale

// Function that permits the user to edit an activity
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivity(navController: NavController, vm: TripViewModel, activityId: Int) {

    // Determine which trip to use based on the user's current action (editing or creating a trip)
    val currentTrip = when (vm.userAction) {
        TripViewModel.UserAction.EDIT_ACTIVITY -> {

            vm.selectedTrip.value
        }

        TripViewModel.UserAction.CREATE_TRIP -> {

            if (vm.newTrip.isValid()) vm.newTrip else vm.selectedTrip.value
        }

        else -> vm.selectedTrip.value
    }

    // Find the activity the user wants to edit using the provided activityId
    val activityToEdit = currentTrip.activities.values.flatten().find { it.id == activityId }

    // If the activity does not exist, display an error and exit the function early
    if (activityToEdit == null) {
        Text("Activity not found.")
        return
    }

    // State variables for form fields initialized with existing activity values.
    var isGroupActivityChecked by rememberSaveable { mutableStateOf(activityToEdit.isGroupActivity) }
    var activityDescription by rememberSaveable { mutableStateOf(activityToEdit.description) }

    // Tracks whether the description input field has been interacted with.
    var descriptionTouched = remember { mutableStateOf(false) }

    // Validation: Check if the description is not empty and contains at least one letter.
    val descriptionHasErrors by remember {
        derivedStateOf {
            descriptionTouched.value && (activityDescription.isBlank() || !activityDescription.any { it.isLetter() })
        }
    }

    // State for storing the selected date and time
    var activityDate by remember {
        mutableStateOf(toCalendar(activityToEdit.date).toStringDate())
    }
    var selectedTime by remember { mutableStateOf(activityToEdit.time) }

    // State for managing date validation errors
    var showDateError by remember { mutableStateOf(false) }
    var dateErrorMessage by remember { mutableStateOf("") }

    var showDatePicker = remember { mutableStateOf(false) }

    // Main layout container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3EDF7))
    ) {
        // State object to control the scroll position of the LazyColumn
        val listState = rememberLazyListState()

        // Scrollable vertical list of form fields and buttons
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

            // Checkbox for group activity
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(.8f)
                        .background(Color(0xFFD6D0D9))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Group activity")
                    Spacer(modifier = Modifier.weight(1f))
                    Checkbox(
                        checked = isGroupActivityChecked,
                        onCheckedChange = { isGroupActivityChecked = it }
                    )
                }
            }

            // Date picker for selecting the activity date
            item {
                
                if (showDatePicker.value) {
                    DatePickerChecked(
                        upperContext = LocalContext.current,
                        activityToEdit,
                        currentTrip = currentTrip,
                    ) { activityDateString, error, errorMessage ->
                        activityDate = activityDateString
                        showDateError = error
                        dateErrorMessage = errorMessage
                    }.show()
                }


//                // Create and configure a DatePickerDialog
//                val startDatePickerDialog =// remember {
//                    DatePickerDialog(
//                        context,
//                        { _: DatePicker, y: Int, m: Int, d: Int ->
//                            val pickedCalendar = Calendar.getInstance().apply {
//                                set(Calendar.YEAR, y)
//                                set(Calendar.MONTH, m)
//                                set(Calendar.DAY_OF_MONTH, d)
//                                set(Calendar.HOUR_OF_DAY, 0)
//                                set(Calendar.MINUTE, 0)
//                                set(Calendar.SECOND, 0)
//                                set(Calendar.MILLISECOND, 0)
//                            }
//
//                            // Convert trip start and end dates to Calendar for comparison
//                            val tripStartCal = toCalendar(currentTrip.startDate).apply {
//                                set(Calendar.HOUR_OF_DAY, 0)
//                                set(Calendar.MINUTE, 0)
//                                set(Calendar.SECOND, 0)
//                                set(Calendar.MILLISECOND, 0)
//                            }
//
//                            val tripEndCal = toCalendar(currentTrip.endDate).apply {
//                                set(Calendar.HOUR_OF_DAY, 23)
//                                set(Calendar.MINUTE, 59)
//                                set(Calendar.SECOND, 59)
//                                set(Calendar.MILLISECOND, 999)
//                            }
//
//                            // Validate that selected date is within trip range
//                            val isValid =
//                                !pickedCalendar.before(tripStartCal) && !pickedCalendar.after(
//                                    tripEndCal
//                                )
//
//                            if (isValid) {
//                                activityDate = "$d/${m + 1}/$y"
//                                showDateError = false
//                                dateErrorMessage = ""
//                            } else {
//                                showDateError = true
//                                dateErrorMessage =
//                                    "Activity date must be within the trip period \n(${tripStartCal.toStringDate()} - ${tripEndCal.toStringDate()})"
//                            }
//                        }, year, month, day
//                    ).apply {
//                        // Restrict date picker to the trip period only
//                        val tripStartCal = toCalendar(currentTrip.startDate).apply {
//                            set(Calendar.HOUR_OF_DAY, 0)
//                            set(Calendar.MINUTE, 0)
//                            set(Calendar.SECOND, 0)
//                            set(Calendar.MILLISECOND, 0)
//                        }
//
//                        val tripEndCal = toCalendar(currentTrip.endDate).apply {
//                            set(Calendar.HOUR_OF_DAY, 23)
//                            set(Calendar.MINUTE, 59)
//                            set(Calendar.SECOND, 59)
//                            set(Calendar.MILLISECOND, 999)
//                        }
//
//                        datePicker.minDate = tripStartCal.timeInMillis
//                        datePicker.maxDate = tripEndCal.timeInMillis
//                    }
//                }

                // Button to launch date picker dialog
                Button(
                    onClick = { showDatePicker.value = true },
                    modifier = Modifier
                        .fillMaxWidth(.8f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.Black,
                        containerColor = Color(0xFFD6D0D9)
                    )
                ) {
                    Text(if (activityDate.isNotEmpty()) "Selected Date: $activityDate" else "Select date")
                }

                // Display error message if the selected date is invalid
                if (showDateError) {
                    Text(
                        text = dateErrorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Time Picker for selecting the activity time
            item {
                val context = LocalContext.current
                val calendar = remember {
                    val cal = Calendar.getInstance()
                    try {
                        cal.time = parseAndSetTime(cal, selectedTime).time
                    } catch (_: Exception) {
                        cal
                    }
                    cal
                }
                val showTimePicker = remember { mutableStateOf(false) }

                // If true, show a TimePickerDialog
                if (showTimePicker.value) {
                    TimePickerDialog(
                        context,
                        { _: TimePicker, hourOfDay: Int, minute: Int ->
                            val cal = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, hourOfDay)
                                set(Calendar.MINUTE, minute)
                            }
                            val hour = cal.get(Calendar.HOUR)
                            val amPm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
                            selectedTime = String.format(
                                Locale.getDefault(),
                                "%02d:%02d %s",
                                if (hour == 0) 12 else hour,
                                minute,
                                amPm
                            )
                            showTimePicker.value = false
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        false
                    ).show()
                }

                // Button to show time picker dialog
                Button(
                    onClick = { showTimePicker.value = true },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.Black,
                        containerColor = Color(0xFFD6D0D9)
                    )
                ) {
                    Text("Select Time: $selectedTime")
                }
            }

            // Input field for activity description with real-time validation
            item {
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

            // Buttons: Cancel and Update
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Cancel button: navigate back without saving
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Update button: validate inputs and submit the updated activity
                    Button(
                        onClick = {
                            val parsedDate = try {
                                activityDate.toCalendar()
                            } catch (_: Exception) {
                                null
                            }

                            // 验证日期格式
                            if (parsedDate == null) {
                                showDateError = true
                                dateErrorMessage = "Invalid date format. Please select a date."
                                return@Button
                            }

                            // 验证日期是否在行程期间内
                            val activityCalendar = parsedDate
                            val tripStartCal = toCalendar(currentTrip.startDate).apply {
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }

                            val tripEndCal = toCalendar(currentTrip.endDate).apply {
                                set(Calendar.HOUR_OF_DAY, 23)
                                set(Calendar.MINUTE, 59)
                                set(Calendar.SECOND, 59)
                                set(Calendar.MILLISECOND, 999)
                            }

                            val isDateValid =
                                !activityCalendar.before(tripStartCal) && !activityCalendar.after(
                                    tripEndCal
                                )

                            if (!isDateValid) {
                                showDateError = true
                                dateErrorMessage = "Activity date must be within the trip period"
                                return@Button
                            }

                            // 触发描述验证
                            descriptionTouched.value = true

                            // 只有在所有字段都有效时才继续
                            if (!showDateError && !descriptionHasErrors) {
                                // 创建更新后的活动对象
                                val updatedActivity = Trip.Activity(
                                    id = activityId,
                                    date = Timestamp(activityCalendar.time),
                                    time = selectedTime,
                                    isGroupActivity = isGroupActivityChecked,
                                    description = activityDescription
                                )

                                // 更新活动（这里会自动处理日期归类）
                                vm.editActivity(activityId, updatedActivity)

                                // 更新完成后导航回去
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Update")
                    }
                }
            }
        }
    }
}


@Composable
fun DatePickerChecked(
    upperContext: Context,
    activityToEdit: Trip.Activity,
    currentTrip: Trip,
    callback: (String, Boolean, String) -> Unit
)
        : DatePickerDialog {

    val context = upperContext
    val calendar = activityToEdit.date
    val year = toCalendar(calendar).get(Calendar.YEAR)
    val month = toCalendar(calendar).get(Calendar.MONTH)
    val day = toCalendar(calendar).get(Calendar.DAY_OF_MONTH)

    val datePicker = DatePickerDialog(
        context,
        { _: DatePicker, y: Int, m: Int, d: Int ->
            val pickedCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m)
                set(Calendar.DAY_OF_MONTH, d)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Convert trip start and end dates to Calendar for comparison
            val tripStartCal = toCalendar(currentTrip.startDate).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val tripEndCal = toCalendar(currentTrip.endDate).apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            // Validate that selected date is within trip range
            val isValid =
                !pickedCalendar.before(tripStartCal) && !pickedCalendar.after(
                    tripEndCal
                )

            if (isValid) {
                callback("$d/${m + 1}/$y", false, "")
//                    activityDate = "$d/${m + 1}/$y"
//                    showDateError = false
//                    dateErrorMessage = ""
            } else {
                callback(
                    "",
                    true,
                    "Activity date must be within the trip period \n(${tripStartCal.toStringDate()} - ${tripEndCal.toStringDate()})"
                )
//                     "Activity date mustshowDateError = true
////                    dateErrorMessage =
////                        be within the trip period \n(${tripStartCal.toStringDate()} - ${tripEndCal.toStringDate()})"
            }
        }, year, month, day
    ).apply {
        // Restrict date picker to the trip period only
        val tripStartCal = toCalendar(currentTrip.startDate).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val tripEndCal = toCalendar(currentTrip.endDate).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        datePicker.minDate = tripStartCal.timeInMillis
        datePicker.maxDate = tripEndCal.timeInMillis
    }

    return datePicker
}