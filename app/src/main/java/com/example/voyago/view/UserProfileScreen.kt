package com.example.voyago.view

import android.util.Log
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
    rvm: ReviewViewModel
) {
    // Collect user data as state
    val user by uvm.getUserData(userId).collectAsState(initial = null)
    //List of trip created and published by the logged in user (id=1)
    val publishedTrips by vm.publishedTrips.collectAsState()
    //List of trip the logged in user (id=1) joined
    val joinedTrips by vm.joinedTrips.collectAsState()


    if (user == null) {
        Log.d("UserProfileScreen", "User data is null")
        return
    }

    LaunchedEffect(user?.id) {
        if (user?.id != 0) {
            Log.d("Trips", "my trips id: ${user?.id}")
            vm.creatorPublicFilter(user!!.id)
            vm.creatorPrivateFilter(user!!.id)
            vm.tripUserJoined(user!!.id)
        }
    }

    val listState = rememberLazyListState()

    val painterChat = painterResource(R.drawable.start_chat)
    //Rating updated real time
    val rating = rvm.calculateRatingById(user!!.id).collectAsState(0.0f)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color(0xdf, 0xd1, 0xe0, 255), shape = RectangleShape)
            ) {
                Image(
                    painter = painterChat, "userChat", modifier = Modifier
                        .size(60.dp)
                        .align(alignment = Alignment.TopEnd)
                        .padding(16.dp)
                        .clickable {

                        }
                )

                ProfilePhoto(
                    Modifier
                        .align(Alignment.Center)
                        .offset(y = (-50).dp),
                    user!!,
                    false
                )
                Text(
                    text = user!!.username,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 10.dp)
                        .offset(y = 40.dp)
                )
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

        item {
            Row(
                modifier = Modifier.offset(y = (-25).dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                RatingAndReliability(rating.value, user!!.reliability)
            }
        }

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






