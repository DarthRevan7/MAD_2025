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
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.voyago.R
import com.example.voyago.model.NavItem
import com.example.voyago.model.User
import com.example.voyago.model.UserModel
import com.example.voyago.view.ActivitiesList
import com.example.voyago.view.ArticleDetailScreen
import com.example.voyago.view.ArticleSearchScreen
import com.example.voyago.view.ChatScreen
import com.example.voyago.view.CompleteAccount
import com.example.voyago.view.CreateAccount2Screen
import com.example.voyago.view.CreateAccountScreen
import com.example.voyago.view.CreateArticleScreen
import com.example.voyago.view.CreateNewTrip
import com.example.voyago.view.EditActivity
import com.example.voyago.view.EditProfileScreen
import com.example.voyago.view.EditTrip
import com.example.voyago.view.ExplorePage
import com.example.voyago.view.FiltersSelection
import com.example.voyago.view.FirebaseChatRoomScreen
import com.example.voyago.view.HomePageScreen
import com.example.voyago.view.LoginScreen
import com.example.voyago.view.MyProfileScreen
import com.example.voyago.view.MyReviews
import com.example.voyago.view.MyTripsPage
import com.example.voyago.view.NewActivity
import com.example.voyago.view.NotificationView
import com.example.voyago.view.RegistrationVerificationCodeScreen
import com.example.voyago.view.RetrievePassword
import com.example.voyago.view.TripApplications
import com.example.voyago.view.TripDetails
import com.example.voyago.view.UserProfileScreen
import com.example.voyago.viewmodel.ArticleFactory
import com.example.voyago.viewmodel.ArticleViewModel
import com.example.voyago.viewmodel.ChatFactory
import com.example.voyago.viewmodel.ChatViewModel
import com.example.voyago.viewmodel.Factory
import com.example.voyago.viewmodel.NotificationFactory
import com.example.voyago.viewmodel.NotificationViewModel
import com.example.voyago.viewmodel.ReviewFactory
import com.example.voyago.viewmodel.ReviewViewModel
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserFactory
import com.example.voyago.viewmodel.UserViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// List of screens in the app, routing to different parts of the app
sealed class Screen(val route: String) {
    object Explore : Screen("explore_root")
    object MyTrips : Screen("my_trips_root")
    object Home : Screen("home_root")
    object Chats : Screen("chats_root")
    object Profile : Screen("profile_root")
    object Notifications : Screen("notifications")
    object Login : Screen("login_root")
}

// Function to create a new user in Firestore
fun createNewUser(firebaseUser: FirebaseUser, newUser: User, onResult: (Boolean, User?) -> Unit) {
    // Get Firestore instance and reference to the user counter document
    val firestore = com.google.firebase.Firebase.firestore
    val counterRef = firestore.collection("metadata").document("userCounter")

    // Set the new user's email
    newUser.email = firebaseUser.email ?: ""

    // Run a transaction to ensure atomicity when creating a new user
    firestore.runTransaction { transaction ->
        // Get the current value of the user counter
        val snapshot = transaction.get(counterRef)
        val lastUserId = snapshot.getLong("lastUserId") ?: 0
        // Increment the user ID and create a new user document
        val newUserId = lastUserId + 1
        transaction.update(counterRef, "lastUserId", newUserId)
        val userWithId = newUser.copy(id = newUserId.toInt())
        val userDocRef = firestore.collection("users").document(newUserId.toString())
        // Set the user document with the new user ID
        transaction.set(userDocRef, userWithId)
        userWithId
    }.addOnSuccessListener { user ->
        onResult(true, user)
    }.addOnFailureListener { e ->
        onResult(false, null)
    }
}

class MainActivity : ComponentActivity() {
    // Executor for camera operations
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var context: Context

