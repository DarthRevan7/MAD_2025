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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.voyago.viewmodel.TripViewModel
import com.google.firebase.Timestamp
import java.util.Calendar
import com.example.voyago.toStringDate

@Composable
fun EditTrip(navController: NavController, vm: TripViewModel) {
    val trip = vm.editTrip
    vm.userAction = TripViewModel.UserAction.EDIT_TRIP
    val originalTripState = remember {
        vm.editTrip.copy(
            // activities：Map<String, List<Activity>> – 逐层 copy
            activities = vm.editTrip.activities
                .mapValues { (_, acts) -> acts.map { it.copy() } },

            // List<String> / Map<…> 等如果后面会改，也一并 copy
            typeTravel   = vm.editTrip.typeTravel.toList(),
            participants = vm.editTrip.participants.toMap(),
            appliedUsers = vm.editTrip.appliedUsers.toMap(),
            rejectedUsers= vm.editTrip.rejectedUsers.toMap()
        )
    }
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
            trip.estimatedPrice.toString(),
            trip.groupSize.toString(),
        )
    }
    val fieldNames = listOf("Title", "Destination", "Price Estimated", "Group Size")
    val fieldErrors = remember { mutableStateListOf(false, false, false, false) }
    val fieldTouched = remember { mutableStateListOf(false, false, false, false) }


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

    // 添加用于确认对话框的状态
    var showReallocationDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var onConfirmReallocation by remember { mutableStateOf<(() -> Unit)?>(null) }
    var onCancelReallocation by remember { mutableStateOf<(() -> Unit)?>(null) }

    // 存储原始日期范围以便比较
    val originalStartDate = remember { vm.editTrip.startDate }
    val originalEndDate = remember { vm.editTrip.endDate }


    val coroutineScope = rememberCoroutineScope()

    fun validateField(index: Int, value: String) {
        when (index) {
            0, 1 -> { // Title and Destination
                fieldErrors[index] = value.isBlank() || !value.any { it.isLetter() }
            }
            2 -> { // Price
                fieldErrors[index] = value.isBlank() ||
                        value.toDoubleOrNull()?.let { it <= 0.0 } != false ||
                        !value.matches(Regex("^\\d+(\\.\\d{1,2})?$"))
            }
            3 -> { // Group Size
                fieldErrors[index] = value.isBlank() ||
                        value.toIntOrNull()?.let { it <= 1 } != false
            }
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

                            ValidatingInputTextField(
                                item.toString(),
                                { newValue ->
                                    fieldValues[index] = newValue
                                    // 🔴 设置触摸状态
                                    fieldTouched[index] = true
                                    // 🔴 实时验证
                                    validateField(index, newValue)
                                },
                                // 🔴 只有触摸后才显示错误
                                fieldTouched[index] && fieldErrors[index],
                                fieldNames[index]
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
                val context = LocalContext.current
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                // 获取今天的日期作为最小可选日期
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // 修改日期选择器逻辑 - 直接处理，不显示对话框
                val startDatePickerDialog = remember {
                    DatePickerDialog(
                        context,
                        { _: DatePicker, y: Int, m: Int, d: Int ->
                            startDate = "$d/${m + 1}/$y"
                            val newStartCalendar = Calendar.getInstance().apply {
                                set(y, m, d, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            startCalendar = newStartCalendar

                        }, year, month, day
                    ).apply {
                        datePicker.minDate = today.timeInMillis
                    }
                }

                val endDatePickerDialog = remember {
                    DatePickerDialog(
                        context,
                        { _: DatePicker, y: Int, m: Int, d: Int ->
                            endDate = "$d/${m + 1}/$y"
                            val newEndCalendar = Calendar.getInstance().apply {
                                set(y, m, d, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            endCalendar = newEndCalendar

                        }, year, month, day
                    )
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
                        OutlinedButton(onClick = {
                            startDatePickerDialog.datePicker.minDate = today.timeInMillis
                            startDatePickerDialog.show()
                        }) {
                            Text("Start Date")
                        }

                        if (startDate.isNotEmpty()) {
                            Text(
                                "Start: $startDate",
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp)
                    ) {
                        OutlinedButton(onClick = {
                            if (startCalendar != null) {
                                val startDateMin = Calendar.getInstance().apply {
                                    timeInMillis = startCalendar!!.timeInMillis
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                endDatePickerDialog.datePicker.minDate = startDateMin.timeInMillis
                            } else {
                                endDatePickerDialog.datePicker.minDate = today.timeInMillis
                            }
                            endDatePickerDialog.show()
                        }) {
                            Text("End Date")
                        }

                        if (endDate.isNotEmpty()) {
                            Text("End: $endDate", modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }

                if (dateError.isNotEmpty()) {
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
                    //Cancel Button
                    Button(
                        onClick = {
                            vm.editTrip = originalTripState
                            vm.setSelectedTrip(originalTripState)
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
                    Button(
                        onClick = {
                            Log.d("NextButton", "=== Next button clicked ===")

                            vm.userAction = TripViewModel.UserAction.EDIT_TRIP

                            // 验证旅行类型
                            typeTravelError = selected.isEmpty()

                            // 验证日期
                            val isDateValid = validateDateOrder(startCalendar, endCalendar)
                            dateError = if (!isDateValid) {
                                "Start Date and End Date cannot be empty.\n End Date must be after Start Date"
                            } else {
                                ""
                            }

                            // 检查所有字段错误
                            val hasFieldErrors = fieldErrors.any { it }

                            if (!typeTravelError && dateError.isEmpty() && !hasFieldErrors) {
                                // 如果日期发生了变化，进行最终的智能重新分配
                                val originalStartCal = Calendar.getInstance().apply { time = originalStartDate.toDate() }
                                val originalEndCal = Calendar.getInstance().apply { time = originalEndDate.toDate() }

                                val hasDateChanged = startCalendar?.timeInMillis != originalStartCal.timeInMillis ||
                                        endCalendar?.timeInMillis != originalEndCal.timeInMillis

                                if (hasDateChanged && startCalendar != null && endCalendar != null) {
                                    Log.d("NextButton", "Date changed, performing final reallocation...")

                                    // 🔴 选择1：完全自动，不询问
                                    smartReallocateActivitiesDirectly(
                                        vm = vm,
                                        oldStartCal = originalStartCal,
                                        oldEndCal = originalEndCal,
                                        newStartCal = startCalendar!!,
                                        newEndCal = endCalendar!!
                                    )

                                    updateTripAndNavigate(
                                        vm, startCalendar!!, endCalendar!!, navController,
                                        selected, fieldValues[0], fieldValues[1],
                                        fieldValues[3].toIntOrNull() ?: 2, imageUri
                                    )

                                    // 🔴 选择2：只在 Next 按钮时询问一次（如果你想保留一次确认）



                                } else {
                                    // 没有日期变化，直接更新行程
                                    updateTripAndNavigate(
                                        vm, startCalendar!!, endCalendar!!, navController,
                                        selected, fieldValues[0], fieldValues[1],
                                        fieldValues[3].toIntOrNull() ?: 2, imageUri
                                    )
                                }
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

    // 重新分配确认对话框
    if (showReallocationDialog) {
        AlertDialog(
            onDismissRequest = { showReallocationDialog = false },
            title = { Text("Activity Reallocation") },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReallocationDialog = false
                        onConfirmReallocation?.invoke()
                    }
                ) {
                    // 🔴 修改：更新按钮文本
                    Text("Move to Last Day")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showReallocationDialog = false
                        onCancelReallocation?.invoke()
                    }
                ) {
                    Text("Delete Activities")
                }
            }
        )
    }
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
val KEY_DATE_FORMAT = "yyyy-MM-dd"
// 智能活动重新分配函数
fun smartReallocateActivities(vm: TripViewModel, oldStartCal: Calendar, oldEndCal: Calendar, newStartCal: Calendar, newEndCal: Calendar) {
    val currentTrip = vm.editTrip


    // 计算原始和新的日期间隔
    val oldIntervalDays = calculateDaysBetween(oldStartCal, oldEndCal)
    val newIntervalDays = calculateDaysBetween(newStartCal, newEndCal)

    Log.d("SmartReallocation", "Original interval: $oldIntervalDays days, New interval: $newIntervalDays days")

    val updatedActivities = mutableMapOf<String, List<Trip.Activity>>()

    when {
        // 情况1: 间隔相同 - 保留所有活动，只调整日期
        oldIntervalDays == newIntervalDays -> {
            Log.d("SmartReallocation", "Same interval - adjusting dates")
            reallocateWithSameInterval(currentTrip.activities, oldStartCal, newStartCal, updatedActivities)
        }

        // 情况2: 间隔变长 - 调整活动日期到新范围，多余日期留空
        newIntervalDays > oldIntervalDays -> {
            Log.d("SmartReallocation", "Longer interval - adjusting to new range")
            reallocateWithLongerInterval(currentTrip.activities, oldStartCal, newStartCal, updatedActivities)
        }

        // 情况3: 间隔变短 - 提供选择：删除超出活动 或 重新分配到边界日期
        newIntervalDays < oldIntervalDays -> {
            Log.d("SmartReallocation", "Shorter interval - reallocating overflow activities")
            reallocateWithShorterInterval(currentTrip.activities, oldStartCal, newStartCal, newEndCal, updatedActivities)
        }
    }

    // 更新行程
    vm.editTrip = currentTrip.copy(
        activities = updatedActivities,
        startDate = Timestamp(newStartCal.time),
        endDate = Timestamp(newEndCal.time)
    )

    vm.setSelectedTrip(vm.editTrip)
}

// 计算两个日期之间的天数
fun calculateDaysBetween(startCal: Calendar, endCal: Calendar): Int {
    val startDate = Calendar.getInstance().apply {
        timeInMillis = startCal.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val endDate = Calendar.getInstance().apply {
        timeInMillis = endCal.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val diffInMillis = endDate.timeInMillis - startDate.timeInMillis
    return (diffInMillis / (24 * 60 * 60 * 1000)).toInt() + 1
}

// 情况1: 相同间隔 - 保持相对位置，调整绝对日期
private fun reallocateWithSameInterval(
    originalActivities: Map<String, List<Trip.Activity>>,
    oldStartCal: Calendar,
    newStartCal: Calendar,
    updatedActivities: MutableMap<String, List<Trip.Activity>>
) {
    val dayOffset = calculateDaysBetween(oldStartCal, newStartCal) - 1

    originalActivities.forEach { (oldDateKey, activities) ->
        try {
            val oldActivityDate = parseActivityDate(oldDateKey)
            val newActivityDate = Calendar.getInstance().apply {
                timeInMillis = oldActivityDate.timeInMillis
                add(Calendar.DAY_OF_MONTH, dayOffset)
            }

            val newDateKey = newActivityDate.toStringDate()

            val updatedActivityList = activities.map { activity ->
                activity.copy(date = Timestamp(newActivityDate.time))
            }

            updatedActivities[newDateKey] = updatedActivityList
            Log.d("SmartReallocation", "Moved activities from $oldDateKey to $newDateKey")

        } catch (e: Exception) {
            Log.e("SmartReallocation", "Error processing date $oldDateKey", e)
        }
    }
}

// 情况2: 更长间隔 - 按比例调整活动位置
private fun reallocateWithLongerInterval(
    originalActivities: Map<String, List<Trip.Activity>>,
    oldStartCal: Calendar,
    newStartCal: Calendar,
    updatedActivities: MutableMap<String, List<Trip.Activity>>
) {
    originalActivities.forEach { (oldDateKey, activities) ->
        try {
            val oldActivityDate = parseActivityDate(oldDateKey)

            // 计算在原始行程中的相对位置（第几天）
            val relativeDay = calculateDaysBetween(oldStartCal, oldActivityDate) - 1

            // 在新的日期范围中保持相同的相对位置
            val newActivityDate = Calendar.getInstance().apply {
                timeInMillis = newStartCal.timeInMillis
                add(Calendar.DAY_OF_MONTH, relativeDay)
            }

            val newDateKey = newActivityDate.toStringDate()

            val updatedActivityList = activities.map { activity ->
                activity.copy(date = Timestamp(newActivityDate.time))
            }

            updatedActivities[newDateKey] = updatedActivityList
            Log.d("SmartReallocation", "Reallocated activities from day ${relativeDay + 1} ($oldDateKey) to $newDateKey")

        } catch (e: Exception) {
            Log.e("SmartReallocation", "Error processing date $oldDateKey", e)
        }
    }
}


// 辅助函数：检查是否为时间戳格式
fun isTimestampLong(dateKey: String): Boolean {
    return try {
        dateKey.toLong()
        dateKey.length > 10 // 时间戳通常比较长
    } catch (e: NumberFormatException) {
        false
    }
}



// 删除超出范围的活动（破坏性操作）
private fun deleteOverflowActivities(vm: TripViewModel, newStartCal: Calendar, newEndCal: Calendar) {
    val currentTrip = vm.editTrip
    val updatedActivities = mutableMapOf<String, List<Trip.Activity>>()

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

    currentTrip.activities.forEach { (dateKey, activities) ->
        try {
            val activityDate = parseActivityDate(dateKey)

            if (activityDate.timeInMillis >= newStartDate.timeInMillis &&
                activityDate.timeInMillis <= newEndDate.timeInMillis) {
                updatedActivities[dateKey] = activities
                Log.d("SmartReallocation", "Kept activities for $dateKey (within range)")
            } else {
                Log.d("SmartReallocation", "Deleted activities for $dateKey (outside range)")
            }
        } catch (e: Exception) {
            Log.e("SmartReallocation", "Error processing date $dateKey", e)
        }
    }

    vm.editTrip = currentTrip.copy(
        activities = updatedActivities,
        startDate = Timestamp(newStartCal.time),
        endDate = Timestamp(newEndCal.time)
    )

    vm.setSelectedTrip(vm.editTrip)
}

// 日期验证函数
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

    // 结束日期必须等于或晚于开始日期
    val isValid = endDate.timeInMillis >= startDate.timeInMillis

    Log.d("DateValidation", "Date order is valid: $isValid")
    Log.d("DateValidation", "=== End date validation ===")

    return isValid
}

// 辅助函数：更新行程并导航
private fun updateTripAndNavigate(
    vm: TripViewModel,
    startCalendar: Calendar,
    endCalendar: Calendar,
    navController: NavController,
    selected: MutableList<String>,
    title: String,
    destination: String,
    groupSize: Int,
    imageUri: Uri?
) {
    Log.d("UpdateTrip", ">>> updateTripAndNavigate() called, tripId=${vm.editTrip.id}")

    // 更新行程的其他信息
    vm.editTrip = vm.editTrip.copy(
        typeTravel = selected.toList(),
        title = title,
        destination = destination,
        groupSize = groupSize,
        photo = imageUri?.toString() ?: vm.editTrip.photo,
        startDate = Timestamp(startCalendar.time),
        endDate = Timestamp(endCalendar.time)
    )

    // 🔴 关键修复：同步更新 selectedTrip，确保 ActivitiesList 显示正确的数据
    vm.setSelectedTrip(vm.editTrip)

    // 🔴 确保 userAction 设置正确
    vm.userAction = TripViewModel.UserAction.EDIT_TRIP

    // 🔴 添加活动数据调试日志
    Log.d("EditTrip", "EditTrip activities: ${vm.editTrip.activities}")
    Log.d("EditTrip", "SelectedTrip activities: ${vm.selectedTrip.value.activities}")
    Log.d("EditTrip", "Navigation to activities_list...")

    // 🔴 修复：只保留一个导航调用
    navController.navigate("activities_list")
}
fun smartReallocateActivitiesDirectly(
    vm: TripViewModel,
    oldStartCal: Calendar,
    oldEndCal: Calendar,
    newStartCal: Calendar,
    newEndCal: Calendar
) {
    val oldIntervalDays = calculateDaysBetween(oldStartCal, oldEndCal)
    val newIntervalDays = calculateDaysBetween(newStartCal, newEndCal)

    Log.d("SmartReallocation", "Direct reallocation - Old: $oldIntervalDays days, New: $newIntervalDays days")

    // 🔴 直接处理所有情况，不询问用户
    smartReallocateActivities(vm, oldStartCal, oldEndCal, newStartCal, newEndCal)
}

fun parseActivityDate(dateKey: String): Calendar {
    return try {
        when {
            dateKey.toLongOrNull() != null && dateKey.length > 10 -> {
                Calendar.getInstance().apply {
                    timeInMillis = dateKey.toLong()
                }
            }
            else -> {
                parseDateManually(dateKey)
            }
        }
    } catch (e: Exception) {
        Log.e("DateParsing", "Errore nel parsing della data $dateKey: ${e.message}")
        try {
            parseDateManually(dateKey, "d/M/yyyy")
        } catch (e2: Exception) {
            Log.e("DateParsing", "Tutti i tentativi di parsing sono falliti per $dateKey")
            Calendar.getInstance()
        }
    }
}

private fun parseDateManually(dateString: String, format: String = "YYYY-MM-DD"): Calendar {
    val parts = dateString.split("-", "/")

    if (parts.size != 3) {
        throw IllegalArgumentException("Formato data non valido: $dateString")
    }

    return when (format) {
        "YYYY-MM-DD" -> {
            val anno = parts[0].toInt()
            val mese = parts[1].toInt() - 1
            val giorno = parts[2].toInt()

            Calendar.getInstance().apply {
                set(anno, mese, giorno)
            }
        }
        "d/M/yyyy" -> {
            val giorno = parts[0].toInt()
            val mese = parts[1].toInt() - 1
            val anno = parts[2].toInt()

            Calendar.getInstance().apply {
                set(anno, mese, giorno)
            }
        }
        else -> {
            throw IllegalArgumentException("Formato di parsing non supportato: $format")
        }
    }
}

/*
// 修改 parseActivityDate 函数以更好地处理日期
fun parseActivityDate(dateKey: String, dateFormat: SimpleDateFormat): Calendar {
    return try {
        when {
            // 检查是否为时间戳格式（纯数字且长度大于10）
            dateKey.toLongOrNull() != null && dateKey.length > 10 -> {
                Calendar.getInstance().apply {
                    timeInMillis = dateKey.toLong()
                }
            }
            // 尝试按照标准格式解析
            else -> {
                Calendar.getInstance().apply {
                    val parsedDate = dateFormat.parse(dateKey)
                    if (parsedDate != null) {
                        time = parsedDate
                    } else {
                        throw IllegalArgumentException("Cannot parse date: $dateKey")
                    }
                }
            }
        }
    } catch (e: Exception) {
        Log.e("DateParsing", "Error parsing date $dateKey: ${e.message}")
        // 如果解析失败，尝试其他格式
        try {
            // 尝试 "d/M/yyyy" 格式
            val alternativeFormat = SimpleDateFormat("d/M/yyyy", Locale.US)
            Calendar.getInstance().apply {
                time = alternativeFormat.parse(dateKey) ?: throw IllegalArgumentException()
            }
        } catch (e2: Exception) {
            // 如果所有格式都失败，返回当前日期
            Log.e("DateParsing", "All parsing attempts failed for $dateKey")
            Calendar.getInstance()
        }
    }
}
*/
// 更新的 reallocateWithShorterInterval 函数
private fun reallocateWithShorterInterval(
    originalActivities: Map<String, List<Trip.Activity>>,
    oldStartCal: Calendar,
    newStartCal: Calendar,
    newEndCal: Calendar,
    updatedActivities: MutableMap<String, List<Trip.Activity>>
) {
    Log.d("SmartReallocation", "=== Shorter Interval Reallocation ===")

    // 标准化日期用于比较
    val oldStart = Calendar.getInstance().apply {
        timeInMillis = oldStartCal.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val newStart = Calendar.getInstance().apply {
        timeInMillis = newStartCal.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val newEnd = Calendar.getInstance().apply {
        timeInMillis = newEndCal.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // 计算新行程的天数
    val newTripDays = calculateDaysBetween(newStart, newEnd)
    Log.d("SmartReallocation", "New trip has $newTripDays days")

    // 用于存储溢出活动
    val overflowActivities = mutableListOf<Trip.Activity>()

    // 创建一个映射来存储每天的活动
    val dayToActivitiesMap = mutableMapOf<Int, MutableList<Trip.Activity>>()

    // 处理每个原始活动
    originalActivities.forEach { (oldDateKey, activities) ->
        try {
            val activityDate = parseActivityDate(oldDateKey)

            // 标准化活动日期
            val normalizedActivityDate = Calendar.getInstance().apply {
                timeInMillis = activityDate.timeInMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // 计算这是原始行程的第几天
            val dayNumber = calculateDaysBetween(oldStart, normalizedActivityDate)

            Log.d("SmartReallocation",
                "Processing activities from $oldDateKey (Day $dayNumber)")

            if (dayNumber <= newTripDays) {
                // 如果在新行程范围内，保持在相同的天数
                if (!dayToActivitiesMap.containsKey(dayNumber)) {
                    dayToActivitiesMap[dayNumber] = mutableListOf()
                }
                dayToActivitiesMap[dayNumber]?.addAll(activities)

                Log.d("SmartReallocation",
                    "Keeping Day $dayNumber activities (${activities.size} items)")
            } else {
                // 如果超出新行程范围，添加到溢出活动
                overflowActivities.addAll(activities)
                Log.d("SmartReallocation",
                    "Day $dayNumber exceeds new trip length, adding ${activities.size} activities to overflow")
            }

        } catch (e: Exception) {
            Log.e("SmartReallocation", "Error processing date $oldDateKey: ${e.message}")
            // 错误情况下，将活动添加到溢出
            overflowActivities.addAll(activities)
        }
    }

    // 将活动分配到新的日期
    dayToActivitiesMap.forEach { (dayNumber, activities) ->
        val newDate = Calendar.getInstance().apply {
            timeInMillis = newStart.timeInMillis
            add(Calendar.DAY_OF_MONTH, dayNumber - 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val newDateKey = newDate.toStringDate()

        val updatedActivityList = activities.map { activity ->
            activity.copy(date = Timestamp(newDate.time))
        }

        updatedActivities[newDateKey] = updatedActivityList

        Log.d("SmartReallocation",
            "Assigned ${activities.size} activities to Day $dayNumber ($newDateKey)")
    }

    // 将溢出活动添加到最后一天
    if (overflowActivities.isNotEmpty()) {
        val lastDayKey = newEnd.toStringDate()

        val overflowWithNewDate = overflowActivities.map { activity ->
            activity.copy(date = Timestamp(newEnd.time))
        }

        updatedActivities[lastDayKey] =
            (updatedActivities[lastDayKey] ?: emptyList()) + overflowWithNewDate

        Log.d("SmartReallocation",
            "Added ${overflowActivities.size} overflow activities to last day ($lastDayKey)")
    }

    Log.d("SmartReallocation", "=== Final Distribution ===")
    updatedActivities.forEach { (date, activities) ->
        Log.d("SmartReallocation", "$date: ${activities.size} activities")
    }
}