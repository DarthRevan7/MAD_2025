package com.example.voyago.view

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.voyago.R
import com.example.voyago.activities.*
import com.example.voyago.model.Article
import com.example.voyago.model.Review
import com.example.voyago.model.Trip
import com.example.voyago.model.User
import com.example.voyago.viewmodel.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(vm: TripViewModel, navController: NavController, vm2: ArticleViewModel, uvm: UserViewModel) {
    //Get the logged in user (id=1)
    val user by uvm.loggedUser.collectAsState()
    //List of trip created and published by the logged in user (id=1)
    val publishedTrips by vm.publishedTrips.collectAsState()
    //List of trip the logged in user (id=1) joined
    val joinedTrips by vm.joinedTrips.collectAsState()

    LaunchedEffect(user.id) {
        if (user.id != 0) {
            Log.d("Trips", "my trips id: ${user.id}")
            vm.creatorPublicFilter(user.id)
            vm.creatorPrivateFilter(user.id)
            vm.tripUserJoined(user.id)
        }
    }

    //Icons
    val painterLogout = painterResource(R.drawable.logout)
    val painterEdit = painterResource(R.drawable.edit)

    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            //Box with Profile Photo, Username and Logout, Back and Edit icons
            Box(modifier =
                Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color(0xdf, 0xd1, 0xe0, 255), shape = RectangleShape)) {

                Image(painter = painterLogout, "logout", modifier = Modifier
                    .size(60.dp)
                    .align(alignment = Alignment.TopEnd)
                    .padding(16.dp)
                    .clickable {/*TODO*/}
                )

                Image(painter = painterEdit, "edit", modifier = Modifier
                    .size(60.dp)
                    .align(alignment = Alignment.BottomEnd)
                    .padding(16.dp)
                    .offset(y = (-30).dp)
                    .clickable {
                        uvm.setProfileImageUri(user.profilePictureUrl?.toUri())
                        navController.navigate("edit_profile")
                    }
                )

                ProfilePhoto(
                    user,
                    false,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-50).dp),
                    uvm
                )

                Text(
                    text = user.username,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 10.dp)
                        .offset(y = (40).dp)
                )

                Text(
                    text = user.firstname + " " + user.surname,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .offset(y = (-50).dp)
                )

                Spacer( Modifier.height(20.dp))

                Text(
                    text = user.country,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .offset(y = (-20).dp)
                )
            }
        }

        item {
            //Row with rating and reliability
            Row(
                modifier = Modifier
                    .offset(y = (-25).dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                RatingAndReliability(
                    user.rating,
                    user.reliability
                )
            }
        }

        item {
            //Tab About, My Trips, Review
            TabAboutTripsReview(user, joinedTrips, publishedTrips, vm, vm2, navController, uvm)
        }
    }
}

