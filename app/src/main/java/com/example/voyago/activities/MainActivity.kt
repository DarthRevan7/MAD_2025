package com.example.voyago.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Camera
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
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
import com.example.voyago.databinding.ActivityCameraBinding
import com.example.voyago.view.ActivitiesList
import com.example.voyago.view.CreateNewTrip
import com.example.voyago.view.EditActivity
import com.example.voyago.view.EditProfileScreen
import com.example.voyago.view.EditTrip
import com.example.voyago.view.ExplorePage
import com.example.voyago.view.FiltersSelection
import com.example.voyago.view.HomePageScreen
import com.example.voyago.view.MyProfileScreen
import com.example.voyago.view.MyTripsPage
import com.example.voyago.view.NewActivity
import com.example.voyago.view.TripApplications
import com.example.voyago.view.TripDetails
import com.example.voyago.view.UserProfileScreen
import com.example.voyago.viewmodel.ArticleViewModel
import com.example.voyago.viewmodel.Factory
import com.example.voyago.viewmodel.TripViewModel
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.example.voyago.view.MyReviews
import com.example.voyago.viewmodel.ReviewFactory
import com.example.voyago.viewmodel.ReviewViewModel
import com.example.voyago.viewmodel.UserFactory
import com.example.voyago.viewmodel.UserViewModel
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.voyago.model.User
import com.example.voyago.viewmodel.ArticleFactory
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import com.example.voyago.model.ReviewModel
import com.example.voyago.model.UserModel
import com.example.voyago.view.NotificationView
import com.example.voyago.viewmodel.ArticleFactory
import com.example.voyago.viewmodel.NotificationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging


sealed class Screen(val route: String) {
    object Explore : Screen("explore_root")
    object MyTrips : Screen("my_trips_root")
    object Home : Screen("home_root")
    object Chats : Screen("chats_root")
    object Profile : Screen("profile_root")
    object Notifications : Screen("notifications")
}

class MainActivity : ComponentActivity() {


    private lateinit var viewBinding: ActivityCameraBinding

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()
        context = this

        cameraExecutor = Executors.newSingleThreadExecutor()


        FirebaseMessaging.getInstance().subscribeToTopic("all")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Subscribed to 'all' topic")
                } else {
                    Log.e("FCM", "Failed to subscribe to 'all' topic", task.exception)
                }
            }


        // Check for camera permissions
        if (!allPermissionsGranted()) {
            requestPermissions()
        }

        setContent {
            MainScreen()
        }

    }

    /*private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            *//*val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }*//*

            imageCapture = ImageCapture.Builder().build()

            val imageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                    Log.d(TAG, "Average luminosity: $luma")
                })
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }*/

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "Camera"
        @SuppressLint("ObsoleteSdkInt")
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val permissionGranted = permissions.entries.all {
                it.key in REQUIRED_PERMISSIONS && it.value
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext, "Permission request denied", Toast.LENGTH_SHORT).show()
            } /*else {
                *//*startCamera()*//*
            }*/
        }

    private class LuminosityAnalyzer(private val listener: (Double) -> Unit) : ImageAnalysis.Analyzer {
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()
            val data = ByteArray(remaining())
            get(data)
            return data
        }

        override fun analyze(image: ImageProxy) {
            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()
            listener(luma)
            image.close()
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val reviewModel = ReviewModel()
    val userModel = UserModel()
    val notificationViewModel = NotificationViewModel()
    userModel.refreshAllRatings(reviewModel)
    Scaffold(
        topBar = { TopBar(
            nvm = notificationViewModel,
            navController = navController)
        },
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
        val navBackStackEntry by navController.currentBackStackEntryAsState()
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
                    if (item.label == "Home") {
                        navController.navigate(item.startRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    } else {
                        // Restore last visited tab for other items
                        navController.navigate(item.startRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
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
        homeNavGraph(navController)
        chatsNavGraph()
        profileNavGraph(navController)
        composable("camera") {
            val context = LocalContext.current
            CameraScreen(
                context = context,
                //modifier = Modifier.fillMaxSize(),
                onImageCaptured = { uri ->
                    Toast.makeText(context, "Saved to: $uri", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            )
        }
        val notificationViewModel = NotificationViewModel()
        composable(Screen.Notifications.route) {
            NotificationView(notificationViewModel)
        }
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
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            val reviewViewModel: ReviewViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            TripDetails(navController = navController, vm = tripViewModel, owner = false, uvm = userViewModel, rvm = reviewViewModel)
        }

        composable("user_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })) { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Explore.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            val reviewViewModel: ReviewViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            val vm2: ArticleViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = ArticleFactory
            )

            val userId = entry.arguments?.getInt("userId") ?: 1
            UserProfileScreen(
                navController = navController,
                vm = tripViewModel,
                vm2 = vm2,
                userId = userId,
                uvm = userViewModel,
                rvm = reviewViewModel
            )
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
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            MyTripsPage(navController = navController, vm = tripViewModel, uvm = userViewModel)
        }

        composable("trip_details") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            val reviewViewModel: ReviewViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            TripDetails(navController = navController, vm = tripViewModel, owner = true, uvm = userViewModel, rvm = reviewViewModel)
        }

        composable("my_reviews") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = UserFactory
            )
            val reviewViewModel: ReviewViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = ReviewFactory
            )
            MyReviews(navController = navController, vm = tripViewModel, uvm = userViewModel,
                rvm = reviewViewModel)
        }

        composable("trip_applications") { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            TripApplications(vm = tripViewModel, userViewModel, navController)
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

        composable("user_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })) { entry ->
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            val reviewViewModel: ReviewViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            val vm2: ArticleViewModel = viewModel(factory = ArticleFactory)
            val userId = entry.arguments?.getInt("userId") ?: 1
            UserProfileScreen(
                navController = navController,
                vm = tripViewModel,
                vm2 = vm2,
                userId = userId,
                uvm = userViewModel,
                rvm = reviewViewModel
            )
        }
    }
}


