package com.example.voyago.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voyago.R
import com.example.voyago.activities.ProfilePhoto
import com.example.voyago.viewmodel.ArticleViewModel
import com.example.voyago.viewmodel.ChatViewModel
import com.example.voyago.viewmodel.ReviewViewModel
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    vm: TripViewModel,
    navController: NavController,
    vm2: ArticleViewModel,
    userId: Int,
    uvm: UserViewModel,
    rvm: ReviewViewModel,
    chatViewModel: ChatViewModel
) {
    // Collect user data as state
    val user by uvm.getUserData(userId).collectAsState(initial = null)

    //Get the list of trips published by the user
    val publishedTrips by vm.publishedTrips.collectAsState()

    //Get the list of trips the user has joined (not created)
    val joinedTrips by vm.joinedTrips.collectAsState()

    // Get the currently logged in user
    val loggedUser by uvm.loggedUser.collectAsState()


    // Handle null case
    if (user == null) {
        return
    }

    // When user ID changes (or screen recomposes), trigger trip data filters for that user
    LaunchedEffect(user?.id) {
        if (user?.id != 0) {

            vm.creatorPublicFilter(user!!.id)       // Get public trips created by this user
            vm.creatorPrivateFilter(user!!.id)      // Get private trips created by this user
            vm.tripUserJoined(user!!.id)            // Get trips this user has joined
        }
    }

    // Keeps track of scroll position for the LazyColumn
    val listState = rememberLazyListState()

    // Painter for the "start chat" icon
    val painterChat = painterResource(R.drawable.start_chat)

    // Real-time user rating (from reviews)
    val rating = rvm.calculateRatingById(user!!.id).collectAsState(0.0f)

    // Main vertical scrollable layout
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {

        // Profile Header Section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color(0xdf, 0xd1, 0xe0, 255), shape = RectangleShape)
            ) {
                // Chat icon in the top right corner to start a private chat
                Image(
                    painter = painterChat, "userChat", modifier = Modifier
                        .size(60.dp)
                        .align(alignment = Alignment.TopEnd)
                        .padding(16.dp)
                        .clickable {
                            // Create or fetch chat room between logged user and viewed user
                            chatViewModel.createOrGetPrivateChatRoom(
                                currentUserId = loggedUser.id,
                                otherUserId = user!!.id
                            ) { roomId ->
                                // Navigate to chat screen using the generated room ID
                                navController.navigate("chat/$roomId") // Assuming you have a route like this
                            }
                        }
                )

                // User profile photo positioned in the center
                ProfilePhoto(
                    Modifier
                        .align(Alignment.Center)
                        .offset(y = (-50).dp),
                    user!!,
                    false
                )

                // Username below photo
                Text(
                    text = user!!.username,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 10.dp)
                        .offset(y = 40.dp)
                )

                // Full name (first and last)
                Text(
                    text = "${user!!.firstname} ${user!!.surname}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .offset(y = (-50).dp)
                )

                Spacer(Modifier.height(20.dp))

                // User’s country
                Text(
                    text = user!!.country,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .offset(y = (-20).dp)
                )
            }
        }

        // Ratings & Reliability section (just below header)
        item {
            Row(
                modifier = Modifier.offset(y = (-25).dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                RatingAndReliability(rating.value, user!!.reliability)
            }
        }

        // Tabs section (About, Trips, Reviews)
        item {
            TabAboutTripsReview(
                user!!,
                joinedTrips,
                publishedTrips,
                vm,
                vm2,
                navController,
                uvm,
                rvm,
                defaultTabIndex = 0
            )
        }
    }
}






