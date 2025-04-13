package com.example.voyago.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.example.voyago.LazyUser
import com.example.voyago.NavItem
import com.example.voyago.R
import com.example.voyago.view.TravelProposalScreen
import com.example.voyago.view.UserProfileScreen
import com.example.voyago.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel = ViewModelProvider(this)[ProfileViewModel::class]

            val navController = rememberNavController()
            NavHost(navController= navController, startDestination= "main_page", builder= {
                composable("main_page"){
                    val context = LocalContext.current
                    MainPage(navController, context)
                }
                composable("user_profile") {
                    UserProfileScreen(viewModel)
                }
                composable("travel_proposal") {
                    TravelProposalScreen()
                }
            })
        }
    }
}

@Composable
fun MainPage(navController: NavController, context: Context) {
    Column (
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            context.startActivity(Intent(context, MyProfileActivity::class.java))
        }) {
            Text("Go To Own Profile")
        }
        Button(onClick = {
            navController.navigate("user_profile")
        }) {
            Text("Go To Other Profile")
        }
        Button(onClick = {
            navController.navigate("travel_proposal")
        }) {
            Text("View Travel Proposal")
        }
    }
}

@Composable
fun ProfilePhoto(firstname: String, surname: String, isSmall: Boolean, profileImage: Uri?, modifier: Modifier = Modifier) {
    val initials = "${firstname.first()}"+"${surname.first()}"

    if(profileImage == null)
    {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .size(130.dp)
                .background(Color.Blue, shape = CircleShape)
        ) {
            if(isSmall) {
                Text(
                    text = initials,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            else {
                Text(
                    text = initials,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }

        }
    }
    else
    {
        //use the icon set in the user data
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .size(width = 130.dp, height = 130.dp)
                .background( color = Color.Blue , shape = CircleShape)
                //.clip( CircleShape )

        ) {
            //Icon(profileImage)
            AsyncImage(profileImage,"profilePic",
                modifier=Modifier
                    //.size(130.dp)
                    .fillMaxSize()
                    .clip( shape = CircleShape)
            )
                    //.matchParentSize())

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {

    //Top Bar Images
    val painterLogo = painterResource(R.drawable.logo)
    val painterNews = painterResource(R.drawable.news)
    val painterNotification = painterResource(R.drawable.notifications)

    TopAppBar(
        colors = topAppBarColors(
            containerColor = Color(0xe6, 0xe0, 0xe9, 255),
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Image(
                painter = painterLogo,
                contentDescription = "logo",
                modifier = Modifier.size(120.dp)
            )

        },
        actions = {
            IconButton(onClick = {/*TO DO*/}) {
                Image(
                    painter = painterNews,
                    contentDescription = "news",
                    modifier = Modifier.padding(end = 10.dp)
                )
            }
            IconButton(onClick = {/*TO DO*/}) {
                Image(
                    painter = painterNotification,
                    contentDescription = "notification",
                    modifier = Modifier.padding(end = 10.dp)
                )

            }
        },
        modifier = Modifier.shadow(8.dp)
    )
}

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBar(selectedIndex: Any?) {

    //NavBarItem
    val navItemList = listOf(
        NavItem("Explore", Icons.Default.LocationOn),
        NavItem("My Trips", Icons.Default.Commute),
        NavItem("Home", Icons.Default.Language),
        NavItem("Chats", Icons.Default.ChatBubble),
        NavItem("Profile", Icons.Default.AccountCircle)
    )

    NavigationBar(
        containerColor = Color(0xf3, 0xed, 0xf7, 255),
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        navItemList.forEachIndexed { index, navItem ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = {},
                icon = {
                    Icon(
                        imageVector = navItem.icon,
                        contentDescription = "Icon",
                        modifier = Modifier.size(30.dp)) },
                label = {
                    Text(text = navItem.label)
                }
            )
        }
    }
}