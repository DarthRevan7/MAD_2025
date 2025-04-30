package com.example.voyago.view


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import com.example.voyago.activities.*
import com.example.voyago.model.Trip
import com.example.voyago.viewmodel.TripListViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewActivity(navController: NavController, vm: TripListViewModel) {


    var isGroupActivityChecked by rememberSaveable { mutableStateOf(false) }
    var activityDescription by rememberSaveable { mutableStateOf("") }
    var activityDate by rememberSaveable { mutableStateOf("") }
    var selectedTime by rememberSaveable {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
        mutableStateOf(String.format(Locale.ITALY, "%02d:%02d %s", if (hour == 0) 12 else hour, minute, amPm))
    }
    var showDateError by rememberSaveable { mutableStateOf(false) }
    var dateErrorMessage by rememberSaveable { mutableStateOf("") }


    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(1)
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF3EDF7))
        ) {
            val listState = rememberLazyListState()

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize(.8f)
                    .fillMaxHeight(0.9f)
                    .align(Alignment.Center)
                    .background(
                        color = (Color(0xFFE6E0E9)),
                        shape = RoundedCornerShape(24.dp)
                    ),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }


                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(.8f)
                            .background(Color(0xFFD6D0D9))
                    ) {


                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 16.dp)
                        ) {
                            Text(text = "Group activity")

                            Spacer(modifier = Modifier.weight(1f))

                            Checkbox(
                                checked = isGroupActivityChecked,
                                onCheckedChange = { isGroupActivityChecked = it }
                            )
                        }
                    }
                }


                item {
                    val context = LocalContext.current
                    val calendar = Calendar.getInstance()
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)



                    val startDatePickerDialog = remember {
                        DatePickerDialog(
                            context,
                            { _: DatePicker, y: Int, m: Int, d: Int ->
                                activityDate = "$d/${m + 1}/$y"
                            }, year, month, day
                        )
                    }


                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                    ) {
                        Button(
                            onClick = { startDatePickerDialog.show() },
                            colors = ButtonDefaults.buttonColors(
                                contentColor = Color(0xFF6750A4),
                                containerColor = Color(0xFFD6D0D9)
                            )
                        ) {
                            if (activityDate.isNotEmpty()) {
                                Text("Date: $activityDate")
                            } else {
                                Text("Select date")
                            }
                        }


                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                        ) {
                            if (showDateError) {
                                Text(
                                    text = dateErrorMessage,
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }
                        }
                    }

                }

                item {
                    val context = LocalContext.current

                    val calendar = remember { Calendar.getInstance() }
//                    var selectedTime by rememberSaveable {
//                        val hour = calendar.get(Calendar.HOUR)
//                        val minute = calendar.get(Calendar.MINUTE)
//                        val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
//                        mutableStateOf(String.format(Locale.ITALY, "%02d:%02d %s", if (hour == 0) 12 else hour, minute, amPm))
//                    }

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
                                selectedTime = String.format(Locale.ITALY,"%02d:%02d %s", if (hour == 0) 12 else hour, minute, amPm)
                                showTimePicker.value = false
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                        ).show()
                    }

                    Button(
                        onClick = { showTimePicker.value = true },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.Black,
                            containerColor = Color(0xFFD6D0D9)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(text = "Select Time: $selectedTime")
                    }
                }


                item{
                    TextField(
                        value = activityDescription,
                        onValueChange = { activityDescription = it },
                        label = { Text("Activity description") },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                }





                item {
                    Spacer(modifier = Modifier.height(50.dp))
                }


                item {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                    ) {

                        Button(
                            onClick = {
                                navController.popBackStack()
                            },
                            modifier = Modifier
                                .width(140.dp)
                                .height(60.dp)
                                .padding(top = 16.dp)
                        ) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                val currentTrip = vm.currentTrip
                                val existingActivities = currentTrip?.activities?.values?.flatten()?.map { it.id } ?: listOf()
                                val newId = if (existingActivities.isNotEmpty()) existingActivities.max()!! + 1 else 1

                                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                val parsedDate = try {
                                    dateFormat.parse(activityDate)
                                } catch (e: Exception) {
                                    null
                                }


                                if (parsedDate == null) {
                                    showDateError = true
                                    dateErrorMessage = "Invalid date format. Please select a date."
                                    return@Button
                                }

                                val activityCalendar = Calendar.getInstance().apply {
                                    time = parsedDate
                                }

                                if (currentTrip != null && (activityCalendar.before(currentTrip.startDate) || activityCalendar.after(currentTrip.endDate))) {
                                    showDateError = true
                                    dateErrorMessage = "Activity date must be within the trip period (${dateFormat.format(currentTrip.startDate.time)} - ${dateFormat.format(currentTrip.endDate.time)})."
                                    return@Button
                                }


                                val newActivity = Trip.Activity(
                                    id = newId,
                                    date = activityCalendar,
                                    time = selectedTime,
                                    isGroupActivity = isGroupActivityChecked,
                                    description = activityDescription
                                )

                                vm.addActivityToSelectedTrip(newActivity)
                                navController.popBackStack()

                            },
                            modifier = Modifier
                                .width(140.dp)
                                .height(60.dp)
                                .padding(top = 16.dp)
                        ) {
                            Text("Add")
                        }

                    }

                }

            }
        }
    }
}






