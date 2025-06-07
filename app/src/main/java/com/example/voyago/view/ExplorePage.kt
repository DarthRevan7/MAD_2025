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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.voyago.R
import com.example.voyago.model.Trip
import com.example.voyago.viewmodel.Factory
import com.example.voyago.viewmodel.TripViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorePage(navController: NavController, vm: TripViewModel = viewModel(factory = Factory)) {

    val filteredTrips by vm.filteredList.collectAsState()

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
        vm.updatePublishedTrip()
        vm.setMaxMinPrice()

        if (vm.userAction != TripViewModel.UserAction.SEARCHING && vm.userAction != TripViewModel.UserAction.VIEW_TRIP) {
            vm.resetFilters()
        }
        vm.applyFilters()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                //Filters Button
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

        //List of trips
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
            items(filteredTrips) { trip ->
                TripCard(trip, navController, vm, false)
            }
        }
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun TripCard(trip: Trip, navController: NavController, vm: TripViewModel, edit: Boolean) {
    //Clicking on the card the user goes to the page that show the details of the trip
    Card(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 10.dp)
            .fillMaxWidth()
            .height(200.dp),
        shape = CardDefaults.elevatedShape,
        onClick = {
            vm.setSelectedTrip(trip)
            vm.userAction = TripViewModel.UserAction.VIEW_TRIP
            navController.navigate("trip_details")

        }
    ) {
        Box {
            TripImageExplore(trip)

            //Destination and Title information
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(vertical = 10.dp, horizontal = 10.dp)
                    .background(
                        color = Color(0xAA444444),
                        shape = MaterialTheme.shapes.small
                    )
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = trip.destination,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = trip.title,
                        color = Color.White
                    )
                }

            }

            //If the trip can be edit
            if (edit) {
                //Edit button that send the user to the edit page
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp, horizontal = 10.dp)
                        .align(alignment = Alignment.TopEnd)
                        .wrapContentSize()
                        .background(
                            color = Color(0xe6, 0xe0, 0xe9, 255),
                            shape = MaterialTheme.shapes.small
                        )
                ) {
                    val painterEdit = painterResource(R.drawable.edit)
                    Image(
                        painter = painterEdit, "edit", modifier = Modifier
                            .size(35.dp)
                            .clickable {
                                vm.editTrip = trip
                                vm.userAction = TripViewModel.UserAction.EDIT_TRIP
                                navController.navigate("edit_trip")
                            }
                    )
                }
            }

            //If the trip has the max number of participants or it's already started
            if (trip.status == Trip.TripStatus.COMPLETED.toString()) {
                //Banner that indicated that the trip has already happened
                CompletedBanner(Modifier.align(Alignment.TopEnd))
            } else if (!trip.canJoin()) {
                //Banner that shows that nobody can join the trip anymore
                BookedBanner(Modifier.align(Alignment.TopEnd))
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun TripImageExplore(trip: Trip) {
    val context = LocalContext.current
    var imageUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(trip.photo) {
        imageUrl = trip.getPhoto()
    }
    if (imageUrl != null && imageUrl!!.isNotBlank()) {
        GlideImage(
            model = imageUrl,
            contentDescription = "Trip Photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