    // Initialize Firebase and set up the main activity
    override fun onCreate(savedInstanceState: Bundle?) {
        // Call the superclass onCreate method
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize the UserViewModel with the UserFactory
        val viewModel: UserViewModel by viewModels { UserFactory }

        // Handle deep link for email verification
        val data = intent?.data
        val mode = data?.getQueryParameter("mode")
        val oobCode = data?.getQueryParameter("oobCode")

        // If the mode is "verifyEmail" and oobCode is not null, apply the action code
        if (mode == "verifyEmail" && oobCode != null) {
            FirebaseAuth.getInstance().applyActionCode(oobCode)
                .addOnSuccessListener {
                    // Get the current user and pending user from the ViewModel
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val pendingUser = viewModel.pendingUser

                    // If the current user is not null, email is verified, and pending user exists,
                    if (currentUser != null && currentUser.isEmailVerified && pendingUser != null) {
                        // Create a new user in Firestore
                        createNewUser(
                            firebaseUser = currentUser,
                            newUser = pendingUser,
                            onResult = { success, _ ->
                                if (success) {
                                    viewModel.clearUser()
                                    viewModel.setUserVerified(true)
                                }
                            }
                        )
                    } else {
                        // If user data is missing or not verified, show a toast message
                        Toast.makeText(this, "User data missing or not verified", Toast.LENGTH_LONG)
                            .show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Verification failed: ${it.message}", Toast.LENGTH_LONG)
                        .show()
                }
        }

        // Enable edge-to-edge mode for the activity
        // This allows the app to use the full screen, including the status bar and navigation bar
        enableEdgeToEdge()
        context = this
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Subscribe to the "all" topic for Firebase Cloud Messaging
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

        // Set the content view with the main screen
        setContent {
            MainScreen(viewModel)
        }

    }

    // Request camera permissions
    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    // Check if all required permissions are granted
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // Destroy the camera executor when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    // Companion object to hold the required permissions
    companion object {
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

    // Activity result launcher for handling permission requests
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val permissionGranted = permissions.entries.all {
                it.key in REQUIRED_PERMISSIONS && it.value
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext, "Permission request denied", Toast.LENGTH_SHORT).show()
            }
        }
}

