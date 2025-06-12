package com.example.voyago.view

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.voyago.model.Article
import com.example.voyago.model.User
import com.example.voyago.viewmodel.ArticleViewModel
import com.example.voyago.viewmodel.UserViewModel
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    navController: NavController,
    articleId: Int,
    articleViewModel: ArticleViewModel,
    userViewModel: UserViewModel
) {
    var article by remember { mutableStateOf<Article?>(null) }
    var author by remember { mutableStateOf<User?>(null) }
    var imageUrls by remember { mutableStateOf<List<String>>(emptyList()) }

    // Load article data
    LaunchedEffect(articleId) {
        // Get article by ID
        article = articleViewModel.articleList.first().find { it.id == articleId }

        // Get author info
        article?.authorId?.let { authorId ->
            author = userViewModel.getUserById(authorId)
        }

        // Get image URLs
        article?.let { art ->
            val urls = mutableListOf<String>()
            art.photo?.let { photo ->
                try {
                    urls.add(art.getPhoto() ?: "")
                } catch (e: Exception) {
                    // Handle error
                }
            }
            // Add more images if available in contentUrl or other fields
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
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Author Avatar
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8E0E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    author?.let { user ->
                                        val initials = "${user.firstname.firstOrNull() ?: ""}${user.surname.firstOrNull() ?: ""}"
                                        Text(
                                            text = initials,
                                            color = Color(0xFF6B5B95),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = author?.let { "${it.firstname} ${it.surname}" } ?: "Unknown Author",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Views Count
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
                                    text = "5000 views", // You can make this dynamic
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
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
                            // Add placeholder images for demo
                            items(3) { index ->
                                ArticleImageItem(imageUrl = null)
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
            // Placeholder for demo
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

// Extension function to get user by ID (add this to UserViewModel)
suspend fun UserViewModel.getUserById(userId: Int): User? {
    return try {
        // This should be implemented in your UserViewModel to fetch user from Firestore
        // For now, returning a placeholder
        User(
            id = userId,
            firstname = "Emily",
            surname = "Carter"
        )
    } catch (e: Exception) {
        null
    }
}