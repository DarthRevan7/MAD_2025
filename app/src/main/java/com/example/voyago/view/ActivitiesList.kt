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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voyago.activities.*
import com.example.voyago.model.Trip
import com.example.voyago.viewmodel.TripListViewModel
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import java.util.Calendar


fun allDaysHaveActivities(trip: Trip?): Boolean {

    if(trip != null) {

        return trip.activities.values.all { list -> list.isNotEmpty() }
    }

    return false

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesList(navController: NavController, vm: TripListViewModel) {


    val selectedTrip = vm.currentTrip

    var showIncompleteDialog by rememberSaveable { mutableStateOf(false) }

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
                        ActivitiesListContent(it, vm, navController)
                    } ?: run {
                        Text("No trip selected.", modifier = Modifier.padding(16.dp))
                    }
                }


                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }

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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
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
                                    if (allDaysHaveActivities(selectedTrip)) {
                                        //Save the editing
                                        //Editing is saved already (!)
                                        //Go to the owned travel proposal
                                        navController.navigate("owned_travel_proposal_list") {
                                            popUpTo("owned_travel_proposal_list") {
                                                inclusive = false // don't remove page1
                                            }
                                            launchSingleTop = true // avoid multiple copies of page1 on top
                                        }
                                        /*
                                        navController.popBackStack("owned_travel_proposal_list",
                                            inclusive = false,
                                            saveState = true
                                            )

                                         */
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

                        if (showIncompleteDialog) {
                            Text(
                                text = "Each day of the trip must have at least one activity.",
                                color = Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                            )
                        }
                    }
                }


            }
        }
    }
}



@Composable
fun ActivitiesListContent(trip: Trip, vm: TripListViewModel, navController: NavController){
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
                val dayIndex =
                    ((day.timeInMillis - trip.startDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt() + 1
                val activitiesForDay = trip.activities[day] ?: emptyList()

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Day $dayIndex",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF555555)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    var activityToDelete by rememberSaveable { mutableStateOf<Trip.Activity?>(null) }

                    activitiesForDay.forEach { activity ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth()
                        ) {

                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Activity",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        navController.navigate("edit_Activity/${activity.id}")
                                    }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${activity.time} - ${activity.description}" +
                                            if (activity.isGroupActivity) " (group activity)" else "",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            OutlinedButton(
                                onClick = { activityToDelete = activity },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Delete", color = Color.Red)
                            }
                        }
                    }

                    activityToDelete?.let { activity ->
                        AlertDialog(
                            onDismissRequest = { activityToDelete = null },
                            title = { Text("Delete Activity") },
                            text = { Text("Are you sure you want to delete this activity?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    vm.deleteActivity(activity)
                                    activityToDelete = null
                                }) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { activityToDelete = null }) {
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








