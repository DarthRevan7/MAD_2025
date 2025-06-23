package com.example.voyago.view

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.voyago.formatMessageTimestamp
import com.example.voyago.model.FirebaseChatMessage
import com.example.voyago.model.User
import com.example.voyago.toCalendar
import com.example.voyago.viewmodel.ChatViewModel
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SingleChatScreen(
    chatViewModel: ChatViewModel,
    roomId: String,
    uvm: UserViewModel,
    tripViewModel: TripViewModel,
    navController: NavController,
    onBack: () -> Unit
) {
    val messages by chatViewModel.messages.collectAsState()
    val user by uvm.loggedUser.collectAsState()
    val senderNames by chatViewModel.senderNames.collectAsState()
    val chatRoom by chatViewModel.chatRoom.collectAsState()


    // Fetch messages and room name when screen shows
    LaunchedEffect(roomId) {
        chatViewModel.fetchMessagesForRoom(roomId)
        chatViewModel.fetchChatRoomName(roomId, user.id)
        chatViewModel.removeUserFromUsersNotRead(roomId, user.id.toString())
        chatViewModel.getChatRoomFromId(roomId)
    }

    val chatRoomNames by chatViewModel.chatRoomNames.collectAsState()
    val chatRoomName = chatRoomNames[roomId] ?: "Chat"
    val chatRoomTypes by chatViewModel.chatRoomTypes.collectAsState()
    val chatRoomType = chatRoomTypes[roomId] ?: "private"

    var newMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {

        // Custom Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            val coroutineScope = rememberCoroutineScope()

            Text(
                text = chatRoomName.ifBlank { "Chat" },
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable(enabled = chatRoomType == "group" || chatRoomType == "private") {
                        if (chatRoomType == "group") {
                            coroutineScope.launch {
                                val trip = tripViewModel.getTripByTitle(chatRoomName)
                                if (trip != null) {
                                    navController.navigate("chat_details/${trip.id}")
                                }
                            }
                        }
                        else if(chatRoomType == "private")
                        {
                            var userId = 0
                            chatRoom?.participants?.forEach{
                                    if(it != user.id){
                                    userId = it
                                }
                            }
                            if(userId != 0)
                            {
                                navController.navigate("user_profile/${userId}")
                            }

                        }
                    }
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            reverseLayout = false
        ) {
            items(messages) { message ->
                val isOwnMessage = message.senderId == user.id.toString()
                val senderName = senderNames[message.senderId] ?: "..."

                ChatMessage(isOwnMessage, senderName, message)
            }
        }

        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .background(Color.LightGray.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") },
                singleLine = true
            )
            IconButton(
                onClick = {
                    if (newMessage.isNotBlank()) {
                        chatViewModel.sendMessage(
                            roomId,
                            FirebaseChatMessage(
                                senderId = user.id.toString(),
                                content = newMessage
                            )
                        )
                        newMessage = ""
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
fun ChatMessage(isOwnMessage: Boolean,
                senderName: String,
                message: FirebaseChatMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
    ) {
        // Show sender name only if it's NOT from the logged-in user
        if (!isOwnMessage) {
            Text(
                text = senderName,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }

        Text(
            text = message.content,
            modifier = Modifier
                .background(
                    if (isOwnMessage) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            color = Color.White
        )

        Text(
            text = formatMessageTimestamp(message.timestamp)
        )
    }
}






// Firebase chat message data class
//data class FirebaseChatMessage(
//    val id: String = "",
//    val content: String = "",
//    val senderId: String = "",
//    val senderUserId: Int = 0, // User ID from your project
//    val senderName: String = "",
//    val senderUsername: String = "",
//    val senderAvatar: String = "",
//    val timestamp: Long = System.currentTimeMillis(),
//    val type: String = "TEXT", // TEXT, IMAGE, FILE
//    val imageUrl: String? = null,
//    val fileName: String? = null,
//    val chatRoomId: String = "general" // Chat room ID
//) {
//    // Firestore requires no-argument constructor
//    constructor() : this("", "", "", 0, "", "", "", 0L, "TEXT", null, null, "general")
//
//    fun toMap(): Map<String, Any?> {
//        return mapOf(
//            "id" to id,
//            "content" to content,
//            "senderId" to senderId,
//            "senderUserId" to senderUserId,
//            "senderName" to senderName,
//            "senderUsername" to senderUsername,
//            "senderAvatar" to senderAvatar,
//            "timestamp" to timestamp,
//            "type" to type,
//            "imageUrl" to imageUrl,
//            "fileName" to fileName,
//            "chatRoomId" to chatRoomId
//        )
//    }
//}
//
//// Firebase online user data class
//data class OnlineUser(
//    val id: String = "",
//    val userId: Int = 0, // User ID from your project
//    val name: String = "",
//    val username: String = "",
//    val avatar: String = "",
//    val lastSeen: Long = System.currentTimeMillis(),
//    val isOnline: Boolean = false
//) {
//    constructor() : this("", 0, "", "", "", 0L, false)
//
//    fun toMap(): Map<String, Any> {
//        return mapOf(
//            "id" to id,
//            "userId" to userId,
//            "name" to name,
//            "username" to username,
//            "avatar" to avatar,
//            "lastSeen" to lastSeen,
//            "isOnline" to isOnline
//        )
//    }
//}

// Firebase chat room ViewModel
//class FirebaseChatRoomViewModel(
//    private val currentUser: User? = null // Pass your project's User object
//) {
//    private val firestore = FirebaseFirestore.getInstance()
//    private val storage = FirebaseStorage.getInstance()
//    private val auth = FirebaseAuth.getInstance()
//
//    private val _messages = mutableStateListOf<FirebaseChatMessage>()
//    val messages: List<FirebaseChatMessage> = _messages
//
//    private val _onlineUsers = mutableStateListOf<OnlineUser>()
//    val onlineUsers: List<OnlineUser> = _onlineUsers
//
//    private var messagesListener: ListenerRegistration? = null
//    private var usersListener: ListenerRegistration? = null
//
//    private val chatRoomId = "general" // Can be dynamically set for different chat rooms
//
//    init {
//        setupRealtimeListeners()
//        updateUserOnlineStatus(true)
//    }
//
//    /**
//     * Setup real-time listeners
//     */
//    private fun setupRealtimeListeners() {
//        // Listen for message changes
//        messagesListener = firestore
//            .collection("chatRooms")
//            .document(chatRoomId)
//            .collection("messages")
//            .orderBy("timestamp", Query.Direction.ASCENDING)
//            .addSnapshotListener { snapshot, error ->
//                if (error != null) {
//                    return@addSnapshotListener
//                }
//
//                val newMessages = snapshot?.documents?.mapNotNull { doc ->
//                    doc.toObject(FirebaseChatMessage::class.java)?.copy(id = doc.id)
//                } ?: emptyList()
//
//                _messages.clear()
//                _messages.addAll(newMessages)
//            }
//
//        // Listen for online users changes
//        usersListener = firestore
//            .collection("chatRooms")
//            .document(chatRoomId)
//            .collection("onlineUsers")
//            .addSnapshotListener { snapshot, error ->
//                if (error != null) {
//                    return@addSnapshotListener
//                }
//
//                val users = snapshot?.documents?.mapNotNull { doc ->
//                    doc.toObject(OnlineUser::class.java)?.copy(id = doc.id)
//                } ?: emptyList()
//
//                _onlineUsers.clear()
//                _onlineUsers.addAll(users.filter {
//                    System.currentTimeMillis() - it.lastSeen < 5 * 60 * 1000 // Active within 5 minutes
//                })
//            }
//    }
//
//    /**
//     * Send text message
//     */
//    suspend fun sendTextMessage(content: String) {
//        val firebaseUser = auth.currentUser ?: return
//        val user = currentUser ?: return
//
//        val message = FirebaseChatMessage(
//            id = UUID.randomUUID().toString(),
//            content = content,
//            senderId = firebaseUser.uid,
//            senderUserId = user.id,
//            senderName = "${user.firstname} ${user.surname}",
//            senderUsername = user.username,
//            senderAvatar = user.profilePictureUrl ?: "",
//            timestamp = System.currentTimeMillis(),
//            type = "TEXT",
//            chatRoomId = chatRoomId
//        )
//
//        try {
//            firestore
//                .collection("chatRooms")
//                .document(chatRoomId)
//                .collection("messages")
//                .document(message.id)
//                .set(message.toMap())
//                .await()
//        } catch (e: Exception) {
//            // Error handling
//        }
//    }
//
//    /**
//     * Send image message
//     */
//    suspend fun sendImageMessage(imageUri: Uri, context: Context) {
//        val firebaseUser = auth.currentUser ?: return
//        val user = currentUser ?: return
//        val imageRef = storage.reference
//            .child("chat_images/${UUID.randomUUID()}.jpg")
//
//        try {
//            // Upload image to Firebase Storage
//            val uploadTask = imageRef.putFile(imageUri).await()
//            val downloadUrl = imageRef.downloadUrl.await()
//
//            val message = FirebaseChatMessage(
//                id = UUID.randomUUID().toString(),
//                content = "Sent an image",
//                senderId = firebaseUser.uid,
//                senderUserId = user.id,
//                senderName = "${user.firstname} ${user.surname}",
//                senderUsername = user.username,
//                senderAvatar = user.profilePictureUrl ?: "",
//                timestamp = System.currentTimeMillis(),
//                type = "IMAGE",
//                imageUrl = downloadUrl.toString(),
//                chatRoomId = chatRoomId
//            )
//
//            firestore
//                .collection("chatRooms")
//                .document(chatRoomId)
//                .collection("messages")
//                .document(message.id)
//                .set(message.toMap())
//                .await()
//        } catch (e: Exception) {
//            // Error handling
//        }
//    }
//
//    /**
//     * Update user online status
//     */
//    private fun updateUserOnlineStatus(isOnline: Boolean) {
//        val firebaseUser = auth.currentUser ?: return
//        val user = currentUser ?: return
//
//        val onlineUser = OnlineUser(
//            id = firebaseUser.uid,
//            userId = user.id,
//            name = "${user.firstname} ${user.surname}",
//            username = user.username,
//            avatar = user.profilePictureUrl ?: "",
//            lastSeen = System.currentTimeMillis(),
//            isOnline = isOnline
//        )
//
//        firestore
//            .collection("chatRooms")
//            .document(chatRoomId)
//            .collection("onlineUsers")
//            .document(firebaseUser.uid)
//            .set(onlineUser.toMap())
//    }
//
//    /**
//     * Cleanup listeners
//     */
//    fun cleanup() {
//        messagesListener?.remove()
//        usersListener?.remove()
//        updateUserOnlineStatus(false)
//    }
//
//    fun getCurrentUserId(): String? = auth.currentUser?.uid
//    fun getCurrentUser(): User? = currentUser
//}
//
///**
// * Firebase real-time chat room interface
// * @param currentUser Current logged-in user (from your UserViewModel)
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun FirebaseChatRoomScreen(
//    currentUser: User? = null,
//    onBackClick: (() -> Unit)? = null
//) {
//    val viewModel = remember { FirebaseChatRoomViewModel(currentUser) }
//    var inputText by remember { mutableStateOf("") }
//    var showEmojiPicker by remember { mutableStateOf(false) }
//    var showAttachmentMenu by remember { mutableStateOf(false) }
//
//    val listState = rememberLazyListState()
//    val coroutineScope = rememberCoroutineScope()
//    val context = LocalContext.current
//
//    // Auto-scroll to latest message
//    LaunchedEffect(viewModel.messages.size) {
//        if (viewModel.messages.isNotEmpty()) {
//            listState.animateScrollToItem(viewModel.messages.size - 1)
//        }
//    }
//
//    // Cleanup listeners
//    DisposableEffect(Unit) {
//        onDispose {
//            viewModel.cleanup()
//        }
//    }
//
//    // Image picker
//    val imagePickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let {
//            coroutineScope.launch {
//                viewModel.sendImageMessage(it, context)
//            }
//        }
//        showAttachmentMenu = false
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFF5F5F5))
//    ) {
//        // Top app bar
//        TopAppBar(
//            title = {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "Chat Room",
//                        fontWeight = FontWeight.Bold
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        text = "${viewModel.onlineUsers.size} online",
//                        fontSize = 12.sp,
//                        color = Color.White.copy(alpha = 0.7f)
//                    )
//                }
//            },
//            navigationIcon = {
//                onBackClick?.let { callback ->
//                    IconButton(onClick = callback) {
//                        Icon(
//                            Icons.Default.ArrowBack,
//                            contentDescription = "Back",
//                            tint = Color.White
//                        )
//                    }
//                }
//            },
//            actions = {
//                IconButton(onClick = { /* Open settings */ }) {
//                    Icon(
//                        Icons.Default.Settings,
//                        contentDescription = "Settings",
//                        tint = Color.White
//                    )
//                }
//            },
//            colors = TopAppBarDefaults.topAppBarColors(
//                containerColor = MaterialTheme.colorScheme.primary,
//                titleContentColor = Color.White
//            )
//        )
//
//        // Online users list
//        if (viewModel.onlineUsers.isNotEmpty()) {
//            LazyRow(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(Color.White)
//                    .padding(8.dp),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                items(viewModel.onlineUsers) { user ->
//                    OnlineUserItemFirebase(user = user)
//                }
//            }
//        }
//
//        // Messages list
//        LazyColumn(
//            state = listState,
//            modifier = Modifier
//                .weight(1f)
//                .fillMaxWidth()
//                .padding(horizontal = 8.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp),
//            contentPadding = PaddingValues(vertical = 8.dp)
//        ) {
//            items(viewModel.messages) { message ->
//                FirebaseMessageItem(
//                    message = message,
//                    isFromCurrentUser = message.senderId == viewModel.getCurrentUserId()
//                )
//            }
//        }
//
//        // Emoji picker
//        if (showEmojiPicker) {
//            EmojiPicker(
//                onEmojiSelected = { emoji ->
//                    inputText += emoji
//                    showEmojiPicker = false
//                },
//                onDismiss = { showEmojiPicker = false }
//            )
//        }
//
//        // Attachment menu
//        if (showAttachmentMenu) {
//            AttachmentMenu(
//                onImageClick = { imagePickerLauncher.launch("image/*") },
//                onFileClick = { /* Implement file upload */ },
//                onDismiss = { showAttachmentMenu = false }
//            )
//        }
//
//        // Input area
//        Surface(
//            modifier = Modifier.fillMaxWidth(),
//            shadowElevation = 8.dp,
//            color = Color.White
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                verticalAlignment = Alignment.Bottom
//            ) {
//                // Attachment button
//                IconButton(
//                    onClick = { showAttachmentMenu = !showAttachmentMenu }
//                ) {
//                    Icon(
//                        Icons.Default.Add,
//                        contentDescription = "Attachments",
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                }
//
//                // Input field
//                OutlinedTextField(
//                    value = inputText,
//                    onValueChange = { inputText = it },
//                    modifier = Modifier
//                        .weight(1f)
//                        .padding(horizontal = 8.dp),
//                    placeholder = { Text("Type a message...") },
//                    shape = RoundedCornerShape(24.dp),
//                    maxLines = 3,
//                    trailingIcon = {
//                        IconButton(
//                            onClick = { showEmojiPicker = !showEmojiPicker }
//                        ) {
//                            Icon(
//                                Icons.Default.Face,
//                                contentDescription = "Emoji",
//                                tint = MaterialTheme.colorScheme.primary
//                            )
//                        }
//                    }
//                )
//
//                // Send button
//                FloatingActionButton(
//                    onClick = {
//                        if (inputText.trim().isNotEmpty()) {
//                            coroutineScope.launch {
//                                viewModel.sendTextMessage(inputText.trim())
//                                inputText = ""
//                            }
//                        }
//                    },
//                    modifier = Modifier.size(48.dp),
//                    containerColor = MaterialTheme.colorScheme.primary
//                ) {
//                    Icon(
//                        Icons.Default.Send,
//                        contentDescription = "Send",
//                        tint = Color.White
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun OnlineUserItemFirebase(user: OnlineUser) {
//    var avatarUrl by remember { mutableStateOf<String?>(null) }
//
//    // Asynchronously get avatar URL from your database
//    LaunchedEffect(user.avatar) {
//        try {
//            if (user.avatar.isNotEmpty()) {
//                // Create temporary User object to get avatar from your system
//                val tempUser = User(
//                    id = user.userId,
//                    profilePictureUrl = user.avatar
//                )
//                avatarUrl = tempUser.getProfilePhoto()
//            }
//        } catch (e: Exception) {
//            avatarUrl = null
//        }
//    }
//
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier.clickable { /* Click to view user info */ }
//    ) {
//        Box {
//            // Show user initials if no avatar available
//            if (avatarUrl != null) {
//                AsyncImage(
//                    model = avatarUrl,
//                    contentDescription = user.name,
//                    modifier = Modifier
//                        .size(40.dp)
//                        .clip(CircleShape)
//                        .border(2.dp, Color.Gray, CircleShape),
//                    contentScale = ContentScale.Crop
//                )
//            } else {
//                // Show initials as fallback
//                Box(
//                    contentAlignment = Alignment.Center,
//                    modifier = Modifier
//                        .size(40.dp)
//                        .background(MaterialTheme.colorScheme.primary, CircleShape)
//                        .border(2.dp, Color.Gray, CircleShape)
//                ) {
//                    Text(
//                        text = getInitials(user.name),
//                        color = Color.White,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//            }
//
//            if (user.isOnline) {
//                Box(
//                    modifier = Modifier
//                        .size(12.dp)
//                        .background(Color.Green, CircleShape)
//                        .align(Alignment.BottomEnd)
//                        .border(2.dp, Color.White, CircleShape)
//                )
//            }
//        }
//
//        Text(
//            text = user.username,
//            fontSize = 10.sp,
//            color = Color.Gray,
//            modifier = Modifier.padding(top = 4.dp)
//        )
//    }
//}
//
//@Composable
//fun FirebaseMessageItem(
//    message: FirebaseChatMessage,
//    isFromCurrentUser: Boolean
//) {
//    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
//    val timeString = timeFormat.format(Date(message.timestamp))
//
//    Column(
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        if (isFromCurrentUser) {
//            // Current user message - right aligned
//            Column(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalAlignment = Alignment.End
//            ) {
//                Row(
//                    verticalAlignment = Alignment.Bottom
//                ) {
//                    Text(
//                        text = timeString,
//                        fontSize = 12.sp,
//                        color = Color.Gray,
//                        modifier = Modifier.padding(end = 8.dp, bottom = 4.dp)
//                    )
//
//                    FirebaseMessageBubble(
//                        message = message,
//                        isFromCurrentUser = true
//                    )
//                }
//            }
//        } else {
//            // Other users' messages - left aligned
//            Column(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalAlignment = Alignment.Start
//            ) {
//                Row(
//                    verticalAlignment = Alignment.Top,
//                    modifier = Modifier.padding(bottom = 4.dp)
//                ) {
//                    UserAvatarAsync(
//                        user = User(
//                            id = message.senderUserId,
//                            profilePictureUrl = message.senderAvatar
//                        ),
//                        size = 32.dp
//                    )
//
//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    Column {
//                        Text(
//                            text = message.senderUsername,
//                            fontSize = 12.sp,
//                            color = Color.Gray,
//                            fontWeight = FontWeight.Medium
//                        )
//
//                        Spacer(modifier = Modifier.height(4.dp))
//
//                        Row(
//                            verticalAlignment = Alignment.Bottom
//                        ) {
//                            FirebaseMessageBubble(
//                                message = message,
//                                isFromCurrentUser = false
//                            )
//
//                            Text(
//                                text = timeString,
//                                fontSize = 12.sp,
//                                color = Color.Gray,
//                                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun UserAvatarAsync(
//    user: User,
//    size: androidx.compose.ui.unit.Dp
//) {
//    var avatarUrl by remember { mutableStateOf<String?>(null) }
//
//    LaunchedEffect(user.profilePictureUrl) {
//        try {
//            if (!user.profilePictureUrl.isNullOrEmpty()) {
//                avatarUrl = user.getProfilePhoto()
//            }
//        } catch (e: Exception) {
//            avatarUrl = null
//        }
//    }
//
//    // Show user avatar or initials
//    if (avatarUrl != null) {
//        AsyncImage(
//            model = avatarUrl,
//            contentDescription = "${user.firstname} ${user.surname}",
//            modifier = Modifier
//                .size(size)
//                .clip(CircleShape),
//            contentScale = ContentScale.Crop
//        )
//    } else {
//        // Show initials as fallback
//        Box(
//            contentAlignment = Alignment.Center,
//            modifier = Modifier
//                .size(size)
//                .background(MaterialTheme.colorScheme.primary, CircleShape)
//        ) {
//            Text(
//                text = getInitials("${user.firstname} ${user.surname}"),
//                color = Color.White,
//                fontSize = (size.value * 0.4).sp,
//                fontWeight = FontWeight.Bold
//            )
//        }
//    }
//}
//
//// Helper function to get user initials
//fun getInitials(fullName: String): String {
//    if (fullName.isBlank()) return "?"
//
//    return fullName.trim()
//        .split(" ")
//        .filter { it.isNotBlank() }
//        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
//        .take(2)
//        .joinToString("")
//        .takeIf { it.isNotEmpty() } ?: "?"
//}
//
//@Composable
//fun FirebaseMessageBubble(
//    message: FirebaseChatMessage,
//    isFromCurrentUser: Boolean
//) {
//    val backgroundColor = if (isFromCurrentUser) {
//        MaterialTheme.colorScheme.primary
//    } else {
//        Color.White
//    }
//
//    val textColor = if (isFromCurrentUser) {
//        Color.White
//    } else {
//        Color.Black
//    }
//
//    Surface(
//        shape = RoundedCornerShape(
//            topStart = 18.dp,
//            topEnd = 18.dp,
//            bottomStart = if (isFromCurrentUser) 18.dp else 4.dp,
//            bottomEnd = if (isFromCurrentUser) 4.dp else 18.dp
//        ),
//        color = backgroundColor,
//        shadowElevation = if (isFromCurrentUser) 0.dp else 1.dp,
//        modifier = Modifier.widthIn(max = 280.dp)
//    ) {
//        when (message.type) {
//            "TEXT" -> {
//                SelectionContainer {
//                    Text(
//                        text = message.content,
//                        color = textColor,
//                        modifier = Modifier.padding(12.dp),
//                        fontSize = 16.sp
//                    )
//                }
//            }
//
//            "IMAGE" -> {
//                Column(
//                    modifier = Modifier.padding(8.dp)
//                ) {
//                    message.imageUrl?.let { url ->
//                        AsyncImage(
//                            model = url,
//                            contentDescription = "Image",
//                            modifier = Modifier
//                                .size(200.dp)
//                                .clip(RoundedCornerShape(8.dp)),
//                            contentScale = ContentScale.Crop
//                        )
//                    }
//                    if (message.content.isNotEmpty()) {
//                        Text(
//                            text = message.content,
//                            color = textColor,
//                            modifier = Modifier.padding(top = 8.dp),
//                            fontSize = 14.sp
//                        )
//                    }
//                }
//            }
//
//            else -> {
//                Text(
//                    text = message.content,
//                    color = textColor,
//                    modifier = Modifier.padding(12.dp),
//                    fontSize = 16.sp
//                )
//            }
//        }
//    }
//}
//
//// Emoji picker component - multi-row display
//@Composable
//fun EmojiPicker(
//    onEmojiSelected: (String) -> Unit,
//    onDismiss: () -> Unit
//) {
//    val emojis = listOf(
//        "😀", "😂", "🥰", "😍", "🤔", "😅", "😢", "😡",
//        "👍", "👎", "❤️", "💔", "🔥", "⭐", "🎉", "🎈",
//        "🚀", "🌟", "🎯", "💡", "📱", "💻", "🎵", "🍕",
//        "🌈", "🌸", "🌺", "🌻", "🌷", "🎂", "🍰", "🍎"
//    )
//
//    // Group emojis, 8 per row
//    val emojiRows = emojis.chunked(8)
//
//    Surface(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(160.dp) // Increased height for multiple rows
//            .clickable { onDismiss() },
//        color = Color.White,
//        shadowElevation = 8.dp
//    ) {
//        LazyColumn(
//            modifier = Modifier.padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            items(emojiRows) { emojiRow ->
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceEvenly
//                ) {
//                    emojiRow.forEach { emoji ->
//                        Text(
//                            text = emoji,
//                            fontSize = 24.sp,
//                            modifier = Modifier
//                                .clickable { onEmojiSelected(emoji) }
//                                .padding(8.dp)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//// Attachment menu component
//@Composable
//fun AttachmentMenu(
//    onImageClick: () -> Unit,
//    onFileClick: () -> Unit,
//    onDismiss: () -> Unit
//) {
//    Surface(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onDismiss() },
//        color = Color.White,
//        shadowElevation = 8.dp
//    ) {
//        Row(
//            modifier = Modifier.padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceEvenly
//        ) {
//            AttachmentMenuItem(
//                icon = Icons.Default.Image,
//                text = "Image",
//                onClick = onImageClick
//            )
//
//            AttachmentMenuItem(
//                icon = Icons.Default.AttachFile,
//                text = "File",
//                onClick = onFileClick
//            )
//        }
//    }
//}
//
//// Attachment menu item component
//@Composable
//fun AttachmentMenuItem(
//    icon: ImageVector,
//    text: String,
//    onClick: () -> Unit
//) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier.clickable { onClick() }
//    ) {
//        Surface(
//            shape = CircleShape,
//            color = MaterialTheme.colorScheme.primary,
//            modifier = Modifier.size(48.dp)
//        ) {
//            Icon(
//                icon,
//                contentDescription = text,
//                tint = Color.White,
//                modifier = Modifier.padding(12.dp)
//            )
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Text(
//            text = text,
//            fontSize = 12.sp,
//            color = Color.Gray
//        )
//    }
//}
//
//// Helper function: get file name
//fun getFileName(context: Context, uri: Uri): String? {
//    return try {
//        val cursor = context.contentResolver.query(uri, null, null, null, null)
//        cursor?.use {
//            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
//            it.moveToFirst()
//            it.getString(nameIndex)
//        }
//    } catch (e: Exception) {
//        null
//    }
//}