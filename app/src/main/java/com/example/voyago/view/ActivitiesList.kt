package com.example.voyago.view


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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voyago.activities.*
import com.example.voyago.model.Trip
import com.example.voyago.viewmodel.TripListViewModel
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesList(navController: NavController, vm: TripListViewModel) {


    val selectedTrip = vm.currentTrip


    //val activities = selectedTrip?.let { vm.getActivities(it) } ?: emptyList()

    //val sortedDays = selectedTrip?.activities?.keys?.sortedBy { it.timeInMillis } ?: emptyList()



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
                    selectedTrip?.let {
                        ActivitiesListContent(it)
                    } ?: run {
                        Text("No trip selected.", modifier = Modifier.padding(16.dp))
                    }
                }


                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }

                item{
                    Button(
                        onClick = {
                            navController.navigate("new_activity")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .width(200.dp)
                            .height(50.dp)
                        //.padding(top = 16.dp)
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                    ) {

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

                        Button(
                            onClick = {

                                navController.navigate("main_page")


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

@Composable
fun ActivityItem(activity: Trip.Activity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Activity on ${activity.date.time}", fontSize = 18.sp)
        Text(text = activity.time, fontSize = 16.sp)
        Text(text = activity.description, fontSize = 14.sp)
    }
}

@Composable
fun ActivitiesListContent(trip: Trip) {
    val sortedDays = trip.activities.keys.sortedBy { it.timeInMillis }

    // Check if all activity lists are empty
    val hasNoActivities = trip.activities.values.all { it.isEmpty() }

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
                val dayIndex = ((day.timeInMillis - trip.startDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt() + 1
                val activitiesForDay = trip.activities[day] ?: emptyList()

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
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Schedule,
                                contentDescription = "activity time",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${activity.time} - ${activity.description}" +
                                        if (activity.isGroupActivity) " (group activity)" else "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}








