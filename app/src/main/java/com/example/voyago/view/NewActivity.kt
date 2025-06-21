package com.example.voyago.view


import android.annotation.SuppressLint
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
import java.util.Calendar
import java.util.Locale
import com.example.voyago.viewmodel.TripViewModel
import com.google.firebase.Timestamp
import java.util.Date


// 修复1970年日期问题 - 关键修复点用注释标记

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewActivity(navController: NavController, vm: TripViewModel) {

    // 🔴 修复点1: 使用LaunchedEffect确保获取最新的Trip状态
    val selectedTrip by vm.selectedTrip

    // 🔴 修复点2: 添加调试日志监控Trip状态
    LaunchedEffect(selectedTrip.id) {
        Log.d("NewActivity", "=== Trip State Debug ===")
        Log.d("NewActivity", "Trip ID: ${selectedTrip.id}")
        Log.d("NewActivity", "Trip title: ${selectedTrip.title}")
        Log.d("NewActivity", "Start date seconds: ${selectedTrip.startDate.seconds}")
        Log.d("NewActivity", "Start date: ${selectedTrip.startDate.toDate()}")
        Log.d("NewActivity", "End date seconds: ${selectedTrip.endDate.seconds}")
        Log.d("NewActivity", "Activities: ${selectedTrip.activities}")
    }

    var isGroupActivityChecked by rememberSaveable { mutableStateOf(false) }

    var activityDescription by rememberSaveable { mutableStateOf("") }
    var descriptionTouched = remember { mutableStateOf(false) }
    val descriptionHasErrors by remember {
        derivedStateOf {
            descriptionTouched.value && (activityDescription.isBlank() || !activityDescription.any { it.isLetter() })
        }
    }

    // 🔴 修复点3: 使用derivedStateOf确保日期始终基于最新的Trip状态
    val defaultDate by remember {
        derivedStateOf {
            try {
                Log.d("NewActivity", "Computing default date...")
                Log.d("NewActivity", "Trip start date seconds: ${selectedTrip.startDate.seconds}")

                when {
                    selectedTrip.startDate.seconds > 0 -> {
                        val formatted = toCalendar(selectedTrip.startDate).toStringDate()
                        Log.d("NewActivity", "Using trip start date: $formatted")
                        formatted
                    }
                    else -> {
                        val currentDate = Calendar.getInstance().toStringDate()
                        Log.d("NewActivity", "Using current date: $currentDate")
                        currentDate
                    }
                }
            } catch (e: Exception) {
                Log.e("NewActivity", "Error computing default date", e)
                Calendar.getInstance().toStringDate()
            }
        }
    }

    // 🔴 修复点4: 改变日期状态管理方式
    var activityDate by rememberSaveable { mutableStateOf("") }

    // 🔴 修复点5: 当defaultDate改变时更新activityDate（只在activityDate为空时）
    LaunchedEffect(defaultDate) {
        if (activityDate.isEmpty()) {
            activityDate = defaultDate
            Log.d("NewActivity", "Set activity date to: $activityDate")
        }
    }

    var selectedTime by rememberSaveable { mutableStateOf("09:00 AM") }

    var showDateError by rememberSaveable { mutableStateOf(false) }
    var dateErrorMessage by rememberSaveable { mutableStateOf("") }

    // 🔴 修复点6: 添加Trip有效性检查，防止无效数据
    if (selectedTrip.id == 0 || selectedTrip.startDate.seconds <= 0) {
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
            Button(onClick = { navController.popBackStack() }) {
                Text("Go Back")
            }
        }
        return
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

            // Group Activity selection
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

            // Select Date Button
            item {
                val context = LocalContext.current

                // 🔴 修复点7: 使用当前Trip状态计算初始Calendar，添加remember依赖
                val initialCalendar = remember(selectedTrip.startDate.seconds) {
                    Calendar.getInstance().apply {
                        try {
                            if (selectedTrip.startDate.seconds > 0) {
                                time = selectedTrip.startDate.toDate()
                                Log.d("NewActivity", "Initial calendar set to trip start: $time")
                            } else {
                                time = Date()
                                Log.d("NewActivity", "Initial calendar set to current: $time")
                            }
                        } catch (e: Exception) {
                            Log.e("NewActivity", "Error setting initial calendar", e)
                            time = Date()
                        }
                    }
                }

                val year = initialCalendar.get(Calendar.YEAR)
                val month = initialCalendar.get(Calendar.MONTH)
                val day = initialCalendar.get(Calendar.DAY_OF_MONTH)

                // 🔴 修复点8: 添加remember依赖确保DatePickerDialog基于最新Trip状态
                val startDatePickerDialog = remember(selectedTrip.id) {
                    DatePickerDialog(
                        context,
                        { _: DatePicker, y: Int, m: Int, d: Int ->
                            try {
                                val pickedCalendar = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, y)
                                    set(Calendar.MONTH, m)
                                    set(Calendar.DAY_OF_MONTH, d)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }

                                // 🔴 修复点9: 使用最新的Trip状态验证日期
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

                                Log.d("NewActivity", "Validating date: ${pickedCalendar.time}")
                                Log.d("NewActivity", "Trip range: ${tripStartCal.time} to ${tripEndCal.time}")

                                val isValid = !pickedCalendar.before(tripStartCal) && !pickedCalendar.after(tripEndCal)

                                if (isValid) {
                                    activityDate = "$d/${m + 1}/$y"
                                    showDateError = false
                                    dateErrorMessage = ""
                                    Log.d("NewActivity", "Date selected: $activityDate")
                                } else {
                                    showDateError = true
                                    dateErrorMessage = "Activity date must be within the trip period \n(${tripStartCal.toStringDate()} - ${tripEndCal.toStringDate()})"
                                    Log.w("NewActivity", "Invalid date selected: $activityDate")
                                }
                            } catch (e: Exception) {
                                Log.e("NewActivity", "Error handling date selection", e)
                                showDateError = true
                                dateErrorMessage = "Error selecting date. Please try again."
                            }
                        }, year, month, day
                    ).apply {
                        try {
                            // 🔴 修复点10: 设置正确的日期限制
                            if (selectedTrip.startDate.seconds > 0 && selectedTrip.endDate.seconds > 0) {
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

                                datePicker.minDate = tripStartCal.timeInMillis
                                datePicker.maxDate = tripEndCal.timeInMillis

                                Log.d("NewActivity", "Date picker limits set: ${tripStartCal.time} to ${tripEndCal.time}")
                            }
                        } catch (e: Exception) {
                            Log.e("NewActivity", "Error setting date picker limits", e)
                        }
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
                    Text("Selected Date: $activityDate")
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

            // Select Time Button
            item {
                val context = LocalContext.current
                val showTimePicker = remember { mutableStateOf(false) }

                if (showTimePicker.value) {
                    // 🔴 修复点11: 增强时间解析错误处理
                    val (currentHour, currentMinute) = remember {
                        try {
                            val cal = Calendar.getInstance()
                            cal.time = parseAndSetTime(cal, selectedTime).time
                            Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                        } catch (e: Exception) {
                            Log.e("NewActivity", "Error parsing time", e)
                            Pair(9, 0) // 默认9:00
                        }
                    }

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
                                Locale.US,
                                "%02d:%02d %s",
                                if (hour == 0) 12 else hour,
                                minute,
                                amPm
                            )
                            showTimePicker.value = false
                            Log.d("NewActivity", "Time selected: $selectedTime")
                        },
                        currentHour,
                        currentMinute,
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

            // Activity Description
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

            // Cancel Button and Add Button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Cancel Button
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Add Button
                    Button(
                        onClick = {
                            // 🔴 修复点12: 添加详细的调试日志
                            Log.d("NewActivity", "=== Add Button Clicked ===")
                            Log.d("NewActivity", "Activity date: $activityDate")
                            Log.d("NewActivity", "Selected time: $selectedTime")
                            Log.d("NewActivity", "Trip ID: ${selectedTrip.id}")
                            Log.d("NewActivity", "Trip start: ${selectedTrip.startDate.toDate()}")
                            Log.d("NewActivity", "Trip end: ${selectedTrip.endDate.toDate()}")

                            // 🔴 修复点13: 增强日期解析和验证
                            val parsedDate = try {
                                activityDate.toCalendar()
                            } catch (e: Exception) {
                                Log.e("NewActivity", "Date parsing failed: $activityDate", e)
                                null
                            }

                            if (parsedDate == null) {
                                showDateError = true
                                dateErrorMessage = "Invalid date format. Please select a date."
                                return@Button
                            }

                            val activityCalendar = parsedDate
//                            Calendar.getInstance().apply {
//                                time = parsedDate.time
//                                set(Calendar.HOUR_OF_DAY, 0)
//                                set(Calendar.MINUTE, 0)
//                                set(Calendar.SECOND, 0)
//                                set(Calendar.MILLISECOND, 0)
//                            }

                            Log.d("NewActivity", "Activity calendar: ${activityCalendar.time}")

                            // 🔴 修复点14: 使用当前Trip状态进行最终验证
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

                            val isDateValid = !activityCalendar.before(tripStartCal) && !activityCalendar.after(tripEndCal)

                            Log.d("NewActivity", "Date validation result: $isDateValid")

                            if (!isDateValid) {
                                showDateError = true
                                dateErrorMessage = "Activity date must be within the trip period"
                                return@Button
                            }

                            descriptionTouched.value = true

                            if (!showDateError && !descriptionHasErrors) {
                                // 🔴 修复点15: 改进活动ID生成逻辑
                                val existingIds = selectedTrip.activities.values.flatten().map { it.id }.toSet()
                                var newActivityId = if (existingIds.isEmpty()) 1 else (existingIds.maxOrNull() ?: 0) + 1
                                while (existingIds.contains(newActivityId)) {
                                    newActivityId++
                                }

                                val newActivity = Trip.Activity(
                                    id = newActivityId,
                                    date = Timestamp(activityCalendar.time),
                                    time = selectedTime,
                                    isGroupActivity = isGroupActivityChecked,
                                    description = activityDescription
                                )

                                // 🔴 修复点16: 添加详细的活动创建日志
                                Log.d("NewActivity", "Creating activity: $newActivity")
                                Log.d("NewActivity", "Activity date timestamp: ${newActivity.date.seconds}")

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


