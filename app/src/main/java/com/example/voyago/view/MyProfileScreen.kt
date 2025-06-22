@file:Suppress("DEPRECATION")

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.voyago.R
import com.example.voyago.activities.ProfilePhoto
import com.example.voyago.model.Article
import com.example.voyago.model.Review
import com.example.voyago.model.Trip
import com.example.voyago.model.User
import com.example.voyago.toCalendar
import com.example.voyago.toStringDate
import com.example.voyago.viewmodel.ArticleViewModel
import com.example.voyago.viewmodel.ReviewViewModel
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    vm: TripViewModel,
    navController: NavController,
    vm2: ArticleViewModel,
    uvm: UserViewModel,
    rvm: ReviewViewModel,
    defaultTabIndex: Int = 0
) {

    // Get current context for resource access or other context-dependent operations
    val context = LocalContext.current

    // Collect the logged-in user state from UserViewModel as a Compose State object
    val user by uvm.loggedUser.collectAsState()

    // Collect the user's rating as a State; initial default is 0.0f
    val rating = rvm.calculateRatingById(user.id).collectAsState(0.0f)

    // Collect the list of trips published by the logged-in user from TripViewModel
    val publishedTrips by vm.publishedTrips.collectAsState()

    // Collect the list of trips joined by the logged-in user from TripViewModel
    val joinedTrips by vm.joinedTrips.collectAsState()

    // When the user ID changes and is valid (not zero),
    // trigger loading of trips and reviews related to this user
    LaunchedEffect(user.id) {
        if (user.id != 0) {
            vm.creatorPublicFilter(user.id)     // Load public trips created by user
            vm.creatorPrivateFilter(user.id)    // Load private trips created by user
            vm.tripUserJoined(user.id)          // Load trips the user joined
            rvm.getUserReviews(user.id)         // Load user reviews
        }
    }

    // Load icons for logout and edit actions from drawable resources
    val painterLogout = painterResource(R.drawable.logout)
    val painterEdit = painterResource(R.drawable.edit)

    // Remember the scroll state of the lazy column (list)
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,      // Set the remembered scroll state
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            // Top section box containing profile picture, username,
            // and icons for logout and edit actions
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color(0xdf, 0xd1, 0xe0, 255), shape = RectangleShape)
            ) {
                // Logout icon positioned at the top-right corner
                Image(
                    painter = painterLogout, "logout", modifier = Modifier
                        .size(60.dp)
                        .align(alignment = Alignment.TopEnd)
                        .padding(16.dp)
                        .clickable {
                            // Configure Google Sign-In client for sign-out
                            val gso =
                                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(context.getString(R.string.default_web_client_id))
                                    .requestEmail()
                                    .build()
                            val googleSignInClient = GoogleSignIn.getClient(context, gso)

                            // Firebase Auth sign-out
                            val auth = FirebaseAuth.getInstance()
                            auth.signOut()

                            // Google client sign-out
                            googleSignInClient.signOut()

                            // Navigate to home screen clearing back stack (no back navigation to profile)
                            navController.navigate("home_main") {
                                popUpTo(0) { inclusive = true } // clear back stack
                            }
                        }
                )

                // Edit icon positioned at the bottom-right, slightly offset upwards
                Image(
                    painter = painterEdit, "edit", modifier = Modifier
                        .size(60.dp)
                        .align(alignment = Alignment.BottomEnd)
                        .padding(16.dp)
                        .offset(y = (-30).dp)
                        .clickable {
                            // Set the current profile image URI in UserViewModel for editing
                            uvm.setProfileImageUri(user.profilePictureUrl?.toUri())
                            // Navigate to the profile editing screen
                            navController.navigate("edit_profile")
                        }
                )

                // Display the profile photo at the center, offset upwards slightly
                ProfilePhoto(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-50).dp),
                    user,
                    false
                )

                // Display username below the profile photo, centered
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 10.dp)
                        .offset(y = (40).dp)
                )

                // Display full name (first + surname) near the bottom center of the box
                Text(
                    text = user.firstname + " " + user.surname,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .offset(y = (-50).dp)
                )

                // Spacer for vertical space
                Spacer(Modifier.height(20.dp))

                // Display user’s country below the full name, centered near bottom
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
            // Row showing user’s rating and reliability stats, offset upwards slightly
            Row(
                modifier = Modifier
                    .offset(y = (-25).dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                RatingAndReliability(
                    rating.value,
                    user.reliability
                )
            }
        }

        item {
            // Tabs component that switches between About, My Trips, and Reviews sections
            TabAboutTripsReview(
                user,
                joinedTrips,
                publishedTrips,
                vm,
                vm2,
                navController,
                uvm,
                rvm,
                defaultTabIndex
            )
        }
    }
}

