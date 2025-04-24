@file:JvmName("TravelProposalScreenKt")

package com.example.voyago.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelProposalList(navController: NavController, vm: TripListViewModel = viewModel(factory = Factory)) {

    val allPublishedTrips by vm.allPublishedTrips.collectAsState()

    LaunchedEffect(Unit) {
        vm.updatePublishedTrip()
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

            items(allPublishedTrips) { trip ->
                TripCard(trip, navController, vm, false)
            }
        }

        /*LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item{

                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f) // Ensure the Row takes up the full width
                        .padding(top = 15.dp), // Padding for the Row
                    horizontalArrangement = Arrangement.Start // Align items to the left in the Row
                ) {
                    Button(
                        onClick = {
                            // Click functionality
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(top = 15.dp)
                        .height(200.dp)
                        .clip(RoundedCornerShape(30.dp)) // round corners for the whole box
                ) {
                    // uncomment the follow lines of code for the image.
//                    Image(
//                        painter = painterResource(id = R.drawable.your_image), // replace with your image
//                        contentDescription = "Background image",
//                        modifier = Modifier
//                            .fillMaxSize(),
//                        contentScale = ContentScale.Crop // makes the image fill the box nicely
//                    )

                    // semi-transparent overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x55000000)) // optional overlay for better text visibility
                    )

                    // Content (text, icons, etc.) goes here
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "Title or Info",
                            color = Color.White,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "More details here",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Price",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Date - Days",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Participants",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

         */
    }
}
