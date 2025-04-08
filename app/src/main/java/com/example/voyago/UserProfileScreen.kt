package com.example.voyago

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.example.voyago.activities.*
import com.example.voyago.activities.ProfilePhoto
import com.example.voyago.activities.RatingAndReliability
import com.example.voyago.activities.TabAboutTripsReview
import com.example.voyago.activities.TopBar
import com.example.voyago.viewmodel.MyProfileViewModel


import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(viewModel: MyProfileViewModel) {

    //Icons
    val painterStartChat = painterResource(R.drawable.start_chat)

    val calendar = Calendar.getInstance()
    calendar.set(1992,Calendar.MARCH,12)
    //User info
    val user = UserProfileInfo(
        0,
        "Isabella",
        "Martinez",
        "IsaWanders",
        calendar,
        "Spain",
        "isabellamartinez@gmail.com",
        "isa7rt5",
        null,
        listOf(TypeTravel.CULTURE, TypeTravel.ADVENTURE),
        listOf("Kyoto"),
        5.0f, 96, null, null, null, null, null,
    )

    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(false)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            //Box with Profile Photo, Username and Logout and Edit icons
            Box(modifier =
                Modifier
                    .fillMaxWidth()
                    .height(245.dp)
                    .background(Color(0xdf,0xd1,0xe0,255), shape = RectangleShape)
            ) {

                Image(painter = painterStartChat, "start chat", modifier = Modifier
                    .size(60.dp)
                    .align(alignment = Alignment.TopEnd)
                    .padding(16.dp)
                    .clickable{/*TODO*/}
                )

                ProfilePhoto(
                    user.firstname, user.surname,
                    modifier = Modifier.align(Alignment.Center).offset(y = (-20).dp)
                )

               Text(
                   text = user.username,
                   style = MaterialTheme.typography.headlineLarge,
                   fontWeight = FontWeight.Bold,
                   modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)
                       .offset(y = (-20).dp)
               )
            }

            //Row with rating and reliability
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally).offset(y = (-25).dp)
            ) {
                RatingAndReliability(user.rating, user.reliability)
            }

            //Tab About, My Trips, Review
            TabAboutTripsReview(viewModel)
        }
    }
}


