package com.example.voyago.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.voyago.activities.ProfilePhoto
import com.example.voyago.model.UserData
import com.example.voyago.viewmodel.TripViewModel

@Composable
fun TripApplications(vm: TripViewModel) {

    val listState = rememberLazyListState()
    val trip = vm.selectedTrip.value

    if (trip != null) {
        vm.applications.value = vm.getTripApplicants(trip)
    }

    if (trip != null){
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {

            //Trip photo
            item {
                Hero(trip)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            //Group Size and Available spots
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

            //Approved users
            item {
                Text(
                    text = "Approved Applications:",
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            if (trip.participants.size > 1) {
                var participants = vm.applications.value

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

            //Applications that must be still approved or rejected
            item {
                Text(
                    text = "Pending Applications:",
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            if (trip.appliedUsers.isNotEmpty()) {
                var applicants = vm.applications.value
                items(applicants) { user ->
                    ShowApplications(user, vm)
                }
            } else {
                item {
                    Row (
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (trip.hasAvailableSpots()) {
                            Text("There aren't any new applications for this trip.")
                        } else {
                            Text("The group for the trip is completed. There won't be any new applications.")
                        }
                    }
                }
            }

            //Rejected users
            item {
                Text(
                    text = "Rejected Applications:",
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            if (trip.rejectedUsers.isNotEmpty()) {
                var applicants = vm.getTripRejectedUsers(trip)
                items(applicants) { user ->
                    ShowParticipants(user)
                }
            } else {
                item {
                    Row (
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("There aren't any rejected applications for this trip.")
                    }
                }
            }
        }
    }
}

@Composable
fun ShowParticipants(user: UserData) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        //Profile image of the participant
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .size(30.dp)
                .background(Color.Gray, shape = CircleShape)
        ) {
            ProfilePhoto(user.firstname, user.surname,true, null)
        }

        //Participant information
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp)
        ) {
            //User information
            Text("${user.firstname} ${user.surname}")

            if (user.requestedSpots > 1) {
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF9C4DFF), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Multiple spots",
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${user.requestedSpots}",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }

        //Participant's rating
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
fun ShowApplications(user: UserData, vm: TripViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var isAcceptAction by remember { mutableStateOf(true) }

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
            //Profile image of the applicant
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .size(30.dp)
                    .background(Color.Gray, shape = CircleShape)
            ) {
                ProfilePhoto(user.firstname, user.surname, true, null)
            }
            //User information
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text("${user.firstname} ${user.surname}")

                if (user.requestedSpots > 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFF9C4DFF), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Multiple spots",
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${user.requestedSpots}",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        //Applicant's rating
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

        //Accept and Reject Icons
        Row(
            modifier = Modifier
                .weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically)
        {
            //Approve Icon
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "approve",
                modifier = Modifier
                    .background(Color.Green)
                    .clickable{
                        isAcceptAction = true
                        showDialog = true
                    }
            )

            Spacer(modifier = Modifier.padding(5.dp))

            //Reject Icon
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "reject",
                modifier = Modifier
                    .background(Color.Red)
                    .clickable{
                        isAcceptAction = false
                        showDialog = true
                    }
            )
        }
    }

    //PopUp of confirmation for acceptance/rejection of an application
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (isAcceptAction) "Accept Application" else "Reject Application") },
            text = {
                Text("Are you sure you want to ${if (isAcceptAction) "accept" else "reject"} ${user.firstname} ${user.surname}'s application?")
            },
            confirmButton = {
                TextButton(onClick = {
                    if (isAcceptAction) {
                        vm.acceptApplication(vm.selectedTrip.value, user.id)
                    } else {
                        vm.rejectApplication(vm.selectedTrip.value, user.id)
                    }
                    showDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}