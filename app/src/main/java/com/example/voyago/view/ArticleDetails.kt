package com.example.voyago.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.voyago.model.Article
import com.example.voyago.model.User
import com.example.voyago.viewmodel.ArticleViewModel
import com.example.voyago.viewmodel.UserViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    navController: NavController,
    articleId: Int,
    articleViewModel: ArticleViewModel,
    userViewModel: UserViewModel
) {
    val articles by articleViewModel.articleList.collectAsState()
    val article = articles.find { it.id == articleId }

    var author by remember { mutableStateOf<User?>(null) }
    var imageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var viewCount by remember { mutableStateOf(0) }
    var showViewCount by remember { mutableStateOf(true) }
    var viewIncremented by remember { mutableStateOf(false) }

    // 副作用只触发一次
    LaunchedEffect(article) {
        article?.let { art ->
            if (!viewIncremented) {
                incrementViewCount(art.id ?: return@LaunchedEffect) { newCount ->
                    viewCount = newCount
                }
                viewIncremented = true
            }

            art.authorId?.let { authorId ->
                author = getUserFromFirestore(authorId)
            }

            val urls = mutableListOf<String>()
            art.photo?.let { photo ->
                try {
                    val url = art.getPhoto()
                    Log.d("ArticleDetail", "getPhoto resolved: $url")
                    if (!url.isNullOrEmpty()) {
                        urls.add(url)
                    }
                } catch (e: Exception) {
                    Log.e("ArticleDetail", "Failed to get photo URL", e)
                }
            }
            imageUrls = urls
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Toggle view count visibility
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
                        // Author Info and Views
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Author Info
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Author Avatar with ProfilePhoto
                                author?.let { user ->
                                    ProfilePhoto(
                                        user = user,
                                        small = true,
                                        modifier = Modifier.size(40.dp),
                                        uvm = userViewModel
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

                                Text(
                                    text = author?.let { "${it.firstname} ${it.surname}" } ?: "Loading...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Views Count (conditionally shown)
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

                        // Article Content
                        Text(
                            text = art.text ?: "",
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = Color(0xFF333333)
                        )
                    }
                }

                // Article Images Section
                if (imageUrls.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Article Images",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(imageUrls) { imageUrl ->
                                ArticleImageItem(imageUrl = imageUrl)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        } ?: run {
            // Loading or error state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ArticleImageItem(imageUrl: String?) {
    Card(
        modifier = Modifier
            .size(150.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (imageUrl != null && imageUrl.isNotEmpty()) {
            GlideImage(
                model = imageUrl,
                contentDescription = "Article Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Image",
                    color = Color.Gray
                )
            }
        }
    }
}

// Function to get user from Firestore
suspend fun getUserFromFirestore(userId: Int): User? {
    return try {
        val db = FirebaseFirestore.getInstance()
        val document = db.collection("users")
            .document(userId.toString())
            .get()
            .await()

        if (document.exists()) {
            document.toObject(User::class.java)
        } else {
            null
        }
    } catch (e: Exception) {
        Log.e("ArticleDetail", "Failed to get user from Firestore", e)
        null
    }
}

// Function to increment view count
fun incrementViewCount(articleId: Int, onComplete: (Int) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val articleRef = db.collection("articles").document(articleId.toString())

    // First, get current view count
    articleRef.get()
        .addOnSuccessListener { document ->
            val currentViews = document.getLong("viewCount") ?: 0
            val newViews = currentViews + 1

            // Update view count
            articleRef.update("viewCount", newViews)
                .addOnSuccessListener {
                    onComplete(newViews.toInt())
                }
                .addOnFailureListener { e ->
                    Log.e("ArticleDetail", "Failed to update view count", e)
                    onComplete(currentViews.toInt())
                }
        }
        .addOnFailureListener { e ->
            Log.e("ArticleDetail", "Failed to get article", e)
            onComplete(0)
        }
}