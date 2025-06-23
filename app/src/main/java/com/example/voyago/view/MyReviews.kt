package com.example.voyago.view

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.voyago.activities.ProfilePhoto
import com.example.voyago.model.Review
import com.example.voyago.model.User
import com.example.voyago.viewmodel.NotificationViewModel
import com.example.voyago.viewmodel.ReviewViewModel
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun MyReviews(
    navController: NavController, vm: TripViewModel, uvm: UserViewModel,
    rvm: ReviewViewModel, nvm: NotificationViewModel
) {

    // Observe the currently selected trip
    val trip by vm.selectedTrip

    // Collect the trip review from state flow
    val tripReview by rvm.tripReview.collectAsState()

    // Live reference to user-specific reviews
    val usersReviews = remember { rvm.usersReviews }

    // Whether the user has already submitted reviews
    val hasReviews by rvm.isReviewed.collectAsState()

    // Run when trip ID changes: load participants and reviews
    LaunchedEffect(trip.id) {
        vm.getTripParticipants(trip)
        rvm.getTripReview(tripId = trip.id, userId = uvm.loggedUser.value.id)
        rvm.getUsersReviews(uvm.loggedUser.value.id, trip.id)
        rvm.isReviewed(uvm.loggedUser.value.id, trip.id)
    }

    // State for trip participants mapped by user
    val participantsMap by vm.tripParticipants.collectAsState()

    // Scroll state for the lazy column
    val listState = rememberLazyListState()

    // Form state for title, comment, rating and tracking touched fields
    val titleMap = remember { mutableStateMapOf<String, String>() }
    val reviewMap = remember { mutableStateMapOf<String, String>() }
    val ratingMap = remember { mutableStateMapOf<String, Float>() }

    val titleTouchedMap = remember { mutableStateMapOf<String, Boolean>() }
    val reviewTouchedMap = remember { mutableStateMapOf<String, Boolean>() }
    val ratingTouchedMap = remember { mutableStateMapOf<String, Boolean>() }

    // State to track selected photo URIs
    val selectedUris by rvm.selectedUris.collectAsState()

    // Image picker launcher for adding photos to trip review
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            rvm.updateSelectedUris(uris)
        }
    }

    // Coroutine scope for launching suspend operations from composables
    val composableScope = rememberCoroutineScope()


    // Validation functions
    fun isTitleInvalid(key: String): Boolean {
        val title = titleMap[key].orEmpty()
        return (titleTouchedMap[key] == true) &&
                (title.isBlank() || !title.any { it.isLetter() })
    }

    fun isReviewInvalid(key: String): Boolean {
        val review = reviewMap[key].orEmpty()
        return (reviewTouchedMap[key] == true) &&
                (review.isBlank() || !review.any { it.isLetter() })
    }

    fun isRatingInvalid(key: String): Boolean {
        val rating = ratingMap[key] ?: 0f
        return (ratingTouchedMap[key] == true) && rating < 0.5f
    }

    // Observing others' reviews for the current trip
    val othersReviews = usersReviews.collectAsState()

    // MAIN UI START
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header image of the trip
            item {
                Hero(trip, vm, User())
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Basic trip details: date, group size, and price
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

            item {
                Spacer(Modifier.padding(5.dp))
            }

            // Trip review section
            item {
                TitleBox("Trip Review")
            }

            item {
                Spacer(Modifier.padding(5.dp))
            }

            if (hasReviews) {
                if (tripReview.isValidReview()) {
                    // Show existing trip review
                    item {
                        ShowReview(tripReview, uvm, navController)
                    }
                } else {
                    item {
                        Text("No review yet")
                    }
                }
            } else {
                item {
                    // Show form to submit a trip review
                    val key = "trip"
                    val rating = ratingMap.getOrElse(key) { 0f }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Rating bar
                        Text("Rate your experience:", fontSize = 20.sp)

                        Spacer(modifier = Modifier.height(16.dp))

                        RatingBar(
                            rating = rating,
                            onRatingChanged = {
                                ratingMap[key] = it
                                ratingTouchedMap[key] = true
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Your rating: $rating")

                        if (isRatingInvalid(key)) {
                            Text(
                                "Rating must be at least 0.5",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Title and review inputs
                        val title = titleMap.getOrElse(key) { "" }
                        ValidatingInputTextField(
                            title,
                            {
                                titleMap[key] = it
                                titleTouchedMap[key] = true
                            },
                            isTitleInvalid(key),
                            "Title"
                        )

                        val review = reviewMap.getOrElse(key) { "" }
                        ValidatingInputTextField(
                            review,
                            {
                                reviewMap[key] = it
                                reviewTouchedMap[key] = true
                            },
                            isReviewInvalid(key),
                            "Review"
                        )

                        // Button to pick images
                        Button(
                            onClick = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                        ) {
                            Text("Select Photos")
                        }

                        // Display selected images
                        if (selectedUris.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(selectedUris) { uri ->
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }

                    }
                }
            }

            // User Reviews Section
            item {
                TitleBox("Users Review")
            }

            item {
                Spacer(Modifier.padding(5.dp))
            }


            if (hasReviews) {
                // If reviews have been made, show existing ones
                if (othersReviews.value.isNotEmpty()) {
                    items(othersReviews.value) { review ->
                        ShowReview(review, uvm, navController)
                    }
                } else {
                    item {
                        Text("No reviews yet")
                    }
                }
            } else {
                // Form for reviewing other participants
                items(participantsMap.entries.toList()) { entry ->
                    val user = entry.key
                    val loggedUser by uvm.loggedUser.collectAsState()

                    if (user.id != loggedUser.id) {
                        // Display user info and link to profile
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.CenterStart,
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Color.Gray, shape = CircleShape)
                            ) {
                                ProfilePhoto(Modifier, user, true)
                            }
                            Text(
                                "${user.firstname} ${user.surname}",
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .clickable {
                                        navController.navigate("user_profile/${user.id}")
                                    },
                                fontSize = 20.sp

                            )
                        }

                        val key = user.id
                        val rating = ratingMap.getOrElse(key.toString()) { 0f }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            //Star Rating
                            Text("Rate your Travel Buddy:", fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(16.dp))

                            RatingBar(
                                rating = rating,
                                onRatingChanged = {
                                    ratingMap[key.toString()] = it
                                    ratingTouchedMap[key.toString()] = true
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Your rating: $rating")

                            if (isRatingInvalid(key.toString())) {
                                Text(
                                    "Rating must be at least 0.5",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            val title = titleMap.getOrElse(key.toString()) { "" }
                            ValidatingInputTextField(
                                title,
                                {
                                    titleMap[key.toString()] = it
                                    titleTouchedMap[key.toString()] = true
                                },
                                isTitleInvalid(key.toString()),
                                "Title"
                            )

                            val review = reviewMap.getOrElse(key.toString()) { "" }
                            ValidatingInputTextField(
                                review,
                                {
                                    reviewMap[key.toString()] = it
                                    reviewTouchedMap[key.toString()] = true
                                },
                                isReviewInvalid(key.toString()),
                                "Review"
                            )
                        }
                    }
                }

                // Publish button: submits all reviews
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                val allKeys = mutableListOf("trip")
                                for (user in participantsMap.keys) {
                                    if (user.id != uvm.loggedUser.value.id) {
                                        allKeys.add(user.id.toString())
                                    }
                                }

                                var allValid = true

                                for (key in allKeys) {
                                    titleTouchedMap[key] = true
                                    reviewTouchedMap[key] = true
                                    ratingTouchedMap[key] = true

                                    val titleInvalid = isTitleInvalid(key)
                                    val reviewInvalid = isReviewInvalid(key)
                                    val ratingInvalid = isRatingInvalid(key)

                                    if (titleInvalid || reviewInvalid || ratingInvalid) {
                                        allValid = false
                                    }
                                }


                                if (allValid) {
                                    val reviewsToSubmit = mutableListOf<Review>()
                                    val currentDate = Calendar.getInstance()

                                    for (key in allKeys) {
                                        val isTripReview = key == "trip"
                                        val reviewedUserId = if (isTripReview) -1 else key.toInt()
                                        val title = titleMap[key].orEmpty()
                                        val comment = reviewMap[key].orEmpty()
                                        val rawScore = ratingMap[key] ?: 0f
                                        val score = (rawScore * 2).toInt()
                                        val photos =
                                            if (isTripReview) selectedUris.map { it.toString() } else emptyList()

                                        val review = Review(
                                            reviewId = -1,
                                            tripId = trip.id,
                                            isTripReview = isTripReview,
                                            reviewerId = uvm.loggedUser.value.id,
                                            reviewedUserId = reviewedUserId,
                                            title = title,
                                            comment = comment,
                                            score = score,
                                            photos = photos,
                                            date = Timestamp(currentDate.time)
                                        )
                                        reviewsToSubmit.add(review)
                                    }

                                    // Submit reviews asynchronously
                                    composableScope.launch {
                                        rvm.addAllTripReviews(reviewsToSubmit) { success, reviews ->
                                            if (success) {
                                                for (review in reviews) {
                                                    if (review.reviewerId != -1) {
                                                        rvm.calculateRatingById(review.reviewedUserId)
                                                    }
                                                }
                                                // Notify users reviewed
                                                val title =
                                                    "New review from ${uvm.loggedUser.value.username}!"
                                                val body = "Check you profile for more information!"
                                                val notificationType = "REVIEW"
                                                val idLink = uvm.loggedUser.value.id

                                                participantsMap.entries.toList()
                                                    .forEach { entry ->
                                                        val user = entry.key
                                                        if (user.id != uvm.loggedUser.value.id) {
                                                            val userId = user.id.toString()
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
                                        }
                                    }

                                }

                                // Update user reliability score
                                uvm.updateUserReliability(
                                    uvm.loggedUser.value.id,
                                    +2
                                ) { success ->
                                    if (success) {
                                        Log.d("TripDetails", "Reliability updated successfully")
                                    } else {
                                        Log.e("TripDetails", "Failed to update reliability")
                                    }
                                }
                            },
                            modifier = Modifier
                                .width(160.dp)
                                .height(60.dp)
                                .padding(bottom = 16.dp)
                        ) {
                            Text("Publish")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RatingBar(
    modifier: Modifier = Modifier,
    rating: Float,
    onRatingChanged: (Float) -> Unit
) {
    val starCount = 5       // Total number of stars in the rating bar

    // Layout a horizontal row of stars
    Row(modifier = modifier) {
        // Determine which icon to use based on the rating:
        // - Full star if index is less than or equal to the rating
        // - Half star if rating is between (i - 0.5) and i
        // - Empty (bordered) star otherwise
        for (i in 1..starCount) {
            val icon = when {
                i <= rating -> Icons.Default.Star
                i - 0.5f <= rating -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Default.StarBorder
            }

            // Display the star icon
            Icon(
                imageVector = icon,
                contentDescription = "Star $i",
                modifier = Modifier
                    .size(32.dp)    // Set a fixed size for each star
                    .pointerInput(Unit) {
                        // Detect taps on each star
                        detectTapGestures { offset ->
                            // If the tap occurred on the left half of the star, assign a half-star value
                            val tappedHalf = offset.x < size.width / 2
                            val newRating = if (tappedHalf) i - 0.5f else i.toFloat()

                            // Notify parent about the updated rating
                            onRatingChanged(newRating)
                        }
                    },
                tint = Color(0xFFFFD700)    // Set star color
            )
        }
    }
}
