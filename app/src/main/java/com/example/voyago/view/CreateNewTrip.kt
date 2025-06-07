package com.example.voyago.view

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.Calendar
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.voyago.model.Trip
import com.example.voyago.model.TypeTravel
import com.example.voyago.viewmodel.TripViewModel
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNewTrip(navController: NavController, vm: TripViewModel) {


    vm.userAction = TripViewModel.UserAction.CREATE_TRIP

    var imageUri by rememberSaveable {mutableStateOf<Uri?>(null)}
    var photoTouched = remember {mutableStateOf(false)}
    var tripImageError by rememberSaveable {mutableStateOf(false)}


    val fieldValues = rememberSaveable(saver = listSaver(
        save = { it.toList() },
        restore = { it.toMutableStateList() }
    )) {
        mutableStateListOf(
            "",
            "",
            "",
            "",
        )
    }
    val fieldNames = listOf("Title", "Destination", "Price Estimated", "Group Size")
    val fieldTouched = remember {mutableStateListOf(false, false, false, false)}
    var fieldErrors = arrayOf(false, false, false, false)


    val typeTravel = listOf("Party", "Adventure", "Culture", "Relax")
    val selected = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) {
        mutableStateListOf<String>()
    }
    var typeTravelError by rememberSaveable { mutableStateOf(false) }

    var startDate by rememberSaveable { mutableStateOf("") }
    var startCalendar by rememberSaveable { mutableStateOf<Calendar?>(null) }

    var endDate by rememberSaveable { mutableStateOf("") }
    var endCalendar by rememberSaveable { mutableStateOf<Calendar?>(null) }

    var dateError by rememberSaveable { mutableStateOf("") }

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

            //Image Trip with Error check
            item {
                TripImage(imageUri = imageUri, onUriSelected = { uri -> imageUri = uri }, photoTouched)
            }

            if (tripImageError && !photoTouched.value) {
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
                    //TextFields with various info
                    fieldValues.forEachIndexed { index, item ->
                        val fieldIsBeenTouched = fieldTouched[index]
                        //Title and Destination Fields
                        if (index == 0 || index == 1) {
                            val textHasErrors = fieldIsBeenTouched && (item.toString().isBlank() ||
                                    !item.toString().any { it.isLetter() })
                            fieldErrors[index] = textHasErrors

                            ValidatingInputTextField(
                                item,
                                {
                                    fieldValues[index] = it
                                    if(!fieldIsBeenTouched) {
                                        fieldTouched[index] = true
                                    }
                                },
                                textHasErrors,
                                fieldNames[index]
                            )
                        } else if (index == 2) { //Price Estimated Field
                            val floatHasErrors = fieldIsBeenTouched && (item.toString().isBlank() ||
                                    item.toString().toDoubleOrNull()?.let { it <= 0.0 } != false ||
                                    !item.toString().matches(Regex("^\\d+(\\.\\d+)?$")))

                            fieldErrors[index] = floatHasErrors

                            ValidatingInputFloatField(
                                item,
                                {
                                    fieldValues[index] = it
                                    if(!fieldIsBeenTouched) {
                                        fieldTouched[index] = true
                                    }
                                },
                                floatHasErrors,
                                fieldNames[index]
                            )
                        } else { //Group Size Field
                            val intHasErrors = fieldIsBeenTouched &&
                                    (item.isBlank() || item.toIntOrNull()?.let { it <= 1 } != false)

                            fieldErrors[index] = intHasErrors

                            ValidatingInputIntField(
                                item,
                                {
                                    fieldValues[index] = it
                                    if(!fieldIsBeenTouched) {
                                        fieldTouched[index] = true
                                    }
                                },
                                intHasErrors,
                                fieldNames[index]
                            )
                        }
                    }
                }
            }

            //Trip Type with Error Check
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

            //Data Selection with Error Check
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
                            startDate = "$d/${m + 1}/$y"
                            startCalendar = Calendar.getInstance().apply {
                                set(y, m, d, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                        }, year, month, day
                    )
                }

                val endDatePickerDialog = remember {
                    DatePickerDialog(
                        context,
                        { _: DatePicker, y: Int, m: Int, d: Int ->
                            endDate = "$d/${m + 1}/$y"
                            endCalendar = Calendar.getInstance().apply {
                                set(y, m, d, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
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
                        OutlinedButton(onClick = { startDatePickerDialog.show() }) {
                            Text("Start Date")
                        }

                        if (startDate.isNotEmpty()) {
                            Text("Start: $startDate", modifier = Modifier.padding(top = 8.dp))
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp)
                    ) {
                        OutlinedButton(onClick = { endDatePickerDialog.show() }) {
                            Text("End Date")
                        }

                        if (endDate.isNotEmpty()) {
                            Text("End: $endDate", modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }

                if (dateError.isNotEmpty() && !validateDateOrder(startCalendar, endCalendar)) {
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
                            vm.userAction = TripViewModel.UserAction.NOTHING
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

                            tripImageError = !imageUri.toString().isUriString()

                            fieldTouched.forEachIndexed { index, _ ->
                                fieldTouched[index] = true
                            }

                            typeTravelError = selected.isEmpty()

                            dateError = if (!validateDateOrder(startCalendar, endCalendar)) {
                                "Start Date and End Date cannot be empty.\n End Date must be after Start Date"
                            } else {
                                ""
                            }

                            if (!tripImageError && !fieldErrors.any{it} && !typeTravelError && validateDateOrder(startCalendar, endCalendar)) {
                                val creatorId = 1

                                val activities = mutableMapOf<String, MutableList<Trip.Activity>>()

                                val newTrip = Trip(
                                    title = fieldValues[0],
                                    destination = fieldValues[1],
                                    startDate = Timestamp(startCalendar!!.time),
                                    endDate = Timestamp(endCalendar!!.time),
                                    estimatedPrice = fieldValues[2].toDouble(),
                                    groupSize = fieldValues[3].toInt(),
                                    activities = activities,
                                    typeTravel = selected.map { TypeTravel.valueOf(it.uppercase()).toString() },
                                    creatorId = creatorId,
                                    published = false,
                                    id = -1,
                                    participants = emptyMap(),
                                    status = Trip.TripStatus.NOT_STARTED.toString(),
                                    appliedUsers = emptyMap(),
                                    rejectedUsers = emptyMap()
                                )

                                vm.newTrip = newTrip
                                vm.setSelectedTrip(newTrip)
                                vm.userAction = TripViewModel.UserAction.CREATE_TRIP
                                navController.navigate("activities_list")
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

fun validateDateOrder(start: Calendar?, end: Calendar?): Boolean {
    return start != null && end != null && end.after(start)
}

@Composable
fun TripImage(imageUri: Uri?, onUriSelected: (Uri?) -> Unit, photoTouched: MutableState<Boolean>) {
    val context = LocalContext.current

    val pickMedia = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri ->
        onUriSelected(uri)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {

        if (imageUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Selected Trip Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            photoTouched.value = true
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Placeholder Add Photo Icon",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap icon to add photo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            photoTouched.value = false
        }

        IconButton(
            onClick = {
                pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            },
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ValidatingInputTextField(text: String, updateState: (String) -> Unit,
                             validatorHasErrors: Boolean, label: String) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .wrapContentSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    keyboardController?.hide()
                })
            }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            value = text,
            onValueChange = updateState,
            label = { Text(label) },
            isError = validatorHasErrors,
            supportingText = {
                if (validatorHasErrors) {
                    Text("This field cannot be empty and cannot contains only numbers")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
    }
}

@Composable
fun ValidatingInputFloatField(text:String, updateState: (String) -> Unit, validatorHasErrors: Boolean, label: String) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .wrapContentSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    keyboardController?.hide()
                })
            }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            value = text,
            onValueChange = updateState,
            label = { Text(label) },
            isError = validatorHasErrors,
            supportingText = {
                if (validatorHasErrors) {
                    Text("This field cannot be empty and must be a number greater that 0.0")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
fun ValidatingInputIntField(text:String, updateState: (String) -> Unit, validatorHasErrors: Boolean, label: String) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .wrapContentSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    keyboardController?.hide()
                })
            }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            value = text,
            onValueChange = updateState,
            label = { Text(label) },
            isError = validatorHasErrors,
            supportingText = {
                if (validatorHasErrors) {
                    Text("This field cannot be empty and must be an integer number greater than 1")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

