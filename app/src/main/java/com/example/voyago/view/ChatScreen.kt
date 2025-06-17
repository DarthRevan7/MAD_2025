package com.example.voyago.view

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.voyago.activities.BottomBar
import com.example.voyago.activities.TopBar
import com.example.voyago.model.ChatGroup
import com.example.voyago.model.ChatModel
import com.example.voyago.model.PrivateChat
import com.example.voyago.model.TripModel
import com.example.voyago.model.UserModel
import com.example.voyago.viewmodel.ChatViewModel
import com.example.voyago.viewmodel.NotificationViewModel
import com.example.voyago.viewmodel.UserViewModel


@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val groupChats = chatViewModel.filteredChatGroups.value
    val privateChats = chatViewModel.filteredPrivateChats.value
    val searchQuery = chatViewModel.searchQuery

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = chatViewModel::updateSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            label = { Text("Search chats") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(24.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            // Group Chat Header
            if (groupChats.isNotEmpty()) {
                item {
                    Text(
                        "Group Chats",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(groupChats) { groupChat ->
                    ChatGroupItem(chatGroup = groupChat, onClick = { /* Handle group click */ })
                }
            }

            // Private Chat Header
            if (privateChats.isNotEmpty()) {
                item {
                    Text(
                        "Private Chats",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(privateChats) { privateChat ->
                    PrivateChatItem(
                        privateChat = privateChat,
                        onClick = { /* Handle private click */ })
                }
            }
        }
    }
}


@Composable
fun ChatGroupItem(chatGroup: ChatGroup, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar or icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Group Icon",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title and message column with weight to push badge to the end
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(chatGroup.title, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    chatGroup.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Unread badge pushed fully to the right
            if (chatGroup.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .background(Color(0xFF7E57C2), shape = CircleShape)
                        .size(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chatGroup.unreadCount.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
fun PrivateChatItem(privateChat: PrivateChat, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(privateChat.username, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    privateChat.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (privateChat.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .background(Color(0xFF7E57C2), shape = CircleShape)
                        .size(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = privateChat.unreadCount.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ChatScreenFullPreview() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val dummyNotificationViewModel = NotificationViewModel()
    val dummyUserViewModel = UserViewModel(UserModel())
    val dummyChatViewModel = ChatViewModel(ChatModel(), UserModel(), TripModel())

    MaterialTheme {
        Scaffold(
            topBar = {
                TopBar(
                    nvm = dummyNotificationViewModel,
                    navController = navController,
                    uvm = dummyUserViewModel
                )
            },
            bottomBar = { BottomBar(navController) }
        ) { innerPadding ->
            ChatScreen(
                chatViewModel = dummyChatViewModel,
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

