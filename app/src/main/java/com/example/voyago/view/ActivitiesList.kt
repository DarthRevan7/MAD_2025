package com.example.voyago.view

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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.voyago.toCalendar
import com.example.voyago.toStringDate
import com.example.voyago.viewmodel.TripViewModel
import com.google.firebase.firestore.FirebaseFirestore
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
    var showIncompleteDialog by rememberSaveable { mutableStateOf(false) }

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
                                // 使用取消编辑方法恢复原始状态
                                when (vm.userAction) {
                                    TripViewModel.UserAction.EDIT_TRIP -> {
                                        vm.cancelEditing()
                                    }
                                    TripViewModel.UserAction.CREATE_TRIP -> {
                                        // 对于新建行程，恢复到初始状态
                                        entryTripState?.let {
                                            vm.newTrip = it
                                            vm.setSelectedTrip(it)
                                        }
                                    }
                                    else -> {}
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

                        // Spacer to push finish button to the right
                        Spacer(modifier = Modifier.weight(1f))

                        // Finish button to save or update the trip
                        Button(
                            onClick = {
                                // 首先验证是否所有天都有至少一个活动
                                if (selectedTrip.hasActivityForEachDay()) {
                                    // 如果用户正在创建新行程
                                    if (vm.userAction == TripViewModel.UserAction.CREATE_TRIP) {
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

                                        updatedTrip.participants =
                                            mapOf(updatedTrip.creatorId.toString() to creatorJoinRequest)

                                        vm.editTripInDatabase(updatedTrip) { success ->
                                            if (success) {
                                                navController.navigate("my_trips_main") {
                                                    popUpTo("my_trips_main") { inclusive = false }
                                                    launchSingleTop = true
                                                }
                                            }
                                        }

                                    } else if (vm.userAction == TripViewModel.UserAction.EDIT_TRIP
                                        || vm.userAction == TripViewModel.UserAction.EDIT_ACTIVITY
                                    ) {
                                        // 使用完成编辑方法，正式保存并移除 draft 标记
                                        vm.finishEditing { success ->
                                            if (success) {
                                                navController.navigate("my_trips_main") {
                                                    popUpTo("my_trips_main") { inclusive = false }
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

// Function that showcase the activities list content with fixed date-based grouping
@Composable
fun ActivitiesListContent(trip: Trip?, vm: TripViewModel, navController: NavController) {
    LaunchedEffect(trip?.id) {
        debugTripData(trip, vm)
    }

    // Log function entry

    Log.d("ActivitiesListContent", "Function entered")
    Log.d("ActivitiesListContent", "Trip: ${trip?.let { "ID=${it.id}, Destination=${it.destination}" } ?: "null"}")

    if (trip == null) {
        Log.w("ActivitiesListContent", "No trip provided, showing 'No trip selected' message")
        Text("No trip selected", modifier = Modifier.padding(16.dp))
        return
    }

    // Generate complete date range from trip start to end date
    val tripStartCal = trip.startDateAsCalendar()
    val tripEndCal = trip.endDateAsCalendar()
    Log.d("ActivitiesListContent", "Trip dates - Start: ${tripStartCal.time}, End: ${tripEndCal.time}")
    Log.d("ActivitiesListContent", "Trip start date string: ${trip.startDate}")
    Log.d("ActivitiesListContent", "Trip end date string: ${trip.endDate}")

    val dateRange = generateDateRange(tripStartCal, tripEndCal)
    Log.d("ActivitiesListContent", "Generated date range: ${dateRange.size} days - ${dateRange.joinToString()}")
    Log.w("ActivitiesListContent", "ISSUE CHECK - Date range vs Activity dates:")
    Log.w("ActivitiesListContent", "  Date range keys: ${dateRange.joinToString()}")
    Log.w("ActivitiesListContent", "  Activity date keys: ${trip.activities.keys.joinToString()}")

    // State for delete confirmation dialog
    var activityToDelete by rememberSaveable { mutableStateOf<Trip.Activity?>(null) }

    // Log activities data
    Log.d("ActivitiesListContent", "Total activity keys: ${trip.activities.keys.size}")
    trip.activities.forEach { (date, activities) ->
        Log.d("ActivitiesListContent", "Date $date has ${activities.size} activities")
        activities.forEachIndexed { index, activity ->
            Log.v("ActivitiesListContent", "  Activity $index: ID=${activity.id}, Time=${activity.time}, Desc=${activity.description}")
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Check if there are any activities
        val hasNoActivities = trip.activities.values.all { it.isEmpty() }
        Log.d("ActivitiesListContent", "Has no activities: $hasNoActivities")

        if (hasNoActivities) {
            Log.i("ActivitiesListContent", "Displaying 'no activities' message")
            Text(
                text = "No activities for trip to ${trip.destination}.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        } else {
            Log.i("ActivitiesListContent", "Displaying activities for ${dateRange.size} days")

            // Iterate through each day in the date range and display activities
            dateRange.forEachIndexed { index, dateKey ->
                val dayIndex = index + 1
                Log.v("ActivitiesListContent", "Processing Day $dayIndex with dateKey: $dateKey")

                // Find all activities for this specific date
                val activitiesForDay = findActivitiesForDate(dateKey, trip.activities)
                Log.d("ActivitiesListContent", "Day $dayIndex has ${activitiesForDay.size} activities")

                // DEBUG: Check if dateKey matches any activity keys
                val matchingActivityKeys = trip.activities.keys.filter { it.contains(dateKey) || dateKey.contains(it) }
                if (matchingActivityKeys.isNotEmpty()) {
                    Log.w("ActivitiesListContent", "  Potential matches for $dateKey: $matchingActivityKeys")
                } else {
                    Log.w("ActivitiesListContent", "  No matches found for dateKey: $dateKey")
                    Log.w("ActivitiesListContent", "  Available activity keys: ${trip.activities.keys.take(3).joinToString()}")
                }

                // Only display the day if it has activities
                if (activitiesForDay.isNotEmpty()) {
                    Log.d("ActivitiesListContent", "Rendering Day $dayIndex with activities")

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
                            Log.v("ActivitiesListContent", "Rendering activity $activityIndex for Day $dayIndex: ${activity.description}")

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
                                            Log.d("ActivitiesListContent", "Edit clicked for activity ID: ${activity.id}")
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
                                        Log.d("ActivitiesListContent", "Delete button clicked for activity ID: ${activity.id}")
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
                    Log.v("ActivitiesListContent", "Day $dayIndex has no activities, skipping render")
                }
            }
        }

        // Delete confirmation dialog
        activityToDelete?.let { activity ->
            Log.d("ActivitiesListContent", "Showing delete confirmation dialog for activity: ${activity.description}")

            AlertDialog(
                onDismissRequest = {
                    Log.d("ActivitiesListContent", "Delete dialog dismissed")
                    activityToDelete = null
                },
                title = { Text("Delete Activity") },
                text = { Text("Are you sure you want to delete this activity?") },
                confirmButton = {
                    TextButton(onClick = {
                        Log.i("ActivitiesListContent", "Confirming deletion of activity ID: ${activity.id}")
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

    Log.d("ActivitiesListContent", "Function completed successfully")
}

/*
这是一个私有函数，输入两个 Calendar 类型的日期：startCal（开始日期）和 endCal（结束日期），输出是一个 List<String>，每个字符串表示一个日期
用来存储所有的日期字符串
example:
startCal = 2025年6月20日
endCal = 2025年6月23日
["2025-06-20", "2025-06-21", "2025-06-22", "2025-06-23"]
 */
// Helper function: Generate date range from start to end date
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
        // 🔥 修改：使用 DD/MM/YYYY 格式
        val dateString = String.format("%d/%d/%d",
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

// Helper function: Find all activities for a specific date
/*
根据某个目标日期（targetDateKey）从活动列表中筛选出这一天的所有活动，并按时间排序返回
目标日期字符串，比如 "2025-06-23"
活动字典，键是日期字符串，值是该日期的活动列表
2025-06-24  key
08:00 - 酒店早餐  value
09:30 - 客户会议
12:15 - 午餐
 */

// Helper function: Parse activity date from string key
private fun findActivitiesForDate(targetDateKey: String, allActivities: Map<String, List<Trip.Activity>>): List<Trip.Activity> {
    Log.d("ActivitySearch", "Looking for activities with dateKey: '$targetDateKey'")

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
        } catch (e: Exception) {
            // Silently handle conversion errors
        }
    }

    Log.d("ActivitySearch", "Found ${activities.size} activities for '$targetDateKey'")

    // 按时间排序
    return activities.sortedBy { activity ->
        parseTimeToMinutes(activity.time)
    }
}


// Helper function: Convert time string to minutes for sorting
/*
将时间字符串（例如 "02:30 PM"）解析成从午夜开始的分钟数。
返回值是 Int 类型，例如：
"02:30 PM" → 14 * 60 + 30 = 870 分钟
"07:15 AM" → 7 * 60 + 15 = 435 分钟
定义一个私有函数，输入是 String 类型的时间字符串，返回该时间距当日午夜的分钟数（Int 类型）
 */
private fun parseTimeToMinutes(timeString: String): Int {
    //使用 try-catch 块来处理可能的时间解析异常
    return try {
        /*
        创建一个时间格式解析器：
           hh：12小时制（01-12）
           mm：分钟（00-59）
           a：AM 或 PM
           Locale.US：确保解析时使用英文 AM/PM，否则某些语言（如中文）可能导致解析失败。
         */
        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
        /*
        使用 formatter 将传入的时间字符串解析成一个 LocalTime 对象（只包含小时和分钟，不包含日期）
         */
        val time = LocalTime.parse(timeString, formatter)
        time.hour * 60 + time.minute
    } catch (e: Exception) {
        // If parsing fails, return a default value
        Log.e("TimeParsing", "Error parsing time: $timeString", e)
        0
    }
}

fun debugTripData(trip: Trip?, vm: TripViewModel) {
    Log.d("TripDebug", "=== COMPREHENSIVE TRIP DEBUG START ===")

    if (trip == null) {
        Log.e("TripDebug", "Trip is NULL!")
        return
    }

    // 基本信息
    Log.d("TripDebug", "基本信息")
    Log.d("TripDebug", "Trip ID: ${trip.id}")
    Log.d("TripDebug", "Trip Title: '${trip.title}'")
    Log.d("TripDebug", "Trip Destination: '${trip.destination}'")
    Log.d("TripDebug", "Trip Creator ID: ${trip.creatorId}")
    Log.d("TripDebug", "Trip Published: ${trip.published}")
    Log.d("TripDebug", "Trip Draft: ${trip.isDraft}")
    Log.d("TripDebug", "基本信息")

    // 日期信息
    Log.d("TripDebug", "日期信息")
    Log.d("TripDebug", "Start Date: ${trip.startDate}")
    Log.d("TripDebug", "End Date: ${trip.endDate}")
    Log.d("TripDebug", "Start Date Calendar: ${trip.startDateAsCalendar().time}")
    Log.d("TripDebug", "End Date Calendar: ${trip.endDateAsCalendar().time}")
    Log.d("TripDebug", "日期信息")

    // 活动信息 - 详细分析
    Log.d("TripDebug", "=== ACTIVITIES DEBUG ===")
    Log.d("TripDebug", "Activities Map Size: ${trip.activities.size}")
    Log.d("TripDebug", "Activities Map Keys: ${trip.activities.keys.joinToString()}")
    Log.d("TripDebug", "Activities Map Empty: ${trip.activities.isEmpty()}")
    Log.d("TripDebug", "=== ACTIVITIES DEBUG ===")

    if (trip.activities.isNotEmpty()) {
        trip.activities.forEach { (dateKey, activities) ->
            Log.d("TripDebug", "Date Key: '$dateKey' -> ${activities.size} activities")
            activities.forEachIndexed { index, activity ->
                Log.d("TripDebug", "  Activity $index: ID=${activity.id}, Time='${activity.time}', Desc='${activity.description}'")
                Log.d("TripDebug", "  Activity Date: ${activity.date}")
            }
        }
    } else {
        Log.w("TripDebug", "NO ACTIVITIES FOUND IN TRIP!")
    }

    // ViewModel 状态检查
    Log.d("TripDebug", "ViewModel User Action: ${vm.userAction}")
    Log.d("TripDebug", "ViewModel Selected Trip ID: ${vm.selectedTrip.value.id}")
    Log.d("TripDebug", "ViewModel Selected Trip Activities: ${vm.selectedTrip.value.activities.size}")

    when (vm.userAction) {
        TripViewModel.UserAction.CREATE_TRIP -> {
            Log.d("TripDebug", "NewTrip ID: ${vm.newTrip.id}")
            Log.d("TripDebug", "NewTrip Activities: ${vm.newTrip.activities.size}")
            vm.newTrip.activities.forEach { (k, v) ->
                Log.d("TripDebug", "NewTrip - $k: ${v.size} activities")
            }
        }
        TripViewModel.UserAction.EDIT_TRIP -> {
            Log.d("TripDebug", "EditTrip ID: ${vm.editTrip.id}")
            Log.d("TripDebug", "EditTrip Activities: ${vm.editTrip.activities.size}")
            vm.editTrip.activities.forEach { (k, v) ->
                Log.d("TripDebug", "EditTrip - $k: ${v.size} activities")
            }
        }
        else -> Log.d("TripDebug", "Other action: ${vm.userAction}")
    }

    // 检查三个Trip对象是否一致
    Log.d("TripDebug", "=== TRIP OBJECTS COMPARISON ===")
    Log.d("TripDebug", "selectedTrip vs input trip - Same ID: ${vm.selectedTrip.value.id == trip.id}")
    Log.d("TripDebug", "selectedTrip vs input trip - Same activities count: ${vm.selectedTrip.value.activities.size == trip.activities.size}")

    // 检查是否有状态不同步的问题
    if (vm.userAction == TripViewModel.UserAction.CREATE_TRIP) {
        Log.d("TripDebug", "newTrip vs selectedTrip - Same ID: ${vm.newTrip.id == vm.selectedTrip.value.id}")
        Log.d("TripDebug", "newTrip vs selectedTrip - Same activities: ${vm.newTrip.activities.size == vm.selectedTrip.value.activities.size}")
    } else if (vm.userAction == TripViewModel.UserAction.EDIT_TRIP) {
        Log.d("TripDebug", "editTrip vs selectedTrip - Same ID: ${vm.editTrip.id == vm.selectedTrip.value.id}")
        Log.d("TripDebug", "editTrip vs selectedTrip - Same activities: ${vm.editTrip.activities.size == vm.selectedTrip.value.activities.size}")
    }
}

// 🔧 修复版本：简化的调试函数，直接接收 Trip 对象
fun debugTripData1(trip: Trip, vm: TripViewModel) {
    Log.d("TripDebug1", "=== COMPREHENSIVE TRIP DEBUG ===")

    // 基本信息
    Log.d("TripDebug1", "Trip ID: ${trip.id}")
    Log.d("TripDebug1", "Trip Destination: '${trip.destination}'")
    Log.d("TripDebug1", "Trip Creator ID: ${trip.creatorId}")
    Log.d("TripDebug1", "Trip Published: ${trip.published}")
    Log.d("TripDebug1", "Trip Draft: ${trip.isDraft}")

    // 日期信息
    Log.d("TripDebug1", "Start Date: ${trip.startDate}")
    Log.d("TripDebug1", "End Date: ${trip.endDate}")
    Log.d("TripDebug1", "Start Date Calendar: ${trip.startDateAsCalendar().time}")
    Log.d("TripDebug1", "End Date Calendar: ${trip.endDateAsCalendar().time}")

    // 活动信息 - 详细分析
    Log.d("TripDebug1", "Activities Map Size: ${trip.activities.size}")
    Log.d("TripDebug1", "Activities Map Keys: ${trip.activities.keys.joinToString()}")
    Log.d("TripDebug1", "Activities Map Empty: ${trip.activities.isEmpty()}")

    if (trip.activities.isNotEmpty()) {
        trip.activities.forEach { (dateKey, activities) ->
            Log.d("TripDebug1", "Date Key: '$dateKey' -> ${activities.size} activities")
            activities.forEachIndexed { index, activity ->
                Log.d("TripDebug1", "  Activity $index: ID=${activity.id}, Time='${activity.time}', Desc='${activity.description}'")
                Log.d("TripDebug1", "  Activity Date: ${activity.date}")
            }
        }
    } else {
        Log.w("TripDebug1", "NO ACTIVITIES FOUND IN TRIP!")
    }

    // ViewModel 状态检查
    Log.d("TripDebug1", "ViewModel User Action: ${vm.userAction}")
    Log.d("TripDebug1", "ViewModel Selected Trip ID: ${vm.selectedTrip.value.id}")
    Log.d("TripDebug1", "ViewModel Selected Trip Activities: ${vm.selectedTrip.value.activities.size}")

    when (vm.userAction) {
        TripViewModel.UserAction.CREATE_TRIP -> {
            Log.d("TripDebug1", "NewTrip ID: ${vm.newTrip.id}")
            Log.d("TripDebug1", "NewTrip Activities: ${vm.newTrip.activities.size}")
            vm.newTrip.activities.forEach { (k, v) ->
                Log.d("TripDebug1", "NewTrip - $k: ${v.size} activities")
            }
        }
        TripViewModel.UserAction.EDIT_TRIP -> {
            Log.d("TripDebug1", "EditTrip ID: ${vm.editTrip.id}")
            Log.d("TripDebug1", "EditTrip Activities: ${vm.editTrip.activities.size}")
            vm.editTrip.activities.forEach { (k, v) ->
                Log.d("TripDebug1", "EditTrip - $k: ${v.size} activities")
            }
        }
        else -> Log.d("TripDebug1", "Other action: ${vm.userAction}")
    }

    // 检查三个Trip对象是否一致
    Log.d("TripDebug1", "=== TRIP OBJECTS COMPARISON ===")
    Log.d("TripDebug1", "selectedTrip vs input trip - Same ID: ${vm.selectedTrip.value.id == trip.id}")
    Log.d("TripDebug1", "selectedTrip vs input trip - Same activities count: ${vm.selectedTrip.value.activities.size == trip.activities.size}")


}