@Composable
fun RatingAndReliability(rating: Float, reliability: Int) {

    // Load the star icon resource to visually represent the rating
    val painterStar = painterResource(R.drawable.star)

    // Load the mobile icon resource to visually represent reliability
    val painterMobile = painterResource(R.drawable.mobile)

    // Main container row to hold the rating and reliability boxes side by side
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth() // Ensure the row uses full width to allow centering
    ) {
        // Box representing the user rating
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(149.dp, 44.dp)
                .background(Color(0xc1, 0xa5, 0xc3, 255), shape = RoundedCornerShape(10.dp))
                .border(2.dp, color = Color.White, shape = RoundedCornerShape(10.dp))
        ) {
            // Row inside the rating box to arrange star icon and rating text horizontally
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // Star icon showing rating symbol
                Image(
                    painter = painterStar, contentDescription = "star",
                    modifier = Modifier.size(40.dp)
                )

                // Text displaying the rating value followed by "approval"
                Text(
                    text = "$rating approval",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Spacer to add horizontal space between the two boxes (rating and reliability)
        Spacer(modifier = Modifier.width(16.dp))

        // Box representing the user's reliability percentage
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(149.dp, 44.dp)
                .background(Color(0xc1, 0xa5, 0xc3, 255), shape = RoundedCornerShape(10.dp))
                .border(2.dp, color = Color.White, shape = RoundedCornerShape(10.dp))
        ) {
            // Row inside the reliability box to arrange mobile icon and text horizontally
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // Mobile icon representing reliability visually
                Image(
                    painter = painterMobile, contentDescription = "mobile",
                    modifier = Modifier.size(30.dp)
                )

                // Text displaying the reliability percentage followed by "reliable"
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
fun TabAboutTripsReview(
    user: User,
    joinedTrips: List<Trip>,
    publishedTrips: List<Trip>,
    vm: TripViewModel,
    vm2: ArticleViewModel,
    navController: NavController,
    uvm: UserViewModel,
    rvm: ReviewViewModel,
    defaultTabIndex: Int
) {

    // Define the titles for the tabs displayed in this screen
    val tabs = listOf("About", "Trips & Articles", "Reviews")

    // Remember the currently selected tab index with state that survives process death (saveable)
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(defaultTabIndex) }

    // On initial composition, trigger filters to update trips related to this user
    LaunchedEffect(Unit) {
        vm.creatorPublicFilter(user.id)     // Fetch trips created by the user that are public
        vm.tripUserJoined(user.id)          // Fetch trips that the user has joined
    }

    // Collect the list of reviews for the current user from the ReviewViewModel
    val reviews by rvm.userReviews.collectAsState()

    // Whenever the user ID changes and is valid, fetch the latest reviews
    LaunchedEffect(user.id) {
        if (user.id != 0) {
            rvm.getUserReviews(user.id)
        }
    }

    // TabRow displays the tabs horizontally and handles tab selection
    TabRow(
        selectedTabIndex = selectedTabIndex,    // Highlights the selected tab
        modifier = Modifier.background(Color(0xfe, 0xf7, 0xff, 255)),
        contentColor = Color.Black
    ) {
        // Iterate over tabs to create each tab
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,   // Mark tab as selected if it matches selected index
                onClick = {
                    selectedTabIndex = index
                }, // When user clicks, update selected tab index
                text = {
                    Text(
                        title, color = if (index == selectedTabIndex) {
                            // Color for selected tab
                            Color(0x65, 0x55, 0x8f, 255)
                        } else {
                            // Color for unselected tab
                            Color.Black
                        }
                    )
                }
            )
        }
    }

    // Container box fills available space below tabs, provides white background and padding
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {

        // Show content based on selected tab index using when expression
        when (selectedTabIndex) {
            // ABOUT TAB
            0 -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Display user's profile description text
                    Text(user.userDescription)

                    // Section title for travel preferences
                    Text(
                        text = "Preferences about the type of travel:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    // Use a FlowRow to display travel type preferences as chips
                    FlowRow(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        user.typeTravel.forEach { type ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(type.toString().lowercase()) },
                                colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                                    labelColor = Color(0x4f, 0x37, 0x8b, 255)
                                )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }

                    // Section title for desired destinations
                    Text(
                        text = "Most desired destinations:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    // Use a FlowRow to display destination chips
                    FlowRow(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        user.desiredDestination.forEach { destination ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(destination) },
                                colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                                    labelColor = Color(0x4f, 0x37, 0x8b, 255)
                                )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                }
            }
            // TRIPS & ARTICLES TAB
            1 -> {
                Column {
                    // Section title for trips
                    Text(
                        text = "Trips:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
                    )

                    // Box container for trips list or empty message
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(
                                Color(0xdf, 0xd1, 0xe0, 255),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp)
                    ) {
                        if (publishedTrips.isEmpty() && joinedTrips.isEmpty()) {
                            // Show message if user has no trips (either published or joined)
                            Text(
                                text = "Didn't take part to any trip yet",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            // Show a scrollable list of trips (both published and joined)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier
                                    .height((3 * 43).dp)    // Height to show approx 3 items at once
                                    .verticalScroll(rememberScrollState())  // Enable vertical scrolling
                            ) {
                                // Display each published trip using ShowUserTrip composable
                                publishedTrips.forEach { item ->
                                    ShowUserTrip(item, vm, navController)
                                }
                                // Display each joined trip as well
                                joinedTrips.forEach { item ->
                                    ShowUserTrip(item, vm, navController)
                                }
                            }
                        }
                    }

                    // Section title for articles
                    Text(
                        text = "Articles:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
                    )

                    // Box container for articles list or empty message
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(
                                Color(0xdf, 0xd1, 0xe0, 255),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp)
                    ) {

                        // Collect articles written by the user as a state to observe changes
                        val articles by vm2.articlesByUserId(user.id)
                            .collectAsState(initial = emptyList())

                        if (articles.isEmpty()) {
                            // Show message if user hasn't written any articles
                            Text(
                                text = "Didn't write any article yet",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            // Show a scrollable list of user articles
                            Column(
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier
                                    .height((3 * 43).dp)    // Show approx 3 items at once
                                    .verticalScroll(rememberScrollState())  // Enable vertical scroll
                            ) {
                                // Display each article with ShowUserArticle composable
                                articles.forEach { item ->
                                    ShowUserArticle(item, navController)
                                }
                            }
                        }
                    }
                }
            }
            // REVIEWS TAB
            2 -> {
                Column {
                    // Container box for reviews list or empty message
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(
                                Color(0xdf, 0xd1, 0xe0, 255),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp)
                    ) {
                        // Column for reviews list, scrollable with spacing between reviews
                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .height((7 * 43).dp)    // Approx height for 7 reviews shown at once
                                .verticalScroll(rememberScrollState())  // Enable vertical scrolling
                        ) {
                            if (reviews.isEmpty()) {
                                // Show message if no reviews received yet
                                Text(
                                    text = "Didn't receive any review yet",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                // For each review, display with ShowUserReview composable
                                reviews.forEach { review ->
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


@OptIn(ExperimentalGlideComposeApi::class)
@SuppressLint("DiscouragedApi")
@Composable
fun ShowUserTrip(trip: Trip, vm: TripViewModel, navController: NavController) {

    // Format the trip's start date into a readable string using extension/helper functions
    val formattedDate = toCalendar(trip.startDate).toStringDate()

    // State to hold the URL of the trip's photo; initialized as null (no photo loaded yet)
    var imageUrl by remember { mutableStateOf<String?>(null) }

    // Create a coroutine scope tied to this composable lifecycle for asynchronous work
    val coroutineScope = rememberCoroutineScope()

    // Launch a side-effect that runs when trip.id changes
    // This is used to fetch the photo URL asynchronously for the given trip
    LaunchedEffect(trip.id) {
        coroutineScope.launch {
            // Call the trip's method to get the photo URL
            imageUrl = trip.getPhoto()
        }
    }

    // The main UI row container for displaying a single trip item
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xf9, 0xf6, 0xf9, 255))
            .clickable {
                // When clicked, set the user action in ViewModel to view another trip's details
                vm.userAction = TripViewModel.UserAction.VIEW_OTHER_TRIP
                // Store the selected trip in the ViewModel for detail screen use
                vm.setOtherTrip(trip)
                // Navigate to the "trip_details" screen via NavController
                navController.navigate("trip_details")
            }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Box to hold the circular photo or fallback background
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Gray) // fallback background
        ) {
            // If imageUrl has been loaded successfully, show the image
            if (imageUrl != null) {
                GlideImage(
                    model = imageUrl,
                    contentDescription = "Trip or Article Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        }

        // Spacer to add horizontal space between photo and trip details
        Spacer(modifier = Modifier.width(12.dp))

        // Column for trip destination text
        Column(modifier = Modifier.weight(1f)) {
            // Trip destination displayed as single line with ellipsis if too long
            Text(text = trip.destination, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        // Display the formatted trip start date aligned at the end
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End
        )
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@SuppressLint("DiscouragedApi")
@Composable
fun ShowUserArticle(article: Article, navController: NavController) {

    // Trasform the article date into a calendar
    val formattedDate = toCalendar(Timestamp(Date(article.date!!)))

    // State variable to hold the URL of the first image associated with the article
    var imageUrl by remember { mutableStateOf<String?>(null) }

    // Coroutine scope tied to this composable's lifecycle for launching async tasks
    val coroutineScope = rememberCoroutineScope()

    // Side effect that triggers whenever article.photo changes,
    // used to asynchronously fetch the first photo URL
    LaunchedEffect(article.photo) {
        coroutineScope.launch {
            try {
                // Attempt to fetch the first photo URL asynchronously (may throw exceptions)
                imageUrl = article.getPhoto()
            } catch (e: Exception) {
                Log.e("ShowUserArticle", "Failed to get photo", e)
            }
        }
    }

    // Main horizontal row container displaying the article summary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xf9, 0xf6, 0xf9, 255))
            .clickable {
                // On click, navigate to article detail screen with the article's ID
                navController.navigate("article_detail/${article.id}")
            }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Box to contain the article image or fallback icon inside a circular shape
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Gray) // fallback background
        ) {
            when {
                imageUrl != null -> {
                    // If we have a photo URL from Firebase Storage,
                    // load and display the image using GlideImage
                    GlideImage(
                        model = imageUrl,
                        contentDescription = "Article Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                article.photo.isNotEmpty() -> {
                    // If no URL but there are local drawable resources named in article.photo,
                    // try to get the resource ID from the first photo string and display it
                    val context = LocalContext.current
                    val firstPhoto = article.photo.first()

                    // Remember the resource id lookup so it’s not repeated unnecessarily
                    val resId = remember(firstPhoto) {
                        context.resources.getIdentifier(
                            firstPhoto,
                            "drawable",
                            context.packageName
                        )
                    }

                    if (resId != 0) {
                        // If resource ID is valid, display the drawable resource as image
                        GlideImage(
                            model = resId,
                            contentDescription = "Article Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        // If resource ID is invalid, show a placeholder icon indicating no image
                        Icon(
                            Icons.Filled.AddPhotoAlternate,
                            contentDescription = "No Image",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                else -> {
                    // If no image URL and no local photos, show the placeholder icon by default
                    Icon(
                        Icons.Filled.AddPhotoAlternate,
                        contentDescription = "No Image",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Horizontal spacer between image and text column
        Spacer(modifier = Modifier.width(12.dp))

        // // Column for article title and photo count text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = article.title ?: "No title",     // Show article title or fallback text
                maxLines = 1,
                overflow = TextOverflow.Ellipsis        // Ellipsis if title is too long
            )

            // Optionally show how many photos are attached if any exist
            if (article.photo.isNotEmpty()) {
                Text(
                    text = "${article.photo.size} photo(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }

        // Display the formatted date aligned to the right
        Text(
            text = formattedDate.toStringDate(),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun ShowUserReview(review: Review, navController: NavController, uvm: UserViewModel) {

    // Observe user data for the reviewer using their ID.
    // Starts with an empty User() while loading.
    val reviewer by uvm.getUserData(review.reviewerId).collectAsState(initial = User())

    // Outer container for the entire review block
    Column {
        // Top row with reviewer's profile and star rating
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Sub-row for profile photo and name, clickable to go to profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    // Navigate to the reviewer's profile when tapped
                    navController.navigate("user_profile/${review.reviewerId}")
                }
            ) {
                // Circle avatar box for the reviewer's profile picture
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color.Gray, shape = CircleShape)
                ) {
                    // Load and show the reviewer's profile photo
                    ProfilePhoto(modifier = Modifier, reviewer!!, true)
                }

                // Display reviewer's full name next to the profile photo
                Text(
                    text = "${reviewer!!.firstname} ${reviewer!!.surname}",
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            // Star rating shown on the right, aligned with space between name and stars
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PrintStars(review.score)    // Displays stars based on review score
            }
        }

        // Row to display the review title, padded to align with comment below
        Row {
            Text(
                text = review.title,
                modifier = Modifier.padding(start = 50.dp, end = 16.dp),
                fontWeight = FontWeight.Bold
            )
        }

        // Row to show the main body of the review (the comment)
        Row {
            Text(
                text = review.comment,
                modifier = Modifier.padding(start = 50.dp, end = 16.dp)
            )
        }

        // Spacer to separate this review visually from others
        Spacer(Modifier.padding(16.dp))
    }
}





