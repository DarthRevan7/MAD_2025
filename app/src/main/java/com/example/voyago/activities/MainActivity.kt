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

        // Set the content view to the main activity layout
        context = this
        // Initialize the camera executor for handling camera operations
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
        // Register for the result of requesting multiple permissions
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Check if all required permissions are granted
            val permissionGranted = permissions.entries.all {
                it.key in REQUIRED_PERMISSIONS && it.value
            }
            // If any permission is denied, show a toast message
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
        // If the user is verified, navigate to the profile overview screen
        if (userVerified) {
            // Navigate to the profile overview screen
            navController.navigate("profile_overview") {
                // Pop up to the start destination of the navigation graph
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                // Launch the profile overview screen as a single top instance
                launchSingleTop = true
                // Restore the state of the profile overview screen
                restoreState = true
            }
            // Reset the user verification state in the ViewModel
            viewModel.resetUserVerified()
        }
    }

    // Set the main content with a Scaffold that includes a top bar and bottom navigation
    Scaffold(
        topBar = {
            // Display the top bar with notifications and articles
            TopBar(
                nvm = notificationViewModel,
                navController = navController,
                uvm = UserViewModel(UserModel())
            )
        },
        bottomBar = {
            // Display the bottom navigation bar
            BottomBar(navController)
        }
    ) { innerPadding ->
        // Main content of the app, which includes the navigation graph
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

    // Create a NavigationBar to display the bottom navigation bar
    NavigationBar {
        // Get the current back stack entry as state
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        // Get the current destination from the back stack entry
        val currentDestination = navBackStackEntry?.destination

        // Create a navigation bar item for each item in the list
        items.forEach { item ->
            // Check if the current destination matches the item's root route
            val selected = currentDestination
                ?.hierarchy
                ?.any { it.route == item.rootRoute } == true

            // Create a NavigationBarItem for the item
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    // Navigate to the selected route
                    if (item.label == "Home") {
                        navController.navigate(item.startRoute) {
                            // If the selected item is "Home", pop up to the start destination
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Launch the home screen as a single top instance
                            launchSingleTop = true
                            // Restore the state of the home screen
                            restoreState = false
                        }
                    } else {
                        // For other items, navigate to the start route of the item
                        navController.navigate(item.startRoute) {
                            // Pop up to the start destination of the navigation graph
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Launch the selected screen as a single top instance
                            launchSingleTop = true
                            // Restore the state of the selected screen
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
    // Get the logged-in user from the UserViewModel
    val user by uvm.loggedUser.collectAsState()
    val userId = user.id.toString()
    // Check if there are new notifications
    val hasNewNotification by nvm.hasNewNotification
    // Get the current context
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
                // Display the news icon
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
                        // Launch the Notifications screen as a single top instance
                        launchSingleTop = true
                    }
                }
            ) {
                // Notification Icon with a red dot if there are new notifications
                // Use a Box to overlay the notification icon and the red dot
                Box(
                    // Set the modifier for the box to position it correctly
                    modifier = Modifier
                        .padding(top = 7.dp, end = 10.dp)
                        .size(45.dp),
                    // Align the content to the top end of the box
                    contentAlignment = Alignment.TopEnd
                ) {
                    // Display the notification icon
                    Image(
                        painter = painterNotification,
                        contentDescription = "notification"
                    )
                    // If there are new notifications, display a red dot
                    if (hasNewNotification) {
                        // Red dot to indicate new notifications
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
        // Set the modifier for the top app bar to add a shadow effect
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

        // WE NEED TO ADD THE FOLLOWING ROUTES AND NAVIGATION'S GRAPH: NOTIFICATIONS, ARTICLE
        // FROM THIS POINT, WE NEED TO RELOCATE STUFF

        // 添加全局的 article_search 路由，这样从任何地方都可以访问
        composable("article_search") {
            val articleViewModel: ArticleViewModel = viewModel(factory = ArticleFactory)

            ArticleSearchScreen(
                navController = navController,
                articleViewModel = articleViewModel
            )
        }
        // 添加 create_article 路由
        composable("create_article") {
            RequireAuth(navController) {
                val articleViewModel: ArticleViewModel = viewModel(factory = ArticleFactory)
                val userViewModel: UserViewModel = viewModel(factory = UserFactory)
                val notificationViewModel: NotificationViewModel =
                    viewModel(factory = NotificationFactory)

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
            route = "article_detail/{articleId}",
            arguments = listOf(
                navArgument("articleId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getInt("articleId") ?: 0
            val articleViewModel: ArticleViewModel = viewModel(factory = ArticleFactory)

            ArticleDetailScreen(
                navController = navController,
                articleId = articleId,
                articleViewModel = articleViewModel
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
            val avm: ArticleViewModel =
                viewModel(viewModelStoreOwner = parentEntry, factory = ArticleFactory)

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
            // Get the back stack entry for the login graph
            val loginGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Login.route)
            }
            // Create a UserViewModel instance using the Factory
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = loginGraphEntry,
                factory = Factory
            )
            // Pass the UserViewModel and FirebaseAuth instance to the LoginScreen composable
            LoginScreen(navController = navController, auth = auth, uvm = userViewModel)
        }

        // Define the composable for the retrieve password screen
        composable("retrieve_password") {
            // Pass the NavController to the RetrievePassword composable
            RetrievePassword(navController = navController)
        }

        // Define the composable for the registration screens
        composable("register") {
            // Pass the NavController to the CreateAccountScreen composable
            CreateAccountScreen(navController)
        }
        composable("register2") { entry ->
            // Get the back stack entry for the login graph
            val loginGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Login.route)
            }
            // Create a UserViewModel instance using the Factory
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = loginGraphEntry,
                factory = Factory
            )
            // Pass the NavController and UserViewModel to the CreateAccount2Screen composable
            CreateAccount2Screen(navController, userViewModel)
        }

        // Define the composable for the registration verification email screen
        composable("register_verification_code") { entry ->
            // Get the back stack entry for the login graph
            val loginGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Login.route)
            }
            // Create a UserViewModel instance using the Factory
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = loginGraphEntry,
                factory = Factory
            )
            // Pass the NavController and UserViewModel to the RegistrationVerificationCodeScreen composable
            RegistrationVerificationCodeScreen(navController, userViewModel)
        }

        // Define the composable for the completion of profile for new users that did the sign in with google
        composable("complete_profile") { entry ->
            // Get the back stack entry for the login graph
            val loginGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Login.route)
            }
            // Create a UserViewModel instance using the Factory
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = loginGraphEntry,
                factory = Factory
            )
            // Pass the NavController and UserViewModel to the CompleteAccount composable
            CompleteAccount(navController, uvm = userViewModel)
        }

        // Define the composable for the retrieve password screen
        composable("retrieve_password") { entry ->
            // Pass the NavController to the RetrievePassword composable
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
            // Get the back stack entry for the explore graph
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Explore.route)
            }
            // Create instances of the TripViewModel using the Factory
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            // Create an instance of the UserViewModel using the Factory
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            // Pass the NavController and ViewModels to the ExplorePage composable
            ExplorePage(navController = navController, vm = tripViewModel, userViewModel)
        }

        // Define the composable for the trip details page
        composable("trip_details") { entry ->
            // Get the back stack entry for the explore graph
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Explore.route)
            }
            // Create instances of the TripViewModel using the Factory
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            // Create an instance of the UserViewModel using the Factory
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            // Create an instance of the ReviewViewModel using the Factory
            val reviewViewModel: ReviewViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            // Create an instance of the NotificationViewModel using the NotificationFactory
            val notificationViewModel: NotificationViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = NotificationFactory
            )
            // Pass the NavController and ViewModels to the TripDetails composable
            TripDetails(
                navController = navController,
                vm = tripViewModel,
                owner = false,
                uvm = userViewModel,
                rvm = reviewViewModel,
                nvm = notificationViewModel
            )
        }

        // Define the composable for the article search page
        composable("article_search") { entry ->
            // Get the back stack entry for the explore graph
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Explore.route)
            }
            // Create an instance of the ArticleViewModel using the ArticleFactory
            val articleViewModel: ArticleViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = ArticleFactory
            )
            // Pass the NavController, ArticleViewModel, and UserViewModel to the ArticleSearchScreen composable
            ArticleSearchScreen(
                navController = navController,
                articleViewModel = articleViewModel
            )
        }

        // Define the composable to view a user's profile
        composable(
            route = "user_profile/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.IntType
                }
            )
        ) { entry ->
            // Get the back stack entry for the explore graph
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Explore.route)
            }
            // Create instances of the TripViewModel using the Factory
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            // Create an instance of the UserViewModel using the Factory
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            // Create an instance of the ReviewViewModel using the Factory
            val reviewViewModel: ReviewViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            // Create an instance of the ArticleViewModel using the ArticleFactory
            val vm2: ArticleViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = ArticleFactory
            )
            // Get the userId from the arguments, defaulting to -1 if not provided
            val userId = entry.arguments?.getInt("userId") ?: -1
            // Pass the NavController, ViewModels, userId to the UserProfileScreen composable
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
            // Get the back stack entry for the explore graph
            val exploreGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Explore.route)
            }
            // Create instances of the TripViewModel using the Factory
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            // Create an instance of the UserViewModel using the Factory
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = exploreGraphEntry,
                factory = Factory
            )
            // Pass the NavController and ViewModels to the FiltersSelection composable
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
                // Get the back stack entry for the My Trips graph
                val myTripGraphEntry = remember(entry) {
                    navController.getBackStackEntry(Screen.MyTrips.route)
                }
                // Create instances of the TripViewModel using the Factory
                val tripViewModel: TripViewModel = viewModel(
                    viewModelStoreOwner = myTripGraphEntry,
                    factory = Factory
                )
                // Create an instance of the UserViewModel using the Factory
                val userViewModel: UserViewModel = viewModel(
                    viewModelStoreOwner = myTripGraphEntry,
                    factory = Factory
                )
                // Pass the NavController and ViewModels to the MyTripsPage composable
                MyTripsPage(navController = navController, vm = tripViewModel, uvm = userViewModel)
            }

        }

        // Define the composable for the trip details page with an owner parameter
        composable(
            route = "trip_details?owner={owner}",
            arguments = listOf(
                navArgument("owner") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { entry ->
            // Get the back stack entry for the My Trips graph
            val myTripGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            // Create instances of the TripViewModel using the Factory
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = Factory
            )
            // Create an instance of the UserViewModel using the Factory
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = Factory
            )
            // Create an instance of the ReviewViewModel using the Factory
            val reviewViewModel: ReviewViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = Factory
            )
            // Create an instance of the NotificationViewModel using the NotificationFactory
            val notificationViewModel: NotificationViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = NotificationFactory
            )
            // Get the owner parameter from the arguments, defaulting to false if not provided
            // Pass the NavController, ViewModels, and owner parameter to the TripDetails composable
            TripDetails(
                navController = navController,
                vm = tripViewModel,
                owner = true,
                uvm = userViewModel,
                rvm = reviewViewModel,
                nvm = notificationViewModel
            )
        }

        // Define the composable for the My Reviews page
        composable("my_reviews") { entry ->
            // Get the back stack entry for the My Trips graph
            val myTripGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            // Create instances of the TripViewModel using the Factory
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = Factory
            )
            // Create an instance of the UserViewModel using the UserFactory
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = UserFactory
            )
            // Create an instance of the ReviewViewModel using the ReviewFactory
            val reviewViewModel: ReviewViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = ReviewFactory
            )
            // Create an instance of the NotificationViewModel using the NotificationFactory
            val notificationViewModel: NotificationViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = NotificationFactory
            )
            // Pass the NavController and ViewModels to the MyReviews composable
            MyReviews(
                navController = navController,
                vm = tripViewModel,
                uvm = userViewModel,
                rvm = reviewViewModel,
                nvm = notificationViewModel
            )
        }

        // Define the composable for the trip applications page
        composable("trip_applications") { entry ->
            // Get the back stack entry for the My Trips graph
            val myTripGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            // Create instances of the TripViewModel using the Factory
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = Factory
            )
            // Create an instance of the UserViewModel using the Factory
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = Factory
            )
            // Create an instance of the NotificationViewModel using the NotificationFactory
            val notificationViewModel: NotificationViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = NotificationFactory
            )
            // Pass the NavController and ViewModels to the TripApplications composable
            TripApplications(
                vm = tripViewModel,
                uvm = userViewModel,
                navController = navController,
                nvm = notificationViewModel
            )
        }

        // Define the composable for editing a trip
        composable("edit_trip") { entry ->
            // Get the back stack entry for the My Trips graph
            val myTripGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            // Create instances of the TripViewModel using the Factory
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = Factory
            )
            // Pass the NavController and TripViewModel to the EditTrip composable
            EditTrip(navController = navController, vm = tripViewModel)
        }

        // Define the composable for creating a new trip
        composable("create_new_trip") { entry ->
            // Get the back stack entry for the My Trips graph
            val myTripGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            // Create instances of the TripViewModel using the Factory
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = Factory
            )
            // Create an instance of the UserViewModel using the Factory
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = Factory
            )
            // Pass the NavController, TripViewModel, and UserViewModel to the CreateNewTrip composable
            CreateNewTrip(navController = navController, vm = tripViewModel, uvm = userViewModel)
        }

        // Define the composable for the activities list page
        composable("activities_list") { entry ->
            // Get the back stack entry for the My Trips graph
            val myTripGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            // Create instances of the TripViewModel using the Factory
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = Factory
            )
            // Pass the NavController and TripViewModel to the ActivitiesList composable
            ActivitiesList(navController = navController, vm = tripViewModel)
        }

        // Define the composable for creating a new activity
        composable("new_activity") { entry ->
            // Get the back stack entry for the My Trips graph
            val myTripGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            // Create instances of the TripViewModel using the Factory
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = Factory
            )
            // Pass the NavController and TripViewModel to the NewActivity composable
            NewActivity(navController = navController, vm = tripViewModel)
        }

        // Define the composable for editing an activity
        composable(
            route = "edit_activity/{activityId}",
            arguments = listOf(
                navArgument("activityId") {
                    type = NavType.IntType
                }
            )
        ) { entry ->
            // Get the back stack entry for the My Trips graph
            val myTripGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            // Create instances of the TripViewModel using the Factory
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = Factory
            )
            // Get the activityId from the arguments, defaulting to -1 if not provided
            val activityId = entry.arguments?.getInt("activityId") ?: -1
            // Pass the NavController, TripViewModel, and activityId to the Edit
            EditActivity(navController = navController, vm = tripViewModel, activityId)
        }

        // Define the composable for viewing a user's profile
        composable(
            route = "user_profile/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.IntType
                }
            )
        ) { entry ->
            // Get the back stack entry for the My Trips graph
            val myTripGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.MyTrips.route)
            }
            // Create instances of the TripViewModel using the Factory
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = Factory
            )
            // Create an instance of the UserViewModel using the Factory
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = Factory
            )
            // Create an instance of the ReviewViewModel using the Factory
            val reviewViewModel: ReviewViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = Factory
            )
            // Create an instance of the ArticleViewModel using the ArticleFactory
            val articleViewModel: ArticleViewModel = viewModel(
                viewModelStoreOwner = myTripGraphEntry,
                factory = ArticleFactory
            )
            // Get the userId from the arguments, defaulting to -1 if not provided
            val userId = entry.arguments?.getInt("userId") ?: -1
            // Pass the NavController, ViewModels, userId to the UserProfileScreen composable
            UserProfileScreen(
                navController = navController,
                vm = tripViewModel,
                vm2 = articleViewModel,
                userId = userId,
                uvm = userViewModel,
                rvm = reviewViewModel
            )
        }
    }
}

