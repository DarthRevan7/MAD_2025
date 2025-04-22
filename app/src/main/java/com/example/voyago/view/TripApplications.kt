package com.example.voyago.view

import androidx.collection.intFloatMapOf
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.voyago.activities.BottomBar
import com.example.voyago.activities.TopBar
import com.example.voyago.viewmodel.TripListViewModel

@Composable
fun TripApplications(vm: TripListViewModel) {
    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(1)
        }
    ) { innerPadding ->

        val listState = rememberLazyListState()
        val trip = vm.selectedTrip

        if (trip != null) {

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                item {
                    Hero(trip)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp)
                    ) {
                        Text(
                            text = "${trip.groupSize} people" +
                                    if(trip.availableSpots() > 0) {
                                        " (${trip.availableSpots()} spots left)"
                                    } else { "" },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }

                item {
                    TitleBox("Applications")
                }

                item {
                    Text(
                        text = "Approved Applications:",
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                if (trip.participants.size > 1) {
                    item {
                        Text("List of approved participants.")
                    }
                } else {
                    item {
                        Row (
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("There aren't any participants yet.")
                        }
                    }
                }

                item {
                    Text(
                        text = "Pending Applications:",
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                if (trip.appliedUsers.isNotEmpty()) {
                    item {
                        Text("List of applied users.")
                    }
                } else {
                    item {
                        Row (
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("There aren't any new applications for this trip.")
                        }
                    }
                }

            }
        }
    }
}