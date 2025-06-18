package com.example.voyago.view


import android.util.Log
import com.example.voyago.view.KEY_DATE_FORMAT
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voyago.model.Trip
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import com.example.voyago.model.deepCopy
import com.example.voyago.model.isTimestampLong
import com.example.voyago.model.stringToCalendar
import com.example.voyago.model.timestampToCalendar
import com.example.voyago.viewmodel.TripViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesList(navController: NavController, vm: TripViewModel) {

    val selectedTrip by vm.selectedTrip

    var showIncompleteDialog by rememberSaveable { mutableStateOf(false) }
    // 保存进入页面时的状态
    val entryTripState = remember {
        when (vm.userAction) {
            TripViewModel.UserAction.EDIT_TRIP -> vm.editTrip.deepCopy()
            TripViewModel.UserAction.CREATE_TRIP -> vm.newTrip.deepCopy()
            else -> null
        }
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }

            //Activity List
            item {
                ActivitiesListContent(selectedTrip, vm, navController)
            }


            item {
                Spacer(modifier = Modifier.height(40.dp))
            }

            //New Activity Button
            item {
                Button(
                    onClick = {
                        navController.navigate("new_activity")
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

            item {
                Spacer(modifier = Modifier.height(50.dp))
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {

                    if (showIncompleteDialog) {
                        Text(
                            text = "Each day of the trip must have at least one activity.",
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(top = 8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        //Back Button
                        Button(
                            onClick = {
                                entryTripState?.let {
                                    when (vm.userAction) {
                                        TripViewModel.UserAction.EDIT_TRIP   -> vm.editTrip  = it
                                        TripViewModel.UserAction.CREATE_TRIP -> vm.newTrip   = it
                                        else                                 -> {}
                                    }
                                    vm.setSelectedTrip(it)   // 同步列表
                                }
                                navController.popBackStack()
                            },

                            modifier = Modifier
                                .width(160.dp)
                                .height(60.dp)
                                .padding(top = 16.dp)
                        ) {
                            Text("Back")
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        //Finish Button
                        //Finish Button
                        Button(
                            onClick = {
                                if (selectedTrip.hasActivityForEachDay()) {

                                    if(vm.userAction == TripViewModel.UserAction.CREATE_TRIP) {
                                        // 对于CREATE_TRIP，Trip已经包含了正确的photo路径
                                        val updatedTrip = vm.newTrip.copy(
                                            activities = vm.newTrip.activities,
                                            published = false,
                                            isDraft = false
                                        )

                                        val creatorJoinRequest = Trip.JoinRequest(
                                            userId = updatedTrip.creatorId,
                                            requestedSpots = 1,
                                            unregisteredParticipants = emptyList(),
                                            registeredParticipants = listOf(updatedTrip.creatorId)
                                        )

                                        updatedTrip.participants = mapOf(updatedTrip.creatorId.toString() to creatorJoinRequest)

                                        vm.editTrip(updatedTrip) { success ->
                                            if (success) {
                                                navController.navigate("my_trips_main") {
                                                    popUpTo("my_trips_main") {
                                                        inclusive = false
                                                    }
                                                    launchSingleTop = true
                                                }
                                            }
                                        }

                                    } else if(vm.userAction == TripViewModel.UserAction.EDIT_TRIP
                                        ||
                                        vm.userAction == TripViewModel.UserAction.EDIT_ACTIVITY
                                    ){
                                        val updatedTrip = vm.editTrip.copy(
                                            activities = vm.editTrip.activities,
                                            isDraft = false
                                        )

                                        vm.editTrip(updatedTrip) { success ->
                                            if (success) {
                                                navController.navigate("my_trips_main") {
                                                    popUpTo("my_trips_main") {
                                                        inclusive = false
                                                    }
                                                    launchSingleTop = true
                                                }
                                            }
                                        }
                                    }

                                } else {
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

// 在 ActivitiesListContent 函数中修改日期计算逻辑
// 🔴 修复天数索引计算问题

@Composable
fun ActivitiesListContent(trip: Trip?, vm: TripViewModel, navController: NavController){
    if (trip == null) {
        Text("No trip selected", modifier = Modifier.padding(16.dp))
        return
    }

        val dateFormat = SimpleDateFormat(KEY_DATE_FORMAT, Locale.US)
       val sortedDays = trip.activities.keys.sortedBy { key ->
               if (isTimestampLong(key)) key.toLong()
                else parseActivityDate(key, dateFormat).timeInMillis
            }
    val hasNoActivities = trip.activities.values.all { it.isEmpty() }
    var activityToDelete by rememberSaveable { mutableStateOf<Trip.Activity?>(null) }

    val selectedTrip by vm.selectedTrip
    var showIncompleteDialog by rememberSaveable { mutableStateOf(false) }

    // 保存进入页面时的状态
    val entryState = remember {
        when (vm.userAction) {
            TripViewModel.UserAction.EDIT_TRIP -> {
                vm.editTrip.copy(
                    activities = vm.editTrip.activities.toMap()
                )
            }
            TripViewModel.UserAction.CREATE_TRIP -> {
                vm.newTrip.copy(
                    activities = vm.newTrip.activities.toMap()
                )
            }
            else -> null
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (hasNoActivities) {
            Text(
                text = "No activities for trip to ${trip.destination}.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        } else {
            sortedDays.forEach { day ->
                Log.d("L1", "Activity List")

                // 修复后的日期计算逻辑
                val activityCalendar = if (isTimestampLong(day)) {
                    Log.d("L1", "Day is a timestamp: $day")
                    timestampToCalendar(day)
                } else {
                    Log.d("L1", "Day is a string: $day")
                    stringToCalendar(day)
                }

                Log.d("L1", "Activity calendar: $activityCalendar")
                Log.d("L1", "Trip start calendar: ${trip.startDateAsCalendar()}")

                // 🔴 修复：使用更新后的行程开始日期来计算天数索引
                val currentTripStartCalendar = trip.startDateAsCalendar()
                val dayIndex = calculateDayIndex(activityCalendar, currentTripStartCalendar)

                Log.d("L1", "Calculated day index: $dayIndex")

                val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
                val activitiesForDay = (trip.activities[day] ?: emptyList())
                    .sortedBy { LocalTime.parse(it.time, formatter) }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Day $dayIndex",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF555555)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    activitiesForDay.forEach { activity ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth()
                        ) {
                            //Edit Activity Button
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

                            //Print Activity information
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${activity.time} - ${activity.description}" +
                                            if (activity.isGroupActivity) " (group activity)" else "",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            //Delete Activity Button
                            OutlinedButton(
                                onClick = {
                                    Log.d("DeleteButton", "Delete button clicked for activity: ${activity.id}")
                                    activityToDelete = activity
                                },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Delete", color = Color.Red)
                            }
                        }
                    }

                    activityToDelete?.let { activity ->
                        AlertDialog(
                            onDismissRequest = {
                                Log.d("DeleteDialog", "Dialog dismissed")
                                activityToDelete = null
                            },
                            title = { Text("Delete Activity") },
                            text = { Text("Are you sure you want to delete this activity?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    Log.d("DeleteDialog", "Confirming delete for activity: ${activity.id}")
                                    vm.deleteActivity(activity)
                                    activityToDelete = null
                                    Log.d("DeleteDialog", "Delete operation completed")
                                }) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    Log.d("DeleteDialog", "Delete cancelled")
                                    activityToDelete = null
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



// 🔴 额外修复：确保智能重新分配时日期格式正确
private fun reallocateWithShorterInterval(
    originalActivities: Map<String, List<Trip.Activity>>,
    oldStartCal: Calendar,
    newStartCal: Calendar,
    newEndCal: Calendar,
    updatedActivities: MutableMap<String, List<Trip.Activity>>,
    dateFormat: SimpleDateFormat
) {
    val newStartDate = Calendar.getInstance().apply {
        timeInMillis = newStartCal.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val newEndDate = Calendar.getInstance().apply {
        timeInMillis = newEndCal.timeInMillis
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    val lastDayKey = dateFormat.format(newEndDate.time)
    val activitiesToLastDay = mutableListOf<Trip.Activity>()

    originalActivities.forEach { (oldDateKey, activities) ->
        try {
            val oldActivityDate = parseActivityDate(oldDateKey, dateFormat)

            when {
                // 活动在新范围内 - 计算相对位置并重新分配
                oldActivityDate.timeInMillis >= newStartDate.timeInMillis &&
                        oldActivityDate.timeInMillis <= newEndDate.timeInMillis -> {

                    val relativeDay = calculateDaysBetween(oldStartCal, oldActivityDate) - 1
                    val newActivityDate = Calendar.getInstance().apply {
                        timeInMillis = newStartCal.timeInMillis
                        add(Calendar.DAY_OF_MONTH, minOf(relativeDay, calculateDaysBetween(newStartCal, newEndCal) - 1))
                    }

                    val newDateKey = dateFormat.format(newActivityDate.time)
                    val updatedActivityList = activities.map { activity ->
                        // 🔴 确保活动的日期也正确更新
                        activity.copy(date = Timestamp(newActivityDate.time))
                    }

                    updatedActivities[newDateKey] = (updatedActivities[newDateKey] ?: emptyList()) + updatedActivityList
                    Log.d("SmartReallocation", "Kept activities from $oldDateKey at $newDateKey")
                }

                // 所有超出范围的活动都分配到最后一天
                else -> {
                    val updatedActivityList = activities.map { activity ->
                        // 🔴 确保活动的日期也正确更新为最后一天
                        activity.copy(date = Timestamp(newEndDate.time))
                    }
                    activitiesToLastDay.addAll(updatedActivityList)
                    Log.d("SmartReallocation", "Moving activities from $oldDateKey (outside range) to last day")
                }
            }

        } catch (e: Exception) {
            Log.e("SmartReallocation", "Error processing date $oldDateKey", e)
            // 解析失败的活动也分配到最后一天
            val updatedActivityList = activities.map { activity ->
                activity.copy(date = Timestamp(newEndDate.time))
            }
            activitiesToLastDay.addAll(updatedActivityList)
        }
    }

    // 将所有溢出的活动添加到最后一天
    if (activitiesToLastDay.isNotEmpty()) {
        updatedActivities[lastDayKey] = (updatedActivities[lastDayKey] ?: emptyList()) + activitiesToLastDay
        Log.d("SmartReallocation", "Added ${activitiesToLastDay.size} overflow activities to last day")
    }
}

// 正确计算天数索引的辅助函数
fun calculateDayIndex(activityCalendar: Calendar, tripStartCalendar: Calendar): Int {
    // 标准化日期，去除时间部分以确保准确比较
    val activityDate = Calendar.getInstance().apply {
        timeInMillis = activityCalendar.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0) // 设置小时为0
        set(Calendar.MINUTE, 0) // 设置分钟为0
        set(Calendar.SECOND, 0) // 设置秒为0
        set(Calendar.MILLISECOND, 0) // 设置毫秒为0
    }

    val tripStartDate = Calendar.getInstance().apply {
        timeInMillis = tripStartCalendar.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0) // 设置小时为0
        set(Calendar.MINUTE, 0) // 设置分钟为0
        set(Calendar.SECOND, 0) // 设置秒为0
        set(Calendar.MILLISECOND, 0) // 设置毫秒为0
    }

    // 计算天数差异
    val diffInMillis = activityDate.timeInMillis - tripStartDate.timeInMillis
    val diffInDays = diffInMillis / (24 * 60 * 60 * 1000) // 转换为天数

    Log.d("DayCalculation", "Activity date: ${activityDate.time}")
    Log.d("DayCalculation", "Trip start date: ${tripStartDate.time}")
    Log.d("DayCalculation", "Difference in millis: $diffInMillis")
    Log.d("DayCalculation", "Difference in days: $diffInDays")

    // 返回从第1天开始的索引
    return (diffInDays + 1).toInt()
}

