package com.example.voyago.activities

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import coil3.compose.AsyncImage
import com.example.voyago.model.NavItem
import com.example.voyago.R
import com.example.voyago.view.ActivitiesList
import com.example.voyago.view.CreateNewTrip
import com.example.voyago.view.EditActivity
import com.example.voyago.view.EditTrip
import com.example.voyago.view.ExplorePage
import com.example.voyago.view.FiltersSelection
import com.example.voyago.view.MyTripsPage
import com.example.voyago.view.NewActivity
import com.example.voyago.view.TripApplications
import com.example.voyago.view.TripDetails
import com.example.voyago.viewmodel.Factory
import com.example.voyago.viewmodel.TripViewModel

sealed class Screen(val route: String) {
    object Explore : Screen("explore_root")
    object MyTrips : Screen("my_trips_root")
    object Home : Screen("home_root")
    object Chats : Screen("chats_root")
    object Profile : Screen("profile_root")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        NavigationGraph(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val items = listOf(
        NavItem("Explore", Icons.Filled.LocationOn, Screen.Explore.route, "explore_main"),
        NavItem("My Trips", Icons.Filled.Commute, Screen.MyTrips.route, "my_trips_main"),
        NavItem("Home", Icons.Filled.Language, Screen.Home.route, Screen.Home.route),
        NavItem("Chats", Icons.Filled.ChatBubble, Screen.Chats.route, "chats_list"),
        NavItem("Profile", Icons.Filled.AccountCircle, Screen.Profile.route, "profile_overview")
    )

    NavigationBar {
        val navBackStackEntry = navController.currentBackStackEntryAsState().value
        val currentDestination = navBackStackEntry?.destination

        items.forEach { item ->
            val selected = currentDestination
                ?.hierarchy
                ?.any { it.route == item.rootRoute } == true

            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    navController.navigate(item.startRoute) {
                        navController.graph.startDestinationRoute?.let { screenRoute ->
                            popUpTo(screenRoute) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}


@Composable
fun NavigationGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Screen.Home.route, modifier = modifier) {
        exploreNavGraph(navController)
        myTripsNavGraph(navController)
        homeNavGraph()
        chatsNavGraph()
        profileNavGraph()
    }
}

fun NavGraphBuilder.exploreNavGraph(navController: NavController) {
    navigation(startDestination = "explore_main", route = Screen.Explore.route) {
        composable("explore_main") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Explore.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            ExplorePage(navController = navController, vm = tripViewModel)
        }

        composable("trip_details") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Explore.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            TripDetails(navController = navController, vm = tripViewModel, owner = false)
        }

        composable("filters_selection") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Explore.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            FiltersSelection(navController = navController, vm = tripViewModel)
        }
    }
}


fun NavGraphBuilder.myTripsNavGraph(navController: NavController) {
    navigation(startDestination = "my_trips_main", route = Screen.MyTrips.route) {
        composable("my_trips_main") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            MyTripsPage(navController = navController, vm = tripViewModel)
        }

        composable("trip_details") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            TripDetails(navController = navController, vm = tripViewModel, owner = true)
        }

        composable("trip_applications") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            TripApplications(vm = tripViewModel)
        }

        composable("edit_trip") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            EditTrip(navController = navController, vm = tripViewModel)
        }

        composable("create_new_trip") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            CreateNewTrip(navController = navController, vm = tripViewModel)
        }

        composable("activities_list") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            ActivitiesList(navController = navController, vm = tripViewModel)
        }

        composable("new_activity") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            NewActivity(navController = navController, vm = tripViewModel)
        }

        composable("edit_activity/{activityId}",
            arguments = listOf(navArgument("activityId") { type = NavType.IntType })) { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            val activityId = entry.arguments?.getInt("activityId") ?: -1
            EditActivity(navController = navController, vm = tripViewModel, activityId)
        }
    }
}

fun NavGraphBuilder.homeNavGraph() {
    composable(Screen.Home.route) {
        Text("Home Screen")
    }
}

fun NavGraphBuilder.chatsNavGraph() {
    navigation(startDestination = "chats_list", route = Screen.Chats.route) {
        composable("chats_list") {
            Text("Chats List Screen")
        }
        composable("chat_detail/{chatId}") { backStackEntry ->
            Text("Chat Detail Screen with ID: ${backStackEntry.arguments?.getString("chatId")}")
        }
    }
}

fun NavGraphBuilder.profileNavGraph() {
    navigation(startDestination = "profile_overview", route = Screen.Profile.route) {
        composable("profile_overview") {
            Text("Profile Overview Screen")
        }
        composable("profile_settings") {
            Text("Profile Settings Screen")
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