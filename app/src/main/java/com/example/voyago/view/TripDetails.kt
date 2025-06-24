package com.example.voyago.view

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.voyago.activities.ProfilePhoto
import com.example.voyago.model.Review
import com.example.voyago.model.Trip
import com.example.voyago.model.Trip.Activity
import com.example.voyago.model.Trip.JoinRequest
import com.example.voyago.model.Trip.Participant
import com.example.voyago.model.User
import com.example.voyago.model.isTimestampLong
import com.example.voyago.model.timestampToCalendar
import com.example.voyago.toCalendar
import com.example.voyago.viewmodel.ChatViewModel
import com.example.voyago.viewmodel.NotificationViewModel
import com.example.voyago.viewmodel.ReviewViewModel
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Serializable
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

// Notification data structure to pass data between screen
data class TripNotification(
    val id: Int,
    var photo: String? = null,
    var title: String,
    var destination: String,
    var startDate: Timestamp,
    var endDate: Timestamp,
    var estimatedPrice: Double,
    var groupSize: Int,
    var participants: Map<String, JoinRequest>,                   // userId, id JoinedRequest
    var activities: Map<String, List<Activity>>,                  // Map<Date, Activity>
    var status: String,
    var typeTravel: List<String>,
    var creatorId: Int,
    var appliedUsers: Map<String, JoinRequest>,                   // userId, id JoinedRequest
    var rejectedUsers: Map<String, JoinRequest>,                  // userId, number of spots
    var published: Boolean,
    var isDraft: Boolean
) : Serializable