// Composable function to set up the navigation graph for the "Home" section
fun NavGraphBuilder.homeNavGraph(navController: NavHostController) {
    // Define the start destination for the home navigation graph
    navigation(startDestination = "home_main", route = Screen.Home.route) {
        // Define the composable for the main Home page
        composable("home_main") { entry ->
            // Get the back stack entry for the home graph
            val homeGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            // Create instances of the TripViewModel using the Factory
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = homeGraphEntry,
                factory = Factory
            )
            // Create an instance of the UserViewModel using the Factory
            val articleViewModel: ArticleViewModel = viewModel(
                viewModelStoreOwner = homeGraphEntry,
                factory = ArticleFactory
            )
            // Pass the NavController and ViewModels to the HomePageScreen composable
            HomePageScreen(
                navController = navController,
                vm1 = tripViewModel,
                vm2 = articleViewModel,
                onTripClick = { trip ->
                    // When a trip is clicked, set the selected trip in the TripViewModel
                    tripViewModel.setSelectedTrip(trip)
                    // Navigate to the trip details screen
                    navController.navigate("trip_details")
                }
            )
        }
        // Define the composable for the trip details page
        composable("trip_details") { entry ->
            // Get the back stack entry for the home graph
            val homeGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            // Create instances of the TripViewModel using the Factory
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = homeGraphEntry,
                factory = Factory
            )
            // Create an instance of the UserViewModel using the Factory
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = homeGraphEntry,
                factory = Factory
            )
            // Create an instance of the ReviewViewModel using the Factory
            val reviewViewModel: ReviewViewModel = viewModel(
                viewModelStoreOwner = homeGraphEntry,
                factory = Factory
            )
            // Create an instance of the NotificationViewModel using the NotificationFactory
            val notificationViewModel: NotificationViewModel = viewModel(
                viewModelStoreOwner = homeGraphEntry,
                factory = NotificationFactory
            )
            // Pass the NavController and ViewModels to the TripDetails composable
            TripDetails(
                navController = navController,
                vm = tripViewModel,
                owner = false,
                uvm = userViewModel,
                rvm = reviewViewModel,
                nvm = notificationViewModel
            )
        }

        composable(
            route = "user_profile/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.IntType
                }
            )
        ) { entry ->
            // Get the back stack entry for the home graph
            val homeGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            // Create instances of the TripViewModel using the Factory
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = homeGraphEntry,
                factory = Factory
            )
            // Create an instance of the UserViewModel using the Factory
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = homeGraphEntry,
                factory = Factory
            )
            // Create an instance of the ReviewViewModel using the Factory
            val reviewViewModel: ReviewViewModel = viewModel(
                viewModelStoreOwner = homeGraphEntry,
                factory = Factory
            )
            // Create an instance of the ArticleViewModel using the ArticleFactory
            val articleViewModel: ArticleViewModel = viewModel(
                viewModelStoreOwner = homeGraphEntry,
                factory = ArticleFactory
            )
            // Get the userId from the arguments, defaulting to -1 if not provided
            val userId = entry.arguments?.getInt("userId") ?: -1
            // Pass the NavController, ViewModels, userId to the UserProfileScreen composable
            UserProfileScreen(
                navController = navController,
                vm = tripViewModel,
                vm2 = articleViewModel,
                userId = userId,
                uvm = userViewModel,
                rvm = reviewViewModel
            )
        }
    }
}