// Composable function to display the main screen with a top bar and bottom navigation
@Composable
fun MainScreen(viewModel: UserViewModel) {
    val navController = rememberNavController()
    val notificationViewModel = NotificationViewModel()
    val userVerified by viewModel.userVerified.collectAsState()

    // Observe user verification state and navigate to profile overview if verified
    LaunchedEffect(userVerified) {
        if (userVerified) {
            navController.navigate("profile_overview") {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
            viewModel.resetUserVerified()
        }
    }

    // Set the main content with a Scaffold that includes a top bar and bottom navigation
    Scaffold(
        topBar = {
            TopBar(
                nvm = notificationViewModel,
                navController = navController,
                uvm = UserViewModel(UserModel())
            )
        },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        NavigationGraph(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}

// Composable function to display the bottom navigation bar
@Composable
fun BottomBar(navController: NavHostController) {
    // Define the navigation items for the bottom bar
    val items = listOf(
        NavItem("Explore", Icons.Filled.LocationOn, Screen.Explore.route, "explore_main"),
        NavItem("My Trips", Icons.Filled.Commute, Screen.MyTrips.route, "my_trips_main"),
        NavItem("Home", Icons.Filled.Language, Screen.Home.route, Screen.Home.route),
        NavItem("Chats", Icons.Filled.ChatBubble, Screen.Chats.route, "chats_list"),
        NavItem("Profile", Icons.Filled.AccountCircle, Screen.Profile.route, "profile_overview")
    )

    // Get the current back stack entry to determine the selected item
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        // Create a navigation bar item for each item in the list
        items.forEach { item ->
            val selected = currentDestination
                ?.hierarchy
                ?.any { it.route == item.rootRoute } == true

            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    // Navigate to the selected route
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

// Composable function to display the top app bar with logo,  notifications and articles
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(nvm: NotificationViewModel, navController: NavController, uvm: UserViewModel) {
    val user by uvm.loggedUser.collectAsState()
    val userId = user.id.toString()
    val hasNewNotification by nvm.hasNewNotification
    val context = LocalContext.current

    // Load notifications for the user when the top bar is first composed
    LaunchedEffect(userId) {
        nvm.loadNotificationsForUser(context, userId)
    }

    //Top Bar Images (logo, news, notification)
    val painterLogo = painterResource(R.drawable.logo)
    val painterNews = painterResource(R.drawable.news)
    val painterNotification = painterResource(R.drawable.notifications)

    // Top App Bar with logo, news icon and notification icon
    TopAppBar(
        // Set the background color
        colors = topAppBarColors(
            containerColor = Color(0xe6, 0xe0, 0xe9, 255),
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        // Set the title with the logo
        title = {
            Image(
                painter = painterLogo,
                contentDescription = "logo",
                modifier = Modifier.size(120.dp)
            )
        },
        actions = {
            // News Icon - Click to go to ArticleSearch
            IconButton(onClick = {
                // Navigate to the ArticleSearch page
                navController.navigate("article_search")
            }) {
                Image(
                    painter = painterNews,
                    contentDescription = "news",
                    modifier = Modifier.padding(end = 10.dp)
                )
            }

            // Notification Icon - Click to mark notifications as read and navigate to Notifications screen
            IconButton(
                onClick = {
                    // Mark notifications as read
                    nvm.markNotificationsRead(userId)
                    // Navigate to the Notifications screen
                    navController.navigate(Screen.Notifications.route) {
                        launchSingleTop = true // Prevents multiple instances of the same screen
                    }
                }
            ) {
                // Notification Icon with a red dot if there are new notifications
                Box(
                    modifier = Modifier
                        .padding(top = 7.dp, end = 10.dp)
                        .size(45.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    // Display the notification icon
                    Image(
                        painter = painterNotification,
                        contentDescription = "notification"
                    )
                    // If there are new notifications, display a red dot
                    if (hasNewNotification) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color.Red, shape = CircleShape)
                                .align(Alignment.TopEnd)
                                .offset(x = (-2).dp, y = 2.dp)
                                .zIndex(1f)
                        )
                    }
                }
            }
        },
        modifier = Modifier.shadow(8.dp)
    )
}

// Composable function to require authentication before accessing certain screens
@Composable
fun RequireAuth(navController: NavController, content: @Composable () -> Unit) {
    // Get the current context and Firebase user
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Check if the user is authenticated
    if (currentUser != null) {
        // If authenticated, display the content
        content()
    } else {
        // If not authenticated, show a toast message and navigate to the login screen
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Please log in to access this section", Toast.LENGTH_SHORT)
                .show()
            navController.navigate(Screen.Login.route)
        }
    }
}

// Composable function to set up the navigation graph for the app
@Composable
fun NavigationGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    // Initialize Firebase Auth instance
    val auth = FirebaseAuth.getInstance()
    // Initialize the start destination for the navigation graph
    val startDest = Screen.Home.route

    // Create a NavHost with the NavController and start destination
    NavHost(navController = navController, startDestination = startDest, modifier = modifier) {
        // Define the navigation graphs for different sections of the app
        exploreNavGraph(navController)
        myTripsNavGraph(navController)
        homeNavGraph(navController)
        chatsNavGraph(navController)
        profileNavGraph(navController)
        loginNavGraph(navController, auth)

        // WE NEED TO ADD THE FOLLOWING ROUTES AND NAVIGATIONS GRAPH: NOTIFICATIONS, ARTICLE
        // FROM THIS POINT, WE NEED TO RELOCATE STUFF

        // 添加全局的 article_search 路由，这样从任何地方都可以访问
        composable("article_search") {
            val articleViewModel: ArticleViewModel = viewModel(factory = ArticleFactory)
            val userViewModel: UserViewModel = viewModel(factory = UserFactory)

            ArticleSearchScreen(
                navController = navController,
                articleViewModel = articleViewModel,
                userViewModel = userViewModel
            )
        }
        // 添加 create_article 路由
        composable("create_article") {
            RequireAuth(navController) {
                val articleViewModel: ArticleViewModel = viewModel(factory = ArticleFactory)
                val userViewModel: UserViewModel = viewModel(factory = UserFactory)
                val notificationViewModel: NotificationViewModel = viewModel(factory = NotificationFactory)

                CreateArticleScreen(
                    navController = navController,
                    articleViewModel = articleViewModel,
                    userViewModel = userViewModel,
                    nvm = notificationViewModel
                )
            }
        }

        // 添加 article_detail 路由
        composable(
            "article_detail/{articleId}",
            arguments = listOf(navArgument("articleId") { type = NavType.IntType })
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getInt("articleId") ?: 0
            val articleViewModel: ArticleViewModel = viewModel(factory = ArticleFactory)
            val userViewModel: UserViewModel = viewModel(factory = UserFactory)

            ArticleDetailScreen(
                navController = navController,
                articleId = articleId,
                articleViewModel = articleViewModel,
                userViewModel = userViewModel
            )
        }

        composable("camera") {
            val context = LocalContext.current
            CameraScreen(
                context = context,
                onImageCaptured = { uri ->
                    Toast.makeText(context, "Saved to: $uri", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Notifications.route) { entry ->
            val parentEntry = remember(entry) {
                navController.getBackStackEntry(startDest)
            }

            val nvm: NotificationViewModel =
                viewModel(viewModelStoreOwner = parentEntry, factory = NotificationFactory)
            val uvm: UserViewModel = viewModel(viewModelStoreOwner = parentEntry, factory = Factory)
            val vm: TripViewModel = viewModel(viewModelStoreOwner = parentEntry, factory = Factory)
            val avm: ArticleViewModel = viewModel(viewModelStoreOwner = parentEntry, factory = ArticleFactory)

            NotificationView(navController, nvm, uvm, vm, avm)
        }
    }
}


// Composable function to set up the navigation graph for the "Login" section
fun NavGraphBuilder.loginNavGraph(navController: NavHostController, auth: FirebaseAuth) {
    // Define the start destination for the login navigation graph
    navigation(startDestination = "login", route = Screen.Login.route) {
        // Define the composable for the login screen
        composable("login") { entry ->
            val loginGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Login.route)
            }
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = loginGraphEntry,
                factory = Factory
            )
            LoginScreen(navController = navController, auth = auth, uvm = userViewModel)
        }

        // Define the composable for the retrieve password screen
        composable("retrieve_password") {
            RetrievePassword(navController = navController)
        }

        // Define the composable for the registration screens
        composable("register") {
            CreateAccountScreen(navController)
        }
        composable("register2") { entry ->
            val loginGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Login.route)
            }
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = loginGraphEntry,
                factory = Factory
            )
            CreateAccount2Screen(navController, userViewModel)
        }

        // Define the composable for the registration verification email screen
        composable("register_verification_code") { entry ->
            val loginGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Login.route)
            }
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = loginGraphEntry,
                factory = Factory
            )
            RegistrationVerificationCodeScreen(navController, userViewModel)
        }

        // Define the composable for the completion of profile for new users that did the sign in with google
        composable("complete_profile") { entry ->
            val loginGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Login.route)
            }
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = loginGraphEntry,
                factory = Factory
            )
            CompleteAccount(navController, uvm = userViewModel)
        }

        // Define the composable for the retrieve password screen
        composable("retrieve_password") { entry ->
            RetrievePassword(navController)
        }
    }
}