@Composable
fun RatingAndReliability(rating: Float, reliability: Int) {

    val painterStar = painterResource(R.drawable.star)
    val painterMobile = painterResource(R.drawable.mobile)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth() // Ensure the row uses full width to allow centering
    ) {
        //Box with rating
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(149.dp, 44.dp)
                .background(Color(0xc1, 0xa5, 0xc3, 255), shape = RoundedCornerShape(10.dp))
                .border(2.dp, color = Color.White, shape = RoundedCornerShape(10.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterStar, contentDescription = "star",
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "$rating approval",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        //Box with reliability
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(149.dp, 44.dp)
                .background(Color(0xc1, 0xa5, 0xc3, 255), shape = RoundedCornerShape(10.dp))
                .border(2.dp, color = Color.White, shape = RoundedCornerShape(10.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterMobile, contentDescription = "mobile",
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    text = "$reliability% reliable",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TabAboutTripsReview(user: User, joinedTrips: List<Trip>, publishedTrips: List<Trip>, vm: TripViewModel, vm2: ArticleViewModel, navController: NavController, uvm: UserViewModel) {

    // TAB with About, Trips & Articles, Reviews
    val tabs = listOf("About", "Trips & Articles", "Reviews")

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }


    LaunchedEffect(Unit) {
        vm.creatorPublicFilter(user.id)
        vm.tripUserJoined(user.id)
    }

    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier.background(Color(0xfe, 0xf7, 0xff, 255)),
        contentColor = Color.Black
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = {selectedTabIndex = index},
                text = {
                    Text(title, color = if (index == selectedTabIndex) {
                        Color(0x65, 0x55, 0x8f, 255)
                    } else {
                        Color.Black
                    })
                }
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)
    ) {

        when(selectedTabIndex) {

            0 -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(user.userDescription)
                    Text(text = "Preferences about the type of travel:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    FlowRow(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        user.typeTravel.forEach { type ->
                            SuggestionChip(
                                onClick = {},
                                label = {Text(type.toString().lowercase())},
                                colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                                    labelColor = Color(0x4f, 0x37, 0x8b, 255)
                                )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }

                    Text(text = "Most desired destinations:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    FlowRow(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        user.desiredDestination.forEach { destination ->
                            SuggestionChip(
                                onClick = {},
                                label = {Text(destination)},
                                colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                                    labelColor = Color(0x4f, 0x37, 0x8b, 255)
                                )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                }
            }

            1 -> {
                Column {
                    Text(
                        text = "Trips:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(Color(0xdf, 0xd1, 0xe0, 255), shape = RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        if (publishedTrips.isEmpty() && joinedTrips.isEmpty()) {
                            Text(
                                text = "Didn't take part to any trip yet",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier
                                    .height((3 * 43).dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                publishedTrips.forEach { item ->
                                    ShowUserTrip(item, vm, navController)
                                }
                                joinedTrips.forEach { item ->
                                    ShowUserTrip(item, vm, navController)
                                }
                            }
                        }
                    }

                    Text(text = "Articles:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(140.dp)
                            .wrapContentWidth()
                            .background(Color(0xdf, 0xd1, 0xe0, 255), shape = RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .height((3*43).dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            val articles by vm2.articlesByUserId(user.id).collectAsState(initial = emptyList())

                            articles.forEach { item ->
                                ShowUserArticle(item)
                            }
                        }
                    }
                }
            }

            2 -> {
                Column {

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(Color(0xdf, 0xd1, 0xe0, 255), shape = RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .height((7*43).dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            val reviewsList = vm.getUserReviews(user.id)
                            if (reviewsList.isEmpty()){
                                Text(text = "No reviews yet",
                                    textAlign = TextAlign.Center)
                            } else {
                                reviewsList.forEach { review ->
                                    ShowUserReview(review, navController, uvm)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@SuppressLint("DiscouragedApi")
@Composable
fun ShowUserTrip(trip: Trip, vm: TripViewModel, navController: NavController) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(trip.startDateAsLong())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xf9, 0xf6, 0xf9, 255))
            .clickable{
                vm.userAction = TripViewModel.UserAction.VIEW_OTHER_TRIP
                vm.setOtherTrip(trip)
                navController.navigate("trip_details")
            }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Gray) // fallback background
        ) {
            if (trip.photo.isNotBlank()) {
                val context = LocalContext.current

                val imageModel = if (!trip.photo.isUriString()) {
                    // Local drawable resource
                    context.resources.getIdentifier(trip.photo, "drawable", context.packageName)
                } else {
                    // URI string
                    trip.photo
                }

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Trip or Article Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = trip.destination, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End
        )
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun ShowUserArticle(article: Article) {

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(article.date ?: 0L)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xf9, 0xf6, 0xf9, 255))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Gray) // fallback background
        ) {
            if (article.photo?.isNotBlank() == true) {
                val context = LocalContext.current

                val imageModel = if (article.photo?.isUriString() == true) {
                    // Local drawable resource
                    context.resources.getIdentifier(article.photo, "drawable", context.packageName)
                } else {
                    // URI string
                    article.photo
                }

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Trip or Article Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = article.title?:"No title", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun ShowUserReview(review: Review, navController: NavController, uvm: UserViewModel) {

    val reviewer by uvm.getUserData(review.reviewerId).collectAsState(initial = User())

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    navController.navigate("user_profile/${review.reviewerId}")
                }
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color.Gray, shape = CircleShape)
                ) {
                    ProfilePhoto(reviewer!!, true, uvm = uvm, modifier = Modifier)
                }
                Text(
                    text = "${reviewer!!.firstname} ${reviewer!!.surname}",
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PrintStars(review.score)
            }
        }

        Row {
            Text(
                text = review.title,
                modifier = Modifier.padding(start = 50.dp, end = 16.dp),
                fontWeight = FontWeight.Bold
            )
        }

        Row {
            Text(
                text = review.comment,
                modifier = Modifier.padding(start = 50.dp, end = 16.dp)
            )
        }

        Spacer(Modifier.padding(16.dp))
    }
}






