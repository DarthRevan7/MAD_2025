package com.example.voyago

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val navController = rememberNavController()
            NavHost(navController= navController, startDestination= "main_page", builder= {
                composable("main_page"){
                    MainPage(navController)
                }
                composable("user_profile") {
                    UserProfileScreen()
                }
                composable("my_profile") {
                    MyProfileScreen()
                }
                composable("travel_proposal") {
                    TravelProposalScreen()
                }
            })
        }
    }
}

@Composable
fun MainPage(navController: NavController) {
    Column (
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            navController.navigate("my_profile")
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
fun ProfilePhoto(firstname: String, surname: String, modifier: Modifier = Modifier) {
    val initials = "${firstname.firstOrNull()}"+"${surname.firstOrNull()}"

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(130.dp)
            .background(Color.Blue, shape = CircleShape)
    ) {
        Text(
            text = initials,
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
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

@Composable
fun RatingAndReliability(rating: Float, reliability: Int) {

    val painterStar = painterResource(R.drawable.star)
    val painterMobile = painterResource(R.drawable.mobile)

    //Box with rating
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(149.dp, 44.dp)
            .background(Color(0xc1, 0xa5, 0xc3, 255), shape = RoundedCornerShape(10.dp))
            .border(2.dp, color = Color.White, shape = RoundedCornerShape(10.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(painter = painterStar, "star", modifier = Modifier
                .size(40.dp)
            )
            Text(
                text = "$rating approval",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
    Spacer(modifier = Modifier.width(16.dp))

    //Box with reliability
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(149.dp, 44.dp)
            .background(Color(0xc1, 0xa5, 0xc3, 255), shape = RoundedCornerShape(10.dp))
            .border(2.dp, color = Color.White, shape = RoundedCornerShape(10.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(painter = painterMobile, "mobile", modifier = Modifier
                .size(30.dp)
            )
            Text(
                text = "${reliability}% reliable",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TabAboutTripsReview(user: UserProfileInfo) {
    val tabs = listOf("About", "Trips", "Reviews")

    var selectedTabIndex by remember {
        mutableIntStateOf(0)
    }

    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier.background(Color(0xfe, 0xf7, 0xff, 255)),
        contentColor = Color.Black
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = {selectedTabIndex = index},
                text = {
                    Text(title, color = if (index == selectedTabIndex) {
                        Color(0x65, 0x55, 0x8f, 255)
                    } else {
                        Color.Black
                    })
                }
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)
    ) {
        when(selectedTabIndex) {
            0 -> {
                Column {
                    Text(user.userDescription)
                    Text(text = "Preferences about the type of travel:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    FlowRow(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        user.typeTravel.forEach { type ->
                            SuggestionChip(
                                onClick = {},
                                label = {Text(type)},
                                colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                                    labelColor = Color(0x4f, 0x37, 0x8b, 255)
                                )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }

                    Text(text = "Most desired destinations:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    FlowRow(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        user.desiredDestination.forEach { destination ->
                            SuggestionChip(
                                onClick = {},
                                label = {Text(destination)},
                                colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                                    labelColor = Color(0x4f, 0x37, 0x8b, 255)
                                )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                }
            }
            1 -> {
                Column {
                    Text(text = "Trips:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.width(391.dp).wrapContentHeight()
                            .background(Color(0xdf, 0xd1, 0xe0, 255), shape = RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.size(376.dp, 43.dp)
                                    .background(
                                        Color(0xf9, 0xf6, 0xf9, 255),
                                        shape = RectangleShape
                                    )
                                    .padding(5.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color.Gray, shape = CircleShape)
                                ) {
                                    //Image
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = "Marrakech")
                                Spacer(modifier = Modifier.width(140.dp))
                                Text(text = "March, 2024", modifier = Modifier.weight(1f))
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.size(376.dp, 43.dp)
                                    .background(
                                        Color(0xf9, 0xf6, 0xf9, 255),
                                        shape = RectangleShape
                                    )
                                    .padding(5.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color.Gray, shape = CircleShape)
                                ) {
                                    //Image
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = "Patagonia")
                                Spacer(modifier = Modifier.width(145.dp))
                                Text(text = "May, 2023", modifier = Modifier.weight(1f))
                            }
                        }
                    }


                    Text(text = "Articles:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.width(391.dp).wrapContentHeight()
                            .background(Color(0xdf, 0xd1, 0xe0, 255), shape = RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.size(376.dp, 43.dp)
                                    .background(
                                        Color(0xf9, 0xf6, 0xf9, 255),
                                        shape = RectangleShape
                                    )
                                    .padding(5.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color.Gray, shape = CircleShape)
                                ) {
                                    //Image
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = "Mystic Marrakech")
                                Spacer(modifier = Modifier.width(90.dp))
                                Text(text = "March, 2024", modifier = Modifier.weight(1f))
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.size(376.dp, 43.dp)
                                    .background(
                                        Color(0xf9, 0xf6, 0xf9, 255),
                                        shape = RectangleShape
                                    )
                                    .padding(5.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color.Gray, shape = CircleShape)
                                ) {
                                    //Image
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = "Wild Patagonia")
                                Spacer(modifier = Modifier.width(120.dp))
                                Text(text = "May, 2023", modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            2 -> {

            }
        }
    }
}