// Composable function to set up the navigation graph for the "Chats" section
fun NavGraphBuilder.chatsNavGraph(navController: NavController) {
    // Define the start destination for the chats navigation graph
    navigation(startDestination = "chats_list", route = Screen.Chats.route) {
        // Define the composable for the chats list screen
        composable("chats_list") { entry ->
            // Require authentication before accessing the chats list
            RequireAuth(navController) {
                // Get the back stack entry for the chats graph
                val chatGraphEntry = remember(entry) {
                    navController.getBackStackEntry(Screen.Chats.route)
                }
                // Create an instance of the ChatViewModel using the ChatFactory
                val chatViewModel: ChatViewModel = viewModel(
                    viewModelStoreOwner = chatGraphEntry,
                    factory = ChatFactory
                )
                // Pass the NavController and ChatViewModel to the ChatScreen composable
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

        // Define the composable for the chat detail screen
        composable("chat_detail/{chatId}") { backStackEntry ->
            // Require authentication before accessing the chat detail
            RequireAuth(navController) {
                // Get the back stack entry for the chats graph
                val chatsGraphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.Chats.route)
                }
                // Create an instance of the ChatViewModel using the ChatFactory
                val userViewModel: UserViewModel = viewModel(
                    viewModelStoreOwner = chatsGraphEntry,
                    factory = Factory
                )
                // Get the currentUser from the UserViewModel
                val currentUser by userViewModel.loggedUser.collectAsState()

                // Pass the chatId from the arguments
                FirebaseChatRoomScreen(
                    currentUser = currentUser,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}


fun NavGraphBuilder.profileNavGraph(navController: NavHostController) {
    // Define the start destination for the profile navigation graph
    navigation(startDestination = "profile_overview", route = Screen.Profile.route) {
        // Define the composable for the profile overview screen
        composable(
            route = "profile_overview?tabIndex={tabIndex}",
            arguments = listOf(
                navArgument("tabIndex") {
                    type = NavType.IntType
                    defaultValue = 0 // fallback to "About" tab
                }
            )
        ) { entry ->
            // Require authentication before accessing the profile overview
            RequireAuth(navController) {
                // Get the back stack entry for the profile graph
                val profileGraphEntry = remember(entry) {
                    navController.getBackStackEntry(Screen.Profile.route)
                }
                // Create instances of the ViewModels using the Factory
                val tripViewModel: TripViewModel = viewModel(
                    viewModelStoreOwner = profileGraphEntry,
                    factory = Factory
                )
                // Create an instance of the UserViewModel using the Factory
                val userViewModel: UserViewModel = viewModel(
                    viewModelStoreOwner = profileGraphEntry,
                    factory = Factory
                )
                // Create an instance of the ReviewViewModel using the Factory
                val reviewViewModel: ReviewViewModel = viewModel(
                    viewModelStoreOwner = profileGraphEntry,
                    factory = Factory
                )
                // Create an instance of the ArticleViewModel using the ArticleFactory
                val articleViewModel: ArticleViewModel = viewModel(
                    viewModelStoreOwner = profileGraphEntry,
                    factory = ArticleFactory
                )
                // Gat tab index from the arguments, defaulting to 0 if not provided
                val tabIndex = entry.arguments?.getInt("tabIndex") ?: 0
                // Pass the NavController and ViewModels to the MyProfileScreen composable
                MyProfileScreen(
                    navController = navController,
                    vm = tripViewModel,
                    vm2 = articleViewModel,
                    uvm = userViewModel,
                    rvm = reviewViewModel,
                    defaultTabIndex = tabIndex
                )
            }
        }


        composable(
            route = "user_profile/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.IntType
                }
            )
        ) { entry ->
            // Get the back stack entry for the profile graph
            val profileGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Profile.route)
            }
            // Create an instance of the TripViewModel using the Factory
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = profileGraphEntry,
                factory = Factory
            )
            // Create an instance of the UserViewModel using the Factory
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = profileGraphEntry,
                factory = Factory
            )
            // Create an instance of the ReviewViewModel using the Factory
            val reviewViewModel: ReviewViewModel = viewModel(
                viewModelStoreOwner = profileGraphEntry,
                factory = Factory
            )
            // Create an instance of the ArticleViewModel using the ArticleFactory
            val articleViewModel: ArticleViewModel = viewModel(
                viewModelStoreOwner = profileGraphEntry,
                factory = ArticleFactory
            )
            // Get the userId from the arguments, defaulting to -1 if not provided
            val userId = entry.arguments?.getInt("userId") ?: -1
            // Pass the NavController, ViewModels, userId to the UserProfileScreen composable
            UserProfileScreen(
                navController = navController,
                vm = tripViewModel,
                vm2 = articleViewModel,
                userId = userId,
                uvm = userViewModel,
                rvm = reviewViewModel
            )
        }

        // Define the composable for editing the profile
        composable("edit_profile") { entry ->
            // Get the back stack entry for the profile graph
            val profileGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Profile.route)
            }
            // Create an instance of the UserViewModel using the Factory
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = profileGraphEntry,
                factory = Factory
            )
            // Pass the NavController and ViewModels to the EditProfileScreen composable
            EditProfileScreen(
                navController = navController,
                uvm = userViewModel
            )
        }

        // Define the composable for the camera screen
        composable("camera") { entry ->
            // Get the back stack entry for the profile graph
            val profileGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Profile.route)
            }
            // Create an instance of the UserViewModel using the Factory
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = profileGraphEntry,
                factory = Factory
            )
            // Pass the NavController and UserViewModel to the CameraScreen composable
            CameraScreen(
                context = LocalContext.current,
                onImageCaptured = { uri ->
                    userViewModel.setProfileImageUri(uri)
                    navController.popBackStack()
                }
            )
        }

        // Define the composable for the trip details page
        composable("trip_details") { entry ->
            // Get the back stack entry for the profile graph
            val profileGraphEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Profile.route)
            }
            // Create an instance of the TripViewModel using the Factory
            val tripViewModel: TripViewModel = viewModel(
                viewModelStoreOwner = profileGraphEntry,
                factory = Factory
            )
            // Create an instance of the UserViewModel using the Factory
            val userViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = profileGraphEntry,
                factory = Factory
            )
            // Create an instance of the ReviewViewModel using the Factory
            val reviewViewModel: ReviewViewModel = viewModel(
                viewModelStoreOwner = profileGraphEntry,
                factory = Factory
            )
            // Create an instance of the NotificationViewModel using the NotificationFactory
            val notificationViewModel: NotificationViewModel = viewModel(
                viewModelStoreOwner = profileGraphEntry,
                factory = NotificationFactory
            )
            // Pass the NavController and ViewModels to the TripDetails composable
            TripDetails(
                navController = navController,
                vm = tripViewModel,
                owner = false,
                uvm = userViewModel,
                rvm = reviewViewModel,
                nvm = notificationViewModel
            )
        }
    }
}