@SuppressLint("MutableCollectionMutableState")
@Composable
fun TripDetails(
    navController: NavController,
    vm: TripViewModel,
    owner: Boolean,
    uvm: UserViewModel,
    rvm: ReviewViewModel,
    nvm: NotificationViewModel,
    chatViewModel: ChatViewModel
) {
    // The currently logged-in user
    val loggedUser by uvm.loggedUser.collectAsState()

    // Determine if the user is logged in (basic check using essential user fields)
    val isUserLoggedIn = remember(loggedUser) {
        loggedUser.id != 0 &&
                loggedUser.username.isNotEmpty() &&
                loggedUser.email.isNotEmpty()
    }

    // Determine which trip to display based on the user's current view state
    var trip = when (vm.userAction) {
        TripViewModel.UserAction.VIEW_TRIP -> vm.selectedTrip.value
        TripViewModel.UserAction.VIEW_OTHER_TRIP -> vm.otherTrip.value
        else -> vm.selectedTrip.value

    }

    // If the user arrived here via a notification, extract the trip from savedStateHandle
    val fields =
        navController.previousBackStackEntry?.savedStateHandle?.get<TripNotification>("notificationValues")

    // If data exists from notification, construct the trip object from it
    if (fields != null) {
        trip = Trip(
            fields.id,
            fields.photo,
            fields.title,
            fields.destination,
            fields.startDate,
            fields.endDate,
            fields.estimatedPrice,
            fields.groupSize,
            fields.participants,
            fields.activities,
            fields.status,
            fields.typeTravel,
            fields.creatorId,
            fields.appliedUsers,
            fields.rejectedUsers,
            fields.published,
            fields.isDraft
        )
    }


    // Reactive map of trip participants
    val participantsMap by vm.tripParticipants.collectAsState()

    // Reset the user action after viewing the trip
    DisposableEffect(Unit) {
        onDispose {
            vm.userAction = TripViewModel.UserAction.VIEW_TRIP
        }
    }

    // If trip is not valid yet, show a temporary message
    if (!trip.isValid()) {
        Text("Trip ${vm.otherTrip.value.id} ${vm.userAction} Loading trip details...")
        return
    }

    // Determine if the user joined the trip but is not the creator
    val joined =
        trip.participants.containsKey(loggedUser.id.toString()) && trip.creatorId != loggedUser.id

    //Manage join request

    // Map of trips the user has applied to (trip ID -> requested spots)
    val askedTrips: Map<String, Int> by vm.askedTrips.collectAsState()

    // Sync user's asked trips (on initial load)
    vm.syncAskedTrips(loggedUser.id) {}

    // Retrieve how many spots the user requested for this trip
    val requestedSpots = trip.id.let { askedTrips[it.toString()] } ?: 0
    val hasAsked = requestedSpots > 0

    // UI and state for join request dialog
    var showDialog by remember { mutableStateOf(false) }
    var selectedSpots by remember { mutableIntStateOf(1) }

    // Dialog phase: used to track which step in the join dialog is shown
    var dialogPhase by remember { mutableIntStateOf(0) }

    // Information for group participants (non-logged-in)
    var unregisteredParticipants by remember { mutableStateOf(listOf<Participant>()) }

    // Usernames of registered participants being added (if any)
    var registeredUsernames by remember { mutableStateOf(listOf<String>()) }
    var isRegisteredList by remember { mutableStateOf(listOf<Boolean>()) }

    // Field "touched" states used for validation feedback
    var usernameTouchedList by remember { mutableStateOf(listOf<Boolean>()) }
    var nameTouchedList by remember { mutableStateOf(listOf<Boolean>()) }
    var surnameTouchedList by remember { mutableStateOf(listOf<Boolean>()) }
    var emailTouchedList by remember { mutableStateOf(listOf<Boolean>()) }

    // Holds user objects of registered users being added to the group
    var registeredUserList: MutableList<User> = mutableListOf()

    // Dynamically (re)initialize participant input lists when phase or selectedSpots changes
    LaunchedEffect(dialogPhase, selectedSpots) {
        if (dialogPhase == 1) {
            isRegisteredList =
                List(selectedSpots - 1) { i -> isRegisteredList.getOrNull(i) == true }
            registeredUsernames =
                List(selectedSpots - 1) { i -> registeredUsernames.getOrNull(i) ?: "" }
            unregisteredParticipants = List(selectedSpots - 1) { i ->
                unregisteredParticipants.getOrNull(i) ?: Participant(
                    "",
                    "",
                    ""
                )
            }
        }

        // Sync touched and registered state lists to match expected size
        isRegisteredList = List(selectedSpots - 1) { index ->
            isRegisteredList.getOrNull(index) == true
        }

        usernameTouchedList = List(selectedSpots - 1) { i ->
            usernameTouchedList.getOrNull(i) == true
        }

        nameTouchedList = List(selectedSpots - 1) { i ->
            nameTouchedList.getOrNull(i) == true
        }

        surnameTouchedList = List(selectedSpots - 1) { i ->
            surnameTouchedList.getOrNull(i) == true
        }
        emailTouchedList = List(selectedSpots - 1) { i ->
            emailTouchedList.getOrNull(i) == true
        }
    }

    // Review Logic

    // All reviews for this trip
    val reviews by rvm.tripReviews.collectAsState()

    // Whether current user reviewed this trip
    val isReviewed by rvm.isReviewed.collectAsState()

    // Load reviews and participants for trip on initial load
    LaunchedEffect(trip.id, loggedUser.id) {
        if (trip.id != 0) {
            rvm.getTripReviews(trip.id)
            vm.getTripParticipants(trip)
            rvm.isReviewed(loggedUser.id, trip.id)
        }

    }

    // Scroll state for the LazyColumn
    val listState = rememberLazyListState()

    // Get today’s date with time reset (for comparing with trip date)
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // Check if the trip starts after today
    val isAfterToday = trip.startDateAsCalendar().after(today)

    // Error state for trying to publish without meeting requirements
    var publishError by rememberSaveable { mutableStateOf(false) }

    // Main UI Container
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            //Trip image
            item {
                // Displays trip banner with image and title
                Hero(trip, vm, loggedUser, true)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            //Trip information (Date, Group Size, Price)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp)
                ) {
                    Text(
                        text = formatTripDate(trip.startDateAsCalendar()) + " - " +
                                formatTripDate(trip.endDateAsCalendar()) + "\n" +
                                "${trip.groupSize} people" +
                                if (trip.availableSpots() > 0) {
                                    " (${trip.availableSpots()} spots left)"
                                } else {
                                    ""
                                },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${trip.estimatedPrice} €",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }

            //The logged in user see a trip created by them in the "My Trip" section
            if (owner) {
                // If the trip is published and the current user is indeed the creator
                if (trip.published && trip.creatorId == loggedUser.id) {
                    // UI block with buttons for trip management
                    item {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box {
                                // If the trip has been marked as COMPLETED
                                if (trip.status == Trip.TripStatus.COMPLETED.toString()) {
                                    // "My Reviews" button navigates to the reviews screen
                                    Box {
                                        //Applications Button
                                        Button(
                                            onClick = { navController.navigate("my_reviews") },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xd9, 0x24, 0xd6, 255)
                                            )
                                        ) {
                                            Text("My Reviews")
                                        }

                                        // Blue dot indicates there are reviews pending from the user
                                        if (isReviewed == false) {
                                            Box(
                                                modifier = Modifier
                                                    .size(15.dp)
                                                    .background(
                                                        Color(0xFF448AFF),
                                                        CircleShape
                                                    )      // Blue dot
                                                    .align(Alignment.TopEnd)
                                            )
                                        }
                                    }
                                } else {
                                    // If the trip is not completed: show the "Applications" button
                                    Button(
                                        onClick = {
                                            // Set the trip in ViewModel for reference in the applications screen
                                            vm.setSelectedTrip(trip)
                                            navController.navigate("trip_applications")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0x14, 0xa1, 0x55, 255)
                                        )
                                    ) {
                                        Text("Applications")
                                    }
                                }

                                // Red notification dot if there are any pending applications
                                if (trip.appliedUsers.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(15.dp)
                                            .background(Color.Red, CircleShape)
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }

                            // Spacer to push the next set of buttons further right
                            Spacer(Modifier.weight(3f))

                            // "Private" button — shown only if there are no other participants yet
                            // This allows the trip creator to unpublish (make private) the trip
                            if (!trip.participants.any { it.key != trip.creatorId.toString() }) {
                                Button(
                                    onClick = {
                                        // Change published state (published -> private)
                                        vm.changePublishedStatus(trip.id)
                                        // Refresh the published trips for the user
                                        vm.updatePublishedTrip(uvm.loggedUser.value.id)
                                        // Navigate back in the stack
                                        navController.popBackStack()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0x65, 0x55, 0x8f, 255)
                                    )
                                ) {
                                    Text("Private")
                                }
                            }

                            // Add spacing between buttons
                            Spacer(Modifier.padding(5.dp))

                            // "Delete" button - launches confirmation dialog and deletes trip
                            DeleteMyTrip(trip, navController, vm, uvm = uvm)
                        }
                    }
                }

                // If the logged-in user has joined the trip but is NOT the creator
                if (joined) {
                    item {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            // If the trip is marked as COMPLETED, the user can leave a review
                            if (trip.status == Trip.TripStatus.COMPLETED.toString()) {
                                Box {
                                    // "My Reviews" button navigates to review submission/list screen
                                    Button(
                                        onClick = { navController.navigate("my_reviews") },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xd9, 0x24, 0xd6, 255)
                                        )
                                    ) {
                                        Text("My Reviews")
                                    }

                                    // Blue dot as indicator that the user has not submitted a review yet
                                    if (isReviewed == false) {
                                        Box(
                                            modifier = Modifier
                                                .size(15.dp)
                                                .background(Color(0xFF448AFF), CircleShape)
                                                .align(Alignment.TopEnd)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.weight(1f))

                            // Show "Leave Trip" option only if the user is currently logged in
                            if (isUserLoggedIn) {
                                // This button triggers a confirmation dialog for leaving the trip
                                LeaveTrip(trip, navController, vm, loggedUser, uvm)
                            }
                        }
                    }
                }

                //The trip created by the logged in user is private
                if (!trip.published) {
                    item {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box {
                                //Publish Button
                                Button(
                                    onClick = {
                                        // Ensure the trip start date is in the future before allowing publishing
                                        if (isAfterToday) {
                                            // Change trip status from private to public
                                            vm.changePublishedStatus(trip.id)
                                            // Refresh user's published trips
                                            vm.updatePublishedTrip(uvm.loggedUser.value.id)

                                            // Ensure a group chat exists for this trip; create if needed
                                            chatViewModel.createGroupIfNotExists(
                                                trip.title,
                                                uvm.loggedUser.value.id,
                                                trip.id.toString()
                                            ) { created ->
                                                if (created) {
                                                    Log.d(
                                                        "ChatDebug",
                                                        "Group created: ${trip.title}"
                                                    )
                                                } else {
                                                    Log.d(
                                                        "ChatDebug",
                                                        "Group already exists or error creating it."
                                                    )
                                                }
                                            }

                                            //Send notifications
                                            val title = "Check this out!"
                                            val body =
                                                "This trip to ${trip.destination} looks interesting for you!"
                                            val notificationType = "TRIP"
                                            val idLink = trip.id

                                            // Notify users with compatible travel preferences
                                            uvm.getMatchingUserIdsByTypeTravel(trip.typeTravel) { compatibleUsers ->
                                                compatibleUsers.forEach { userIdInt ->
                                                    if (userIdInt != loggedUser.id) {
                                                        val userId = userIdInt.toString()
                                                        nvm.sendNotificationToUser(
                                                            userId,
                                                            title,
                                                            body,
                                                            notificationType,
                                                            idLink
                                                        )
                                                    }
                                                }

                                                // Navigate back after publishing and notifying
                                                navController.popBackStack()
                                            }
                                        } else {
                                            // Show error if trying to publish a trip that starts today or in the past
                                            publishError = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0x14, 0xa1, 0x55, 255)
                                    )
                                ) {
                                    Text("Publish")
                                }
                            }

                            Spacer(Modifier.weight(1f))

                            // Allows the owner to delete their private trip (with confirmation)
                            DeleteMyTrip(trip, navController, vm, uvm = uvm)
                        }

                        // If publishing fails due to invalid date, show a user-friendly error message
                        if (publishError) {
                            Text(
                                text = "The Start Date of the trip must be after today for it to be published.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Title for the upcoming trip itinerary section
                item {
                    TitleBox("My Itinerary")
                }
            }
            // The logged-in user is viewing a published trip from the "Explore" section
            else {
                item {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (isUserLoggedIn) {
                            // If user can still join the trip, is not the creator, and hasn't joined yet
                            if (trip.canJoin() && trip.creatorId != loggedUser.id && !joined) {
                                //Ask to Join/Asked to Join Button
                                Button(
                                    onClick = {
                                        if (hasAsked) {
                                            // Cancel request to join if already asked
                                            vm.cancelAskToJoin(trip, loggedUser.id)
                                        } else {
                                            // Open the dialog to ask to join with spots selection
                                            selectedSpots = 1
                                            showDialog = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (hasAsked)
                                            Color(0x65, 0xa9, 0x8b, 255)
                                        else
                                            Color(0x14, 0xa1, 0x55, 255)
                                    )
                                ) {
                                    if (hasAsked) {
                                        Icon(Icons.Default.Check, "check")
                                        Text("Asked to Join ($requestedSpots spot${if (requestedSpots > 1) "s" else ""})")
                                    } else {
                                        Text("Ask to Join")
                                    }
                                }
                                // If user already joined the trip (but not the creator) and trip is still active
                            } else if (joined && trip.status != Trip.TripStatus.COMPLETED.toString()) {
                                Button(
                                    onClick = {},
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0x2E, 0x7D, 0x32, 255)
                                    )
                                ) {
                                    Text("Already Joined")
                                }
                            }
                        } else {
                            // For guests (not logged in), show a login call-to-action
                            Button(
                                onClick = {
                                    navController.navigate("login")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x14, 0xa1, 0x55, 255)
                                )
                            ) {
                                Text("Login to Join")
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Title for the upcoming trip itinerary section
                item {
                    TitleBox("Itinerary")
                }
            }

            //The Itinerary of the trip
            item {
                ItineraryText(
                    trip,
                    modifier = Modifier
                        .padding(start = 24.dp, top = 16.dp, end = 20.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                TitleBox("Created by:")
            }

            item {
                // Collect the trip creator’s user data from the ViewModel
                val user = uvm.getUserData(trip.creatorId).collectAsState(initial = User()).value

                // If user exists, show the creator info block
                if (user != null) {
                    ShowParticipants(
                        user = user,
                        joinRequest = JoinRequest(
                            user.id,
                            1,                  // Always 1 spot for the creator
                            emptyList(),        // No unregistered participants
                            emptyList()         // No emails
                        ),
                        uvm = uvm,
                        navController = navController
                    )
                }
            }

            //Reviews section
            if (reviews.isNotEmpty()) {
                item {
                    TitleBox("Reviews")
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Display each review in the reviews list
                items(reviews) { review ->
                    ShowReview(review, uvm, navController)
                }
            }

            // Participant section

            // Only show if there are other participants besides the creator
            if (trip.participants.size > 1) {
                item {
                    TitleBox("Participants:")
                }

                // Iterate through all participants (excluding creator) and display them
                items(participantsMap.entries.toList()) { entry ->
                    val user = entry.key        // User who joined
                    val spots = entry.value     // Number of spots taken by this user
                    if (trip.creatorId != user.id) {
                        ShowParticipants(user, spots, uvm, navController)
                    }
                }
            }

        }

        // Stores validation state for each username input
        var usernameValidationStates by remember { mutableStateOf(mutableMapOf<Int, Boolean>()) }


        //PopUp that appears when the user ask to join a trip
        if (showDialog) {
            val maxSpots = trip.availableSpots()

            AlertDialog(
                onDismissRequest = {
                    // Reset all dialog-related states
                    showDialog = false
                    dialogPhase = 0
                    unregisteredParticipants = emptyList()
                    registeredUsernames = emptyList()
                    usernameValidationStates = mutableMapOf()
                },
                title = {
                    if (dialogPhase == 0) {
                        Text("Select number of spots:")
                    } else {
                        Text("Details other participants:")
                    }
                },
                text = {
                    if (dialogPhase == 0) {
                        // Spot selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Decrease button
                            IconButton(
                                // If the number of spots is > 1 decrease by one
                                onClick = { if (selectedSpots > 1) selectedSpots-- },
                                enabled = selectedSpots > 1
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease")
                            }

                            // Display number of selected spots
                            Text(
                                "$selectedSpots",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.headlineMedium
                            )

                            // Increase Button
                            IconButton(
                                onClick = {
                                    // If the selected spots are < of the max available spots increase by 1
                                    if (selectedSpots < maxSpots) selectedSpots++
                                },
                                enabled = selectedSpots < maxSpots
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase")
                            }
                        }
                    } else {
                        // Participant Information Form
                        LazyColumn {
                            items(selectedSpots - 1) { i ->

                                val isRegistered = isRegisteredList.getOrNull(i) == true
                                val participant =
                                    unregisteredParticipants.getOrNull(i)// ?: Participant("", "", "")

                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Text(
                                        "Participant ${i + 1}",
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    // Checkbox registered user
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isRegistered,
                                            onCheckedChange = { checked ->
                                                isRegisteredList = isRegisteredList.toMutableList()
                                                    .also { it[i] = checked }
                                                // Reset data
                                                if (!checked) {
                                                    registeredUsernames =
                                                        registeredUsernames.toMutableList().also {
                                                            it[i] = ""
                                                        }
                                                }
                                                usernameValidationStates =
                                                    usernameValidationStates.toMutableMap().also {
                                                        it.remove(i)
                                                    }
                                            }
                                        )
                                        Text("Are they registered to Voyago?")
                                    }

                                    // Input fields
                                    // if the user is registered to the app add them with its username
                                    if (isRegistered) {
                                        // Username input field
                                        // Check if the username exists and correspond to a registered user
                                        AsyncValidatingUsernameField(
                                            text = registeredUsernames[i],
                                            updateState = { newValue ->
                                                registeredUsernames =
                                                    registeredUsernames.toMutableList()
                                                        .also { it[i] = newValue }
                                                usernameTouchedList =
                                                    usernameTouchedList.toMutableList()
                                                        .also { it[i] = true }
                                            },
                                            label = "Username",
                                            userViewModel = uvm,
                                            index = i,
                                            validationStates = usernameValidationStates,
                                            onValidationChange = { index, isValid, user ->
                                                usernameValidationStates =
                                                    usernameValidationStates.toMutableMap().also {
                                                        it[index] = isValid
                                                    }
                                                if (user.id > 0) {
                                                    registeredUserList += user
                                                }
                                            }
                                        )

                                    } else {
                                        // If the user is not registered to the app insert Name, Surname and Email
                                        if (participant != null) {

                                            //Name input field with validation
                                            ValidatingInputTextField(
                                                participant.name,
                                                { newValue ->
                                                    unregisteredParticipants =
                                                        unregisteredParticipants.toMutableList()
                                                            .also {
                                                                it[i] = it[i].copy(name = newValue)
                                                            }
                                                    nameTouchedList =
                                                        nameTouchedList.toMutableList()
                                                            .also { it[i] = true }
                                                },
                                                nameTouchedList[i] && (
                                                        participant.name.isBlank() ||                             // Empty or only whitespace
                                                                participant.name.any { !it.isLetter() && it != ' ' } ||   // Contains non-letter and non-space
                                                                participant.name.all { it == ' ' }                         // Only spaces
                                                        ),
                                                "Name"
                                            )

                                            // Surname input field with validation
                                            ValidatingInputTextField(
                                                participant.surname,
                                                { newValue ->
                                                    unregisteredParticipants =
                                                        unregisteredParticipants.toMutableList()
                                                            .also {
                                                                it[i] =
                                                                    it[i].copy(surname = newValue)
                                                            }
                                                    surnameTouchedList =
                                                        surnameTouchedList.toMutableList()
                                                            .also { it[i] = true }
                                                },
                                                surnameTouchedList[i] && (
                                                        participant.surname.isBlank() ||                             // Empty or only whitespace
                                                                participant.surname.any { !it.isLetter() && it != ' ' } ||   // Contains non-letter and non-space
                                                                participant.surname.all { it == ' ' }                         // Only spaces
                                                        ),
                                                "Surname"
                                            )

                                            // Email input field with validation
                                            ValidatingInputEmailField(
                                                participant.email,
                                                { newValue ->
                                                    unregisteredParticipants =
                                                        unregisteredParticipants.toMutableList()
                                                            .also {
                                                                it[i] = it[i].copy(email = newValue)
                                                            }
                                                    emailTouchedList =
                                                        emailTouchedList.toMutableList()
                                                            .also { it[i] = true }
                                                },
                                                emailTouchedList[i] && participant.email.isBlank()
                                            )
                                        }

                                    }
                                }
                            }
                        }

                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (dialogPhase == 0) {
                            //Next phase for registering the participants
                            if (selectedSpots > 1) {
                                dialogPhase = 1
                            } else {
                                // No other participants; send request directly
                                vm.askToJoin(
                                    trip,
                                    loggedUser.id,
                                    selectedSpots,
                                    emptyList(),
                                    emptyList()
                                )

                                // Notify trip creator
                                val title = "New Application!"
                                val body =
                                    "You have a new application for the trip to ${vm.selectedTrip.value.destination}"
                                val notificationType = "NEW_APPLICATION"
                                val idLink = vm.selectedTrip.value.id

                                val userId = vm.selectedTrip.value.creatorId.toString()
                                nvm.sendNotificationToUser(
                                    userId,
                                    title,
                                    body,
                                    notificationType,
                                    idLink
                                )

                                showDialog = false
                            }
                        } else {
                            //Form validation
                            var hasErrors = false

                            // For every participants
                            for (i in 0 until isRegisteredList.count() - 1) {
                                // Check if the username is valid for registered participants
                                if (isRegisteredList[i]) {
                                    val username = registeredUsernames[i]
                                    val isUsernameValid = usernameValidationStates[i] == true

                                    if (username.isBlank() || !username.any { it.isLetter() } || !isUsernameValid) {
                                        hasErrors = true
                                        usernameTouchedList = usernameTouchedList.toMutableList()
                                            .also { it[i] = true }
                                    }
                                } else {
                                    // Check if name, surname and email are valid for unregistered participants
                                    val participant = unregisteredParticipants[i]

                                    if (participant.name.isBlank() || !participant.name.any { it.isLetter() }) {
                                        hasErrors = true
                                        nameTouchedList = nameTouchedList.toMutableList()
                                            .also { it[i] = true }
                                    }
                                    if (participant.surname.isBlank() || !participant.surname.any { it.isLetter() }) {
                                        hasErrors = true
                                        surnameTouchedList = surnameTouchedList.toMutableList()
                                            .also { it[i] = true }
                                    }
                                    if (participant.email.isBlank()) {
                                        hasErrors = true
                                        emailTouchedList = emailTouchedList.toMutableList()
                                            .also { it[i] = true }
                                    }
                                }
                            }

                            // If there are no errors
                            if (!hasErrors) {

                                // Add the ids of registered participants to the map
                                val idList = registeredUserList.map { it.id }.toList()

                                // For each unregistered participants if name, surname or email are not valid
                                // remove it from the map
                                unregisteredParticipants.forEach {
                                    if (it.name.isEmpty() && it.surname.isEmpty() && it.email.isEmpty()) {
                                        unregisteredParticipants -= it
                                    }
                                }

                                // Send the request to join
                                vm.askToJoin(
                                    trip,
                                    loggedUser.id,
                                    selectedSpots,
                                    unregisteredParticipants,
                                    idList
                                )

                                //Cleaning variables
                                showDialog = false
                                dialogPhase = 0
                                unregisteredParticipants = emptyList()
                                registeredUsernames = emptyList()
                                isRegisteredList = emptyList()
                                usernameValidationStates = mutableMapOf()

                                // Reset touched lists
                                usernameTouchedList = emptyList()
                                nameTouchedList = emptyList()
                                surnameTouchedList = emptyList()
                                emailTouchedList = emptyList()
                            }
                        }
                    }) {
                        Text(if (dialogPhase == 0 && selectedSpots > 1) "Next" else "Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        dialogPhase = 0
                        unregisteredParticipants = emptyList()
                        registeredUsernames = emptyList()
                        usernameValidationStates = mutableMapOf()

                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@SuppressLint("DiscouragedApi")
@Composable
fun Hero(trip: Trip, vm: TripViewModel, loggedUser: User, import: Boolean = false) {

    // Holds the URL of the image for the trip
    var imageUrl by remember { mutableStateOf<String?>(null) }

    // Trigger side-effect when trip ID changes
    // This retrieves the photo URL asynchronously
    LaunchedEffect(trip.id) {
        imageUrl = trip.getPhoto()
    }

    // Main container box for the hero section
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        when {
            imageUrl != null -> {
                GlideImage(
                    model = imageUrl,
                    contentDescription = "Trip Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // If image URL is still null or failed to load — show placeholder
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Placeholder",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }

        //Title and Destination of the trip
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(vertical = 30.dp, horizontal = 10.dp)
                .background(
                    color = Color(0xAA444444),
                    shape = MaterialTheme.shapes.small
                )

        ) {
            Text(
                text = trip.destination +
                        "\n" +
                        trip.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )
        }

        // Only show if import mode is active and user is logged in
        if (import && loggedUser != User()) {
            CreateACopyButton(
                trip = trip,
                vm = vm,
                loggedUser = loggedUser,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(vertical = 30.dp, horizontal = 10.dp)
            )
        }

        // Status Banner
        // Show "Completed" banner if trip has already ended
        if (trip.status == Trip.TripStatus.COMPLETED.toString()) {
            //Banner that indicated that the trip has already happened
            CompletedBanner(Modifier.align(Alignment.TopEnd))
            // If no spots left and cannot join, show "Fully Booked" banner
        } else if (!trip.canJoin()) {
            //Banner that shows that nobody can join the trip anymore
            BookedBanner(Modifier.align(Alignment.TopEnd))
        }
    }
}

//Function that create a good format for the dates
fun formatTripDate(calendar: Calendar): String {
    // Get local date
    val localDate = calendar.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    // Get day from local date
    val day = localDate.dayOfMonth
    // Get suffix for the day
    val suffix = getDaySuffix(day)
    // Get month from local date
    val month = localDate.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    // Get year from local date
    val year = localDate.year

    // Return in the format "dayWithSuffix month, year"
    return "$day$suffix $month, $year"
}

// Function that get the right suffix for the day of the month
fun getDaySuffix(day: Int): String {
    return if (day in 11..13) "th" else when (day % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}

// Composable with the box for the titles
@Composable
fun TitleBox(title: String) {
    Box(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp), clip = false)
            .background(
                color = Color(0xFFF4F4F4)
            )
    ) {
        Text(
            text = title,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        )
    }
}

// Extension function on Calendar class to compute how many whole days remain
// between this Calendar instance and another
fun Calendar.daysUntil(other: Calendar): Int {
    // Normalize both calendars to midnight
    val thisMidnight = this.clone() as Calendar
    thisMidnight.set(Calendar.HOUR_OF_DAY, 0)
    thisMidnight.set(Calendar.MINUTE, 0)
    thisMidnight.set(Calendar.SECOND, 0)
    thisMidnight.set(Calendar.MILLISECOND, 0)

    val otherMidnight = other.clone() as Calendar
    otherMidnight.set(Calendar.HOUR_OF_DAY, 0)
    otherMidnight.set(Calendar.MINUTE, 0)
    otherMidnight.set(Calendar.SECOND, 0)
    otherMidnight.set(Calendar.MILLISECOND, 0)

    // Time difference in days
    val millisPerDay = 1000 * 60 * 60 * 24
    val diffMillis = thisMidnight.timeInMillis - otherMidnight.timeInMillis

    // Convert the millisecond difference to whole days and return as Int
    // Integer division truncates toward zero, so partial days are ignored
    return (diffMillis / millisPerDay).toInt()
}


@Composable
fun ItineraryText(trip: Trip, modifier: Modifier = Modifier) {
    // Formatter to parse and format time strings
    val formatter =
        DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

    // String that represents the full itinerary, day by day
    val itineraryString = trip.activities
        // Sort days chronologically
        .toSortedMap(compareBy { it })
        .entries
        // For each day and its associated list of activities, build a formatted string
        .joinToString("\n\n") { (day, activities) ->
            // Determine if 'day' is a timestamp (Long) or a string and calculate the day index
            val dayIndex = if (isTimestampLong(day)) {
                timestampToCalendar(day).daysUntil(trip.startDateAsCalendar()) + 1
            } else {
                day.toCalendar().daysUntil(trip.startDateAsCalendar()) + 1
            }

            // Header for the day
            val dayHeader = "Day $dayIndex:\n"

            // Sort the activities of the day by their start time to display in order
            val sortedActivities = activities.sortedBy { activity ->
                try {
                    // Parse the activity time string to a LocalTime object for sorting
                    LocalTime.parse(activity.time, formatter)
                } catch (_: Exception) {
                    // If parsing fails, default to midnight
                    LocalTime.MIDNIGHT
                }
            }

            // For each sorted activity, create a descriptive string line
            val activityDescriptions = sortedActivities.joinToString("\n") { activity ->
                // Append a note if it's a group activity
                val groupActivity = if (activity.isGroupActivity) " (group activity)" else ""
                "- ${activity.time} → ${activity.description}$groupActivity"
            }

            // Return the full day's block: the day header plus all activities descriptions
            dayHeader + activityDescriptions
        }

    // Finally, display the full itinerary string inside a Text composable
    Text(
        text = itineraryString,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
fun DeleteMyTrip(
    trip: Trip, navController: NavController, vm: TripViewModel,
    uvm: UserViewModel
) {
    // State to control the visibility of the confirmation dialog popup
    val showDialog = remember { mutableStateOf(false) }

    // The Delete Button UI component
    Button(
        onClick = {
            // When clicked, show the confirmation dialog
            showDialog.value = true
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xd8, 0x1f, 0x1f, 255)
        )
    ) {
        Text("Delete")
    }

    // If the dialog visibility state is true, show the confirmation dialog popup
    if (showDialog.value) {
        AlertDialog(
            // Callback when user dismisses the dialog (click outside or back press)
            onDismissRequest = {
                showDialog.value = false
            },
            // Dialog title displayed at the top
            title = {
                Text(text = "Confirm Cancellation")
            },
            // Dialog message text content
            text = {
                // Conditional message based on trip status and number of participants
                if (trip.status != Trip.TripStatus.COMPLETED.toString() && trip.participants.size > 1) {
                    Text("Are you sure you want to delete this trip? This action will affect your reliability.")
                } else {
                    Text("Are you sure you want to delete this trip? This action cannot be undone.")
                }
            },
            // Confirm button shown in the dialog
            confirmButton = {
                Button(
                    onClick = {
                        // Logic for deleting or cancelling the trip based on status and conditions
                        // If trip is not completed
                        if (trip.status != Trip.TripStatus.COMPLETED.toString()) {
                            // If trip is unpublished or has only one participant
                            if (!trip.published || trip.participants.size == 1) {
                                // Reject all pending applications if any exist
                                if (trip.appliedUsers.isNotEmpty()) {
                                    trip.appliedUsers.forEach { userId, joinRequest ->
                                        vm.rejectApplication(trip, userId.toInt())
                                    }
                                }
                                // Proceed with deleting the trip
                                vm.deleteTrip(trip.id)
                                // Update the user's published trip
                                vm.updatePublishedTrip(uvm.loggedUser.value.id)
                            }

                            // If more than one participant is present
                            if (trip.participants.size > 1) {
                                // Select a new owner different from the current creator
                                val newOwner = trip.participants.entries.firstOrNull()
                                { it.key != trip.creatorId.toString() }
                                // Update the trip creator to the new owner if found
                                if (newOwner?.key?.toInt() != null) {
                                    vm.updateTripCreator(
                                        trip.id,
                                        newOwner.key.toInt(),
                                        trip.creatorId.toInt()
                                    )
                                }

                                // Penalize the current creator's reliability score by -10 for deleting the trip
                                uvm.updateUserReliability(
                                    trip.creatorId,
                                    -10
                                ) { success ->
                                    // Log success or failure of reliability
                                    if (success) {
                                        Log.d("TripDetails", "Reliability updated successfully")
                                    } else {
                                        Log.e("TripDetails", "Failed to update reliability")
                                    }
                                }
                            }
                        } else {
                            // If trip is completed, cancel the trip without deleting (hides it from UI)
                            vm.cancelTrip(trip.creatorId.toString(), trip.id.toString())
                        }

                        // Navigate back to the previous screen after deletion/cancellation
                        navController.popBackStack()

                        // Hide the confirmation dialog after operation finishes
                        showDialog.value = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        // Close the dialog
                        showDialog.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LeaveTrip(
    trip: Trip,
    navController: NavController,
    vm: TripViewModel,
    loggedUser: User,
    uvm: UserViewModel
) {
    // State variable to control whether the confirmation dialog is visible or not
    val showDialog = remember { mutableStateOf(false) }

    // Button that initiates the "Leave Trip" action
    Button(
        onClick = {
            // When clicked, show the confirmation dialog to prevent accidental leaving
            showDialog.value = true
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xd8, 0x1f, 0x1f, 255)
        )
    ) {
        Text("Leave Trip")
    }

    //PupUp that asks for confirmation of the cancellation of the trip
    if (showDialog.value) {
        AlertDialog(
            // When user dismisses the dialog (clicks outside or presses back), hide the dialog
            onDismissRequest = {
                showDialog.value = false
            },
            title = {
                Text(text = "Confirm Leave Trip ")
            },
            // The dialog message explaining consequences of leaving the trip
            text = {
                if (trip.status != Trip.TripStatus.COMPLETED.toString()) {
                    Text("Are you sure you want to leave this trip? This action will affect your reliability.")
                } else {
                    Text("Are you sure you want to leave this trip? The trip won't be visible in this section anymore. This action cannot be undone.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // For trips that are not completed
                        if (trip.status != Trip.TripStatus.COMPLETED.toString()) {
                            // Remove the user from the list of participants in the trip
                            vm.updateTripParticipants(trip.id, loggedUser.id)

                            // Penalize the user's reliability score by -5 points for leaving early
                            uvm.updateUserReliability(
                                loggedUser.id,
                                -5
                            ) { success ->
                                if (success) {
                                    Log.d("TripDetails", "Reliability updated successfully")
                                } else {
                                    Log.e("TripDetails", "Failed to update reliability")
                                }
                            }
                        } else {
                            // For completed trips
                            // Hide the trip from the user's view in this section
                            vm.cancelTrip(trip.creatorId.toString(), trip.id.toString())
                        }

                        // Navigate back to the previous screen after leaving the trip
                        navController.popBackStack()

                        // Hide the confirmation dialog once the operation is done
                        showDialog.value = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@SuppressLint("DiscouragedApi")
@Composable
fun ShowReview(
    review: Review,
    uvm: UserViewModel,
    navController: NavController
) {
    // Check if the review is about a trip or a user
    if (review.isTripReview) {
        // Get the user data of the reviewer (the person who wrote this review)
        val reviewer by uvm.getUserData(review.reviewerId).collectAsState(initial = User())

        // Outer Row container for the review header (reviewer info + rating)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Inner Row to hold profile picture, name and star rating
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Show profile photo and name only if reviewer data is valid and available
                if (reviewer != null && reviewer?.isValid() == true) {
                    // Circle-shaped box containing the reviewer's profile photo
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.Gray, shape = CircleShape)
                    ) {
                        // Display the reviewer's profile photo, with a flag indicating something (true)
                        ProfilePhoto(Modifier, reviewer!!, true)
                    }

                    // Show the reviewer's full name next to the profile photo
                    Text(
                        "${reviewer!!.firstname} ${reviewer!!.surname}",
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .clickable {
                                // Navigate to the reviewer's profile when name is clicked
                                navController.navigate("user_profile/${review.reviewerId}")
                            }
                    )
                }

                // Spacer pushes the stars to the right end of the row
                Spacer(modifier = Modifier.weight(1f))

                // Row that holds the star rating aligned to the right
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Display star icons representing the review's score
                    PrintStars(review.score)
                }
            }
        }

        // Row displaying the review title with bold font, padded to align with profile photo + name
        Row {
            Text(
                text = review.title,
                modifier = Modifier.padding(start = 50.dp, end = 16.dp),
                fontWeight = FontWeight.Bold
            )
        }

        // Row displaying the main review content, aligned similarly to title
        Row {
            Text(
                text = review.comment,
                modifier = Modifier.padding(start = 50.dp, end = 16.dp)
            )
        }

        // If the review includes photos, display them
        if (review.photos.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 50.dp, top = 8.dp, end = 16.dp)
            ) {
                // For each photo path in the review photos list
                items(review.photos) { photoPath ->
                    // State variable for the image URL, initially null
                    var imageUrl by remember(photoPath) { mutableStateOf<String?>(null) }

                    // Effect to load the photo URL asynchronously
                    LaunchedEffect(photoPath) {
                        imageUrl = review.getPhotoUrl(photoPath)
                    }

                    // Determine how to display the image based on the image URL or local resource type
                    when {
                        // Case 1: imageUrl is an HTTP or content URI, use GlideImage to load
                        imageUrl != null && (imageUrl!!.startsWith("http") || imageUrl!!.startsWith(
                            "content://"
                        )) -> {
                            GlideImage(
                                model = imageUrl,
                                contentDescription = "Review photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(100.dp)
                                    .padding(end = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                        // Case 2: photoPath is a URI string, use Compose AsyncImage painter
                        photoPath.isUriString() -> {
                            Image(
                                painter = rememberAsyncImagePainter(photoPath),
                                contentDescription = "Review photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(100.dp)
                                    .padding(end = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                        // Case 3: Otherwise treat as drawable resource identifier
                        else -> {
                            val context = LocalContext.current
                            val drawableId = remember(photoPath) {
                                context.resources.getIdentifier(
                                    photoPath,
                                    "drawable",
                                    context.packageName
                                )
                            }
                            // If valid drawable resource found, display it with crossfade animation
                            if (drawableId != 0) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(drawableId)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = photoPath,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(end = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.padding(16.dp))
    }
    // If the review is about another user (not a trip review)
    else {
        // Get data of the user being reviewed
        val reviewed by uvm.getUserData(review.reviewedUserId).collectAsState(initial = User())

        // Similar layout and structure as the trip review section but for user reviews
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Show reviewed user profile photo and name if valid
                if (reviewed != null && reviewed?.isValid() == true) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.Gray, shape = CircleShape)
                    ) {
                        ProfilePhoto(Modifier, reviewed!!, true)
                    }

                    Text(
                        "${reviewed!!.firstname} ${reviewed!!.surname}",
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .clickable {
                                navController.navigate("user_profile/${reviewed!!.id}")
                            }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Stars Rating Row
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrintStars(review.score)
                }
            }
        }

        // Review title with bold styling and padding
        Row {
            Text(
                text = review.title,
                modifier = Modifier.padding(start = 50.dp, end = 16.dp),
                fontWeight = FontWeight.Bold
            )
        }

        //Review content
        Row {
            Text(
                text = review.comment,
                modifier = Modifier.padding(start = 50.dp, end = 16.dp)
            )
        }

        Spacer(Modifier.padding(16.dp))
    }
}

// Function that prints the stars based on the score
@Composable
fun PrintStars(rating: Int) {
    // Calculate how many full stars to display
    val full = rating / 2

    // Calculate if there is a half star needed
    val half = rating - full * 2

    // Calculate how many empty stars to display to complete the total of 5 stars
    val empty = 5 - (full + half)

    // Loop through the number of full stars and display them
    for (i in 1..full) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "filled star",
            tint = Color(0xff, 0xb4, 0x00, 255)
        )
    }

    // If there is a half star (half > 0), display a half star icon
    if (half > 0) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.StarHalf,
            contentDescription = "half star",
            tint = Color(0xff, 0xb4, 0x00, 255)
        )
    }

    // If there are empty stars needed, display empty star icons for the remainder
    if (empty > 0) {
        for (i in 1..empty) {
            Icon(
                imageVector = Icons.Default.StarBorder,
                contentDescription = "empty star",
                tint = Color(0xff, 0xb4, 0x00, 255)
            )
        }
    }
}

@Composable
fun AsyncValidatingUsernameField(
    text: String,
    updateState: (String) -> Unit,
    label: String,
    userViewModel: UserViewModel,
    index: Int,
    validationStates: Map<Int, Boolean>,
    onValidationChange: (Int, Boolean, User) -> Unit
) {
    // Controller to programmatically hide the software keyboard
    val keyboardController = LocalSoftwareKeyboardController.current

    // State variable to track if username validation is currently running
    var isValidating by remember { mutableStateOf(false) }

    // State variable to store the last username text that was validated
    var lastValidatedText by remember { mutableStateOf("") }

    // Side-effect that triggers whenever the text changes
    // This performs asynchronous validation of the username with debounce logic
    LaunchedEffect(text) {
        // Only proceed if:
        // - Text is not blank
        // - Text is different from the last validated text (avoid redundant validation)
        // - Text length is at least 2 characters (minimum username length)
        if (text.isNotBlank() && text != lastValidatedText && text.length >= 2) {
            isValidating = true     // Start showing validation progress indicator
            delay(500) // Debounce delay to prevent validating on every keystroke

            // Call ViewModel to check if user exists asynchronously
            userViewModel.checkUserExistsAsync(text) { exists, user ->
                isValidating = false    // Validation completed, hide progress indicator
                lastValidatedText = text    // Update last validated username
                onValidationChange(
                    index,
                    exists,
                    user
                ) // Notify parent with validation result and user info
            }
        } else if (text.isBlank()) {
            // If input is blank, notify that validation failed and reset User
            onValidationChange(index, false, User())
        }
    }

    // Determine if there are validation errors for this input
    val hasErrors = text.isNotBlank() && validationStates[index] == false

    // Determine if the username is valid according to the validationStates map
    val isValid = validationStates[index] == true

    // Layout container for the input field and associated UI elements
    Column(
        modifier = Modifier
            .wrapContentSize()
            // Detect tap gestures outside the text field to dismiss the keyboard
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    keyboardController?.hide()  // Hide keyboard when tapping outside input
                })
            }
    ) {
        // The actual text input field with outlined styling
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            value = text,
            onValueChange = updateState,
            label = { Text(label) },
            isError = hasErrors,
            // Supporting text displayed below the input field to show validation messages or prompts
            supportingText = {
                when {
                    isValidating -> Text("Validating username...", color = Color.Black)
                    hasErrors -> Text("Username not found or invalid")
                    isValid -> Text("✓ Username found", color = Color.Black)
                    text.isBlank() -> Text("Enter a username")
                    else -> null
                }
            },
            // Trailing icon in the text field indicating validation status
            trailingIcon = {
                when {
                    isValidating -> CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )

                    isValid -> Icon(
                        Icons.Default.Check,
                        contentDescription = "Valid",
                        tint = Color.Black
                    )

                    hasErrors -> Icon(
                        Icons.Default.Close,
                        contentDescription = "Invalid",
                        tint = Color.Black
                    )
                }
            },
            // Configure the keyboard type for this input to be text
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
    }
}

@Composable
fun CreateACopyButton(
    trip: Trip,
    vm: TripViewModel,
    loggedUser: User,
    modifier: Modifier = Modifier
) {

    // Coroutine scope for launching delayed side effects
    val coroutineScope = rememberCoroutineScope()

    // State to determine whether the confirmation popup should be shown
    var showPopup by remember { mutableStateOf(false) }

    // UI container for the "Create a Copy" floating action button
    Box(
        modifier = modifier
            .background(
                color = Color(0xC8F3EDF7), // semi-transparent soft lavender
                shape = CircleShape
            )
            .size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = {
                // Call ViewModel to add a new imported (copied) trip using the current trip's data
                vm.addImportedTrip(
                    trip.photo,
                    trip.title,
                    trip.destination,
                    trip.startDateAsCalendar(),
                    trip.endDateAsCalendar(),
                    trip.estimatedPrice,
                    trip.groupSize,
                    trip.activities,
                    trip.typeTravel,
                    loggedUser.id,
                    false
                ) { success, importedTrip ->
                    if (success) {
                        // Update the list of published trips
                        vm.updatePublishedTrip(loggedUser.id)

                        // Show success popup to user
                        showPopup = true

                        // Launch coroutine to hide popup after 2 seconds
                        coroutineScope.launch {
                            delay(2000)
                            showPopup = false
                        }
                    } else {
                        Log.e("TripDetails", "Failed to create trip copy")
                    }
                }
            },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FileDownload, // Change icon if desired
                contentDescription = "Create a Copy",
                tint = Color(0xFF3C3C43) // High contrast for visibility
            )
        }
    }

    // Show temporary success popup if showPopup is true
    if (showPopup) {
        Popup(
            alignment = Alignment.TopCenter,
            onDismissRequest = { showPopup = false }    // Allow dismissing the popup manually
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(top = 80.dp)
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                // Layout for the confirmation popup content
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckBox,
                        contentDescription = "Success",
                        tint = Color.Green
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Copy created in 'My Trips'",
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}