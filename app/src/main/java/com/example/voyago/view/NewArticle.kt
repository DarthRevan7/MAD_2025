package com.example.voyago.view

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.voyago.model.Article
import com.example.voyago.viewmodel.ArticleViewModel
import com.example.voyago.viewmodel.NotificationViewModel
import com.example.voyago.viewmodel.UserViewModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

// View for creating a new article
@Composable
fun CreateArticleScreen(
    navController: NavController,
    articleViewModel: ArticleViewModel,
    userViewModel: UserViewModel,
    nvm: NotificationViewModel
) {
    // States for title and content input fields
    var articleTitle by rememberSaveable { mutableStateOf("") }         // Article Title input
    var articleContent by rememberSaveable { mutableStateOf("") }       // Article Content input

    // Validation check flags (the content is valid)
    var articleContentCheck by rememberSaveable { mutableStateOf(false) }
    var articleTitleCheck by rememberSaveable { mutableStateOf(false) }

    // Track whether the user has interacted with the fields
    var articleContentTouched by rememberSaveable { mutableStateOf(false) }
    var articleTitleTouched by rememberSaveable { mutableStateOf(false) }

    // State to hold selected images (multiple image support)
    var selectedImageUris by rememberSaveable { mutableStateOf<List<Uri>>(emptyList()) }

    // State for loading UI and error handling
    var isLoading by rememberSaveable { mutableStateOf(false) }     // Controls loading state during article upload
    var showError by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    // For launching suspend functions in Compose
    val coroutineScope = rememberCoroutineScope()

    // Get current logged-in user from ViewModel
    val currentUser by userViewModel.loggedUser.collectAsState()

    // ActivityResultLauncher for selecting multiple images
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        // Save selected images into state
        selectedImageUris = uris
    }

    // Main layout container
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Article Title Input Field
        OutlinedTextField(
            value = articleTitle,
            onValueChange = { item ->
                articleTitle = item    // Update the input
                articleTitleTouched = true     // Set that the user interacted with the field
                // Validation: non-empty, not numeric-only, must contain letter or digit
                articleTitleCheck =
                    item.isNotBlank() && !item.isDigitsOnly() && item.any { it.isLetter() || it.isDigit() }
            },
            label = { Text("Article Title") },
            placeholder = { Text("Input") },
            isError = articleTitleTouched && !articleTitleCheck,    // Error check
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
                // Show a clear icon when the title is not empty
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

        // If there aren't errors in the title
        if (!articleTitleCheck && articleTitleTouched) {
            // Add a little space
            Spacer(modifier = Modifier.height(1.dp))

            // Show a message error
            Text("Empty or invalid field!", color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Article Content Input Field
        OutlinedTextField(
            value = articleContent,
            onValueChange = { item ->
                articleContent = item     // Update the input
                articleContentTouched = true        // Set that the user interacted with the field
                articleContentCheck =
                    item.isNotBlank() && !item.isDigitsOnly() // Validation check on the field
            },
            placeholder = { Text("Write here your article") }, // Placeholder text
            isError = articleContentTouched && !articleContentCheck,    // Error check
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

        // If there aren't errors in the article content
        if (!articleContentCheck && articleContentTouched) {
            // Add a little space
            Spacer(modifier = Modifier.height(1.dp))

            // Show message error
            Text("Empty or invalid field!", color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Image Picker Button
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },    // Opens image selection
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE8E0E9),
                contentColor = Color(0xFF6B5B95)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            // Add Icon
            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.size(24.dp)
            )

            // Spacer between icon and text
            Spacer(modifier = Modifier.width(8.dp))

            // Add images text
            Text(
                "Add Images",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Images Preview and Count
        if (selectedImageUris.isNotEmpty()) {

            // Text with the number of images selected
            Text(
                "${selectedImageUris.size} image(s) selected ✓",
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(top = 8.dp)
            )

            // Grid of selected images
            LazyVerticalGrid(
                columns = GridCells.Fixed(4), // Display 4 images per line
                modifier = Modifier
                    .padding(top = 8.dp)
                    .heightIn(max = 200.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedImageUris) { uri ->
                    Box {
                        // Async load each image
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected image",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Remove image button
                        IconButton(
                            onClick = {
                                selectedImageUris = selectedImageUris - uri
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.5f),
                                    CircleShape
                                )
                        ) {
                            // Remove image Icon
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove image",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bottom Action Buttons Row (Cancel & Publish)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cancel button (navigate back)
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

            // Publish button
            Button(
                onClick = {
                    // If there aren't errors in the title and the content of the article
                    if (articleTitleCheck && articleContentCheck) {
                        // Set isLoading as True
                        isLoading = true

                        // Launch a coroutine
                        coroutineScope.launch {
                            try {
                                // Upload each selected image and get its storage path
                                val photoPaths = selectedImageUris.mapNotNull { uri ->
                                    try {
                                        val imageFileName = "${UUID.randomUUID()}.jpg"
                                        val imageRef = FirebaseStorage.getInstance()
                                            .reference
                                            .child("articles/$imageFileName")

                                        imageRef.putFile(uri).await()
                                        Log.d("CreateArticle", "Image uploaded successfully")

                                        "articles/$imageFileName"
                                    } catch (e: Exception) {
                                        Log.e("CreateArticle", "Image upload failed", e)
                                        null// Ignore failed uploads
                                    }
                                }

                                // Create Article object with uploaded image paths
                                val newArticle = Article(
                                    id = null,
                                    title = articleTitle,
                                    text = articleContent,
                                    authorId = currentUser.id,
                                    date = System.currentTimeMillis(),
                                    photo = photoPaths,
                                    tags = emptyList(),
                                    viewCount = 0
                                )

                                // Publish the article via ViewModel
                                val publishedArticle = articleViewModel.publishArticle(newArticle)

                                // Notify other users about new article
                                val title = "${newArticle.title}"
                                val body = "New article!"
                                val notificationType = "ARTICLE"
                                val idLink = publishedArticle.id!!

                                userViewModel.getAllOtherUserIds(currentUser.id) { otherUsers ->
                                    otherUsers.forEach { userId ->
                                        nvm.sendNotificationToUser(
                                            userId,
                                            title,
                                            body,
                                            notificationType,
                                            idLink
                                        )
                                    }
                                }

                                // Navigate back after success
                                navController.popBackStack()

                            } catch (e: Exception) {
                                Log.e("CreateArticle", "Failed to publish article", e)

                                // Set Error Message
                                errorMessage = "Failed to publish article: ${e.message}"

                                // Show Error Message
                                showError = true
                            } finally {
                                // Set isLoading as False
                                isLoading = false
                            }
                        }
                    } else {
                        // Form validation failed
                        // Set Error Message
                        errorMessage = "Please fill in all required fields"

                        // Show Error Message
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
                // If isLoading is True
                if (isLoading) {
                    // Show the Loading indicator on the button
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    // Otherwise show the "Publish" text on the button
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