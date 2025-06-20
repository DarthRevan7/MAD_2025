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
import com.example.voyago.model.stringToCalendar
import com.example.voyago.model.timestampToCalendar
import com.example.voyago.viewmodel.NotificationViewModel
import com.example.voyago.viewmodel.ReviewViewModel
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

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
    nvm: NotificationViewModel
) {
    // The logged in user

    val loggedUser by uvm.loggedUser.collectAsState()
    // 添加登录状态检查


    //  修复：更准确的登录状态检查
    val isUserLoggedIn = remember(loggedUser) {
        loggedUser.id != 0 &&
                loggedUser.username.isNotEmpty() &&
                loggedUser.email.isNotEmpty()
    }


    var trip = when (vm.userAction) {
        TripViewModel.UserAction.VIEW_TRIP -> vm.selectedTrip.value
        TripViewModel.UserAction.VIEW_OTHER_TRIP -> vm.otherTrip.value
        else -> vm.selectedTrip.value

    }

    val fields =
        navController.previousBackStackEntry?.savedStateHandle?.get<TripNotification>("notificationValues")

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


    //Trip participants map
    val participantsMap by vm.tripParticipants.collectAsState()
    // The logged in user


    DisposableEffect(Unit) {
        onDispose {
            vm.userAction = TripViewModel.UserAction.VIEW_TRIP
        }
    }

    if (!trip.isValid()) {
        Text("Trip ${vm.otherTrip.value.id} ${vm.userAction} Loading trip details...")
        return
    }

    //The user joined the trip but didn't created
    val joined =
        trip.participants.containsKey(loggedUser.id.toString()) && trip.creatorId != loggedUser.id

    //Manage join request
    val askedTrips: Map<String, Int> by vm.askedTrips.collectAsState()
    vm.syncAskedTrips(loggedUser.id) {}
    val requestedSpots = trip.id.let { askedTrips[it.toString()] } ?: 0
    val hasAsked = requestedSpots > 0

    var showDialog by remember { mutableStateOf(false) }
    var selectedSpots by remember { mutableIntStateOf(1) }

    //To manage phase of the dialog
    var dialogPhase by remember { mutableIntStateOf(0) }

    //Data of other participants
    var unregisteredParticipants by remember { mutableStateOf(listOf<Participant>()) }
    var registeredUsernames by remember { mutableStateOf(listOf<String>()) }
    var isRegisteredList by remember { mutableStateOf(listOf<Boolean>()) }

    var usernameTouchedList by remember { mutableStateOf(listOf<Boolean>()) }
    var nameTouchedList by remember { mutableStateOf(listOf<Boolean>()) }
    var surnameTouchedList by remember { mutableStateOf(listOf<Boolean>()) }
    var emailTouchedList by remember { mutableStateOf(listOf<Boolean>()) }

    var registeredUserList: MutableList<User> = mutableListOf()

    //Initialization of the list of other participants
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

    //Get reviews of the trip
    val reviews by rvm.tripReviews.collectAsState()
    val isReviewed by rvm.isReviewed.collectAsState()

    //Manage reviews
    LaunchedEffect(trip.id, loggedUser.id) {
        if (trip.id != 0) {
            rvm.getTripReviews(trip.id)
            vm.getTripParticipants(trip)
            rvm.isReviewed(loggedUser.id, trip.id)
        }

    }

    val listState = rememberLazyListState()

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val isAfterToday = trip.startDateAsCalendar().after(today)
    var publishError by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            //Trip image
            item {
                Hero(trip, vm, loggedUser, true)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            //Trip information
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp)
                ) {
                    Text(
                        text = formatTripDate(trip.startDateAsCalendar()) + " - " +
                                formatTripDate(trip.endDateAsCalendar()) + "\n " +
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
                //The trip created by the logged in user is published
                if (trip.published && trip.creatorId == loggedUser.id) {
                    item {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box {
                                if (trip.status == Trip.TripStatus.COMPLETED.toString()) {
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

                                        if (isReviewed == false) {
                                            Box(
                                                modifier = Modifier
                                                    .size(15.dp)
                                                    .background(Color(0xFF448AFF), CircleShape)
                                                    .align(Alignment.TopEnd)
                                            )
                                        }
                                    }
                                } else {
                                    //Applications Button
                                    Button(
                                        onClick = {
                                            navController.navigate("trip_applications")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0x14, 0xa1, 0x55, 255)
                                        )
                                    ) {
                                        Text("Applications")
                                    }
                                }

                                if (trip.appliedUsers.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(15.dp)
                                            .background(Color.Red, CircleShape)
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }

                            Spacer(Modifier.weight(3f))

                            //"Create a Copy" Button (creates a copy of the trip in the logged in user private trips)
                            //CreateACopyButton(trip, vm, loggedUser)

                            Spacer(Modifier.weight(1f))

                            //Private Button (makes the trip private)
                            if (!trip.participants.any { it.key != trip.creatorId.toString() }) {
                                Button(
                                    onClick = {
                                        vm.changePublishedStatus(trip.id)
                                        vm.updatePublishedTrip()
                                        navController.popBackStack()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0x65, 0x55, 0x8f, 255)
                                    )
                                ) {
                                    Text("Private")
                                }
                            }
                            Spacer(Modifier.padding(5.dp))

                            //Delete button with popup for confirmation
                            DeleteMyTrip(trip, navController, vm, uvm)
                        }
                    }
                }

                if (joined) {
                    item {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (trip.status == Trip.TripStatus.COMPLETED.toString()) {
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

                            if (isUserLoggedIn) {
                                //Delete button with popup for confirmation
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
                                        if (isAfterToday) {
                                            vm.changePublishedStatus(trip.id)
                                            vm.updatePublishedTrip()

                                            //Send notifications
                                            val title = "Check this out!"
                                            val body =
                                                "This trip to ${trip.destination} looks interesting for you!"
                                            val notificationType = "TRIP"
                                            val idLink = trip.id

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

                                                navController.popBackStack()
                                            }
                                        } else {
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

                            //CreateACopyButton(trip, vm, loggedUser)

                            Spacer(Modifier.padding(5.dp))

                            //Delete button with popup for confirmation
                            DeleteMyTrip(trip, navController, vm, uvm)
                        }

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

                item {
                    TitleBox("My Itinerary")
                }
            }
            //The logged in user see a published trip in the "Explore" section
            else {
                item {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        // 只有登录用户才显示 "Create a Copy" 按钮
                        if (isUserLoggedIn) {
                            //"Create a Copy" Button (creates a copy of the trip in the logged in user private trips)
                            //CreateACopyButton(trip, vm, loggedUser)

                            Spacer(Modifier.padding(5.dp))
                        }

                        if (isUserLoggedIn) {
                            //If the user can join the trip
                            if (trip.canJoin() && trip.creatorId != loggedUser.id && !joined) {
                                //Ask to Join/Asked to Join Button
                                Button(
                                    onClick = {
                                        if (hasAsked) {
                                            vm.cancelAskToJoin(trip, loggedUser.id)
                                        } else {
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

                            } else if (joined
                                && trip.status != Trip.TripStatus.COMPLETED.toString()

                            ) {
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
                            // 如果用户未登录，显示提示按钮引导用户登录
                            // 修复：更详细的未登录状态处理
                            Button(
                                onClick = {
                                    Log.d("TripDetails", "User not logged in, navigating to login")
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

                item {
                    TitleBox("Itinerary")
                }
            }

            //The Itinerary of the trip
            item {
                ItineraryText(
                    trip,
                    modifier = Modifier
                        .padding(start = 24.dp, top = 16.dp, end = 20.dp),
                    vm
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                TitleBox("Created by:")
            }

            item {
                val user = uvm.getUserData(trip.creatorId).collectAsState(initial = User()).value

                if (user != null) {
                    ShowParticipants(
                        user = user,
                        joinRequest = JoinRequest(
                            user.id,
                            1,
                            emptyList(),
                            emptyList()
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

                //List of reviews of the trip
                items(reviews) { review ->
                    ShowReview(review, vm, false, uvm, navController)
                }
            }

            if (trip.participants.size > 1) {
                item {
                    TitleBox("Participants:")
                }

                items(participantsMap.entries.toList()) { entry ->
                    val user = entry.key
                    val spots = entry.value
                    if (trip.creatorId != user.id) {
                        ShowParticipants(user, spots, uvm, navController)
                    }
                }
            }

        }


        var usernameValidationStates by remember { mutableStateOf(mutableMapOf<Int, Boolean>()) }


        //PopUp that appears when the user ask to join a trip
        if (showDialog) {
            val maxSpots = trip.availableSpots()

            AlertDialog(
                onDismissRequest = {
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { if (selectedSpots > 1) selectedSpots-- },
                                enabled = selectedSpots > 1
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease")
                            }

                            Text(
                                "$selectedSpots",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.headlineMedium
                            )

                            IconButton(
                                onClick = { if (selectedSpots < maxSpots) selectedSpots++ },
                                enabled = selectedSpots < maxSpots
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase")
                            }
                        }
                    } else {
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
                                                if (checked) {
//                                                    unregisteredParticipants =
//                                                        unregisteredParticipants.toMutableList()
//                                                            .also {
//                                                                it[i] = Participant("", "", "")
//                                                            }
                                                } else {
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
                                    if (isRegistered) {
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
                                        if (participant != null) {
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
                                vm.askToJoin(
                                    trip,
                                    loggedUser.id,
                                    selectedSpots,
                                    emptyList(),
                                    emptyList()
                                )

                                // Notification
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
                            var hasErrors = false

                            for (i in 0 until isRegisteredList.count() - 1) {
                                if (isRegisteredList[i]) {
                                    val username = registeredUsernames[i]
                                    val isUsernameValid = usernameValidationStates[i] == true

                                    if (username.isBlank() || !username.any { it.isLetter() } || !isUsernameValid) {
                                        hasErrors = true
                                        usernameTouchedList = usernameTouchedList.toMutableList()
                                            .also { it[i] = true }
                                    }
                                } else {
                                    // ... 未注册用户验证逻辑保持不变 ... Why Chinese comments?? D:
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


                            if (!hasErrors) {
                                val idList = registeredUserList.map { it.id }.toList()

                                unregisteredParticipants.forEach {
                                    if (it.name.isEmpty() && it.surname.isEmpty() && it.email.isEmpty()) {
                                        unregisteredParticipants -= it
                                    }
                                }

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
                                usernameValidationStates = mutableMapOf() //  重置验证状态

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
                        usernameValidationStates = mutableMapOf() //  重置验证状态

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
    var imageUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(trip.id) {
        imageUrl = trip.getPhoto()
    }
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


        if (trip.status == Trip.TripStatus.COMPLETED.toString()) {
            //Banner that indicated that the trip has already happened
            CompletedBanner(Modifier.align(Alignment.TopEnd))
        } else if (!trip.canJoin()) {
            //Banner that shows that nobody can join the trip anymore
            BookedBanner(Modifier.align(Alignment.TopEnd))
        }
    }
}

//Function that create a good format for the dates
fun formatTripDate(calendar: Calendar): String {
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val suffix = getDayOfMonthSuffix(day)
    val dateFormat = SimpleDateFormat("MMMM d'$suffix', yyyy", Locale.ENGLISH)

    return dateFormat.format(calendar.time)
}

fun getDayOfMonthSuffix(day: Int): String {
    return if (day in 11..13) {
        "th"
    } else when (day % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}

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

    val millisPerDay = 1000 * 60 * 60 * 24
    val diffMillis = thisMidnight.timeInMillis - otherMidnight.timeInMillis

    return (diffMillis / millisPerDay).toInt()
}


@Composable
fun ItineraryText(trip: Trip, modifier: Modifier = Modifier, vm: TripViewModel) {
    val formatter =
        DateTimeFormatter.ofPattern("hh:mm a", Locale.US) // Same format used in your first view

    val itineraryString = trip.activities
        .toSortedMap(compareBy { it }) // Sort days chronologically
        .entries
        .joinToString("\n\n") { (day, activities) ->
            Log.d("L1", "Trip Details")
            val dayIndex = if (isTimestampLong(day)) {
                Log.d("L1", "Day is a timestamp: $day")
                timestampToCalendar(day).daysUntil(trip.startDateAsCalendar()) + 1
            } else {
                Log.d("L1", "Day is a string: $day")
                stringToCalendar(day).daysUntil(trip.startDateAsCalendar()) + 1
            }
            val dayHeader = "Day $dayIndex:\n"

            val sortedActivities = activities.sortedBy { activity ->
                try {
                    LocalTime.parse(activity.time, formatter)
                } catch (e: Exception) {
                    LocalTime.MIDNIGHT // fallback if parsing fails
                }
            }

            val activityDescriptions = sortedActivities.joinToString("\n") { activity ->
                val groupActivity = if (activity.isGroupActivity) " (group activity)" else ""
                "- ${activity.time} → ${activity.description}$groupActivity"
            }

            dayHeader + activityDescriptions
        }

    Text(
        text = itineraryString,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )

    println("Trip start date (timestamp): ${trip.startDate}")
    println("Trip start date (millis): ${trip.startDate.toDate().time}")
    println("Trip start date (formatted): ${trip.startDate.toDate()}")
}

@Composable
fun DeleteMyTrip(trip: Trip, navController: NavController, vm: TripViewModel, uvm: UserViewModel) {
    val showDialog = remember { mutableStateOf(false) }

    //Delete Button
    Button(
        onClick = {
            showDialog.value = true
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xd8, 0x1f, 0x1f, 255)
        )
    ) {
        Text("Delete")
    }

    //PupUp that asks for confirmation of the cancellation of the trip
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDialog.value = false
            },
            title = {
                Text(text = "Confirm Cancellation")
            },
            text = {
                if (trip.status != Trip.TripStatus.COMPLETED.toString() && trip.participants.size > 1) {
                    Text("Are you sure you want to delete this trip? This action will affect your reliability.")
                } else {
                    Text("Are you sure you want to delete this trip? This action cannot be undone.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (trip.status != Trip.TripStatus.COMPLETED.toString()) {
                            if (!trip.published || trip.participants.size == 1) {
                                if (trip.appliedUsers.isNotEmpty()) {
                                    trip.appliedUsers.forEach { userId, joinRequest ->
                                        vm.rejectApplication(trip, userId.toInt())
                                    }
                                }
                                vm.deleteTrip(trip.id)
                                vm.updatePublishedTrip()
                            }

                            if (trip.participants.size > 1) {
                                val newOwner = trip.participants.entries.firstOrNull()
                                { it.key != trip.creatorId.toString() }
                                if (newOwner?.key?.toInt() != null) {
                                    vm.updateTripCreator(
                                        trip.id,
                                        newOwner.key.toInt(),
                                        trip.creatorId.toInt()
                                    )
                                }
                                uvm.updateUserReliability(
                                    trip.creatorId.toString(),
                                    -10
                                ) { success ->
                                    if (success) {
                                        Log.d("TripDetails", "Reliability updated successfully")
                                    } else {
                                        Log.e("TripDetails", "Failed to update reliability")
                                    }
                                }
                            }
                        } else {
                            //Don't show the trip in this section anymore
                            vm.cancelTrip(trip.creatorId.toString(), trip.id.toString())
                        }
                        navController.popBackStack()
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

@Composable
fun LeaveTrip(
    trip: Trip,
    navController: NavController,
    vm: TripViewModel,
    loggedUser: User,
    uvm: UserViewModel
) {
    val showDialog = remember { mutableStateOf(false) }

    //Delete Button
    Button(
        onClick = {
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
            onDismissRequest = {
                showDialog.value = false
            },
            title = {
                Text(text = "Confirm Leave Trip ")
            },
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
                        if (trip.status != Trip.TripStatus.COMPLETED.toString()) {
                            vm.updateTripParticipants(trip.id, loggedUser.id)
                            uvm.updateUserReliability(
                                loggedUser.id.toString(),
                                -5
                            ) { success ->
                                if (success) {
                                    Log.d("TripDetails", "Reliability updated successfully")
                                } else {
                                    Log.e("TripDetails", "Failed to update reliability")
                                }
                            }
                        } else {
                            //Don't show the trip in this section anymore
                            vm.cancelTrip(trip.creatorId.toString(), trip.id.toString())
                        }
                        navController.popBackStack()
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
    vm: TripViewModel,
    myTrip: Boolean,
    uvm: UserViewModel,
    navController: NavController
) {

    if (review.isTripReview) {
        val reviewer by uvm.getUserData(review.reviewerId).collectAsState(initial = User())


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
                //val reviewer = reviewer
                if (reviewer != null && reviewer?.isValid() == true) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.Gray, shape = CircleShape)
                    ) {
                        ProfilePhoto(reviewer!!, true, Modifier, uvm)
                        Log.d("R1", "Reviewer: ${reviewer?.id}")
                    }

                    Text(
                        "${reviewer!!.firstname} ${reviewer!!.surname}",
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .clickable {
                                navController.navigate("user_profile/${review.reviewerId}")
                            }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrintStars(review.score)
                }
            }


            //Stars rating
            Row(
                modifier = Modifier
                    .weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                PrintStars(review.score)
            }
        }

        //Review title
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

        // Show photos if there are any
        if (review.photos.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 50.dp, top = 8.dp, end = 16.dp)
            ) {
                items(review.photos) { photoPath ->
                    var imageUrl by remember(photoPath) { mutableStateOf<String?>(null) }

                    LaunchedEffect(photoPath) {
                        // 获取 Firebase Storage URL
                        imageUrl = review.getPhotoUrl(photoPath)
                    }

                    when {
                        // Firebase Storage 或 URI 图片
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
                        // URI 图片的备用处理
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
                        // Drawable 资源
                        else -> {
                            val context = LocalContext.current
                            val drawableId = remember(photoPath) {
                                context.resources.getIdentifier(
                                    photoPath,
                                    "drawable",
                                    context.packageName
                                )
                            }
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
    //Review for other users
    else {
        val reviewed by uvm.getUserData(review.reviewedUserId).collectAsState(initial = User())


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
                //val reviewer = reviewer
                if (reviewed != null && reviewed?.isValid() == true) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.Gray, shape = CircleShape)
                    ) {
                        ProfilePhoto(reviewed!!, true, Modifier, uvm)
                        Log.d("R1", "Reviewer: ${reviewed?.id}")
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

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrintStars(review.score)
                }
            }


            //Stars rating
            Row(
                modifier = Modifier
                    .weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                PrintStars(review.score)
            }
        }

        //Review title
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

        // Show photos if there are any
        if (review.photos.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 50.dp, top = 8.dp, end = 16.dp)
            ) {
                items(review.photos) { photoPath ->
                    var imageUrl by remember(photoPath) { mutableStateOf<String?>(null) }

                    LaunchedEffect(photoPath) {
                        // 获取 Firebase Storage URL
                        imageUrl = review.getPhotoUrl(photoPath)
                    }

                    when {
                        // Firebase Storage 或 URI 图片
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
                        // URI 图片的备用处理
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
                        // Drawable 资源
                        else -> {
                            val context = LocalContext.current
                            val drawableId = remember(photoPath) {
                                context.resources.getIdentifier(
                                    photoPath,
                                    "drawable",
                                    context.packageName
                                )
                            }
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
}

@Composable
fun PrintStars(rating: Int) {
    val full = rating / 2
    val half = rating - full * 2
    val empty = 5 - (full + half)
    for (i in 1..full) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "filled star",
            tint = Color(0xff, 0xb4, 0x00, 255)
        )
    }
    if (half > 0) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.StarHalf,
            contentDescription = "half star",
            tint = Color(0xff, 0xb4, 0x00, 255)
        )
    }
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
    val keyboardController = LocalSoftwareKeyboardController.current
    var isValidating by remember { mutableStateOf(false) }
    var lastValidatedText by remember { mutableStateOf("") }

    // 🔴 实时验证用户名
    LaunchedEffect(text) {
        if (text.isNotBlank() && text != lastValidatedText && text.length >= 2) {
            isValidating = true
            delay(500) // 防抖动，避免过频繁的查询

            userViewModel.checkUserExistsAsync(text) { exists, user ->
                isValidating = false
                lastValidatedText = text
                onValidationChange(index, exists, user)

                Log.d("UsernameValidation", "Username '$text' exists: $exists")
            }
        } else if (text.isBlank()) {
            onValidationChange(index, false, User())
        }
    }

    val hasErrors = text.isNotBlank() && validationStates[index] == false
    val isValid = validationStates[index] == true

    Column(
        modifier = Modifier
            .wrapContentSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    keyboardController?.hide()
                })
            }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            value = text,
            onValueChange = updateState,
            label = { Text(label) },
            isError = hasErrors,
            supportingText = {
                when {
                    isValidating -> Text("Validating username...", color = Color.Black)
                    hasErrors -> Text("Username not found or invalid")
                    isValid -> Text("✓ Username found", color = Color.Black)
                    text.isBlank() -> Text("Enter a username")
                    else -> null
                }
            },
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
    val coroutineScope = rememberCoroutineScope()
    var showPopup by remember { mutableStateOf(false) }

    // Floating "Create a Copy" IconButton with styled background
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
                Log.d("TripCopy", "Original trip photo: ${trip.photo}")

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
                        Log.d(
                            "TripCopy",
                            "Imported trip created with photo: ${importedTrip?.photo}"
                        )
                        vm.updatePublishedTrip()

                        showPopup = true
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

    // Confirmation Popup
    if (showPopup) {
        Popup(
            alignment = Alignment.TopCenter,
            onDismissRequest = { showPopup = false }
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(top = 80.dp)
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
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
