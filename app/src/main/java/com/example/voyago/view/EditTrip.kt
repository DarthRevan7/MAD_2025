package com.example.voyago.view

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.net.Uri
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
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.voyago.model.Trip
import com.example.voyago.model.TypeTravel
import com.example.voyago.model.toCalendar
import com.example.voyago.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun initUri(vm: TripViewModel): String {
    val trip = vm.selectedTrip.value
    if (vm.editTrip.isValid()) {
        return trip.photo
    }
    return "placeholder_photo"
}


@Composable
fun EditTrip(navController: NavController, vm: TripViewModel) {
    val trip = vm.editTrip
    vm.userAction = TripViewModel.UserAction.EDIT_TRIP

    LaunchedEffect(
        Unit
    ) { initUri(vm = vm) }

    var imageUri by rememberSaveable {
        mutableStateOf<Uri?>(
            if (trip.photo.isUriString()) {
                trip.photo.toUri()
            } else {
                null
            }
        )
    }
    var tripImageError by rememberSaveable {mutableStateOf(false)}

    val fieldValues = rememberSaveable(saver = listSaver(
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
    var startDate by rememberSaveable { mutableStateOf(toCalendar(trip.startDate).toStringDate()) }
    var startCalendar by rememberSaveable { mutableStateOf<Calendar?>(toCalendar(trip.startDate)) }

    var endDate by rememberSaveable { mutableStateOf(toCalendar(trip.endDate).toStringDate()) }
    var endCalendar by rememberSaveable { mutableStateOf<Calendar?>(toCalendar(trip.endDate)) }

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

            //Trip Image
            item {
                TripImageEdit(
                    trip = trip,
                    imageUri = imageUri,
                    onUriSelected = { uri ->
                        imageUri = uri
                    }
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
                    //TextFields with various info
                    fieldValues.forEachIndexed { index, item ->
                        //Title and Destination Fields
                        if (index == 0 || index == 1) {
                            val textHasErrors = item.toString().isBlank() ||
                                    !item.toString().any { it.isLetter() }
                            fieldErrors[index] = textHasErrors

                            ValidatingInputTextField(
                                item.toString(),
                                {
                                    fieldValues[index] = it
                                },
                                textHasErrors,
                                fieldNames[index]
                            )
                        } else if (index == 2) { //Price Estimated Field
                            val floatHasErrors = item.toString().isBlank() ||
                                    item.toString().toDoubleOrNull()?.let { it <= 0.0 } != false ||
                                    !item.toString().matches(Regex("^\\d+(\\.\\d+)?$"))

                            fieldErrors[index] = floatHasErrors

                            ValidatingInputFloatField(
                                item.toString(),
                                {
                                    fieldValues[index] = it
                                },
                                floatHasErrors,
                                fieldNames[index]
                            )
                        } else { //Group Size Field
                            val intHasErrors = (item.toString().isBlank() || item.toString().toIntOrNull()?.let { it <= 1 } != false)

                            fieldErrors[index] = intHasErrors

                            ValidatingInputIntField(
                                item.toString(),
                                {
                                    fieldValues[index] = it
                                },
                                intHasErrors,
                                fieldNames[index]
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
                        OutlinedButton(onClick = { endDatePickerDialog.show() }) {
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

                            typeTravelError = selected.isEmpty()

                            dateError = if (!validateDateOrder(startCalendar, endCalendar)) {
                                "Start Date and End Date cannot be empty.\n End Date must be after Start Date"
                            } else {
                                ""
                            }

                            if (!tripImageError && !fieldErrors.any{it} && !typeTravelError && validateDateOrder(startCalendar, endCalendar)) {
                                if(vm.userAction == TripViewModel.UserAction.EDIT_TRIP) {

                                    val currentTrip = vm.editTrip

                                    if (currentTrip.isValid()) {
                                        val updatedTrip = Trip(
                                            photo = imageUri?.toString() ?: trip.photo,
                                            title = fieldValues[0].toString(),
                                            destination = fieldValues[1].toString(),
                                            startDate = startCalendar!!.timeInMillis,
                                            endDate = endCalendar!!.timeInMillis,
                                            estimatedPrice = fieldValues[2].toString().toDouble(),
                                            groupSize = fieldValues[3].toString().toInt(),
                                            activities = currentTrip.activities,
                                            typeTravel = selected.map { TypeTravel.valueOf(it.uppercase()).toString() },
                                            creatorId = currentTrip.creatorId,
                                            published = currentTrip.published,
                                            id = currentTrip.id,
                                            participants = currentTrip.participants,
                                            status = currentTrip.status,
                                            appliedUsers = currentTrip.appliedUsers,
                                            rejectedUsers = currentTrip.rejectedUsers
                                        )

                                        vm.editTrip = updatedTrip
                                        vm.setSelectedTrip(updatedTrip)
                                        vm.editExistingTrip(vm.editTrip) { success ->
                                            if (success) {
                                                //Go to the list of activities
                                                navController.navigate("activities_list")
                                            }
                                        }
                                    }
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

}

fun Calendar.toStringDate(): String {
    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return format.format(this.time)
}

@SuppressLint("DiscouragedApi")
@Composable
fun TripImageEdit(trip:Trip, imageUri: Uri?, onUriSelected: (Uri?) -> Unit) {
    val context = LocalContext.current

    //Returns result of media loader
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
        if (imageUri.toString().isUriString()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Selected Trip Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(
                            LocalContext.current.resources.getIdentifier(
                                trip.photo,
                                "drawable",
                                LocalContext.current.packageName
                            )
                        )
                        .crossfade(true)
                        .build(),
                    contentDescription = trip.destination,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }

        //Icon Button select photo
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