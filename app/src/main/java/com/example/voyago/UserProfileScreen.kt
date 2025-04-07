package com.example.voyago

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen() {

    //Bottom Bar Images
    val painterExplore = painterResource(R.drawable.explore)
    val painterTrips = painterResource(R.drawable.trips)
    val painterHome = painterResource(R.drawable.home)
    val painterChat = painterResource(R.drawable.chat)
    val painterProfile = painterResource(R.drawable.profile)

    //NavBarItem
    val navItemList = listOf(
        NavItem(
            "Explore", Image(
                painter = painterExplore,
                contentDescription = "explore"
            ),
            painter = painterExplore
        ),
        NavItem(
            "My Trips", Image(
                painter = painterTrips,
                contentDescription = "trips"
            ),
            painter = painterTrips
        ),
        NavItem("Home", Image(
            painter = painterHome,
            contentDescription = "home"
        ),
            painter = painterHome
        ),
        NavItem("Chats", Image(
            painter = painterChat,
            contentDescription = "chats"
        ),
            painter = painterChat
        ),
        NavItem("Profile", Image(
            painter = painterProfile,
            contentDescription = "profile"
        ),
            painter = painterProfile
        ),
    )

    //Icons
    val painterStartChat = painterResource(R.drawable.start_chat)

    //User info
    val user = UserProfileInfo(
        "Isabella",
        "Martinez",
        "IsaWanders",
        listOf("culture", "adventure"),
        listOf("Kyoto"),
        "Hey there! I’m Isa, a culture junkie with a deep love for history, art, and everything that tells a story. I usually travel solo with my camera in one hand and a notebook in the other. I’m all about finding hidden gems, trying local food, and connecting with people from all walks of life. If you ever need museum tips or tea ceremony recs—I’m your girl!",
        5.0f, 96
    )

    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xf3, 0xed, 0xf7, 255),
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = {
                            Icon(
                                navItem.painter,
                                contentDescription = "Icon",
                                modifier = Modifier.size(30.dp)) },
                        label = {
                            Text(text = navItem.label)
                        }
                    )
                }
            }
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

                ProfilePhoto(user.firstname, user.surname,
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
            TabAboutTripsReview()
        }
    }
}


