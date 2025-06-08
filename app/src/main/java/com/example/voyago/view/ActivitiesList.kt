package com.example.voyago.view


import android.util.Log
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
import com.example.voyago.model.isTimestampLong
import com.example.voyago.model.stringToCalendar
import com.example.voyago.model.timestampToCalendar
import com.example.voyago.viewmodel.TripViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesList(navController: NavController, vm: TripViewModel) {

    val selectedTrip by vm.selectedTrip

    var showIncompleteDialog by rememberSaveable { mutableStateOf(false) }

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
                        Button(
                            onClick = {
                                if (selectedTrip.hasActivityForEachDay() == true) {

                                    if(vm.userAction == TripViewModel.UserAction.CREATE_TRIP) {
                                        val updatedTrip = Trip(
                                            title = vm.newTrip.title,
                                            destination = vm.newTrip.destination,
                                            startDate = vm.newTrip.startDate,
                                            endDate = vm.newTrip.endDate,
                                            estimatedPrice = vm.newTrip.estimatedPrice,
                                            groupSize = vm.newTrip.groupSize,
                                            activities = vm.newTrip.activities,
                                            typeTravel = vm.newTrip.typeTravel,
                                            creatorId = vm.newTrip.creatorId,
                                            published = false,
                                            id = vm.newTrip.id,
                                            participants = emptyMap(),
                                            rejectedUsers = emptyMap(),
                                            status = Trip.TripStatus.NOT_STARTED.toString(),
                                            appliedUsers = emptyMap()

                                        )

                                        vm.addNewTrip(updatedTrip) { success, trip ->
                                            if (success && trip != null) {
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
                                        val updatedTrip = Trip(
                                            title = vm.editTrip.title,
                                            destination = vm.editTrip.destination,
                                            startDate = vm.editTrip.startDate,
                                            endDate = vm.editTrip.endDate,
                                            estimatedPrice = vm.editTrip.estimatedPrice,
                                            groupSize = vm.editTrip.groupSize,
                                            activities = vm.editTrip.activities,
                                            typeTravel = vm.editTrip.typeTravel,
                                            creatorId = vm.editTrip.creatorId,
                                            published = vm.editTrip.published,
                                            id = vm.editTrip.id,
                                            participants = vm.editTrip.participants,
                                            rejectedUsers = vm.editTrip.rejectedUsers,
                                            status = vm.editTrip.status,
                                            appliedUsers = vm.editTrip.appliedUsers,
                                            isDraft = false
                                        )

                                        vm.editTrip = updatedTrip

                                        //Go to the owned travel proposal
                                        navController.navigate("my_trips_main") {
                                            popUpTo("my_trips_main") {
                                                inclusive = false
                                            }
                                            launchSingleTop = true
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
@Composable
fun ActivitiesListContent(trip: Trip?, vm: TripViewModel, navController: NavController){
    if (trip == null) { // 如果行程为空
        Text("No trip selected", modifier = Modifier.padding(16.dp)) // 显示"未选择行程"文本
        return // 返回，不继续执行
    }

    val sortedDays = trip.activities.keys.sortedBy { it } // 按日期排序活动的天数

    // Check if all activity lists are empty 检查是否所有活动列表都为空
    val hasNoActivities = trip.activities.values.all { it.isEmpty() } // 检查是否没有活动
    var activityToDelete by rememberSaveable { mutableStateOf<Trip.Activity?>(null) } // 记住要删除的活动状态

    Column( // 创建一个垂直排列的列
        modifier = Modifier.fillMaxSize() // 填满整个可用空间
    ) {
        if (hasNoActivities) { // 如果没有活动
            Text( // 创建文本
                text = "No activities for trip to ${trip.destination}.", // 显示无活动提示文本
                modifier = Modifier.padding(16.dp), // 内边距16dp
                style = MaterialTheme.typography.bodyLarge, // 使用大号正文字体样式
                color = Color.Gray // 文字颜色为灰色
            )
        } else { // 如果有活动
            sortedDays.forEach { day -> // 遍历排序后的天数
                Log.d("L1", "Activity List") // 打印调试日志

                // 修复后的日期计算逻辑
                val activityCalendar = if (isTimestampLong(day)) { // 如果天数是时间戳格式
                    Log.d("L1", "Day is a timestamp: $day") // 打印调试日志
                    timestampToCalendar(day) // 转换时间戳为日历
                } else { // 如果天数是字符串格式
                    Log.d("L1", "Day is a string: $day") // 打印调试日志
                    stringToCalendar(day) // 转换字符串为日历
                }

                Log.d("L1", "Activity calendar: $activityCalendar") // 打印活动日历
                Log.d("L1", "Trip start calendar: ${trip.startDateAsCalendar()}") // 打印行程开始日历

                // 计算正确的天数索引
                val dayIndex = calculateDayIndex(activityCalendar, trip.startDateAsCalendar())

                Log.d("L1", "Calculated day index: $dayIndex") // 打印计算的天数索引

                val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US) // 创建时间格式化器
                val activitiesForDay = (trip.activities[day] ?: emptyList()) // 获取当天的活动列表
                    .sortedBy { LocalTime.parse(it.time, formatter) } // 按时间排序活动

                Column(modifier = Modifier.padding(16.dp)) { // 创建带内边距的列
                    Text( // 创建文本
                        text = "Day $dayIndex", // 显示第几天
                        style = MaterialTheme.typography.titleMedium, // 使用中号标题字体样式
                        fontWeight = FontWeight.Bold, // 字体加粗
                        color = Color(0xFF555555) // 文字颜色为深灰色
                    )

                    Spacer(modifier = Modifier.height(8.dp)) // 创建8dp高度的空白间距

                    activitiesForDay.forEach { activity -> // 遍历当天的活动
                        Row( // 创建一个水平排列的行
                            verticalAlignment = Alignment.CenterVertically, // 垂直居中对齐
                            modifier = Modifier
                                .padding(bottom = 8.dp) // 底部内边距8dp
                                .fillMaxWidth() // 填满可用宽度
                        ) {
                            //Edit Activity Button 编辑活动按钮
                            Icon( // 创建图标
                                imageVector = Icons.Default.Edit, // 使用编辑图标
                                contentDescription = "Edit Activity", // 内容描述
                                tint = Color(0xFF4CAF50), // 图标颜色为绿色
                                modifier = Modifier
                                    .size(20.dp) // 图标大小为20dp
                                    .clickable { // 设置可点击
                                        vm.userAction = TripViewModel.UserAction.EDIT_ACTIVITY // 设置用户操作为编辑活动
                                        navController.navigate("edit_Activity/${activity.id}") // 导航到编辑活动页面
                                    }
                            )

                            Spacer(modifier = Modifier.width(8.dp)) // 创建8dp宽度的空白间距

                            //Print Activity information 打印活动信息
                            Column(modifier = Modifier.weight(1f)) { // 创建占用剩余空间的列
                                Text( // 创建文本
                                    text = "${activity.time} - ${activity.description}" + // 显示活动时间和描述
                                            if (activity.isGroupActivity) " (group activity)" else "", // 如果是团体活动则添加标注
                                    style = MaterialTheme.typography.bodyMedium // 使用中号正文字体样式
                                )
                            }

                            //Delete Activity Button 删除活动按钮
                            OutlinedButton( // 创建轮廓按钮
                                onClick = {
                                    Log.d("DeleteButton", "Delete button clicked for activity: ${activity.id}") // 打印调试日志
                                    activityToDelete = activity // 设置要删除的活动
                                },
                                modifier = Modifier.height(36.dp) // 设置高度为36dp
                            ) {
                                Text("Delete", color = Color.Red) // 显示红色的"Delete"文本
                            }
                        }
                    }

                    activityToDelete?.let { activity -> // 如果有要删除的活动
                        AlertDialog( // 创建警告对话框
                            onDismissRequest = { // 设置取消对话框的事件
                                Log.d("DeleteDialog", "Dialog dismissed") // 打印调试日志
                                activityToDelete = null // 清空要删除的活动
                            },
                            title = { Text("Delete Activity") }, // 对话框标题
                            text = { Text("Are you sure you want to delete this activity?") }, // 对话框内容
                            confirmButton = { // 确认按钮
                                TextButton(onClick = { // 创建文本按钮
                                    Log.d("DeleteDialog", "Confirming delete for activity: ${activity.id}") // 打印调试日志
                                    vm.deleteActivity(activity) // 调用视图模型删除活动
                                    activityToDelete = null // 清空要删除的活动
                                    Log.d("DeleteDialog", "Delete operation completed") // 打印调试日志
                                }) {
                                    Text("Delete") // 显示"Delete"文本
                                }
                            },
                            dismissButton = { // 取消按钮
                                TextButton(onClick = { // 创建文本按钮
                                    Log.d("DeleteDialog", "Delete cancelled") // 打印调试日志
                                    activityToDelete = null // 清空要删除的活动
                                }) {
                                    Text("Cancel") // 显示"Cancel"文本
                                }
                            }
                        )
                    }
                }
            }
        }
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

