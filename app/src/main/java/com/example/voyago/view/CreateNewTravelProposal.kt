package com.example.voyago.view

import android.app.DatePickerDialog
import android.widget.DatePicker
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voyago.activities.*
import java.util.Calendar
import androidx.compose.ui.text.font.FontStyle
import com.example.voyago.model.Trip
import com.example.voyago.model.TypeTravel
import com.example.voyago.viewmodel.TripListViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTravelProposal(navController: NavController, vm: TripListViewModel) {

    var tripName by rememberSaveable {mutableStateOf("")}
    var destination by rememberSaveable {mutableStateOf("")}

    var price by rememberSaveable {mutableStateOf("")}
    var priceError by rememberSaveable {mutableStateOf(false)}
    var priceErrorMessage by rememberSaveable {mutableStateOf("")}

    var groupSize by rememberSaveable {mutableStateOf("")}
    var groupSizeError by rememberSaveable {mutableStateOf(false)}
    var groupSizeErrorMessage by rememberSaveable {mutableStateOf("")}

    val typeTravel = listOf("Party", "Adventure", "Culture", "Relax")
    val selected = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) {
        mutableStateListOf<String>()
    }

    var startDate by rememberSaveable { mutableStateOf("") }
    var startCalendar by rememberSaveable { mutableStateOf<Calendar?>(null) }

    var endDate by rememberSaveable { mutableStateOf("") }
    var endCalendar by rememberSaveable { mutableStateOf<Calendar?>(null) }

    var dateError by rememberSaveable { mutableStateOf("") }




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
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }

                item {


                    TextField(
                        value = tripName,
                        onValueChange = { tripName = it },
                        label = { Text("Trip name") },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {


                    TextField(
                        value = destination,
                        onValueChange = { destination = it },
                        label = { Text("Destination") },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {

                    TextField(
                        value = price,
                        onValueChange = {
                            price = it
                            priceError = false
                        },
                        label = { Text("Price estimate") },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        isError = priceError,
                        supportingText = {
                            if (priceError) {
                                Text(
                                    text = priceErrorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    )
                }


                item {


                    TextField(
                        value = groupSize,
                        onValueChange = {
                            groupSize = it
                            groupSizeError = false
                        },
                        label = { Text("Group Size") },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        isError = groupSizeError,
                        supportingText = {
                            if (groupSizeError) {
                                Text(
                                    text = groupSizeErrorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

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
                            //fontSize = 10.sp
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


                item {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                    ) {

                        Button(
                            onClick = {
                                navController.navigate("main_page")
                            },
                            modifier = Modifier
                                .width(160.dp)
                                .height(60.dp)
                                .padding(top = 16.dp)
                        ) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                if (!validatePrice(price)) {
                                    priceError = true
                                    priceErrorMessage = "Price must be greater than zero"
                                } else if (!validateGroupSize(groupSize)) {
                                    groupSizeError = true
                                    groupSizeErrorMessage = "Group size must be greater than 1"
                                } else if (!validateDateOrder(startCalendar, endCalendar)) {
                                    priceError = false
                                    priceErrorMessage = ""
                                    dateError = "End date must be after start date"
                                } else {
                                    priceError = false
                                    priceErrorMessage = ""
                                    dateError = ""

                                    val creatorId = 1

                                    if (vm.currentTrip == null) {
                                        val activities = mutableMapOf<Calendar, MutableList<Trip.Activity>>()

                                        val newTrip = Trip(
                                            photo = "",
                                            title = tripName,
                                            destination = destination,
                                            startDate = startCalendar!!,
                                            endDate = endCalendar!!,
                                            estimatedPrice = price.toDouble(),
                                            groupSize = groupSize.toInt(),
                                            activities = activities,
                                            typeTravel = selected.map { TypeTravel.valueOf(it.uppercase()) },
                                            creatorId = creatorId,
                                            published = false,
                                            id = 99,
                                            participants = emptyList(),
                                            status = Trip.TripStatus.NOT_STARTED,
                                            appliedUsers = emptyList(),
                                            reviews = emptyList()
                                        )

                                        vm.addNewTrip(newTrip)

                                    } else {

                                        val currentTrip = vm.currentTrip

                                        if (currentTrip != null){
                                            val newTrip = Trip(
                                                photo = "",
                                                title = tripName,
                                                destination = destination,
                                                startDate = startCalendar!!,
                                                endDate = endCalendar!!,
                                                estimatedPrice = price.toDouble(),
                                                groupSize = groupSize.toInt(),
                                                activities = currentTrip.activities,
                                                typeTravel = selected.map { TypeTravel.valueOf(it.uppercase()) },
                                                creatorId = currentTrip.creatorId,
                                                published = currentTrip.published,
                                                id = currentTrip.id,
                                                participants = currentTrip.participants,
                                                status = Trip.TripStatus.NOT_STARTED,
                                                appliedUsers = currentTrip.appliedUsers,
                                                reviews = currentTrip.reviews
                                            )

                                            vm.editNewTrip(newTrip)
                                        }


                                    }


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
}


fun validatePrice(price: String): Boolean {
    return price.toDoubleOrNull()?.let { it > 0 } == true
}

fun validateGroupSize(groupSize: String): Boolean {
    return groupSize.toIntOrNull()?.let { it > 1.0 } == true
}

fun validateDateOrder(start: Calendar?, end: Calendar?): Boolean {
    return start != null && end != null && end.after(start)
}


