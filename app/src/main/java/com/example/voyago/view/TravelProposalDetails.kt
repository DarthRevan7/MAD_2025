package com.example.voyago.view

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.voyago.activities.BottomBar
import com.example.voyago.activities.TopBar
import com.example.voyago.model.Trip
import com.example.voyago.viewmodel.TripListViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import com.example.voyago.activities.ProfilePhoto
import com.example.voyago.model.Review
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Popup
import androidx.core.net.toUri
import com.example.voyago.viewmodel.TripViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TravelProposalDetail(navController: NavController, vm: TripViewModel, owner: Boolean) {
    val trip = vm.selectedTrip



    var showPopup by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val askedTrips: Set<Int> by vm.askedTrips.collectAsState()
    val hasAsked = askedTrips.contains(trip.id)

    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            var id = 0
            if (owner) {
                id = 1
            }
            BottomBar(id)
        }
    ) { innerPadding ->

        val listState = rememberLazyListState()

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
                            text = formatTripDate(trip.startDate) + " - " +
                                    formatTripDate(trip.endDate) + "\n " +
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

                if (owner) {
                    if (trip.published) {
                        item {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Box {
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

                                    if (trip.appliedUsers.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(15.dp)
                                                .background(Color.Red, CircleShape)
                                                .align(Alignment.TopEnd)
                                        )
                                    }
                                }

                                Spacer(Modifier.weight(1f))

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

                                Spacer(Modifier.padding(5.dp))

                                DeleteButtonWithConfirmation(trip, navController, vm)
                            }
                        }
                    }

                    if (!trip.published) {
                        item {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        vm.changePublishedStatus(trip.id)
                                        vm.updatePublishedTrip()
                                        navController.popBackStack()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0x14, 0xa1, 0x55, 255)
                                    )
                                ) {
                                    Text("Publish")
                                }

                                Spacer(Modifier.padding(5.dp))

                                DeleteButtonWithConfirmation(trip, navController, vm)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        TitleBox("My Itinerary")
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    vm.addImportedTrip(
                                        trip.photo,
                                        trip.title,
                                        trip.destination,
                                        trip.startDate,
                                        trip.endDate,
                                        trip.estimatedPrice,
                                        trip.groupSize,
                                        trip.activities,
                                        trip.typeTravel,
                                        1,
                                        false
                                    )
                                    vm.updatePublishedTrip()

                                    showPopup = true
                                    coroutineScope.launch {
                                        delay(2000)
                                        showPopup = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x65, 0x55, 0x8f, 255)
                                )
                            ) {
                                Text("Create a Copy")
                            }

                            Spacer(Modifier.padding(5.dp))

                            if (trip.canJoin()) {
                                Button(
                                    onClick = {
                                        vm.toggleAskToJoin(trip.id)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor =
                                            if (hasAsked)
                                                Color(0x65, 0xa9, 0x8b, 255)
                                            else
                                                Color(0x14, 0xa1, 0x55, 255)
                                    )
                                ) {
                                    if (hasAsked) {
                                        Icon(Icons.Default.Check, "check")
                                        Text("Asked to Join")
                                    } else {
                                        Text("Ask to Join")
                                    }
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

                if (trip.reviews.isNotEmpty()) {
                    item {
                        TitleBox("Reviews")
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(trip.reviews) { review ->
                        ShowReview(review)
                    }
                }
            }

            if (showPopup) {
                Popup(
                    alignment = Alignment.TopCenter,
                    onDismissRequest = {
                        showPopup = false
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(top = 80.dp)
                            .background(Color.White, shape = RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Row {
                            Icon(
                                imageVector = Icons.Default.CheckBox,
                                contentDescription = "check",
                                tint = Color.Green
                            )
                            Spacer(Modifier.padding(5.dp))
                            Text(
                                text = "Copy created in 'My Trips'",
                                color = Color.Black,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun Hero(trip: Trip) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        val context = LocalContext.current
        val drawableId = remember(trip.photo) {
            context.resources.getIdentifier(trip.photo, "drawable", context.packageName)
        }

        if(trip.photo.isUriString()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(trip.photo.toUri())
                    .crossfade(true)
                    .build(),
                contentDescription = "Selected Trip Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(drawableId)
                    .crossfade(true)
                    .build(),
                contentDescription = trip.photo,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )
        }

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

        if (!trip.canJoin()) {
            CompletedBanner(Modifier.align(Alignment.TopEnd))
        }
    }
}

@Composable
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
fun TitleBox(title:String) {
    Box(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp), clip = false)
            .background(
                color = Color(0xFFF4F4F4))
    ) {
        Text(
            text = title,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        )
    }
}

@Composable
fun ItineraryText(trip: Trip, modifier: Modifier = Modifier) {

    val itineraryString = trip.activities.entries.joinToString("\n\n") { (day, activities) ->
        val dayIndex = ((day.timeInMillis - trip.startDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt() + 1
        val dayHeader = "Day $dayIndex:\n"
        val activityDescriptions = activities.joinToString("\n") {activity ->
            val groupActivity = if (activity.isGroupActivity) "(group activity)" else ""
            "- ${activity.time} → ${activity.description} $groupActivity"
        }
        dayHeader + activityDescriptions
    }

    Text(
        text = itineraryString,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        modifier = modifier

    )
}

@Composable
fun DeleteButtonWithConfirmation(trip: Trip, navController: NavController, vm: TripViewModel) {
    val showDialog = remember { mutableStateOf(false) }

    Button(onClick = {
        showDialog.value = true
    },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xd8, 0x1f, 0x1f, 255)
        )
    ) {
        Text("Delete")
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDialog.value = false
            },
            title = {
                Text(text = "Confirm Cancellation")
            },
            text = {
                Text("Are you sure you want to delete this trip?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.deleteTrip(trip.id)
                        vm.updatePublishedTrip()
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
fun ShowReview(review: Review) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .size(30.dp)
                .background(Color.Gray, shape = CircleShape)
        ) {
            ProfilePhoto(review.reviewer.name, review.reviewer.surname,true, null)
        }
        Text("${review.reviewer.name} ${review.reviewer.surname}",
            modifier = Modifier.padding( start = 16.dp))

        Row(
            modifier = Modifier
                .weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically)
        {
            PrintStars(review.rating)
        }
    }

    Row {
        Text(
            text = review.title,
            modifier = Modifier.padding(start = 50.dp, end = 16.dp),
            fontWeight = FontWeight.Bold)
    }
    Row {
        Text(
            text = review.text,
            modifier = Modifier.padding(start = 50.dp, end = 16.dp)
        )
    }

    Spacer(Modifier.padding(16.dp))
}

@Composable
fun PrintStars(rating: Int) {
    val full = rating/2
    val half = rating - full*2
    val empty = 5 - (full+half)
    for(i in 1..full) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "filled star",
            tint = Color(0xff, 0xb4, 0x00, 255))
    }
    if (half > 0 ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.StarHalf,
            contentDescription = "half star",
            tint = Color(0xff, 0xb4, 0x00, 255))
    }
    if (empty > 0) {
        for (i in 1..empty) {
            Icon(
                imageVector = Icons.Default.StarBorder,
                contentDescription = "empty star",
                tint = Color(0xff, 0xb4, 0x00, 255))
        }
    }
}

