package com.example.voyago.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voyago.activities.*
import com.example.voyago.viewmodel.*
import androidx.compose.runtime.getValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    vm: TripViewModel,
    navController: NavController,
    vm2: ArticleViewModel,
    userId: Int,
    uvm: UserViewModel
) {
    // Collect user data as state
    val user by uvm.getUserData(userId).collectAsState(initial = null)

    if (user == null) {
        Log.d("UserProfileScreen", "User data is null")
        return
    }

    val listState = rememberLazyListState()

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
                ProfilePhoto(
                    user!!,
                    false,
                    Modifier
                        .align(Alignment.Center)
                        .offset(y = (-50).dp),
                    uvm
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
                RatingAndReliability(user!!.rating, user!!.reliability)
            }
        }

        item {
            TabAboutTripsReview(user!!, vm, vm2, navController, uvm)
        }
    }
}






