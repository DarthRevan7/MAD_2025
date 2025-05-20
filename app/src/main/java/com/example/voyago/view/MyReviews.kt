package com.example.voyago.view

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voyago.activities.ProfilePhoto
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel
import kotlin.math.roundToInt

@Composable
fun MyReviews(navController: NavController, vm: TripViewModel, uvm: UserViewModel) {
    val trip by vm.selectedTrip
    println("selected trip = ${vm.selectedTrip}")

    //Delete before submission
    if (trip == null) {
        Text("Loading trip details...")
        return
    }

    val nonNullTrip = trip!!

    val listState = rememberLazyListState()

    val hasReviews = vm.isReviewed(uvm.loggedUser.id, nonNullTrip.id)

    var title by rememberSaveable { mutableStateOf("") }
    val titleTouched = remember {mutableStateOf(false)}
    var titleError by rememberSaveable { mutableStateOf(false) }

    var review by rememberSaveable { mutableStateOf("") }
    val reviewTouched = remember {mutableStateOf(false)}
    var reviewError by rememberSaveable { mutableStateOf(false) }

    val numPart = nonNullTrip.participants.size

    val errList: List<Boolean> = List(numPart, init = { item -> false })

    var errorList = rememberSaveable { mutableStateOf(errList) }
    
    var boolCheck: Boolean = true
    boolCheck = !titleError && !reviewError

    errorList.value.forEach { item ->
        boolCheck = boolCheck && !item
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
                    var rating by remember { mutableFloatStateOf(0f) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        //Star Rating
                        Text("Rate your experience:", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        RatingBar(rating = rating, onRatingChanged = { rating = it })

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Your rating: $rating")

                        //Title field
                        titleError = titleTouched.value && (title.toString().isBlank() ||
                                !title.toString().any { it.isLetter() })
                        ValidatingInputTextField(
                            title,
                            {title = it
                                if(!titleTouched.value) {
                                    titleTouched.value = true
                                }
                            },
                            titleError,
                            "Title"
                        )

                        //Review Field
                        reviewError = reviewTouched.value && (review.toString().isBlank() ||
                                !review.toString().any { it.isLetter() })
                        ValidatingInputTextField(
                            review,
                            {review = it
                                if(!reviewTouched.value) {
                                    reviewTouched.value = true
                                }
                            },
                            reviewError,
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

                        var rating by remember { mutableFloatStateOf(0f) }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            //Star Rating
                            Text("Rate your Travel Buddy:", fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(16.dp))

                            RatingBar(rating = rating, onRatingChanged = { rating = it })

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Your rating: $rating")

                            //Title field
                            titleError = titleTouched.value && (title.toString().isBlank() ||
                                    !title.toString().any { it.isLetter() })
                            ValidatingInputTextField(
                                title,
                                {
                                    title = it
                                    if (!titleTouched.value) {
                                        titleTouched.value = true
                                    }
                                },
                                titleError,
                                "Title"
                            )

                            //Review Field
                            reviewError = reviewTouched.value && (review.toString().isBlank() ||
                                    !review.toString().any { it.isLetter() })
                            ValidatingInputTextField(
                                review,
                                {
                                    review = it
                                    if (!reviewTouched.value) {
                                        reviewTouched.value = true
                                    }
                                },
                                reviewError,
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
                            onClick = {},
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
