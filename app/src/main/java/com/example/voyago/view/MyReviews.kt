package com.example.voyago.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voyago.activities.ProfilePhoto
import com.example.voyago.model.Review
import com.example.voyago.viewmodel.ReviewViewModel
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel
import java.util.Calendar

@Composable
fun MyReviews(navController: NavController, vm: TripViewModel, uvm: UserViewModel, rvm: ReviewViewModel) {
    val trip by vm.selectedTrip
    println("selected trip = ${vm.selectedTrip}")

    //Delete before submission
    if (trip == null) {
        Text("Loading trip details...")
        return
    }

    val nonNullTrip = trip!!

    val listState = rememberLazyListState()

    val hasReviews by remember {
        derivedStateOf { vm.isReviewed(uvm.loggedUser.id, nonNullTrip.id) }
    }

    val titleMap = remember { mutableStateMapOf<String, String>() }
    val reviewMap = remember { mutableStateMapOf<String, String>() }
    val ratingMap = remember { mutableStateMapOf<String, Float>() }

    val titleTouchedMap = remember { mutableStateMapOf<String, Boolean>() }
    val reviewTouchedMap = remember { mutableStateMapOf<String, Boolean>() }
    val ratingTouchedMap = remember { mutableStateMapOf<String, Boolean>() }


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

    //Start composable environment
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            //Trip image
            item {
                Hero(nonNullTrip)
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
                        text = formatTripDate(nonNullTrip.startDate) + " - " +
                                formatTripDate(nonNullTrip.endDate) + "\n " +
                                "${nonNullTrip.groupSize} people" +
                                if (nonNullTrip.availableSpots() > 0) {
                                    " (${nonNullTrip.availableSpots()} spots left)"
                                } else {
                                    ""
                                },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${trip?.estimatedPrice} €",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }

            item {
                Spacer(Modifier.padding(5.dp))
            }

            //Inside Lazy Column

            //Trip Review
            item {
                TitleBox("Trip Review")
            }

            item {
                Spacer(Modifier.padding(5.dp))
            }

            if(hasReviews) {
                //Review of the trip made by the logged in user
                val review = vm.tripReview(uvm.loggedUser.id, nonNullTrip.id)
                item {
                    ShowReview(review, vm, true)
                }
            } else {
                item {
                    val key = "trip"
                    val rating = ratingMap.getOrElse(key) { 0f }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        //Star Rating
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

                        Log.d("Rating", "Trip error=${isRatingInvalid(key)}")
                        if (isRatingInvalid(key)) {
                            Text(
                                "Rating must be at least 0.5",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

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
                    }
                }
            }

            //Users reviews
            item {
                TitleBox("Users Review")
            }

            item {
                Spacer(Modifier.padding(5.dp))
            }

            if(hasReviews) {
                //Review of the users made by the logged in user
                val reviews = vm.getUsersReviewsTrip(uvm.loggedUser.id, nonNullTrip.id)
                items(reviews) { review ->
                    ShowReview(review, vm, true)
                }
            } else {
                //Field to complete
                val participantsMap = vm.getTripParticipants(nonNullTrip)

                items(participantsMap.entries.toList()) { entry ->
                    val user = entry.key
                    if (user.id != uvm.loggedUser.id) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            //Profile photo of the reviewer
                            Box(
                                contentAlignment = Alignment.CenterStart,
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Color.Gray, shape = CircleShape)
                            ) {
                                ProfilePhoto(user.firstname, user.surname, true, null)
                            }
                            Text(
                                "${user.firstname} ${user.surname}",
                                modifier = Modifier.padding(start = 16.dp),
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

                            Log.d("Rating", "User error=${isRatingInvalid(key.toString())}")
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
                //Publish button
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                val allKeys = mutableListOf("trip")
                                val participants = vm.getTripParticipants(nonNullTrip)
                                for (user in participants.keys) {
                                    if (user.id != uvm.loggedUser.id) {
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

                                        val review = Review(
                                            reviewId = -1,
                                            tripId = nonNullTrip.id,
                                            isTripReview = isTripReview,
                                            reviewerId = uvm.loggedUser.id,
                                            reviewedUserId = reviewedUserId,
                                            title = title,
                                            comment = comment,
                                            score = score,
                                            photos = emptyList(),
                                            date = currentDate
                                        )

                                        reviewsToSubmit.add(review)
                                    }

                                    rvm.addAllTripReviews(reviewsToSubmit)

                                    println("$hasReviews")

                                    //navController.popBackStack()
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
    val starCount = 5
    Row(modifier = modifier) {
        for (i in 1..starCount) {
            val icon = when {
                i <= rating -> Icons.Default.Star
                i - 0.5f <= rating -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Default.StarBorder
            }

            Icon(
                imageVector = icon,
                contentDescription = "Star $i",
                modifier = Modifier
                    .size(32.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val tappedHalf = offset.x < size.width / 2
                            val newRating = if (tappedHalf) i - 0.5f else i.toFloat()
                            onRatingChanged(newRating)
                        }
                    },
                tint = Color(0xFFFFD700)
            )
        }
    }
}
