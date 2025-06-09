package com.example.voyago.view


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
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
import com.example.voyago.viewmodel.TripViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivity(navController: NavController, vm: TripViewModel, activityId: Int) {

    //  修复：使用更可靠的方式获取当前行程
    val currentTrip = when (vm.userAction) {
        TripViewModel.UserAction.EDIT_ACTIVITY -> {

            if (vm.editTrip.isValid()) vm.editTrip else vm.selectedTrip.value
        }
        TripViewModel.UserAction.CREATE_TRIP -> {

            if (vm.newTrip.isValid()) vm.newTrip else vm.selectedTrip.value
        }
        else -> vm.selectedTrip.value
    }

    //  添加调试日志
    Log.d("EditActivity", "=== Debug Info ===")
    Log.d("EditActivity", "User Action: ${vm.userAction}")
    Log.d("EditActivity", "Activity ID to find: $activityId")
    Log.d("EditActivity", "Current trip ID: ${currentTrip.id}")
    Log.d("EditActivity", "Current trip title: ${currentTrip.title}")
    Log.d("EditActivity", "Activities count: ${currentTrip.activities.values.flatten().size}")
    val activityToEdit = currentTrip.activities.values.flatten().find { it.id == activityId }

    if (activityToEdit == null) {
        Text("Activity not found.")
        return
    }

    var isGroupActivityChecked by rememberSaveable { mutableStateOf(activityToEdit.isGroupActivity) }

    var activityDescription by rememberSaveable { mutableStateOf(activityToEdit.description) }
    var descriptionTouched = remember {mutableStateOf(false)}
    val descriptionHasErrors by remember {
        derivedStateOf {
            descriptionTouched.value && (activityDescription.isBlank() || !activityDescription.any { it.isLetter() })
        }
    }


    var activityDate by rememberSaveable {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(toCalendar(activityToEdit.date).time))
    }
    var selectedTime by rememberSaveable { mutableStateOf(activityToEdit.time) }

    var showDateError by rememberSaveable { mutableStateOf(false) }
    var dateErrorMessage by rememberSaveable { mutableStateOf("") }

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

            //Group Activity selection
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
                val calendar = activityToEdit.date
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
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                            }

                            // 检查选择的日期是否在行程日期范围内
                            val tripStartCal = toCalendar(currentTrip.startDate) // 行程开始日期
                            val tripEndCal = toCalendar(currentTrip.endDate) // 行程结束日期

                            // 将行程开始和结束日期的时间部分设置为0，以便正确比较日期
                            tripStartCal.set(Calendar.HOUR_OF_DAY, 0)
                            tripStartCal.set(Calendar.MINUTE, 0)
                            tripStartCal.set(Calendar.SECOND, 0)
                            tripStartCal.set(Calendar.MILLISECOND, 0)

                            tripEndCal.set(Calendar.HOUR_OF_DAY, 23)
                            tripEndCal.set(Calendar.MINUTE, 59)
                            tripEndCal.set(Calendar.SECOND, 59)
                            tripEndCal.set(Calendar.MILLISECOND, 999)

                            val isValid = !(pickedCalendar.before(currentTrip.startDate) || pickedCalendar.after(currentTrip.endDate))

                            activityDate = "$d/${m + 1}/$y"

                            if (isValid) {
                                showDateError = false
                                dateErrorMessage = ""
                            } else {
                                showDateError = true
                                dateErrorMessage = "Activity date must be within the trip period \n(${dateFormat.format(toCalendar(currentTrip.startDate).time)} - ${dateFormat.format(toCalendar(currentTrip.endDate).time)})"
                            }
                        }, year, month, day
                    ).apply {
                        // 设置日期选择器的最小和最大日期限制
                        val tripStartCal = toCalendar(currentTrip.startDate) // 获取行程开始日期
                        val tripEndCal = toCalendar(currentTrip.endDate) // 获取行程结束日期

                        // 设置时间为一天的开始和结束，确保整天都可以选择
                        tripStartCal.set(Calendar.HOUR_OF_DAY, 0)
                        tripStartCal.set(Calendar.MINUTE, 0)
                        tripStartCal.set(Calendar.SECOND, 0)
                        tripStartCal.set(Calendar.MILLISECOND, 0)

                        tripEndCal.set(Calendar.HOUR_OF_DAY, 23)
                        tripEndCal.set(Calendar.MINUTE, 59)
                        tripEndCal.set(Calendar.SECOND, 59)
                        tripEndCal.set(Calendar.MILLISECOND, 999)

                        datePicker.minDate = tripStartCal.timeInMillis // 设置最小可选日期
                        datePicker.maxDate = tripEndCal.timeInMillis // 设置最大可选日期
                    }
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
                    Text(if (activityDate.isNotEmpty()) "Selected Date: $activityDate" else "Select date")
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
                val calendar = remember {
                    val cal = Calendar.getInstance()
                    try {
                        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        cal.time = sdf.parse(selectedTime) ?: cal.time
                    } catch (e: Exception) {
                        // Fallback to default time
                    }
                    cal
                }
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
                                }
                            }

                            descriptionTouched.value = true

                            if (!showDateError && !descriptionHasErrors) {

                                val updatedActivity = Trip.Activity(
                                    id = activityId, // preserve the original ID
                                    date = Timestamp(activityCalendar.time),
                                    time = selectedTime,
                                    isGroupActivity = isGroupActivityChecked,
                                    description = activityDescription
                                )

                                vm.editActivity(activityId, updatedActivity)
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






