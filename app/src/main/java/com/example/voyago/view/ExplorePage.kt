package com.example.voyago.view

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.voyago.R
import com.example.voyago.model.Trip
import com.example.voyago.viewmodel.Factory
import com.example.voyago.viewmodel.ReviewViewModel
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel


// ExplorePage is the main screen to browse and filter published trips.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorePage(
    navController: NavController, vm: TripViewModel = viewModel(factory = Factory),
    uvm: UserViewModel, rvm: ReviewViewModel
) {

    // Observe filtered trip list from the ViewModel
    val filteredTrips by vm.filteredList.collectAsState()

    // Side-effect to update and apply filters when any filter input changes
    LaunchedEffect(
        vm.filterDestination,
        vm.filterMinPrice,
        vm.filterMaxPrice,
        vm.filterDuration,
        vm.filterGroupSize,
        vm.filtersTripType,
        vm.filterCompletedTrips,
        vm.filterBySeats
    ) {
        // Fetch the latest published trips and reset price bounds
        vm.updatePublishedTrip(uvm.loggedUser.value.id)
        vm.setMaxMinPrice()

        // Reset filters only if not coming from a search or view action
        if (vm.userAction != TripViewModel.UserAction.SEARCHING && vm.userAction != TripViewModel.UserAction.VIEW_TRIP) {
            vm.resetFilters()
        }

        // Re-apply the current filters
        vm.applyFilters(uvm.loggedUser.value.id)
    }

    // Grid layout to display trips
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),   // 2 columns per row
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Top item: Filters button spans full width
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                // Button to navigate to filter selection screen
                Button(
                    onClick = {
                        vm.userAction = TripViewModel.UserAction.FILTER_SELECTION
                        navController.navigate("filters_selection")
                    },
                    border = BorderStroke(1.dp, Color.Black),
                    shape = RoundedCornerShape(10.dp),// Border with rounded corne
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(193, 165, 195), // Change background color
                        contentColor = Color.Black // Change text color
                    )
                ) {
                    Text("Filters")
                }
            }
        }

        // Display message if no trips match the selected filters
        if (filteredTrips.isEmpty()) {
            item {
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text("No trips for the selected filters.")
                }
            }
        } else {
            // Show each trip using TripCard
            items(filteredTrips) { trip ->
                TripCard(trip, navController, vm, false, false, false, uvm, rvm)
            }
        }
    }
}

// Displays a single trip card with image, title, destination, and status indicators.
@SuppressLint("DiscouragedApi")
@Composable
fun TripCard(
    trip: Trip,
    navController: NavController,
    vm: TripViewModel,
    edit: Boolean,
    isDraft: Boolean = false, // Whether it is in draft state, the default value is false
    owner: Boolean,
    uvm: UserViewModel,
    rvm: ReviewViewModel,
    onEditTrip: ((Trip) -> Unit)? = null  // Callback for editing
) {

    // Get the logged in user from the viewmodel
    val loggedUser by uvm.loggedUser.collectAsState()

    // Whether current user reviewed this trip
    val reviewedTrips by rvm.reviewedMap.collectAsState()
    val isReviewed = reviewedTrips[trip.id] == true

    // Local Coroutine scope
    val localScope = rememberCoroutineScope()

    // Check if the trip is been reviewed by the user
    LaunchedEffect(trip.id) {
        if (!reviewedTrips.containsKey(trip.id)) {
            rvm.checkIfReviewed(loggedUser.id, trip.id, localScope)
        }
    }

    //Clicking on the card the user goes to the page that show the details of the trip
    Card(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 10.dp)
            .fillMaxWidth()
            .height(200.dp),
        shape = CardDefaults.elevatedShape,
        onClick = {
            vm.setSelectedTrip(trip) // Set the trip as Selected Trip
            vm.userAction = TripViewModel.UserAction.VIEW_TRIP // Set the user action to VIEW_TRIP
            navController.navigate("trip_details") // Navigate to trip details
        }
    ) {
        // Trip cover image
        Box {
            TripImageExplore(trip)

            // Show DRAFT label if the trip is a draft
            if (isDraft) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(
                            color = Color(0xFFFF9800).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "DRAFT",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Trip destination and title overlay at bottom-left
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(vertical = 10.dp, horizontal = 10.dp)
                    .background(
                        color = if (isDraft) {
                            Color(0xAA666666)
                        } else {
                            Color(0xAA444444)
                        },
                        shape = MaterialTheme.shapes.small
                    )
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Destination name (append "(Copy)" if draft)
                    Text(
                        text = if (isDraft) "${trip.destination} (Copy)" else trip.destination,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    // Trip title
                    Text(
                        text = trip.title,
                        color = Color.White
                    )
                }
            }

            // Show edit button if the trip can be edited
            if (edit) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp, horizontal = 10.dp)
                        .align(alignment = Alignment.TopEnd)
                        .wrapContentSize()
                        .background(
                            color = if (isDraft) {
                                Color(0xe6, 0xe0, 0xe9, 200)
                            } else {
                                Color(0xe6, 0xe0, 0xe9, 255)
                            },
                            shape = MaterialTheme.shapes.small
                        )
                ) {
                    val painterEdit = painterResource(R.drawable.edit)
                    Image(
                        painter = painterEdit,
                        contentDescription = "edit",
                        modifier = Modifier
                            .size(35.dp)
                            .clickable {
                                // 🔥 KEY FIX: Use proper edit handling
                                if (onEditTrip != null) {
                                    // Use the callback if provided (from MyTripsPage)
                                    onEditTrip(trip)
                                } else {
                                    // Fallback: use the new startEditingTrip method directly
                                    vm.startEditingTrip(trip)
                                    navController.navigate("edit_trip")
                                }
                            },
                        colorFilter = if (isDraft) {
                            androidx.compose.ui.graphics.ColorFilter.tint(Color.Gray) // If is a draft the icon turns grey
                        } else null // Otherwise keep the original color
                    )
                }
            }

            //If the trip has already happened
            if (trip.status == Trip.TripStatus.COMPLETED.toString()) {
                //Banner that indicated that the trip has already happened
                CompletedBanner(Modifier.align(Alignment.TopEnd)) // Align to the top right corner
            } else if (!trip.canJoin() && !isDraft) { // If the trip it is not is a draft state and cannot be joined
                //Banner that shows that nobody can join the trip anymore
                BookedBanner(Modifier.align(Alignment.TopEnd)) // Align to top right corner
            }

            // If we are in "MyTrips" section
            if (owner) {
                val applications = trip.appliedUsers.isNotEmpty() && trip.creatorId == loggedUser.id
                val review = trip.status == Trip.TripStatus.COMPLETED.toString() && !isReviewed

                // Red dot in bottom right corner of card
                if (applications || review) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(12.dp)
                            .background(color = Color.Red, shape = CircleShape)
                    )
                }
            }
        }
    }
}

// Displays the main photo of a trip using Glide for image loading
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun TripImageExplore(trip: Trip) {
    // Mutable state to hold the image URL once it's loaded
    var imageUrl by remember { mutableStateOf<String?>(null) }

    // When the trip.photo value changes, launch a coroutine to fetch the photo URL
    LaunchedEffect(trip.photo) {
        // Asynchronously load image URL
        imageUrl = trip.getPhoto()
    }

    // Only display the image if the URL is valid and non-blank
    if (imageUrl != null && imageUrl!!.isNotBlank()) {
        GlideImage(
            model = imageUrl,
            contentDescription = "Trip Photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
