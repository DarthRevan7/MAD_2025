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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voyago.model.ChatRoom
import com.example.voyago.viewmodel.ChatViewModel
import com.example.voyago.viewmodel.UserViewModel


@Composable
fun ChatListScreen(
    chatViewModel: ChatViewModel,
    uvm: UserViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {

    val user by uvm.loggedUser.collectAsState()

    chatViewModel.fetchChatRoomsForUser(user.id)

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
                    chatViewModel = chatViewModel,
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
    chatRoom: ChatRoom,
    currentUserId: Int,
    onClick: () -> Unit
) {

    LaunchedEffect(chatRoom.id) {
        chatViewModel.fetchChatRoomName(chatRoom.id, currentUserId)
    }

    val chatRoomNames by chatViewModel.chatRoomNames.collectAsState()
    val chatRoomName = chatRoomNames[chatRoom.id] ?: "Chat"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (chatRoom.type == "group") Color(0xFF1976D2) else Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (chatRoom.type == "group") Icons.Default.Group else Icons.Default.Person,
                    contentDescription = "Chat Type Icon",
                    tint = Color.White
                )
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
        }
    }
}


//
//@Composable
//fun ChatScreen(
//    chatViewModel: ChatViewModel,
//    firebaseChatRoomViewModel: FirebaseChatRoomViewModel,
//    navController: NavController,
//    modifier: Modifier = Modifier
//) {
//    val privateChats = chatViewModel.filteredPrivateChats.value
//    val searchQuery = chatViewModel.searchQuery
//
//    //val onlineUsers by remember { mutableStateOf(firebaseChatRoomViewModel.onlineUsers) }
//
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // Search bar
//        OutlinedTextField(
//            value = searchQuery,
//            onValueChange = chatViewModel::updateSearch,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 12.dp),
//            label = { Text("Search chats") },
//            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
//            singleLine = true,
//            shape = RoundedCornerShape(24.dp)
//        )
//
//        LazyColumn(modifier = Modifier.weight(1f)) {
//            // Group Chats Header
//            item {
//                Text(
//                    "Group Chat - General Room",
//                    style = MaterialTheme.typography.titleMedium,
//                    modifier = Modifier.padding(vertical = 8.dp)
//                )
//            }
//
//            // Firebase Group Chat
//            val lastMessage = firebaseChatRoomViewModel.messages.lastOrNull()
//
//            item {
//                FirebaseGroupLastMessageItem(
//                    lastMessage = lastMessage,
//                    onClick = {
//                        // Navigate to group chat screen
//                        //navController.navigate("groupChat")
//                    }
//                )
//            }
//
//            // Private Chat Header
//            if (privateChats.isNotEmpty()) {
//                item {
//                    Text(
//                        "Private Chats",
//                        style = MaterialTheme.typography.titleMedium,
//                        modifier = Modifier.padding(vertical = 8.dp)
//                    )
//                }
//                items(privateChats) { privateChat ->
//                    PrivateChatItem(
//                        privateChat = privateChat,
//                        onClick = {
//                            // Handle private chat click
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun FirebaseGroupLastMessageItem(
//    lastMessage: FirebaseChatMessage?,
//    onClick: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//            .clickable { onClick() },
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp, vertical = 12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Group chat icon
//            Box(
//                modifier = Modifier
//                    .size(48.dp)
//                    .clip(CircleShape)
//                    .background(Color(0xFF2196F3)),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Group,
//                    contentDescription = "Group Chat Icon",
//                    tint = Color.White
//                )
//            }
//
//            Spacer(modifier = Modifier.width(12.dp))
//
//            Column(modifier = Modifier.weight(1f)) {
//                Text("General Room", fontWeight = FontWeight.Bold)
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = lastMessage?.let {
//                        if (it.type == "TEXT") "${it.senderName}: ${it.content}"
//                        else if (it.type == "IMAGE") "${it.senderName} sent an image"
//                        else "${it.senderName} sent a file"
//                    } ?: "No messages yet",
//                    style = MaterialTheme.typography.bodySmall,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//        }
//    }
//}
//
//
//@Composable
//fun FirebaseGroupUserItem(
//    onlineUser: OnlineUser,
//    onClick: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//            .clickable { onClick() },
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp, vertical = 12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Avatar or icon
//            Box(
//                modifier = Modifier
//                    .size(48.dp)
//                    .clip(CircleShape)
//                    .background(Color.Gray),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Person,
//                    contentDescription = "User Icon",
//                    tint = Color.White
//                )
//            }
//
//            Spacer(modifier = Modifier.width(12.dp))
//
//            Column(modifier = Modifier.weight(1f)) {
//                Text(onlineUser.name, fontWeight = FontWeight.Bold)
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    "Online",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = Color.Green
//                )
//            }
//        }
//    }
//}
//
//
//
//@Composable
//fun ChatGroupItem(chatGroup: ChatGroup, onClick: () -> Unit) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//            .clickable { onClick() },
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp, vertical = 12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Avatar or icon
//            Box(
//                modifier = Modifier
//                    .size(48.dp)
//                    .clip(CircleShape)
//                    .background(Color.Gray),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Group,
//                    contentDescription = "Group Icon",
//                    tint = Color.White
//                )
//            }
//
//            Spacer(modifier = Modifier.width(12.dp))
//
//            // Title and message column with weight to push badge to the end
//            Column(
//                modifier = Modifier.weight(1f)
//            ) {
//                Text(chatGroup.title, fontWeight = FontWeight.Bold)
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    chatGroup.lastMessage,
//                    style = MaterialTheme.typography.bodySmall,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//
//            // Unread badge pushed fully to the right
//            if (chatGroup.unreadCount > 0) {
//                Box(
//                    modifier = Modifier
//                        .padding(start = 8.dp)
//                        .background(Color(0xFF7E57C2), shape = CircleShape)
//                        .size(28.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = chatGroup.unreadCount.toString(),
//                        color = Color.White,
//                        style = MaterialTheme.typography.bodyMedium,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun PrivateChatItem(privateChat: PrivateChat, onClick: () -> Unit) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//            .clickable { onClick() },
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp, vertical = 12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(48.dp)
//                    .clip(CircleShape)
//                    .background(Color.LightGray),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(Icons.Default.Person, contentDescription = null)
//            }
//
//            Spacer(modifier = Modifier.width(12.dp))
//
//            Column(
//                modifier = Modifier.weight(1f)
//            ) {
//                Text(privateChat.username, fontWeight = FontWeight.Bold)
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    privateChat.lastMessage,
//                    style = MaterialTheme.typography.bodySmall,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//
//            if (privateChat.unreadCount > 0) {
//                Box(
//                    modifier = Modifier
//                        .padding(start = 8.dp)
//                        .background(Color(0xFF7E57C2), shape = CircleShape)
//                        .size(28.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = privateChat.unreadCount.toString(),
//                        color = Color.White,
//                        style = MaterialTheme.typography.bodyMedium,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//            }
//        }
//    }
//}
//
//
////@Preview(showBackground = true)
////@Composable
////fun ChatScreenFullPreview() {
////    val context = LocalContext.current
////    val navController = rememberNavController()
////
////    val dummyNotificationViewModel = NotificationViewModel()
////    val dummyUserViewModel = UserViewModel(UserModel())
////    val dummyChatViewModel = ChatViewModel(ChatModel(), UserModel(), TripModel())
////
////    MaterialTheme {
////        Scaffold(
////            topBar = {
////                TopBar(
////                    nvm = dummyNotificationViewModel,
////                    navController = navController,
////                    uvm = dummyUserViewModel
////                )
////            },
////            bottomBar = { BottomBar(navController) }
////        ) { innerPadding ->
////            ChatScreen(
////                chatViewModel = dummyChatViewModel,
////                navController = navController,
////                modifier = Modifier.padding(innerPadding)
////            )
////        }
////    }
////}
//
