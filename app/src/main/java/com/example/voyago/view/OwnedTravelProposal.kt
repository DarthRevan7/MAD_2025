package com.example.voyago.view

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.voyago.R
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voyago.activities.BottomBar
import com.example.voyago.activities.TopBar
import com.example.voyago.viewmodel.Factory
import com.example.voyago.viewmodel.TripListViewModel
import androidx.compose.runtime.collectAsState
import com.example.voyago.model.Trip
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import androidx.compose.foundation.lazy.items


@SuppressLint("DiscouragedApi")
@Composable
fun OwnedTravelProposalList(navController: NavController, vm: TripListViewModel) {

    val publishedTrips by vm.publishedTrips.collectAsState()
    val privateTrips by vm.privateTrips.collectAsState()

    LaunchedEffect(Unit) {
        vm.creatorPublicFilter(1)
        vm.creatorPrivateFilter(1)
    }


    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(1)
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
            item {
                Text(
                    text = "Published Trips:",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            items(publishedTrips, key = { it.id }) { trip ->
                TripCard(trip, navController, vm)
            }

            item {
                Text(
                    text = "Private Trips:",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            items(privateTrips, key = { it.id }) { trip ->
                TripCard(trip, navController, vm)
            }
        }
    }
}

@Composable
fun TripCard(trip: Trip, navController: NavController, vm: TripListViewModel) {
    Card(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 10.dp)
            .fillMaxWidth(),
        shape = CardDefaults.elevatedShape,
        onClick = {
            vm.selectTrip(trip)
            navController.navigate("travel_proposal_details")

        }
    ) {
        Box {
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
                    .fillMaxWidth()
                    .height(200.dp)
            )
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
                            vm.selectTrip(trip)
                            navController.navigate("edit_travel_proposal")
                        }
                )
            }
        }
    }
}
