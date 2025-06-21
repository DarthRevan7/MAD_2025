package com.example.voyago.view

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import coil.compose.AsyncImage
import com.example.voyago.model.Article
import com.example.voyago.viewmodel.ArticleViewModel
import com.example.voyago.viewmodel.UserViewModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.core.text.isDigitsOnly
import coil3.compose.AsyncImage
import com.example.voyago.viewmodel.NotificationViewModel

// 在 NewArticle.kt 中的修改部分


@Composable
fun CreateArticleScreen(
    navController: NavController,
    articleViewModel: ArticleViewModel,
    userViewModel: UserViewModel,
    nvm: NotificationViewModel
) {
    var articleTitle by remember { mutableStateOf("") }
    var articleContent by remember { mutableStateOf("") }

    var articleContentCheck by remember { mutableStateOf(false) }
    var articleTitleCheck by remember { mutableStateOf(false) }
    var articleContentTouched by remember { mutableStateOf(false) }
    var articleTitleTouched by remember { mutableStateOf(false) }

    // 🔥 修改为支持多张图片
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var isLoading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val currentUser by userViewModel.loggedUser.collectAsState()

    // 🔥 修改图片选择器支持多选（不限数量）
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        // 🔥 不限制图片数量
        selectedImageUris = uris
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
            onValueChange = { item -> articleTitle = item; articleTitleTouched = true;
                articleTitleCheck = item.isNotBlank() && !item.isDigitsOnly() && item.any{ it.isLetter() || it.isDigit()}},
            label = { Text("Article Title") },
            placeholder = { Text("Input") },
            isError = articleTitleTouched && !articleTitleCheck,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6B5B95),
                unfocusedBorderColor = Color(0xFFCCC2DC),
                focusedContainerColor = Color(0xFFE8E0E9),
                unfocusedContainerColor = Color(0xFFE8E0E9)
                //errorBorderColor = Color.Red
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

        if(!articleTitleCheck && articleTitleTouched) {
            Spacer(modifier = Modifier.height(1.dp))
            Text("Empty or invalid field!", color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content Input
        OutlinedTextField(
            value = articleContent,
            onValueChange = { item -> articleContent = item; articleContentTouched = true;
                articleContentCheck = item.isNotBlank() && !item.isDigitsOnly() },
            placeholder = { Text("Write here your article") },
            isError = articleContentTouched && !articleContentCheck,
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

        if(!articleContentCheck && articleContentTouched) {
            Spacer(modifier = Modifier.height(1.dp))
            Text("Empty or invalid field!", color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔥 修改按钮文字，移除数量限制
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
                "Add Images", // 🔥 移除 "(Max 3)" 限制
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // 🔥 显示选中的图片数量和预览
        if (selectedImageUris.isNotEmpty()) {
            Text(
                "${selectedImageUris.size} image(s) selected ✓",
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(top = 8.dp)
            )

            // 🔥 改进的图片预览，支持多行显示
            LazyVerticalGrid(
                columns = GridCells.Fixed(4), // 每行显示4张图片
                modifier = Modifier
                    .padding(top = 8.dp)
                    .heightIn(max = 200.dp), // 最大高度
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedImageUris) { uri ->
                    Box {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected image",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // 🔥 添加删除按钮
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
                    if (articleTitleCheck && articleContentCheck) {
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                Log.d("CreateArticle", "Starting to publish article...")

                                // 🔥 上传多张图片
                                val photoPaths = selectedImageUris.mapNotNull { uri ->
                                    try {
                                        Log.d("CreateArticle", "Uploading image...")
                                        val imageFileName = "${UUID.randomUUID()}.jpg"
                                        val imageRef = FirebaseStorage.getInstance()
                                            .reference
                                            .child("articles/$imageFileName")

                                        imageRef.putFile(uri).await()
                                        Log.d("CreateArticle", "Image uploaded successfully")

                                        "articles/$imageFileName"
                                    } catch (e: Exception) {
                                        Log.e("CreateArticle", "Image upload failed", e)
                                        null
                                    }
                                }

                                // 🔥 构建 Article 对象
                                val newArticle = Article(
                                    id = null,
                                    title = articleTitle,
                                    text = articleContent,
                                    authorId = currentUser.id,
                                    date = System.currentTimeMillis(),
                                    photo = photoPaths, // 🔥 传递图片路径列表
                                    tags = emptyList(),
                                    viewCount = 0
                                )

                                Log.d("CreateArticle", "Publishing article: $newArticle")

                                val publishedArticle = articleViewModel.publishArticle(newArticle)

                                Log.d("CreateArticle", "Article published successfully")

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

                                navController.popBackStack()

                            } catch (e: Exception) {
                                Log.e("CreateArticle", "Failed to publish article", e)
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