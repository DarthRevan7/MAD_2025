package com.example.voyago.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.voyago.activities.ProfilePhoto
import com.example.voyago.model.User
import com.example.voyago.viewmodel.ArticleViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


//Composable that displays the detail screen of a single article.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    navController: NavController,
    articleId: Int,
    articleViewModel: ArticleViewModel
) {

    // Observe the article list from the ViewModel as state
    val articles by articleViewModel.articleList.collectAsState()

    // Try to find the article matching the given ID
    val article = articles.find { it.id == articleId }

    // State holders for dynamic content
    var author by remember { mutableStateOf<User?>(null) }                      // Author info (loaded later)
    var imageUrls by remember { mutableStateOf<List<String>>(emptyList()) }     // List of image URLs
    var viewCount by remember { mutableIntStateOf(0) }                          // View counter
    var showViewCount by remember { mutableStateOf(true) }                      // Toggles visibility of view count
    var viewIncremented by remember { mutableStateOf(false) }                   // Prevents view count from being incremented multiple times

    // LaunchedEffect ensures the block only runs once for the given article
    LaunchedEffect(article) {
        article?.let { art ->
            if (!viewIncremented) {
                // Increment view count on first load
                incrementViewCount(art.id ?: return@LaunchedEffect) { newCount ->
                    viewCount = newCount
                }
                viewIncremented = true
            }

            // Fetch author info from Firestore
            art.authorId?.let { authorId ->
                author = getUserFromFirestore(authorId)
            }

            // Try to fetch all associated photo URLs
            try {
                val urls = art.getAllPhotos()
                imageUrls = urls
            } catch (e: Exception) {
                Log.e("ArticleDetail", "Failed to get photo URLs", e)
            }
        }
    }

    // UI Layout with top app bar and content
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Button to toggle visibility of view count
                    IconButton(onClick = { showViewCount = !showViewCount }) {
                        Icon(
                            imageVector = if (showViewCount) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle view count"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        article?.let { art ->
            // Main content column, scrollable vertically
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .background(Color.White)
            ) {
                // Article Content Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Author Info and View count row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Author Profile
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Show profile photo or placeholder
                                author?.let { user ->
                                    ProfilePhoto(
                                        user = user,
                                        small = true,
                                        modifier = Modifier.size(40.dp)
                                    )
                                } ?: Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8E0E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "?",
                                        color = Color(0xFF6B5B95),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Display author name or loading placeholder
                                Text(
                                    text = author?.let { "${it.firstname} ${it.surname}" }
                                        ?: "Loading...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Show view count if enabled
                            if (showViewCount) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RemoveRedEye,
                                        contentDescription = "Views",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$viewCount views",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Article Title
                        Text(
                            text = art.title ?: "Untitled",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 32.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Article Text Content
                        Text(
                            text = art.text ?: "",
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = Color(0xFF333333)
                        )
                    }
                }

                // Article Images Section (if any)
                if (imageUrls.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Article Images (${imageUrls.size})",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        // Determine number of columns based on image count
                        val columns = when {
                            imageUrls.size == 1 -> 1
                            imageUrls.size <= 4 -> 2
                            else -> 3 // 超过4张图片时使用3列
                        }

                        // Grid display of images
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 600.dp) // Avoid overly long scroll
                        ) {
                            // Render each image using ArticleImageItem
                            items(imageUrls) { imageUrl ->
                                ArticleImageItem(
                                    imageUrl = imageUrl,
                                    modifier = Modifier
                                        .aspectRatio(1f) // Square thumbnails
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        } ?: run {
            // Fallback UI when article is not loaded yet
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()     // Loading indicator
            }
        }
    }
}

// Image display component for articles
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ArticleImageItem(
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    // Card container for image, with rounded corners and slight elevation
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // If image URL is valid, show the image using Glide
        if (imageUrl != null && imageUrl.isNotEmpty()) {
            GlideImage(
                model = imageUrl,                       // Load the image from the URL
                contentDescription = "Article Image",   // For accessibility
                modifier = Modifier.fillMaxSize(),      // Fill the card's space
                contentScale = ContentScale.Crop        // Crop image to fill the container
            )
        } else {
            // Fallback UI for missing or invalid image URL
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE0E0E0)),     // Light gray background
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Image",                // Placeholder text
                    color = Color.Gray      // Dim color for subtlety
                )
            }
        }
    }
}

// Suspended function to fetch a User object from Firestore using their user ID
suspend fun getUserFromFirestore(userId: Int): User? {
    return try {
        // Get an instance of Firestore database
        val db = FirebaseFirestore.getInstance()

        // Attempt to retrieve the user document from the "users" collection
        val document = db.collection("users")
            .document(userId.toString())    // Firestore document IDs are strings
            .get()                          // Initiates the get operation
            .await()                        // Suspends until data is fetched

        // If document exists, deserialize it into a User object
        if (document.exists()) {
            document.toObject(User::class.java)
        } else {
            // No document found for this user ID
            null
        }
    } catch (e: Exception) {
        // Catch and log any exception that occurs during Firestore access
        Log.e("ArticleDetail", "Failed to get user from Firestore", e)
        null    // Return null if an error occurs
    }
}

// Function to increment the view count of an article in Firestore
fun incrementViewCount(articleId: Int, onComplete: (Int) -> Unit) {
    // Get a Firestore database instance
    val db = FirebaseFirestore.getInstance()

    // Reference to the article document based on the given ID
    val articleRef = db.collection("articles").document(articleId.toString())

    // Step 1: Read the current view count from the document
    articleRef.get()
        .addOnSuccessListener { document ->
            // Get the current view count from the document; default to 0 if missing
            val currentViews = document.getLong("viewCount") ?: 0
            val newViews = currentViews + 1     // Increment the count

            // Step 2: Update the view count in the database
            articleRef.update("viewCount", newViews)
                .addOnSuccessListener {
                    // Update succeeded – invoke the callback with new value
                    onComplete(newViews.toInt())
                }
                .addOnFailureListener { e ->
                    // Failed to update – log error and return the current value
                    Log.e("ArticleDetail", "Failed to update view count", e)
                    onComplete(currentViews.toInt())
                }
        }
        .addOnFailureListener { e ->
            // Failed to retrieve article document – log and fallback to 0
            Log.e("ArticleDetail", "Failed to get article", e)
            onComplete(0)
        }
}