fun NavGraphBuilder.homeNavGraph(
    navController: NavHostController,
    //vm1: TripListViewModel,

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
                factory = Factory
            )
            val vm2: ArticleViewModel = viewModel(factory = ArticleFactory)
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
                factory = Factory
            )
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = homeEntry,
                factory = Factory
            )
            val reviewViewModel: ReviewViewModel = viewModel(
                viewModelStoreOwner = homeEntry,
                factory = Factory
            )
            TripDetails(
                navController = navController,
                vm = homeVm,
                owner = false,
                uvm = userViewModel,
                rvm = reviewViewModel
            )
        }

        composable("user_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })) { entry ->
            val profileNavGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = profileNavGraphEntry,
                factory = Factory
            )
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = profileNavGraphEntry,
                factory = Factory
            )
            val reviewViewModel: ReviewViewModel = viewModel(
                viewModelStoreOwner = profileNavGraphEntry,
                factory = Factory
            )
            val vm2: ArticleViewModel = viewModel(factory = ArticleFactory)
            val userId = entry.arguments?.getInt("userId") ?: 1
            UserProfileScreen(
                navController = navController,
                vm = tripViewModel,
                vm2 = vm2,
                userId = userId,
                uvm = userViewModel,
                rvm = reviewViewModel
            )

        }
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


