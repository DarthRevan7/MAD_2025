package com.example.voyago.view


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voyago.model.Article
import com.example.voyago.viewmodel.ArticleViewModel
import com.example.voyago.viewmodel.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun CreateArticleScreen(
    navController: NavController,
    articleViewModel: ArticleViewModel,
    userViewModel: UserViewModel
) {
    var articleTitle by remember { mutableStateOf("") }
    var articleContent by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val currentUser by userViewModel.loggedUser.collectAsState()

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title Input
        OutlinedTextField(
            value = articleTitle,
            onValueChange = { articleTitle = it },
            label = { Text("Article Title") },
            placeholder = { Text("Input") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6B5B95),
                unfocusedBorderColor = Color(0xFFCCC2DC),
                focusedContainerColor = Color(0xFFE8E0E9),
                unfocusedContainerColor = Color(0xFFE8E0E9)
            ),
            shape = RoundedCornerShape(8.dp),
            trailingIcon = {
                if (articleTitle.isNotEmpty()) {
                    IconButton(onClick = { articleTitle = "" }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color.Gray
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content Input
        OutlinedTextField(
            value = articleContent,
            onValueChange = { articleContent = it },
            placeholder = { Text("Write here your article") },
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color(0xFFE8E0E9),
                unfocusedContainerColor = Color(0xFFE8E0E9)
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Add Images Button
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE8E0E9),
                contentColor = Color(0xFF6B5B95)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Add Images",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Show selected image indicator
        if (selectedImageUri != null) {
            Text(
                "Image selected ✓",
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cancel Button
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B5B95),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    "Cancel",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Publish Button
            Button(
                onClick = {
                    if (articleTitle.isNotBlank() && articleContent.isNotBlank()) {
                        isLoading = true
                        coroutineScope.launch {
                            try {

                                // RED: 简化保存，直接调用新的保存方法
                                // RED: 构建 Article，date 使用时间戳
                                val newArticle = Article(
                                    id = null,
                                    title = articleTitle,
                                    text = articleContent,
                                    authorId = currentUser.id,
                                    date = System.currentTimeMillis(),
                                    photo = selectedImageUri?.let { uri ->
                                        val imageRef = FirebaseStorage.getInstance()
                                            .reference
                                            .child("articles/${UUID.randomUUID()}.jpg")
                                        imageRef.putFile(uri).await()
                                        imageRef.downloadUrl.await().toString()
                                    },
                                    tags = emptyList(),
                                    viewCount = 0
                                )

                                // RED: 调用 ViewModel 的方法统一保存
                                articleViewModel.publishArticle(newArticle)

                                // Navigate back
                                navController.popBackStack()
                            } catch (e: Exception) {
                                errorMessage = "Failed to publish article: ${e.message}"
                                showError = true
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        errorMessage = "Please fill in all required fields"
                        showError = true
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B5B95),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(28.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Publish",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Error Dialog
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("OK")
                }
            }
        )
    }
}

// Helper function to upload image to Firebase Storage
suspend fun uploadImageToFirebase(uri: Uri): String {
    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("articles/${UUID.randomUUID()}.jpg")

    imageRef.putFile(uri).await()
    return imageRef.path
}

// Helper function to save article to Firestore
suspend fun saveArticleToFirestore(article: Article) {
    val db = FirebaseFirestore.getInstance()
    val articlesCollection = db.collection("articles")

    // Get next article ID
    val counterRef = db.collection("metadata").document("articleCounter")
    val newId = db.runTransaction { transaction ->
        val snapshot = transaction.get(counterRef)
        val lastId = snapshot.getLong("lastArticleId") ?: 0
        val newId = lastId + 1
        transaction.update(counterRef, "lastArticleId", newId)
        newId
    }.await()

    // Save article with new ID
    val articleWithId = article.copy(id = newId.toInt())
    articlesCollection.document(newId.toString()).set(articleWithId).await()
}