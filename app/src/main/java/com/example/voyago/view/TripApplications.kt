package com.example.voyago.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voyago.activities.ProfilePhoto
import com.example.voyago.model.Trip
import com.example.voyago.model.User
import com.example.voyago.viewmodel.ChatViewModel
import com.example.voyago.viewmodel.NotificationViewModel
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel

@Composable
fun TripApplications(
    vm: TripViewModel,
    uvm: UserViewModel,
    navController: NavController,
    nvm: NotificationViewModel,
    chatViewModel: ChatViewModel
) {
    // Get the currently selected trip from the TripViewModel
    val trip = vm.selectedTrip.value

    // Collect the currently logged-in user as Compose State
    val loggedUser by uvm.loggedUser.collectAsState()

    // Side effect to load trip-related data when the selected trip changes
    LaunchedEffect(trip.id) {
        // Fetch the list of approved participants for the trip
        vm.getTripParticipants(trip)
        // Fetch the list of users who have applied but not yet approved (pending)
        vm.getTripApplicants(trip)
        // Fetch the list of users who have been rejected from the trip
        vm.getTripRejectedUsers(trip)
    }

    // Collect the Flow data from ViewModel as Compose State to reflect UI updates automatically
    val participantsMap by vm.tripParticipants.collectAsState()
    val applicantsMap by vm.tripApplicants.collectAsState()
    val rejectedUsersMap by vm.tripRejectedUsers.collectAsState()

    // Remember the scroll state for the LazyColumn to preserve scroll position on recompositions
    val listState = rememberLazyListState()

    // Main scrollable list container
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Display the hero/trip photo section at the top
        item {
            Hero(trip, vm, User())
        }

        // Small vertical space below hero image
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Display basic trip info: group size and available spots left (if any)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${trip.groupSize} people" +
                            if (trip.availableSpots() > 0) {
                                " (${trip.availableSpots()} spots left)"
                            } else {
                                ""  // No spots left, omit extra text
                            },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }

        // Section title for applications
        item {
            TitleBox("Applications")
        }

        // Section title for approved participants
        item {
            Text(
                text = "Approved Applications:",
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 10.dp
                ),
                fontWeight = FontWeight.Bold
            )
        }

        // Show list of approved participants, excluding the logged-in user
        if (participantsMap.size > 1) { // If more than just the logged-in user
            items(participantsMap.entries.toList()) { entry ->
                val user = entry.key
                val joinRequest = entry.value
                if (user.id != loggedUser.id) {  // Skip the logged-in user in the list
                    ShowParticipants(user, joinRequest, uvm, navController)
                }
            }
        } else {
            // Show message if no participants other than logged-in user are found
            item {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("There aren't any participants yet.")
                }
            }
        }

        // Section title for pending applications
        item {
            Text(
                text = "Pending Applications:",
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 10.dp
                ),
                fontWeight = FontWeight.Bold
            )
        }

        // Show pending applications if any
        if (applicantsMap.isNotEmpty()) {
            items(applicantsMap.entries.toList()) { entry ->
                val user = entry.key
                val joinRequest = entry.value
                // Display each application with options (approve/reject) and relevant info
                ShowApplications(
                    user,
                    joinRequest,
                    vm,
                    uvm,
                    navController,
                    nvm,
                    trip,
                    chatViewModel
                )
            }
        } else {
            // Show message when no new applications are present
            item {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (trip.hasAvailableSpots()) {
                        Text("There aren't any new applications for this trip.")
                    } else {
                        // If group is full, explain that no new applications will be accepted
                        Text("The group for the trip is completed. There won't be any new applications.")
                    }
                }
            }
        }

        // Section title for rejected applications
        item {
            Text(
                text = "Rejected Applications:",
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 10.dp
                ),
                fontWeight = FontWeight.Bold
            )
        }

        // Show rejected applications, if any
        if (rejectedUsersMap.isNotEmpty()) {
            items(rejectedUsersMap.entries.toList()) { entry ->
                val user = entry.key
                val joinRequest = entry.value
                // Show each rejected participant
                ShowParticipants(user, joinRequest, uvm, navController)
            }
        } else {
            // Show message when no rejected applications exist
            item {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("There aren't any rejected applications for this trip.")
                }
            }
        }
    }
}