fun NavGraphBuilder.profileNavGraph(
    navController: NavHostController,
    //vm1: TripListViewModel,

) {

    navigation(
        startDestination = "profile_overview",
        route = Screen.Profile.route
    ) {
        composable("profile_overview") { entry ->
            // 取同一个 HomeGraph 的 VM
            val profileNavGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Profile.route)
            }
            val profileNavGraphEntryVm: TripViewModel = viewModel(
                viewModelStoreOwner = profileNavGraphEntry,
                factory = Factory
            )
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = profileNavGraphEntry,
                factory = Factory
            )
            val reviewViewModel: ReviewViewModel = viewModel(
                viewModelStoreOwner = profileNavGraphEntry,
                factory = Factory
            )
            val vm2: ArticleViewModel = viewModel(factory = ArticleFactory)
            MyProfileScreen(
                navController = navController,
                vm = profileNavGraphEntryVm,
                vm2 = vm2,
                uvm = userViewModel,
                rvm = reviewViewModel
            )
        }

        composable("user_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })) { entry ->
            val profileNavGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Profile.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = profileNavGraphEntry,
                factory = Factory
            )
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = profileNavGraphEntry,
                factory = Factory
            )
            val reviewViewModel: ReviewViewModel = viewModel(
                viewModelStoreOwner = profileNavGraphEntry,
                factory = Factory
            )
            val vm2: ArticleViewModel = viewModel(factory = ArticleFactory)
            val userId = entry.arguments?.getInt("userId") ?: 1
            UserProfileScreen(
                navController = navController,
                vm = tripViewModel,
                vm2 = vm2,
                userId = userId,
                uvm = userViewModel,
                rvm = reviewViewModel
            )


        }

        composable("edit_profile") { entry ->

            val profileNavGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Profile.route)
            }
            val profileNavGraphEntryVm: TripViewModel = viewModel(
                viewModelStoreOwner = profileNavGraphEntry,
                factory = Factory
            )
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = profileNavGraphEntry,
                factory = Factory
            )
            EditProfileScreen(
                navController = navController,
                context = LocalContext.current,
                vm = profileNavGraphEntryVm,
                uvm = userViewModel
            )
        }

        composable("camera") {entry ->

            val profileNavGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Profile.route)
            }
            val profileNavGraphEntryVm: UserViewModel = viewModel(
                viewModelStoreOwner = profileNavGraphEntry,
                factory = Factory
            )
            CameraScreen(
                context = LocalContext.current,
                onImageCaptured = { uri ->
                    profileNavGraphEntryVm.setProfileImageUri(uri)
                    navController.popBackStack()
                }
            )
        }

        composable("trip_details") { entry ->
            val profileNavGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Profile.route)
            }
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = profileNavGraphEntry,
                factory = Factory
            )
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = profileNavGraphEntry,
                factory = Factory
            )
            val reviewViewModel: ReviewViewModel = viewModel(
                viewModelStoreOwner = profileNavGraphEntry,
                factory = Factory
            )
            TripDetails(navController = navController, vm = tripViewModel, owner = false,
                uvm = userViewModel,
                rvm = reviewViewModel)
        }

    }
}

@Composable
fun ProfilePhoto(user: User, isSmall: Boolean, modifier : Modifier, uvm: UserViewModel) {
    //val user by uvm.userGotFromDB.collectAsState()

    var initials = "WU"     //Waiting User

    if (user.firstname.isNotEmpty() && user.surname.isNotEmpty()) {
         initials = "${user.firstname.first()}"+"${user.surname.first()}"
    }

    if(user.profilePictureUrl == null)
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
                .background(color = Color.Blue, shape = CircleShape)

        ) {
            //Icon(profileImage)
            AsyncImage(user.profilePictureUrl,"profilePic",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape = CircleShape)
                    .border(0.dp, Color.White, CircleShape),
                contentScale = ContentScale.Crop
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(nvm: NotificationViewModel, navController: NavController) {

    //Top Bar Images
    val painterLogo = painterResource(R.drawable.logo)
    val painterNews = painterResource(R.drawable.news)
    val painterNotification = painterResource(R.drawable.notifications)

//    val snackbarHostState = remember { SnackbarHostState() }
//    val scope = rememberCoroutineScope()
//    val context = LocalContext.current

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
            val context = LocalContext.current
            IconButton(onClick = {/*TO DO*/}) {
                Image(
                    painter = painterNews,
                    contentDescription = "news",
                    modifier = Modifier.padding(end = 10.dp)
                )
            }
            IconButton(onClick = {
                nvm.markNotificationsRead()
                navController.navigate(Screen.Notifications.route)
            }) {
                Box {
                    Image(painter = painterNotification, contentDescription = "notification")
                    if (nvm.hasNewNotification.value) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color.Red, shape = CircleShape)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
        },
        modifier = Modifier.shadow(8.dp)
    )
}

@SuppressLint("ObsoleteSdkInt")
@Composable
fun CameraScreen(context: Context, onImageCaptured: (Uri?) -> Unit) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e("Camera", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        IconButton(
            onClick = {
                val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
                    }
                }
                val outputOptions = ImageCapture.OutputFileOptions.Builder(
                    context.contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ).build()

                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: ImageCaptureException) {
                            Log.e("Camera", "Photo capture failed: ${exc.message}", exc)
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            Log.d("Camera", "Photo capture succeeded: ${output.savedUri}")
                            onImageCaptured(output.savedUri)
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color.Gray, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = "Capture",
                tint = Color.Black
            )
        }
    }
}


