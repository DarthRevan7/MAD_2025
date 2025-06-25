package com.example.voyago.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.voyago.activities.ProfilePhoto
import com.example.voyago.model.ChatRoom
import com.example.voyago.model.Trip
import com.example.voyago.viewmodel.ChatViewModel
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel


@Composable
fun ChatListScreen(
    chatViewModel: ChatViewModel,
    tripViewModel: TripViewModel,
    uvm: UserViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {

    val user by uvm.loggedUser.collectAsState()

    LaunchedEffect(user.id) {
        chatViewModel.fetchChatRoomsForUser(user.id)
    }


    val chatRooms by chatViewModel.chatRooms.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Chats",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(chatRooms) { chatRoom ->
                ChatRoomItem(
                    userViewModel = uvm,
                    chatViewModel = chatViewModel,
                    tripViewModel = tripViewModel,
                    chatRoom = chatRoom,
                    currentUserId = user.id,
                    onClick = {
                        navController.navigate("chat/${chatRoom.id}")
                    }
                )
            }
        }
    }
}

@Composable
fun ChatRoomItem(
    chatViewModel: ChatViewModel,
    userViewModel: UserViewModel,
    tripViewModel: TripViewModel,
    chatRoom: ChatRoom,
    currentUserId: Int,
    onClick: () -> Unit
) {

    val chatRoomNames by chatViewModel.chatRoomNames.collectAsState()
    val chatRoomName = chatRoomNames[chatRoom.id] ?: "Chat"

    // Check if user has unread messages
    val hasUnreadMessages = chatRoom.usersNotRead.contains(currentUserId.toString())

    val tripFetched = rememberSaveable { mutableStateOf<Trip?>(null) }

    LaunchedEffect(chatRoom.id) {
        chatViewModel.fetchChatRoomName(chatRoom.id, currentUserId)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (chatRoom.type == "group") Color(0xFF1976D2) else Color(
                            0xFF4CAF50
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (chatRoom.type == "group") {
                    Log.d("TC1", "TripId To fetch= ${chatRoom.tripId}")
                    tripViewModel.getTripById(chatRoom.tripId.toInt()) { trip ->
                        if (trip != null) {
                            tripFetched.value = trip
                            trip
                        }
                        null
                    }

                    ProfilePhotoChatList(
                        modifier = Modifier,
                        trip = tripFetched.value,
                        small = true
                    )

                    if (tripFetched.value != null) {
                        Log.d("TC2", "TripId fetched= ${tripFetched.value!!.id}")

                    } else {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Group Chat Icon",
                            tint = Color.White
                        )
                    }


                } else if (chatRoom.type == "private" || chatRoom.type == "blocked") {
                    val userId = chatRoom.participants.find { it != currentUserId }
                    if (userId != null) {
                        val user = userViewModel.getUserData(userId).collectAsState(initial = null)
                        if (user.value != null) {
                            ProfilePhoto(
                                modifier = Modifier,
                                user = user.value!!,
                                small = true
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = chatRoomName, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chatRoom.lastMessage.ifEmpty { "No messages yet" },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 🔔 Show red dot if there are unread messages
            if (hasUnreadMessages) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "New Message",
                    tint = Color.Red,
                    modifier = Modifier
                        .size(30.dp)
                        .padding(start = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfilePhotoChatList(modifier: Modifier = Modifier, trip: Trip?, small: Boolean = false) {

    val profileImageUrl = remember { mutableStateOf<String?>(null) }

    // Asynchronous acquisition Firebase Storage URL
    LaunchedEffect(trip?.photo) {
        // If the user has a profile picture URL, try to load it
        if (!trip?.photo.isNullOrEmpty()) {
            try {
                if (profileImageUrl.value == null) {
                    // Use Firebase Storage to get the profile photo URL
                    profileImageUrl.value = trip?.getPhoto()
                }
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
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Group Chat Icon",
                    tint = Color.White
                )
            }
        }
    }
}