@Composable
fun ShowParticipants(
    user: User,
    joinRequest: Trip.JoinRequest,
    uvm: UserViewModel,
    navController: NavController
) {
    // Local state to control visibility of the participant info popup
    var showPart by remember { mutableStateOf(false) }

    // Main container showing a row with participant details
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Profile picture or avatar for the participant
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .size(30.dp)    // Fixed circular size
                .background(Color.Gray, shape = CircleShape)    // Placeholder background color
        ) {
            // Display profile photo
            ProfilePhoto(Modifier, user, true)
        }

        // Participant information
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 16.dp),

            ) {
            // Display full name of the participant
            Text(
                modifier = Modifier
                    .clickable {
                        // Navigates to user profile on click
                        navController.navigate("user_profile/${user.id}")
                    },
                text = "${user.firstname} ${user.surname}"
            )

            // If the user requested more than one spot
            if (joinRequest.requestedSpots > 1) {

                Spacer(modifier = Modifier.width(8.dp))

                // Visual indicator showing number of people requested
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF9C4DFF), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    // People icon indicating group request
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Multiple spots",
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { showPart = true },     // Show detailed info on click
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Show total requested spots (includes user + guests)
                    Text(
                        text = "${joinRequest.requestedSpots}",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Show participant rating on the right side
        Row(
            modifier = Modifier.weight(1f),     // Pushes rating to the far end
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.StarBorder, "star")      // Star icon
            Spacer(modifier = Modifier.width(5.dp))
            Text(user.rating.toString())                // Numerical rating
        }
    }

    // Dialog that shows additional participant info (for multi-spot requests)
    if (showPart) {
        AlertDialog(
            onDismissRequest = {
                showPart = false
            },    // Close when background or "Close" is clicked
            title = { Text("Participants Info") },
            text = {
                Column {
                    // Show each unregistered guest's name and email
                    joinRequest.unregisteredParticipants.forEach { participant ->
                        Text("Name: ${participant.name} ${participant.surname}")
                        Text("Email: ${participant.email}")
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Show each registered guest's username (resolved by ID)
                    joinRequest.registeredParticipants.forEach { userid ->
                        ParticipantUsername(userid, uvm, navController)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPart = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ParticipantUsername(
    userId: Int,
    uvm: UserViewModel,
    navController: NavController
) {
    // Collect user data from the ViewModel
    val user by uvm.getUserData(userId)
        .collectAsState(initial = User()) // We provide a default (empty) User instance as the initial value to prevent crashes during recomposition

    // If the user has been successfully loaded (ID is not default 0),
    // display the username with a clickable modifier to navigate to their profile.
    if (user!!.id != 0) { // Assuming ID 0 means data hasn't loaded or it's a placeholder user
        Text(
            "Username: ${user!!.username}",     // Display the username
            Modifier.clickable { navController.navigate("user_profile/${userId}") }     // Navigate to profile on tap
        )
    } else {
        // If the user is still loading, show a loading placeholder
        Text("Loading...")
    }
}


@Composable
fun ShowApplications(
    user: User,
    joinRequest: Trip.JoinRequest,
    vm: TripViewModel,
    uvm: UserViewModel,
    navController: NavController,
    nvm: NotificationViewModel,
    trip: Trip,
    chatViewModel: ChatViewModel
) {

    // State for controlling the confirmation dialog
    var showDialog by remember { mutableStateOf(false) }

    // State to track whether the dialog is for accepting or rejecting the application
    var isAcceptAction by remember { mutableStateOf(true) }

    // State to control visibility of participant info dialog (for group applications)
    var showPart by remember { mutableStateOf(false) }

    // UI layout: Card-style row showing applicant info and options
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Applicant's profile photo
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .size(30.dp)
                .background(Color.Gray, shape = CircleShape)
        ) {
            ProfilePhoto(Modifier, user, true)
        }
        // Main user name and group size indicator (if more than 1 spot was requested)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp)
        ) {
            // Full name with clickable navigation to profile
            Text(
                modifier = Modifier
                    .clickable {
                        navController.navigate("user_profile/${user.id}")
                    },
                text = "${user.firstname} ${user.surname}"
            )

            // Show participant count if more than one spot was requested
            if (joinRequest.requestedSpots > 1) {
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
                        modifier = Modifier
                            .size(14.dp)
                            .clickable {
                                showPart = true
                            },     // Show dialog with more participant info
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "${joinRequest.requestedSpots}",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Show applicant’s rating on the right side
        Row(
            modifier = Modifier
                .weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.StarBorder, "star")
            Spacer(modifier = Modifier.width(5.dp))
            Text(user.rating.toString())
        }

        // Accept and Reject action icons
        Row(
            modifier = Modifier
                .weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accept icon (green)
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "approve",
                modifier = Modifier
                    .background(Color.Green)
                    .clickable {
                        isAcceptAction = true
                        showDialog = true
                        // Add user to trip chat group preemptively
                        chatViewModel.addParticipantToGroup(user.id, trip.title)
                    }
            )

            Spacer(modifier = Modifier.padding(5.dp))

            // Reject icon (red)
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "reject",
                modifier = Modifier
                    .background(Color.Red)
                    .clickable {
                        isAcceptAction = false
                        showDialog = true
                    }
            )
        }
    }

    // Confirm accept/reject dialog
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
                        // Accept application in backend
                        vm.acceptApplication(vm.selectedTrip.value, user.id)

                        // Send approval notification
                        val title = "Application approved!"
                        val body = "Time to pack your bags to ${vm.selectedTrip.value.destination}!"
                        val notificationType = "APPROVED"
                        val idLink = vm.selectedTrip.value.id

                        val userId = user.id.toString()
                        nvm.sendNotificationToUser(userId, title, body, notificationType, idLink)


                    } else {
                        // Reject application in backend
                        vm.rejectApplication(vm.selectedTrip.value, user.id)

                        // Send rejection notification
                        val title = "Application rejected"
                        val body =
                            "Your application for the trip to ${vm.selectedTrip.value.destination} was rejected"
                        val notificationType = "REJECTED"
                        val idLink = vm.selectedTrip.value.id

                        val userId = user.id.toString()
                        nvm.sendNotificationToUser(userId, title, body, notificationType, idLink)
                    }

                    // Refresh UI data after action
                    val trip = vm.selectedTrip.value
                    vm.getTripParticipants(trip)
                    vm.getTripApplicants(trip)
                    vm.getTripRejectedUsers(trip)
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

    // Dialog showing extra participant info for group applications
    if (showPart) {
        AlertDialog(
            onDismissRequest = { showPart = false },
            title = {
                Text("Participants Info")
            },
            text = {
                Column {
                    // Unregistered group members
                    joinRequest.unregisteredParticipants.forEach { participant ->
                        Text("Name: ${participant.name} ${participant.surname}")
                        Text("Email: ${participant.email}")
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Registered group members (render username from their IDs)
                    joinRequest.registeredParticipants.forEach { userid ->
                        ParticipantUsername(userid, uvm, navController)
                    }

                }
            },
            confirmButton = {
                TextButton(onClick = { showPart = false }) {
                    Text("Close")
                }
            }
        )
    }
}
