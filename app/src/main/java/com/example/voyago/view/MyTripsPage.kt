package com.example.voyago.view

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.example.voyago.model.Trip
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel


@SuppressLint("DiscouragedApi")
@Composable
fun MyTripsPage(navController: NavController, vm: TripViewModel, uvm: UserViewModel) {

    //Get the logged in user
    val loggedUser by uvm.loggedUser.collectAsState()
    //List of trip created and published by the logged in user
    val publishedTrips by vm.publishedTrips.collectAsState()
    //List of trip created, but not published by the logged in user
    val privateTrips by vm.privateTrips.collectAsState()
    //List of trip the logged in user joined
    val joinedTrips by vm.joinedTrips.collectAsState()
    //List of trip cancelled by the logged in user
    val canceledTrips by vm.canceledTrips.collectAsState()

    LaunchedEffect(loggedUser.id) {
        if (loggedUser.id != 0) {
            vm.creatorPublicFilter(loggedUser.id)
            vm.creatorPrivateFilter(loggedUser.id)
            vm.tripUserJoined(loggedUser.id)
            vm.loadCanceledTrips(loggedUser.id.toString())
        }
    }


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                vm.newTrip = Trip()
                navController.navigate("create_new_trip")
            }) {
                Icon(Icons.Default.Add, "Add")
            }
        }
    ) { innerPadding ->

        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            //List of published trips created by the logged in user
            item {
                Text(
                    text = "Published Trips:",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            if (publishedTrips.isNotEmpty()) {
                val visiblePublishedTrips = publishedTrips.filter { trip ->
                    !canceledTrips.contains(trip.id.toString())
                }
                items(visiblePublishedTrips, key = { it.id }) { trip ->
                    vm.userAction = TripViewModel.UserAction.VIEW_TRIP
                    TripCard(
                        trip,
                        navController,
                        vm,
                        vm.userAction == TripViewModel.UserAction.EDIT_TRIP,
                        isDraft = false
                    )
                }
            } else {
                item {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("You haven't published any trip yet.")
                    }
                }
            }

            //List of private trips created by the logged in user
            item {
                Text(
                    text = "Private Trips:",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            if (privateTrips.isNotEmpty()) {
                items(privateTrips, key = { it.id }) { trip ->
                    vm.userAction = TripViewModel.UserAction.EDIT_TRIP
                    TripCard(
                        trip,
                        navController,
                        vm,
                        vm.userAction == TripViewModel.UserAction.EDIT_TRIP,
                        isDraft = trip.isDraft
                    )
                }
            } else {
                item {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("You don't have private trips yet.")
                    }
                }
            }

            //List of trips the logged in user (id=1) joined
            item {
                Text(
                    text = "Trips I joined:",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            if (joinedTrips.isNotEmpty()) {
                val visibleJoinedTrips = joinedTrips.filter { trip ->
                    !canceledTrips.contains(trip.id.toString())
                }
                items(visibleJoinedTrips, key = { it.id }) { trip ->
                    vm.userAction = TripViewModel.UserAction.VIEW_TRIP
                    TripCard(trip, navController, vm, false)
                }
            } else {
                item {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("You haven't joined any trip yet.")
                    }
                }
            }
        }
    }
}

//Return true is the string is a Uri
fun String.isUriString(): Boolean {
    return try {
        val uri = this.toUri()
        uri.scheme != null

    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

@Composable
fun CompletedBanner(modifier: Modifier) {
    Box(
        modifier = modifier
            .padding(vertical = 10.dp, horizontal = 10.dp)
            .wrapContentSize()
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = Color(0x32, 0xad, 0xe6, 255)
            ),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TaskAlt,
                contentDescription = "completed",
                tint = Color.White
            )
            Spacer(Modifier.padding(3.dp))
            Text(
                text = "Completed",
                color = Color.White
            )
        }
    }
}

@Composable
fun BookedBanner(modifier: Modifier) {
    Box(
        modifier = modifier
            .padding(vertical = 10.dp, horizontal = 10.dp)
            .wrapContentSize()
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = Color(0xfa, 0xa2, 0x61, 255)
            ),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TaskAlt,
                contentDescription = "booked",
                tint = Color.White
            )
            Spacer(Modifier.padding(3.dp))
            Text(
                text = "Fully booked",
                color = Color.White
            )
        }
    }
}