// Composable function to set up the navigation graph for the "Explore" section
fun NavGraphBuilder.exploreNavGraph(navController: NavController) {
    // Define the start destination for the explore navigation graph
    navigation(startDestination = "explore_main", route = Screen.Explore.route) {
        // Define the composable for the explore main page
        composable("explore_main") { entry ->
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
            ExplorePage(navController = navController, vm = tripViewModel, userViewModel)
        }

        // Define the composable for the trip details page
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
            val notificationViewModel: NotificationViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = NotificationFactory
            )
            TripDetails(
                navController = navController,
                vm = tripViewModel,
                owner = false,
                uvm = userViewModel,
                rvm = reviewViewModel,
                nvm = notificationViewModel
            )
        }

        // Why is this here? It is not used in the the explore section
        composable("article_search") {
            val articleViewModel: ArticleViewModel = viewModel(factory = ArticleFactory)
            val userViewModel: UserViewModel = viewModel(factory = UserFactory)  // 添加这行

            ArticleSearchScreen(
                navController = navController,
                articleViewModel = articleViewModel,
                userViewModel = userViewModel
            )
        }

        // Define the composable to view a user's profile
        composable(
            "user_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { entry ->
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

        // Define the composable for the filters selection page
        composable("filters_selection") { entry ->
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
            FiltersSelection(navController = navController, vm = tripViewModel, uvm = userViewModel)
        }
    }
}

