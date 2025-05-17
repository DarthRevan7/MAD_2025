package com.example.voyago.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
<<<<<<< Updated upstream
import com.example.voyago.NavItem
import com.example.voyago.R
import com.example.voyago.view.TravelProposalScreen
import com.example.voyago.view.UserProfileScreen
=======
import com.example.voyago.model.domain.NavItem
import com.example.voyago.R
import com.example.voyago.view.ActivitiesList
import com.example.voyago.view.CreateNewTrip
import com.example.voyago.view.EditActivity
import com.example.voyago.view.EditTrip
import com.example.voyago.view.ExplorePage
import com.example.voyago.view.FiltersSelection
import com.example.voyago.view.HomePageScreen
import com.example.voyago.view.MyTripsPage
import com.example.voyago.view.NewActivity
import com.example.voyago.view.TripApplications
import com.example.voyago.view.TripDetails
import com.example.voyago.viewmodel.ArticleViewModel
import com.example.voyago.viewmodel.TripViewModelFactory
import com.example.voyago.viewmodel.TripViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.voyago.model.data.ArticleRepository
import com.example.voyago.model.data.TripRepository
import com.example.voyago.model.data.UserRepository
import com.example.voyago.viewmodel.ArticleViewModelFactory




sealed class Screen(val route: String) {
    object Explore : Screen("explore_root")
    object MyTrips : Screen("my_trips_root")
    object Home : Screen("home_root")
    object Chats : Screen("chats_root")
    object Profile : Screen("profile_root")
}
>>>>>>> Stashed changes



class MainActivity : ComponentActivity() {
    private val tripRepo    = TripRepository(Firebase.firestore)
    private val userRepo    = UserRepository(Firebase.firestore)
    private val tripFactory = TripViewModelFactory(tripRepo, userRepo)

    // —— 手动创建 Article 的依赖 ——
    private val articleRepo    = ArticleRepository(Firebase.firestore)
    private val articleFactory = ArticleViewModelFactory(articleRepo)
    private val articleVm: ArticleViewModel by viewModels { articleFactory }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
<<<<<<< Updated upstream
        setContent {

            val navController = rememberNavController()
            NavHost(navController= navController, startDestination= "main_page", builder= {
                composable("main_page"){
                    val context = LocalContext.current
                    MainPage(navController, context)
=======
        setContent { MainScreen(
                articleVm   = articleVm,
            tripFactory = tripFactory
        ) }
    }

}


@Composable
fun MainScreen(
    articleVm: ArticleViewModel,  // ← 新增这一行
    tripFactory: TripViewModelFactory    // ← new parameter
) {
    val navController = rememberNavController()
    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        NavigationGraph(navController = navController, modifier = Modifier.padding(innerPadding), articleVm = articleVm, tripFactory   = tripFactory)
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
>>>>>>> Stashed changes
                }
                composable("user_profile") {
                    UserProfileScreen()
                }
                composable("travel_proposal") {
                    TravelProposalScreen()
                }
            })
        }
    }
}

@Composable
<<<<<<< Updated upstream
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
=======
fun NavigationGraph(navController: NavHostController, modifier: Modifier = Modifier, articleVm: ArticleViewModel, tripFactory: TripViewModelFactory) {

    NavHost(navController = navController, startDestination = Screen.Home.route, modifier = modifier) {
        exploreNavGraph(navController,tripFactory)
        myTripsNavGraph(navController,tripFactory)
        homeNavGraph(navController, articleVm,tripFactory)
        chatsNavGraph()
        profileNavGraph()
    }
}

fun NavGraphBuilder.exploreNavGraph(navController: NavController, tripFactory: TripViewModelFactory) {
    navigation(startDestination = "explore_main", route = Screen.Explore.route) {
        composable("explore_main") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Explore.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = tripFactory
            )
            ExplorePage(navController = navController, vm =tripViewModel)
        }

        composable("trip_details") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Explore.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = tripFactory
            )
            TripDetails(navController = navController, vm = tripViewModel, owner = false)
        }

        composable("filters_selection") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Explore.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory =tripFactory
            )
            FiltersSelection(navController = navController, vm = tripViewModel)
        }
    }
}


fun NavGraphBuilder.myTripsNavGraph(navController: NavController, tripFactory: TripViewModelFactory) {
    navigation(startDestination = "my_trips_main", route = Screen.MyTrips.route) {
        composable("my_trips_main") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = tripFactory
            )
            MyTripsPage(navController = navController, vm = tripViewModel)
        }

        composable("trip_details") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = tripFactory
            )
            TripDetails(navController = navController, vm = tripViewModel, owner = true)
        }

        composable("trip_applications") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = tripFactory
            )
            TripApplications(vm = tripViewModel)
        }

        composable("edit_trip") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = tripFactory
            )
            EditTrip(navController = navController, vm = tripViewModel)
        }

        composable("create_new_trip") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = tripFactory
            )
            CreateNewTrip(navController = navController, vm = tripViewModel)
        }

        composable("activities_list") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = tripFactory
            )
            ActivitiesList(navController = navController, vm = tripViewModel)
        }

        composable("new_activity") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = tripFactory
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
                factory = tripFactory
            )
            val activityId = entry.arguments?.getInt("activityId") ?: -1
            EditActivity(navController = navController, vm = tripViewModel, activityId)
        }
    }
}

fun NavGraphBuilder.homeNavGraph(
    navController: NavHostController,
    vm2: ArticleViewModel
    ,tripFactory: TripViewModelFactory
) {
    navigation(
        startDestination = "home_main",
        route = Screen.Home.route
    ) {
        // 1) 首页
        composable("home_main") { entry ->
            // 取同一个 HomeGraph 的 VM
            val homeEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val homeVm: TripViewModel = viewModel(
                viewModelStoreOwner = homeEntry,
                factory = tripFactory
            )
            HomePageScreen(
                navController = navController,
                vm1 = homeVm,
                vm2 = vm2,
                onTripClick = { trip ->
                    homeVm.setSelectedTrip(trip)
                    navController.navigate("trip_details")
                }
            )
        }
        // 2) 详情页，同样用 HomeGraph 的 VMScope
        composable("trip_details") { entry ->
            val homeEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val homeVm: TripViewModel = viewModel(
                viewModelStoreOwner = homeEntry,
                factory = tripFactory
            )
            TripDetails(
                navController = navController,
                vm = homeVm,
                owner = false
            )
>>>>>>> Stashed changes
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