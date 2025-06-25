package com.example.voyago.view

import android.annotation.SuppressLint
import android.util.Log
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
import com.example.voyago.viewmodel.ReviewViewModel
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel


@SuppressLint("DiscouragedApi")
@Composable
fun MyTripsPage(
    navController: NavController,
    vm: TripViewModel,
    uvm: UserViewModel,
    rvm: ReviewViewModel
) {

    // Get the currently logged-in user from the UserViewModel
    val loggedUser by uvm.loggedUser.collectAsState()

    //Trips created and published by the user
    val publishedTrips by vm.publishedTrips.collectAsState()
    //Trips created but not published (private/draft trips)
    val privateTrips by vm.privateTrips.collectAsState()
    //Trips that the user has joined
    val joinedTrips by vm.joinedTrips.collectAsState()
    //Trips that the user created but canceled
    val canceledTrips by vm.canceledTrips.collectAsState()

    // When the logged-in user changes, fetch all trip-related data
    LaunchedEffect(loggedUser.id) {
        if (loggedUser.id != 0) {
            vm.creatorPublicFilter(loggedUser.id)           // Load user's published trips
            vm.creatorPrivateFilter(loggedUser.id)          // Load user's private trips
            vm.tripUserJoined(loggedUser.id)                // Load trips user joined
            vm.loadCanceledTrips(loggedUser.id.toString())  // Load canceled trips by user
        }
    }

    // Scaffold provides layout structure with a floating action button
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                vm.newTrip = Trip()                         // Reset the trip creation form
                vm.userAction = TripViewModel.UserAction.CREATE_TRIP  // Set action for new trip

                navController.navigate("create_new_trip")   // Navigate to trip creation screen
            }) {
                Icon(Icons.Default.Add, "Add")      // Plus icon for FAB
            }
        }
    ) { innerPadding ->

        val listState = rememberLazyListState()      // For maintaining scroll position

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {

            //PUBLISHED TRIPS SECTION
            item {
                Text(
                    text = "Published Trips:",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            if (publishedTrips.isNotEmpty()) {
                // Filter out canceled trips from published list
                val visiblePublishedTrips = publishedTrips.filter { trip ->
                    !canceledTrips.contains(trip.id.toString())
                }
                items(visiblePublishedTrips, key = { it.id }) { trip ->
                    vm.userAction = TripViewModel.UserAction.VIEW_TRIP
                    TripCard(
                        trip = trip,
                        navController = navController,
                        vm = vm,
                        edit = false,        //  Published trips can be edited by creator
                        isDraft = false,    //  Published trips are not drafts
                        owner = true,       //  User is the owner
                        uvm = uvm,
                        rvm = rvm,
                        onEditTrip = { tripToEdit ->  //  NEW: Add edit callback
                            vm.startEditingTrip(tripToEdit)
                            navController.navigate("edit_trip")
                        }
                    )
                }
            } else {
                // Show fallback message if no published trips
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

            //PRIVATE TRIPS SECTION
            item {
                Text(
                    text = "Private Trips:",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            if (privateTrips.isNotEmpty()) {
                items(privateTrips, key = { it.id }) { trip ->
                    //  关键修复：检查是否正在编辑这个trip
                    Log.d("MyTripsPage", "Rendering trip ${trip.id}: trip.isDraft=${trip.isDraft}, vm.editTrip.isDraft=${vm.editTrip.isDraft}")

                    val currentTrip = if (vm.userAction == TripViewModel.UserAction.EDIT_TRIP && trip.id == vm.editTrip.id) {
                        Log.d("MyTripsPage", "Using editTrip for trip ${trip.id}")
                        vm.editTrip
                    } else {
                        Log.d("MyTripsPage", "Using StateFlow trip for trip ${trip.id}")
                        trip
                    }

                    TripCard(
                        trip = currentTrip,  //  使用正确的trip对象
                        navController = navController,
                        vm = vm,
                        edit = true,
                        isDraft = currentTrip.isDraft,  // 🔥 使用正确trip的isDraft状态
                        owner = true,
                        uvm = uvm,
                        rvm = rvm,
                        onEditTrip = { tripToEdit ->
                            vm.startEditingTrip(tripToEdit)
                            navController.navigate("edit_trip")
                        }
                    )
                }
            } else {
                // Show fallback message if no private trips
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

            //JOINED TRIPS SECTION
            item {
                Text(
                    text = "Trips I joined:",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            if (joinedTrips.isNotEmpty()) {
                // Exclude any canceled trips from the joined list
                val visibleJoinedTrips = joinedTrips.filter { trip ->
                    !canceledTrips.contains(trip.id.toString())
                }
                items(visibleJoinedTrips, key = { it.id }) { trip ->
                    vm.userAction = TripViewModel.UserAction.VIEW_TRIP
                    TripCard(
                        trip = trip,
                        navController = navController,
                        vm = vm,
                        edit = false,       //  User cannot edit trips they joined
                        isDraft = false,
                        owner = true,       //  Still show notifications for joined trips
                        uvm = uvm,
                        rvm = rvm,
                        onEditTrip = null   //  NEW: No edit callback for joined trips
                    )
                }
            } else {
                // Show fallback message if no joined trips
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
        // Attempt to convert the string into a URI object
        val uri = this.toUri()
        // Check if the URI has a non-null scheme
        uri.scheme != null

    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

// A composable that displays a banner indicating a completed state
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
        // Horizontal row to align the icon and text side by side
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task checkmark icon indicating success/completion
            Icon(
                imageVector = Icons.Default.TaskAlt,
                contentDescription = "completed",
                tint = Color.White
            )

            // Small space between icon and text
            Spacer(Modifier.padding(3.dp))

            // The "Completed" label text
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
        // Row to place the icon and text horizontally
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Row to place the icon and text horizontally
            Icon(
                imageVector = Icons.Default.TaskAlt,
                contentDescription = "booked",
                tint = Color.White
            )

            // Space between the icon and the label
            Spacer(Modifier.padding(3.dp))

            // The "Fully booked" text message
            Text(
                text = "Fully booked",
                color = Color.White
            )
        }
    }
}
