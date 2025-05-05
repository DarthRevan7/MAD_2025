@file:JvmName("TravelProposalScreenKt")

package com.example.voyago.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.voyago.activities.*
import com.example.voyago.viewmodel.*
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.Alignment


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelProposalList(navController: NavController, vm: TripViewModel = viewModel(factory = Factory)) {

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

        println("User Action = " + vm.userAction)

        if(vm.userAction != TripViewModel.UserAction.SEARCHING) {
            vm.resetFilters()
        }
        vm.applyFilters()
        //Log.d("FilteredTrips", "Filtered trips: $filteredTrips")
    }

    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(0)
        }
    ) { innerPadding ->

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .fillMaxWidth(0.9f)
                        .padding(16.dp), // Padding for the Row
                    horizontalArrangement = Arrangement.Start // Align items to the left in the Row
                ) {
                    Button(
                        onClick = {
                            vm.userAction = TripViewModel.UserAction.FILTER_SELECTION
                            navController.navigate("filter_selection")
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

            if (filteredTrips.isEmpty()) {
                item {
                    Box(
                        contentAlignment = Alignment.TopCenter,
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
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
}