// Profile photo composable function, used to display a user's profile picture or initials if the
// picture is not available.
// The small parameter determines the size of the profile photo.
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfilePhoto(modifier: Modifier = Modifier, user: User, small: Boolean = false) {
    // Use remember to hold the profile image URL state
    val profileImageUrl = remember { mutableStateOf<String?>(null) }
    // Get the initials from the user's first name and surname
    val initials = "${user.firstname.firstOrNull() ?: ""}${user.surname.firstOrNull() ?: ""}"

    // Asynchronous acquisition Firebase Storage URL
    LaunchedEffect(user.profilePictureUrl) {
        // If the user has a profile picture URL, try to load it
        if (!user.profilePictureUrl.isNullOrEmpty()) {
            try {
                // Use Firebase Storage to get the profile photo URL
                profileImageUrl.value = user.getProfilePhoto()
            } catch (e: Exception) {
                // Log the error if the profile photo fails to load
                Log.e("ProfilePhoto", "Failed to load profile photo", e)
                // Reset the profile image URL to null if loading fails
                profileImageUrl.value = null
            }
        }
    }

    // Determine the size of the profile photo based on the small parameter
    val size = if (small) 50.dp else 120.dp

    // Create a Box to center the content and apply a circular background
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .background(Color.Blue, shape = CircleShape)
    ) {
        when {
            // If you have a Firebase Storage URL，use GlideImage
            profileImageUrl.value != null -> {
                // Use GlideImage to load the profile picture from the URL
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

            else -> {
                // If no profile picture is available, display the initials
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

// Camera screen composable function, used to capture images using the device's camera.
@SuppressLint("ObsoleteSdkInt")
@Composable
fun CameraScreen(context: Context, onImageCaptured: (Uri?) -> Unit) {
    // LocalLifecycleOwner provides the current lifecycle owner for the camera
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    // Create a PreviewView to display the camera preview
    val previewView = remember { PreviewView(context) }
    // Create an ImageCapture use case to capture images
    val imageCapture = remember { ImageCapture.Builder().build() }

    // Use LaunchedEffect to set up the camera when the composable is first composed
    LaunchedEffect(Unit) {
        // ProcessCameraProvider is used to bind the camera lifecycle to the composable
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        // Add a listener to the camera provider future to set up the camera
        cameraProviderFuture.addListener(
            {
                // Get the camera provider instance
                val cameraProvider = cameraProviderFuture.get()
                // Set up the preview and bind it to the lifecycle
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                // Use the default back camera for the preview
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                // Bind the use cases to the lifecycle
                try {
                    // Unbind all use cases before binding new ones
                    cameraProvider.unbindAll()
                    // Bind the preview and image capture use cases to the lifecycle
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, imageCapture
                    )
                } catch (exc: Exception) {
                    // Log an error if the use case binding fails
                    Log.e("Camera", "Use case binding failed", exc)
                }
            },
            // Use the main executor to run the listener on the main thread
            ContextCompat.getMainExecutor(context)
        )
    }

    // Create a Box to hold the camera preview and capture button
    Box(Modifier.fillMaxSize()) {
        // Display the camera preview using AndroidView
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        // Create a button to capture images
        IconButton(
            // Capture an image when the button is clicked
            onClick = {
                // Create a name for the captured image using the current timestamp
                val name = SimpleDateFormat(
                    "yyyy-MM-dd-HH-mm-ss-SSS",
                    Locale.US
                ).format(System.currentTimeMillis())
                // Create content values for the captured image
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    // Set the relative path for Android Q and above
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
                    }
                }
                // Create output options for the image capture
                val outputOptions = ImageCapture.OutputFileOptions.Builder(
                    context.contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ).build()

                // Capture the image using the image capture use case
                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    // Handle the result of the image capture
                    object : ImageCapture.OnImageSavedCallback {
                        // Handle the error during image capture
                        override fun onError(exc: ImageCaptureException) {
                            Log.e("Camera", "Photo capture failed: ${exc.message}", exc)
                        }

                        // Handle the successful image capture
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            Log.d("Camera", "Photo capture succeeded: ${output.savedUri}")
                            onImageCaptured(output.savedUri)
                        }
                    }
                )
            },
            // Style the capture button
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color.Gray, CircleShape)
        ) {
            // Display a camera icon inside the capture button
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = "Capture",
                tint = Color.Black
            )
        }
    }
}


