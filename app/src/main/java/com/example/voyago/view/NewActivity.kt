package com.example.voyago.view


import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.example.voyago.model.toCalendar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.voyago.viewmodel.TripViewModel
import com.google.firebase.Timestamp


@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewActivity(navController: NavController, vm: TripViewModel) {
    var currentTrip = Trip()

    if(vm.userAction == TripViewModel.UserAction.CREATE_TRIP) {
        currentTrip = vm.newTrip
    } else if(vm.userAction == TripViewModel.UserAction.EDIT_TRIP) {
        currentTrip = vm.editTrip
    }

    var isGroupActivityChecked by rememberSaveable { mutableStateOf(false) }

    var activityDescription by rememberSaveable { mutableStateOf("") }
    var descriptionTouched = remember {mutableStateOf(false)}
    val descriptionHasErrors by remember {
        derivedStateOf {
            descriptionTouched.value && (activityDescription.isBlank() || !activityDescription.any { it.isLetter() })
        }
    }


    var activityDate by rememberSaveable { mutableStateOf("") }
    var selectedTime by rememberSaveable {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
        mutableStateOf(String.format(Locale.US, "%02d:%02d %s", if (hour == 0) 12 else hour, minute, amPm))
    }
    var showDateError by rememberSaveable { mutableStateOf(false) }
    var dateErrorMessage by rememberSaveable { mutableStateOf("") }

    fun Calendar.stripTime(): Calendar {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        return this
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3EDF7))
    ) {
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
            //Group Activity Check
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

            //Select Date Button
            item {
                val context = LocalContext.current
                val calendar = currentTrip.startDate
                val year = toCalendar(calendar).get(Calendar.YEAR)
                val month = toCalendar(calendar).get(Calendar.MONTH)
                val day = toCalendar(calendar).get(Calendar.DAY_OF_MONTH)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())


                val startDatePickerDialog = remember {
                    DatePickerDialog(
                        context,
                        { _: DatePicker, y: Int, m: Int, d: Int ->
                            val pickedCalendar = Calendar.getInstance().apply {
                                set(Calendar.YEAR, y)
                                set(Calendar.MONTH, m)
                                set(Calendar.DAY_OF_MONTH, d)
                            }.stripTime()

                            val isValid = !(pickedCalendar.before(toCalendar(currentTrip.startDate).stripTime()) ||
                                    pickedCalendar.after(toCalendar(currentTrip.endDate).stripTime()))

                            activityDate = "$d/${m + 1}/$y"

                            if (isValid) {
                                showDateError = false
                                dateErrorMessage = ""
                            } else {
                                showDateError = true
                                dateErrorMessage = "Activity date must be within the trip period \n(${dateFormat.format(toCalendar(currentTrip.startDate).time)} - ${dateFormat.format(toCalendar(currentTrip.endDate).time)})"
                            }
                        }, year, month, day
                    )
                }

                Button(
                    onClick = { startDatePickerDialog.show() },
                    modifier = Modifier
                        .fillMaxWidth(.8f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.Black,
                        containerColor = Color(0xFFD6D0D9)
                    )
                ) {
                    Text(if (activityDate.isNotEmpty()) "Date: $activityDate" else "Select Date of the Activity")
                }

                if (showDateError) {
                    Text(
                        text = dateErrorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            //Select Time Button
            item {
                val context = LocalContext.current
                val calendar = remember { Calendar.getInstance() }
                val showTimePicker = remember { mutableStateOf(false) }

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

            //Activity Description
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

            //Cancel Button and Add Button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    //Cancel Button
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    //Add Button
                    Button(
                        onClick = {
                            val existingActivities = currentTrip.activities.values.flatten().map { it.id }
                            val newId = if (existingActivities.isNotEmpty()) existingActivities.max() + 1 else 1

                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val parsedDate = try {
                                dateFormat.parse(activityDate)
                            } catch (e: Exception) {
                                null
                            }


                            if (parsedDate == null) {
                                showDateError = true
                                dateErrorMessage = "Invalid date format. Please select a date."
                            }

                            val activityCalendar = Calendar.getInstance().apply {
                                if (parsedDate != null) {
                                    time = parsedDate
                                    stripTime()
                                }
                            }

                            descriptionTouched.value = true

                            if (!showDateError && !descriptionHasErrors) {
                                // Directly use the calendar's time as timestamp
                                val timestampTrue = Timestamp(activityCalendar.time)

                                val newActivity = Trip.Activity(
                                    id = newId,
                                    date = timestampTrue,
                                    time = selectedTime,
                                    isGroupActivity = isGroupActivityChecked,
                                    description = activityDescription
                                )

                                vm.addActivityToTrip(newActivity)
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






