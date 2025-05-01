package com.example.voyago.activities

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import coil3.compose.AsyncImage
import com.example.voyago.model.NavItem
import com.example.voyago.R
import com.example.voyago.view.*
import com.example.voyago.viewmodel.Factory
import com.example.voyago.viewmodel.TripListViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "main_page"
            ) {
                composable("main_page") {
                    MainPage(navController)
                }

                //Travel Proposal Flow
                navigation(
                    startDestination = "travel_proposal_list",
                    route = "all_trips_graph"
                ) {
                    composable("travel_proposal_list") { navBackStackEntry ->
                        val parentEntry = remember(navBackStackEntry) {
                            navController.getBackStackEntry("all_trips_graph")
                        }
                        val vm: TripListViewModel = viewModel(parentEntry, factory = Factory)
                        TravelProposalList(navController, vm)
                    }

                    composable("travel_proposal_details") { navBackStackEntry ->
                        val parentEntry = remember(navBackStackEntry) {
                            navController.getBackStackEntry("all_trips_graph")
                        }
                        val vm: TripListViewModel = viewModel(parentEntry, factory = Factory)
                        TravelProposalDetail(navController, vm, false)
                    }

                    composable("filter_selection") { navBackStackEntry ->
                        val parentEntry = remember(navBackStackEntry) {
                            navController.getBackStackEntry("all_trips_graph")
                        }
                        val vm: TripListViewModel = viewModel(parentEntry, factory = Factory)
                        FilterSelection(navController, vm)
                    }
                }


                //Owned Trip Flow
                navigation(
                    startDestination = "owned_travel_proposal_list",
                    route = "owned_trips_graph"
                ) {
                    composable("owned_travel_proposal_list") { navBackStackEntry ->
                        val parentEntry = remember(navBackStackEntry) {
                            navController.getBackStackEntry("owned_trips_graph")
                        }
                        val vm: TripListViewModel = viewModel(parentEntry, factory = Factory)
                        OwnedTravelProposalList(navController, vm)
                    }

                    composable("travel_proposal_details") { navBackStackEntry ->
                        val parentEntry = remember(navBackStackEntry) {
                            navController.getBackStackEntry("owned_trips_graph")
                        }
                        val vm: TripListViewModel = viewModel(parentEntry, factory = Factory)
                        TravelProposalDetail(navController, vm, true)
                    }

                    composable("trip_applications") { navBackStackEntry ->
                        val parentEntry = remember(navBackStackEntry) {
                            navController.getBackStackEntry("owned_trips_graph")
                        }
                        val vm: TripListViewModel = viewModel(parentEntry, factory = Factory)
                        TripApplications(vm)
                    }

                    composable("edit_travel_proposal") { navBackStackEntry ->
                        val parentEntry = remember(navBackStackEntry) {
                            navController.getBackStackEntry("owned_trips_graph")
                        }
                        val vm: TripListViewModel = viewModel(parentEntry, factory = Factory)
                        EditTravelProposal(navController, vm)
                    }

                    composable("create_new_travel_proposal") { navBackStackEntry ->
                        val parentEntry = remember(navBackStackEntry) {
                            navController.getBackStackEntry("owned_trips_graph")
                        }
                        val vm: TripListViewModel = viewModel(parentEntry, factory = Factory)
                        NewTravelProposal(navController, vm)
                    }

                    composable("activities_list") { navBackStackEntry ->
                        val parentEntry = remember(navBackStackEntry) {
                            navController.getBackStackEntry("owned_trips_graph")
                        }
                        val vm: TripListViewModel = viewModel(parentEntry, factory = Factory)
                        ActivitiesList(navController, vm)
                    }

                    composable("new_activity") { navBackStackEntry ->
                        val parentEntry = remember(navBackStackEntry) {
                            navController.getBackStackEntry("owned_trips_graph")
                        }
                        val vm: TripListViewModel = viewModel(parentEntry, factory = Factory)
                        NewActivity(navController, vm)
                    }

                    composable(
                        "edit_activity/{activityId}",
                        arguments = listOf(navArgument("activityId") { type = NavType.IntType })
                    ) { navBackStackEntry ->
                        val parentEntry = remember(navBackStackEntry) {
                            navController.getBackStackEntry("owned_trips_graph")
                        }
                        val vm: TripListViewModel = viewModel(parentEntry, factory = Factory)

                        val activityId = navBackStackEntry.arguments?.getInt("activityId") ?: -1
                        EditActivity(navController, vm, activityId)
                    }
                }




                //To delete
                composable("try_load_images") {
                    LoadImages()
                }
            }
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
            navController.navigate("travel_proposal_list")
        }) {
            Text("Travel Proposal List ")
        }
        Button(onClick = {
            navController.navigate("owned_travel_proposal_list")
        }) {
            Text("Owned Travel Proposal List ")
        }
        Button(onClick = {
            navController.navigate("create_new_travel_proposal")
        }) {
            Text("Create New Travel Proposal")
        }

        //to delete later
        Button(onClick = {
            navController.navigate("try_load_images")
        }) {
            Text("Try load images")
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

        ) {
            //Icon(profileImage)
            AsyncImage(profileImage,"profilePic",
                modifier = Modifier
                    .fillMaxSize()
                    .clip( shape = CircleShape)
                    .border(0.dp, Color.White, CircleShape),
                contentScale = ContentScale.Crop
            )

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