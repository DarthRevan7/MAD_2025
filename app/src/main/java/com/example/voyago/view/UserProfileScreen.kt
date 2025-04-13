package com.example.voyago.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.example.voyago.R
import com.example.voyago.activities.*
import com.example.voyago.user1
import com.example.voyago.user2
import com.example.voyago.viewmodel.*


import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(viewModel: ProfileViewModel) {

    //Icons
    val painterStartChat = painterResource(R.drawable.start_chat)

    val calendar = Calendar.getInstance()
    calendar.set(1992,Calendar.MARCH,12)

    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(false)
        }
    ) { innerPadding ->
        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                //Box with Profile Photo, Username and Logout and Edit icons
                Box(modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color(0xdf,0xd1,0xe0,255), shape = RectangleShape)
                ) {

                    Image(painter = painterStartChat, "start chat", modifier = Modifier
                        .size(60.dp)
                        .align(alignment = Alignment.TopEnd)
                        .padding(16.dp)
                        .clickable{/*TODO*/}
                    )

                    ProfilePhoto(
                        user2.name, user2.surname, false, null,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = (-50).dp)
                    )

                    Text(
                        text = user2.username,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 10.dp)
                            .offset(y = (40).dp)
                    )


                    Text(
                        text = user2.name + " " + user2.surname,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp)
                            .offset(y = (-50).dp)
                    )
                    Spacer( Modifier.height(20.dp) )
                    Text(
                        text = user2.country,
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
                    modifier = Modifier./*align(Alignment.CenterHorizontally).*/offset(y = (-25).dp)
                ) {
                    RatingAndReliability(
                        user2.approvalRate,
                        user2.reliability
                    )
                }
            }

            item {
                //Tab About, My Trips, Review
                TabAboutTripsReview(user2)
            }
        }
    }
}