// Composable function to set up the navigation graph for the "My trips" section
fun NavGraphBuilder.myTripsNavGraph(navController: NavController) {
    // Define the start destination for the my trips navigation graph
    navigation(startDestination = "my_trips_main", route = Screen.MyTrips.route) {
        // Define the composable for the main My Trips page
        composable("my_trips_main") { entry ->
            // Require authentication before accessing the My Trips page
            RequireAuth(navController) {
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

        }

        // Define the composable for the trip details page with an owner parameter
        composable("trip_details?owner={owner}", arguments = listOf(navArgument("owner") {
            type = NavType.BoolType
            defaultValue = false
        }
        )
        ) { entry ->
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
            val notificationViewModel: NotificationViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = NotificationFactory
            )
            TripDetails(
                navController = navController,
                vm = tripViewModel,
                owner = true,
                uvm = userViewModel,
                rvm = reviewViewModel,
                nvm = notificationViewModel
            )
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
            val notificationViewModel: NotificationViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = NotificationFactory
            )
            MyReviews(
                navController = navController, vm = tripViewModel, uvm = userViewModel,
                rvm = reviewViewModel,
                nvm = notificationViewModel
            )
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
            val notificationViewModel: NotificationViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = NotificationFactory
            )
            TripApplications(
                vm = tripViewModel,
                userViewModel,
                navController,
                notificationViewModel
            )
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
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            CreateNewTrip(navController = navController, vm = tripViewModel, uvm = userViewModel)
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

        composable(
            "edit_activity/{activityId}",
            arguments = listOf(navArgument("activityId") { type = NavType.IntType })
        ) { entry ->
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

        composable(
            "user_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { entry ->
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
            val notificationViewModel: NotificationViewModel = viewModel(
                viewModelStoreOwner = homeEntry,
                factory = NotificationFactory
            )
            TripDetails(
                navController = navController,
                vm = homeVm,
                owner = false,
                uvm = userViewModel,
                rvm = reviewViewModel,
                nvm = notificationViewModel
            )
        }

        composable(
            "user_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { entry ->
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


fun NavGraphBuilder.chatsNavGraph(navController: NavController) {
    navigation(startDestination = "chats_list", route = Screen.Chats.route) {
        composable("chats_list") { entry ->
            RequireAuth(navController) {

                val chatNavGraphEntry = remember(entry) {
                    navController.getBackStackEntry(Screen.Chats.route)
                }
//                Text("Chats List Screen")
                val chatViewModel: ChatViewModel = viewModel(
                    viewModelStoreOwner = chatNavGraphEntry,
                    factory = ChatFactory
                )

                ChatScreen(chatViewModel, navController, Modifier)

                // 获取用户信息
                val chatsGraphEntry = remember(entry) {
                    navController.getBackStackEntry(Screen.Chats.route)
                }
                val userViewModel: UserViewModel = viewModel(
                    viewModelStoreOwner = chatsGraphEntry,
                    factory = Factory
                )
                val currentUser by userViewModel.loggedUser.collectAsState()

                // 使用 Firebase 实时聊天室
                FirebaseChatRoomScreen(
                    currentUser = currentUser,
                    onBackClick = null // 主聊天界面不需要返回按钮
                )
            }
        }

        composable("chat_detail/{chatId}") { backStackEntry ->
            RequireAuth(navController) {
                // 获取用户信息
                val chatsGraphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.Chats.route)
                }
                val userViewModel: UserViewModel = viewModel(
                    viewModelStoreOwner = chatsGraphEntry,
                    factory = Factory
                )
                val currentUser by userViewModel.loggedUser.collectAsState()

                // 具体聊天室 - 可以根据 chatId 加载不同的聊天室
                FirebaseChatRoomScreen(
                    currentUser = currentUser,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}


fun NavGraphBuilder.profileNavGraph(
    navController: NavHostController,
    //vm1: TripListViewModel,

) {

    navigation(startDestination = "profile_overview", route = Screen.Profile.route) {
        composable(
            route = "profile_overview?tabIndex={tabIndex}",
            arguments = listOf(
                navArgument("tabIndex") {
                    type = NavType.IntType
                    defaultValue = 0 // fallback to "About" tab
                }
            )
        ) { entry ->
            RequireAuth(navController) {
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
                val tabIndex = entry.arguments?.getInt("tabIndex") ?: 0

                MyProfileScreen(
                    navController = navController,
                    vm = profileNavGraphEntryVm,
                    vm2 = vm2,
                    uvm = userViewModel,
                    rvm = reviewViewModel,
                    defaultTabIndex = tabIndex
                )
            }
        }

        composable(
            "user_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { entry ->
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

        composable("camera") { entry ->

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
            val notificationViewModel: NotificationViewModel = viewModel(
                viewModelStoreOwner = profileNavGraphEntry,
                factory = NotificationFactory
            )
            TripDetails(
                navController = navController, vm = tripViewModel, owner = false,
                uvm = userViewModel,
                rvm = reviewViewModel,
                nvm = notificationViewModel
            )
        }

    }
}

// 在 ProfilePhoto.kt 或相关文件中修改 ProfilePhoto 组件：

// 在 ProfilePhoto 组件中修复：

// 替代方案：不使用委托，直接使用 MutableState

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfilePhoto(
    modifier: Modifier = Modifier,
    user: User,
    small: Boolean = false
) {
    // 🔄 替代方案：直接使用 MutableState
    val profileImageUrl = remember { mutableStateOf<String?>(null) }
    val initials = "${user.firstname.firstOrNull() ?: ""}${user.surname.firstOrNull() ?: ""}"

    // 异步获取 Firebase Storage URL
    LaunchedEffect(user.profilePictureUrl) {
        if (!user.profilePictureUrl.isNullOrEmpty()) {
            try {
                profileImageUrl.value = user.getProfilePhoto() // 使用 .value
            } catch (e: Exception) {
                Log.e("ProfilePhoto", "Failed to load profile photo", e)
                profileImageUrl.value = null
            }
        }
    }

    val size = if (small) 50.dp else 120.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .background(Color.Blue, shape = CircleShape)
    ) {
        when {
            // 如果有 Firebase Storage URL，使用 GlideImage
            profileImageUrl.value != null -> { // 使用 .value
                GlideImage(
                    model = profileImageUrl.value,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(if (small) 1.dp else 2.dp, Color.White, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            // 其他情况保持不变...
            else -> {
                Text(
                    text = initials,
                    color = Color.White,
                    style = if (small) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
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
                val name = SimpleDateFormat(
                    "yyyy-MM-dd-HH-mm-ss-SSS",
                    Locale.US
                ).format(System.currentTimeMillis())
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


