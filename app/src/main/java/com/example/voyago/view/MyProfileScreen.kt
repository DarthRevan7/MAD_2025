package com.example.voyago.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.voyago.activities.ProfilePhoto
import com.example.voyago.activities.RatingAndReliability
import com.example.voyago.activities.TabAboutTripsReview
import com.example.voyago.activities.TopBar
import com.example.voyago.R
import com.example.voyago.activities.BottomBar
import com.example.voyago.viewmodel.MyProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(viewModel: MyProfileViewModel) {

    //MVVM Code
    val userData = viewModel.userData.observeAsState()

    //Icons
    val painterLogout = painterResource(R.drawable.logout)
    val painterEdit = painterResource(R.drawable.edit)


    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(4)
        }
    ) { innerPadding ->
        viewModel.getUserData()
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            //Box with Profile Photo, Username and Logout and Edit icons
            Box(modifier =
                Modifier
                    .fillMaxWidth()
                    .height(245.dp)
                    .background(Color(0xdf, 0xd1, 0xe0, 255), shape = RectangleShape)) {

                Image(painter = painterLogout, "logout", modifier = Modifier
                    .size(60.dp)
                    .align(alignment = Alignment.TopEnd)
                    .padding(16.dp)
                    .clickable {/*TODO*/ }
                )

                Image(painter = painterEdit, "edit", modifier = Modifier
                    .size(60.dp)
                    .align(alignment = Alignment.BottomEnd)
                    .padding(16.dp)
                    .offset(y = (-30).dp)
                    .clickable {/*TODO*/ }
                )

                ProfilePhoto(
                    userData.value?.firstname.toString(), userData.value?.surname.toString(),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-20).dp)
                )

                Text(
                    text = userData.value?.username.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .offset(y = (-20).dp)
                )
            }

            //Row with rating and reliability
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .offset(y = (-25).dp)
            ) {
                RatingAndReliability(
                    userData.value?.rating?.toFloat() ?: 0.0f,
                    userData.value?.reliability?.toInt() ?: 0
                )
            }

            //Tab About, My Trips, Review
            TabAboutTripsReview(viewModel)
        }
    }
}