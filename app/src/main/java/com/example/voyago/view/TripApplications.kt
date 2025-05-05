package com.example.voyago.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.example.voyago.model.LazyUser
import com.example.voyago.viewmodel.TripListViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import com.example.voyago.activities.ProfilePhoto
import com.example.voyago.viewmodel.TripViewModel

@Composable
fun TripApplications(vm: TripViewModel) {

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
                        .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
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
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            if (trip.participants.size > 1) {
                var participants = vm.getTripParticipants(trip)

                items(participants) { user ->
                    if (user.id != 1) {
                        ShowParticipants(user)
                    }
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
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            if (trip.appliedUsers.isNotEmpty()) {
                var applicants = vm.getTripApplicants(trip)
                items(applicants) { user ->
                    ShowApplications(user)
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

@Composable
fun ShowParticipants(user: LazyUser) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .size(30.dp)
                .background(Color.Gray, shape = CircleShape)
        ) {
            //Image
            ProfilePhoto(user.name, user.surname,true, null)
        }
        Text("${user.name} ${user.surname}", modifier = Modifier.padding( start = 16.dp))

        Row(
            modifier = Modifier
                .weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically)
        {
            Icon(Icons.Default.StarBorder, "star")
            Spacer(modifier = Modifier.width(5.dp))
            Text(user.rating.toString())
        }
    }
}

@Composable
fun ShowApplications(user: LazyUser) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .size(30.dp)
                    .background(Color.Gray, shape = CircleShape)
            ) {
                //Image
                ProfilePhoto(user.name, user.surname, true, null)
            }
            Text("${user.name} ${user.surname}", modifier = Modifier.padding(start = 16.dp))
        }

        Row(
            modifier = Modifier
                .weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically)
        {
            Icon(Icons.Default.StarBorder, "star")
            Spacer(modifier = Modifier.width(5.dp))
            Text(user.rating.toString())
        }

        Row(
            modifier = Modifier
                .weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically)
        {
            Icon(Icons.Default.Check, "check", modifier = Modifier.background(Color.Green))
            Spacer(modifier = Modifier.padding(5.dp))
            Icon(Icons.Default.Close, "close", modifier = Modifier.background(Color.Red))
        }
    }
}