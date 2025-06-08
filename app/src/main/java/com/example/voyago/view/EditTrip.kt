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
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.voyago.model.TypeTravel
import com.example.voyago.viewmodel.TripViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@Composable
fun EditTrip(navController: NavController, vm: TripViewModel) {
    val trip = vm.editTrip
    vm.userAction = TripViewModel.UserAction.EDIT_TRIP

    var tripImageError by rememberSaveable { mutableStateOf(false) }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var remoteImageUrl by remember { mutableStateOf<String?>(null) }

    // Load remote image if no local image is selected
    LaunchedEffect(trip.id) {
        remoteImageUrl = trip.getPhoto()
    }

    val fieldValues = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )) {
        mutableStateListOf(
            trip.title,
            trip.destination,
            trip.estimatedPrice,
            trip.groupSize,
        )
    }
    val fieldNames = listOf("Title", "Destination", "Price Estimated", "Group Size")
    var fieldErrors = arrayOf(false, false, false, false)

    val typeTravel = listOf("party", "adventure", "culture", "relax")
    val selected = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) {
        trip.typeTravel.map { it.toString().lowercase() }.toMutableStateList()
    }
    var typeTravelError by rememberSaveable { mutableStateOf(false) }


    //Date Handling
    var startDate by rememberSaveable { mutableStateOf(trip.startDateAsCalendar().toStringDate()) }
    var startCalendar by rememberSaveable { mutableStateOf<Calendar?>(trip.startDateAsCalendar()) }

    var endDate by rememberSaveable { mutableStateOf(trip.endDateAsCalendar().toStringDate()) }
    var endCalendar by rememberSaveable { mutableStateOf<Calendar?>(trip.endDateAsCalendar()) }

    var dateError by rememberSaveable { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

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

            //Trip Image
            item {
                TripImageEdit(
                    trip = trip,
                    imageUri = imageUri,
                    onUriSelected = { uri -> imageUri = uri }
                )
            }

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

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }

            //Title, Destination, Price Estimated, Group Size Fields with Check Errors
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    //TextFields with various info 各种信息的文本字段
                    fieldValues.forEachIndexed { index, item ->
                        //Title and Destination Fields 标题和目的地字段
                        if (index == 0 || index == 1) {
                            val textHasErrors = item.toString().isBlank() || // 检查是否为空
                                    !item.toString().any { it.isLetter() } // 检查是否包含字母

                            fieldErrors[index] = textHasErrors // 设置错误状态

                            ValidatingInputTextField( // 验证输入文本字段
                                item.toString(), // 当前值
                                {
                                    fieldValues[index] = it // 更新值的回调
                                },
                                textHasErrors, // 是否有错误
                                fieldNames[index] // 字段名称
                            )
                        } else if (index == 2) { //Price Estimated Field 价格估算字段
                            // 修改后的价格验证逻辑 - 精确到两位小数
                            val priceText = item.toString() // 获取价格文本
                            val floatHasErrors = priceText.isBlank() || // 检查是否为空
                                    priceText.toDoubleOrNull()?.let { it <= 0.0 } != false || // 检查是否大于0
                                    !priceText.matches(Regex("^\\d+(\\.\\d{1,2})?$")) // 精确到两位小数的正则表达式

                            fieldErrors[index] = floatHasErrors // 设置错误状态

                            ValidatingInputFloatField( // 验证输入浮点数字段
                                item.toString(), // 当前值
                                { newValue ->
                                    // 处理输入时的实时验证和格式化
                                    val filteredValue = newValue.filter { it.isDigit() || it == '.' } // 只允许数字和小数点

                                    // 检查小数点的位置和数量
                                    val decimalIndex = filteredValue.indexOf('.')
                                    val processedValue = if (decimalIndex != -1) {
                                        val beforeDecimal = filteredValue.substring(0, decimalIndex) // 小数点前的部分
                                        val afterDecimal = filteredValue.substring(decimalIndex + 1) // 小数点后的部分

                                        // 限制小数点后最多两位数字
                                        if (afterDecimal.length <= 2) {
                                            filteredValue
                                        } else {
                                            "$beforeDecimal.${afterDecimal.take(2)}" // 截取前两位小数
                                        }
                                    } else {
                                        filteredValue // 没有小数点，直接使用
                                    }

                                    fieldValues[index] = processedValue // 更新处理后的值
                                },
                                floatHasErrors, // 是否有错误
                                fieldNames[index] // 字段名称
                            )
                        } else { //Group Size Field
                            val intHasErrors =
                                (item.toString().isBlank() || item.toString().toIntOrNull()
                                    ?.let { it <= 1 } != false)

                            fieldErrors[index] = intHasErrors // 设置错误状态

                            ValidatingInputIntField( // 验证输入整数字段
                                item.toString(), // 当前值
                                {
                                    fieldValues[index] = it // 更新值的回调
                                },
                                intHasErrors, // 是否有错误
                                fieldNames[index] // 字段名称
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            //Trip Type
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

            //Dates

            item {
                val context = LocalContext.current // 获取当前上下文
                val calendar = Calendar.getInstance() // 获取当前日历实例
                val year = calendar.get(Calendar.YEAR) // 获取当前年份
                val month = calendar.get(Calendar.MONTH) // 获取当前月份
                val day = calendar.get(Calendar.DAY_OF_MONTH) // 获取当前日期

                // 获取今天的日期作为最小可选日期
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0) // 设置小时为0
                    set(Calendar.MINUTE, 0) // 设置分钟为0
                    set(Calendar.SECOND, 0) // 设置秒为0
                    set(Calendar.MILLISECOND, 0) // 设置毫秒为0
                }

                val startDatePickerDialog = remember {
                    DatePickerDialog(
                        context,
                        { _: DatePicker, y: Int, m: Int, d: Int -> // 开始日期选择回调
                            startDate = "$d/${m + 1}/$y" // 更新开始日期显示
                            val newStartCalendar = Calendar.getInstance().apply {
                                set(y, m, d, 0, 0, 0) // 设置选择的日期
                                set(Calendar.MILLISECOND, 0) // 清除毫秒
                            }
                            startCalendar = newStartCalendar // 更新开始日期日历

                            // 检查并清理超出新日期范围的活动
                            if (endCalendar != null) {
                                cleanActivitiesOutsideDateRange(vm, newStartCalendar, endCalendar!!)
                            }
                        }, year, month, day
                    ).apply {
                        // 设置最小日期为今天，不能选择过去的日期
                        datePicker.minDate = today.timeInMillis
                    }
                }

                val endDatePickerDialog = remember {
                    DatePickerDialog(
                        context,
                        { _: DatePicker, y: Int, m: Int, d: Int -> // 结束日期选择回调
                            endDate = "$d/${m + 1}/$y" // 更新结束日期显示
                            val newEndCalendar = Calendar.getInstance().apply {
                                set(y, m, d, 0, 0, 0) // 设置选择的日期
                                set(Calendar.MILLISECOND, 0) // 清除毫秒
                            }
                            endCalendar = newEndCalendar // 更新结束日期日历

                            // 检查并清理超出新日期范围的活动
                            if (startCalendar != null) {
                                cleanActivitiesOutsideDateRange(vm, startCalendar!!, newEndCalendar)
                            }
                        }, year, month, day
                    ).apply {
                        // 动态设置最小日期
                        if (startCalendar != null) {
                            // 如果已选择开始日期，结束日期不能早于开始日期
                            val startDateMin = Calendar.getInstance().apply {
                                timeInMillis = startCalendar!!.timeInMillis
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            datePicker.minDate = startDateMin.timeInMillis
                        } else {
                            // 如果没有选择开始日期，最小日期为今天
                            datePicker.minDate = today.timeInMillis
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth() // 填满可用宽度
                        .padding(horizontal = 35.dp), // 设置水平内边距
                    horizontalArrangement = Arrangement.spacedBy(5.dp) // 设置元素间距
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start, // 水平左对齐
                        modifier = Modifier
                            .weight(1f) // 占用一半宽度
                            .padding(vertical = 8.dp) // 设置垂直内边距
                    ) {
                        OutlinedButton(onClick = {
                            // 每次打开对话框前重新设置最小日期
                            startDatePickerDialog.datePicker.minDate = today.timeInMillis
                            startDatePickerDialog.show()
                        }) { // 开始日期按钮
                            Text("Start Date") // 按钮文本
                        }

                        if (startDate.isNotEmpty()) { // 如果开始日期不为空
                            Text(
                                "Start: $startDate", // 显示开始日期
                                modifier = Modifier.padding(top = 8.dp) // 设置顶部内边距
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End, // 水平右对齐
                        modifier = Modifier
                            .weight(1f) // 占用一半宽度
                            .padding(vertical = 8.dp) // 设置垂直内边距
                    ) {
                        OutlinedButton(onClick = {
                            // 每次打开对话框前重新设置最小日期
                            if (startCalendar != null) {
                                // 如果已选择开始日期，结束日期不能早于开始日期
                                val startDateMin = Calendar.getInstance().apply {
                                    timeInMillis = startCalendar!!.timeInMillis
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                endDatePickerDialog.datePicker.minDate = startDateMin.timeInMillis
                            } else {
                                // 如果没有选择开始日期，最小日期为今天
                                endDatePickerDialog.datePicker.minDate = today.timeInMillis
                            }
                            endDatePickerDialog.show()
                        }) { // 结束日期按钮
                            Text("End Date") // 按钮文本
                        }

                        if (endDate.isNotEmpty()) { // 如果结束日期不为空
                            Text("End: $endDate", modifier = Modifier.padding(top = 8.dp)) // 显示结束日期
                        }
                    }
                }

                if (dateError.isNotEmpty()) { // 如果有日期错误
                    Text(
                        text = dateError, // 显示错误信息
                        color = MaterialTheme.colorScheme.error, // 错误颜色
                        style = MaterialTheme.typography.bodySmall, // 小字体样式
                        modifier = Modifier.padding(top = 6.dp) // 设置顶部内边距
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
                    //Next Button 下一步按钮
                    Button(
                        onClick = {
                            Log.d("NextButton", "=== Next button clicked ===")

                            vm.userAction = TripViewModel.UserAction.EDIT_TRIP

                            // 验证旅行类型
                            typeTravelError = selected.isEmpty()
                            Log.d("NextButton", "Travel type selected: $selected")
                            Log.d("NextButton", "Travel type error: $typeTravelError")

                            // 调试日期解析
                            debugDateParsing(startDate, endDate, startCalendar, endCalendar)

                            // 验证日期
                            val isDateValid = validateDateOrder(startCalendar, endCalendar)
                            dateError = if (!isDateValid) {
                                "Start Date and End Date cannot be empty.\n End Date must be after Start Date"
                            } else {
                                ""
                            }
                            Log.d("NextButton", "Date validation result: $isDateValid")
                            Log.d("NextButton", "Date error message: '$dateError'")

                            // 检查所有字段错误
                            val hasFieldErrors = fieldErrors.any { it }
                            Log.d("NextButton", "Field values: ${fieldValues.map { it.toString() }}")
                            Log.d("NextButton", "Field errors: ${fieldErrors.contentToString()}")
                            Log.d("NextButton", "Has field errors: $hasFieldErrors")

                            // 检查图片错误
                            Log.d("NextButton", "Trip image error: $tripImageError")

                            // 总体验证
                            val allValidationsPass = !tripImageError && !hasFieldErrors && !typeTravelError && isDateValid
                            Log.d("NextButton", "=== Validation Summary ===")
                            Log.d("NextButton", "tripImageError: $tripImageError")
                            Log.d("NextButton", "hasFieldErrors: $hasFieldErrors")
                            Log.d("NextButton", "typeTravelError: $typeTravelError")
                            Log.d("NextButton", "isDateValid: $isDateValid")
                            Log.d("NextButton", "All validations pass: $allValidationsPass")

                            if (allValidationsPass) {
                                Log.d("NextButton", "✅ All validations passed, proceeding...")

                                if (vm.userAction == TripViewModel.UserAction.EDIT_TRIP) {
                                    val currentTrip = vm.editTrip
                                    Log.d("NextButton", "Current trip: $currentTrip")

                                    try {
                                        val updatedTrip = Trip(
                                            photo = currentTrip.photo,
                                            title = fieldValues[0].toString(),
                                            destination = fieldValues[1].toString(),
                                            startDate = Timestamp(startCalendar!!.time),
                                            endDate = Timestamp(endCalendar!!.time),
                                            estimatedPrice = fieldValues[2].toString().toDouble(),
                                            groupSize = fieldValues[3].toString().toInt(),
                                            activities = currentTrip.activities,
                                            typeTravel = selected.map {
                                                TypeTravel.valueOf(it.uppercase()).toString()
                                            },
                                            creatorId = currentTrip.creatorId,
                                            published = currentTrip.published,
                                            id = currentTrip.id,
                                            participants = currentTrip.participants,
                                            status = currentTrip.status,
                                            appliedUsers = currentTrip.appliedUsers,
                                            rejectedUsers = currentTrip.rejectedUsers
                                        )

                                        Log.d("NextButton", "✅ Updated trip created successfully")
                                        vm.editTrip = updatedTrip
                                        vm.setSelectedTrip(updatedTrip)

                                        coroutineScope.launch {
                                            try {
                                                val success = if (imageUri != null) {
                                                    Log.d("NextButton", "Setting new photo...")
                                                    updatedTrip.setPhoto(imageUri!!)
                                                } else {
                                                    Log.d("NextButton", "No new photo to set")
                                                    true
                                                }

                                                if (success) {
                                                    Log.d("NextButton", "Saving trip to database...")
                                                    vm.editExistingTrip(updatedTrip) { success2 ->
                                                        if (success2) {
                                                            Log.d("NextButton", "✅ Trip saved successfully, navigating...")
                                                            navController.navigate("activities_list")
                                                        } else {
                                                            Log.e("NextButton", "❌ Failed to save trip to database")
                                                        }
                                                    }
                                                } else {
                                                    Log.e("NextButton", "❌ Failed to set photo")
                                                }
                                            } catch (e: Exception) {
                                                Log.e("NextButton", "❌ Exception in coroutine: ${e.message}", e)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("NextButton", "❌ Exception creating updated trip: ${e.message}", e)
                                    }
                                }
                            } else {
                                Log.d("NextButton", "❌ Validations failed, showing errors...")
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

}



// 检查字符串是否为时间戳格式的辅助函数
fun isTimestampLong(input: String): Boolean {
    return input.toLongOrNull() != null
}


fun Calendar.toStringDate(): String {
    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return format.format(this.time)
}

@OptIn(ExperimentalGlideComposeApi::class)
@SuppressLint("DiscouragedApi")
@Composable
fun TripImageEdit(trip: Trip, imageUri: Uri?, onUriSelected: (Uri?) -> Unit) {
    val context = LocalContext.current
    val pickMedia = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri ->
        onUriSelected(uri)
    }
    var remoteImageUrl by remember { mutableStateOf<String?>(null) }
    // Always fetch the current trip image if no new image is selected
    LaunchedEffect(trip.photo) {
        if (imageUri == null) {
            remoteImageUrl = trip.getPhoto()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            imageUri != null -> {
                GlideImage(
                    model = imageUri,
                    contentDescription = "Selected Trip Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            remoteImageUrl != null -> {
                GlideImage(
                    model = remoteImageUrl,
                    contentDescription = "Trip Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        IconButton(
            onClick = { pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = "Select photo from gallery",
                tint = Color.White,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

// 在文件末尾添加这个函数，在 TripImageEdit 函数之前


// 修复后的 cleanActivitiesOutsideDateRange 函数
fun cleanActivitiesOutsideDateRange(vm: TripViewModel, startCal: Calendar, endCal: Calendar) {
    val currentTrip = vm.editTrip // 获取当前编辑的行程
    val updatedActivities = currentTrip.activities.toMutableMap() // 复制活动映射
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US) // 日期格式化器

    // 标准化开始和结束日期，去除时间部分
    val startDate = Calendar.getInstance().apply {
        timeInMillis = startCal.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val endDate = Calendar.getInstance().apply {
        timeInMillis = endCal.timeInMillis
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    // 遍历所有活动日期
    val keysToRemove = mutableListOf<String>()
    for ((dateKey, activities) in updatedActivities) {
        try {
            // 解析活动日期
            val activityDate = if (isTimestampLong(dateKey)) {
                // 如果是时间戳格式
                Calendar.getInstance().apply {
                    timeInMillis = dateKey.toLong()
                }
            } else {
                // 如果是字符串格式
                Calendar.getInstance().apply {
                    val parsedDate = dateFormat.parse(dateKey)
                    time = parsedDate ?: java.util.Date() // 使用完整路径
                }
            }

            // 标准化活动日期
            activityDate.set(Calendar.HOUR_OF_DAY, 12) // 设置为中午以避免时区问题
            activityDate.set(Calendar.MINUTE, 0)
            activityDate.set(Calendar.SECOND, 0)
            activityDate.set(Calendar.MILLISECOND, 0)

            // 检查活动日期是否在新的日期范围内 - 使用 timeInMillis 比较
            if (activityDate.timeInMillis < startDate.timeInMillis ||
                activityDate.timeInMillis > endDate.timeInMillis) {
                keysToRemove.add(dateKey) // 标记要删除的日期键
                Log.d("EditTrip", "Removing activities for date $dateKey as it's outside new range")
            }
        } catch (e: Exception) {
            Log.e("EditTrip", "Error parsing activity date: $dateKey", e)
            keysToRemove.add(dateKey) // 解析失败的也删除
        }
    }

    // 删除超出范围的活动
    keysToRemove.forEach { key ->
        updatedActivities.remove(key)
    }

    // 更新行程的活动数据
    vm.editTrip = currentTrip.copy(
        activities = updatedActivities,
        startDate = Timestamp(startCal.time), // 更新开始日期
        endDate = Timestamp(endCal.time) // 更新结束日期
    )

    // 同步更新选中的行程
    vm.setSelectedTrip(vm.editTrip)

    // 如果有活动被删除，显示提示信息
    if (keysToRemove.isNotEmpty()) {
        Log.i("EditTrip", "Removed ${keysToRemove.size} activity dates due to date range change")
    }
}


// 增强的日期验证函数，包含详细调试信息
fun validateDateOrder(startCalendar: Calendar?, endCalendar: Calendar?): Boolean {
    Log.d("DateValidation", "=== Starting date validation ===")
    Log.d("DateValidation", "startCalendar: $startCalendar")
    Log.d("DateValidation", "endCalendar: $endCalendar")

    if (startCalendar == null) {
        Log.e("DateValidation", "startCalendar is null")
        return false
    }

    if (endCalendar == null) {
        Log.e("DateValidation", "endCalendar is null")
        return false
    }

    // 打印原始日期
    Log.d("DateValidation", "Raw start date: ${startCalendar.time}")
    Log.d("DateValidation", "Raw end date: ${endCalendar.time}")

    // 标准化日期，去除时间部分进行比较
    val startDate = Calendar.getInstance().apply {
        timeInMillis = startCalendar.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val endDate = Calendar.getInstance().apply {
        timeInMillis = endCalendar.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // 打印标准化后的日期
    Log.d("DateValidation", "Normalized start date: ${startDate.time}")
    Log.d("DateValidation", "Normalized end date: ${endDate.time}")
    Log.d("DateValidation", "Start millis: ${startDate.timeInMillis}")
    Log.d("DateValidation", "End millis: ${endDate.timeInMillis}")

    // 计算日期差
    val diffMillis = endDate.timeInMillis - startDate.timeInMillis
    val diffDays = diffMillis / (1000 * 60 * 60 * 24)
    Log.d("DateValidation", "Difference in milliseconds: $diffMillis")
    Log.d("DateValidation", "Difference in days: $diffDays")

    // 结束日期必须等于或晚于开始日期
    val isValid = endDate.timeInMillis >= startDate.timeInMillis

    Log.d("DateValidation", "Date order is valid: $isValid")
    Log.d("DateValidation", "=== End date validation ===")

    return isValid
}

// 同时，让我们检查日期字符串的解析
fun debugDateParsing(startDate: String, endDate: String, startCalendar: Calendar?, endCalendar: Calendar?) {
    Log.d("DateParsing", "=== Date Parsing Debug ===")
    Log.d("DateParsing", "Start date string: '$startDate'")
    Log.d("DateParsing", "End date string: '$endDate'")
    Log.d("DateParsing", "Start calendar: $startCalendar")
    Log.d("DateParsing", "End calendar: $endCalendar")

    if (startCalendar != null) {
        Log.d("DateParsing", "Start calendar date: ${startCalendar.time}")
        Log.d("DateParsing", "Start year: ${startCalendar.get(Calendar.YEAR)}")
        Log.d("DateParsing", "Start month: ${startCalendar.get(Calendar.MONTH) + 1}") // +1 因为月份从0开始
        Log.d("DateParsing", "Start day: ${startCalendar.get(Calendar.DAY_OF_MONTH)}")
    }

    if (endCalendar != null) {
        Log.d("DateParsing", "End calendar date: ${endCalendar.time}")
        Log.d("DateParsing", "End year: ${endCalendar.get(Calendar.YEAR)}")
        Log.d("DateParsing", "End month: ${endCalendar.get(Calendar.MONTH) + 1}") // +1 因为月份从0开始
        Log.d("DateParsing", "End day: ${endCalendar.get(Calendar.DAY_OF_MONTH)}")
    }
    Log.d("DateParsing", "=== End Date Parsing Debug ===")
}