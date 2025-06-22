//package com.example.voyago.view
//
//import android.net.Uri
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.itemsIndexed
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.AccountCircle
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.Face
//import androidx.compose.material.icons.filled.Group
//import androidx.compose.material.icons.filled.Send
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.HorizontalDivider
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.LocalTextStyle
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextFieldDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableIntStateOf
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import androidx.navigation.compose.rememberNavController
//import com.example.voyago.activities.BottomBar
//import com.example.voyago.activities.TopBar
//import com.example.voyago.model.ChatMessage
//import com.example.voyago.model.ChatModel
//import com.example.voyago.model.TripModel
//import com.example.voyago.model.UserModel
//import com.example.voyago.viewmodel.ChatViewModel
//import com.example.voyago.viewmodel.NotificationViewModel
//import com.example.voyago.viewmodel.UserViewModel
//
//@Composable
//fun GroupChat(
//    chatViewModel: ChatViewModel,
//    navController: NavController,
//    modifier: Modifier = Modifier
//) {
//    val messages = remember {
//        mutableStateListOf(
//            ChatMessage("Alice", "Looking forward to the hike!", "June 18, 2025", "9:42 AM", false),
//            ChatMessage("You", "Me too! Hope it doesn't rain 🌦", "June 18, 2025", "9:43 AM", true),
//            ChatMessage("Liam", "Forecast looks clear for now!", "June 18, 2025", "9:44 AM", false),
//            ChatMessage("Sofia", "Who's bringing snacks?", "June 18, 2025", "9:45 AM", false),
//            ChatMessage("You", "I'll bring some granola bars.", "June 18, 2025", "9:44 AM", true)
//        )
//    }
//
//    var firstUnreadIndex by remember { mutableIntStateOf(3) }
//    var showUnreadSeparator by remember { mutableStateOf(true) }
//    var autoScrollToUnread by remember { mutableStateOf(true) }
//
//    DisposableEffect(Unit) {
//        onDispose {
//            // Called when GroupChat leaves composition — reset separator
//            showUnreadSeparator = false
//        }
//    }
//
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .background(Color(0xFFF3E5F5))
//    ) {
//        TopBanner(
//            "Weekend Hikers",
//            listOf("@alice_w", "@globetrotliam", "@ethan_nomad", "@sofi_explorer"),
//        )
//        ChatMessagesView(
//            messages = messages,
//            firstUnreadIndex = firstUnreadIndex,
//            showUnreadSeparator = showUnreadSeparator,
//            autoScrollToUnread = autoScrollToUnread,
//            onUnreadSeparatorPassed = {
//                showUnreadSeparator = false
//            },
//            modifier = Modifier.weight(1f)
//        )
//    }
//}
//
//
//@Composable
//fun TopBanner(groupName: String, participants: List<String>) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(Color(0xFF7E57C2))
//            .padding(16.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(
//            imageVector = Icons.Filled.Group,
//            contentDescription = "Group Icon",
//            tint = Color.White,
//            modifier = Modifier
//                .size(48.dp)
//                .background(Color(0xFF5E35B1), CircleShape)
//                .padding(8.dp)
//        )
//        Spacer(modifier = Modifier.width(12.dp))
//        Column(modifier = Modifier.weight(1f)) {
//            Text(
//                groupName,
//                color = Color.White,
//                fontWeight = FontWeight.Bold,
//                fontSize = 18.sp
//            )
//            Text(
//                participants.joinToString(", "),
//                color = Color.White.copy(alpha = 0.8f),
//                fontSize = 12.sp,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
//        }
//    }
//}
//
//
//@Composable
//fun DateSeparator(dateText: String) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp),
//        horizontalArrangement = Arrangement.Center
//    ) {
//        Text(
//            text = dateText,
//            fontSize = 12.sp,
//            color = Color.Gray
//        )
//    }
//}
//
//
//@Composable
//fun MessageBubble(message: ChatMessage) {
//    val backgroundColor = if (message.isCurrentUser) Color(0xFF9575CD) else Color(0xFFD1C4E9)
//    val textColor = if (message.isCurrentUser) Color.White else Color.Black
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 8.dp, vertical = 4.dp),
//        horizontalArrangement = if (message.isCurrentUser) Arrangement.End else Arrangement.Start
//    ) {
//        if (!message.isCurrentUser) {
//            Icon(
//                imageVector = Icons.Filled.AccountCircle,
//                contentDescription = "Avatar",
//                tint = Color(0xFF7E57C2),
//                modifier = Modifier.size(36.dp)
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//        }
//
//        Column(
//            horizontalAlignment = if (message.isCurrentUser) Alignment.End else Alignment.Start
//        ) {
//            if (!message.isCurrentUser) {
//                Text(
//                    text = message.senderName,
//                    fontSize = 12.sp,
//                    color = Color.Gray
//                )
//            }
//
//            Column(
//                horizontalAlignment = if (message.isCurrentUser) Alignment.Start else Alignment.End
//            ) {
//                Box(
//                    modifier = Modifier
//                        .background(backgroundColor, shape = RoundedCornerShape(12.dp))
//                        .padding(horizontal = 12.dp, vertical = 10.dp)
//                ) {
//                    Text(text = message.message, color = textColor)
//                }
//
//                Text(
//                    text = message.time,
//                    fontSize = 10.sp,
//                    color = Color.Gray,
//                    modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
//                )
//            }
//        }
//    }
//}
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ChatMessagesView(
//    messages: List<ChatMessage>,
//    firstUnreadIndex: Int,
//    showUnreadSeparator: Boolean,
//    autoScrollToUnread: Boolean,
//    onUnreadSeparatorPassed: () -> Unit, // might not be called anymore
//    modifier: Modifier = Modifier
//) {
//    val listState = rememberLazyListState()
//
//    LaunchedEffect(autoScrollToUnread, messages.size) {
//        if (autoScrollToUnread && firstUnreadIndex < messages.size) {
//            listState.scrollToItem(firstUnreadIndex)
//        }
//    }
//
//    LazyColumn(
//        state = listState,
//        modifier = modifier.fillMaxSize(),
//        contentPadding = PaddingValues(top = 8.dp, bottom = 0.dp)
//    ) {
//        var lastDate: String? = null
//
//        itemsIndexed(messages) { index, message ->
//            Column {
//                // Date separator if the date changes
//                if (lastDate != message.date) {
//                    lastDate = message.date
//                    DateSeparator(message.date)
//                }
//
//                // Unread separator
//                if (index == firstUnreadIndex && showUnreadSeparator) {
//                    UnreadMessagesSeparator()
//                }
//
//                // Actual message
//                MessageBubble(message)
//
//            }
//        }
//
//    }
//
//    var inputText by remember { mutableStateOf("") }
//    var showEmojiPicker by remember { mutableStateOf(false) }
//    var showAttachmentMenu by remember { mutableStateOf(false) }
//
//// Emoji picker
//    if (showEmojiPicker) {
//        EmojiPicker(
//            onEmojiSelected = { emoji ->
//                inputText += emoji
//                showEmojiPicker = false
//            },
//            onDismiss = { showEmojiPicker = false }
//        )
//    }
//
//// Image picker
//    val imagePickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let {
//            /*coroutineScope.launch {
//                viewModel.sendImageMessage(it, context)
//            }*/
//        }
//        showAttachmentMenu = false
//    }
//
//// Attachment menu
//    if (showAttachmentMenu) {
//        AttachmentMenu(
//            onImageClick = { imagePickerLauncher.launch("image/*") },
//            onFileClick = { /* Implement file upload */ },
//            onDismiss = { showAttachmentMenu = false }
//        )
//    }
//
//// Input area
//    Surface(
//        modifier = Modifier
//            .fillMaxWidth(),
//        color = Color.White
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 8.dp, vertical = 6.dp), // Reduced vertical padding
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Attachment button
//            IconButton(
//                onClick = { showAttachmentMenu = !showAttachmentMenu },
//                modifier = Modifier.size(36.dp)
//            ) {
//                Icon(
//                    Icons.Default.Add,
//                    contentDescription = "Attachments",
//                    tint = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.size(20.dp)
//                )
//            }
//
//            // Input field
//            OutlinedTextField(
//                value = inputText,
//                onValueChange = { inputText = it },
//                modifier = Modifier
//                    .weight(1f)
//                    .height(52.dp) // Slightly taller to fit the placeholder
//                    .padding(horizontal = 6.dp),
//                placeholder = {
//                    Text("Type a message...", fontSize = 16.sp)
//                },
//                shape = RoundedCornerShape(20.dp),
//                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
//                singleLine = true,
//                trailingIcon = {
//                    IconButton(
//                        onClick = { showEmojiPicker = !showEmojiPicker },
//                        modifier = Modifier.size(32.dp)
//                    ) {
//                        Icon(
//                            Icons.Default.Face,
//                            contentDescription = "Emoji",
//                            tint = MaterialTheme.colorScheme.primary,
//                            modifier = Modifier.size(18.dp)
//                        )
//                    }
//                },
//                colors = TextFieldDefaults.outlinedTextFieldColors(
//                    focusedBorderColor = MaterialTheme.colorScheme.primary,
//                    unfocusedBorderColor = Color.LightGray
//                )
//            )
//
//            // Send button
//            IconButton(
//                onClick = {
//                    if (inputText.trim().isNotEmpty()) {
//                        /*coroutineScope.launch {
//                            viewModel.sendTextMessage(inputText.trim())
//                            inputText = ""
//                        }*/
//                    }
//                },
//                modifier = Modifier.size(36.dp)
//            ) {
//                Icon(
//                    Icons.Default.Send,
//                    contentDescription = "Send",
//                    tint = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.size(20.dp)
//                )
//            }
//        }
//    }
//
//}
//
//
//@Composable
//fun UnreadMessagesSeparator() {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 12.dp),
//        horizontalArrangement = Arrangement.Center
//    ) {
//        HorizontalDivider(
//            modifier = Modifier
//                .weight(1f)
//                .align(Alignment.CenterVertically),
//            color = Color.Gray.copy(alpha = 0.4f)
//        )
//        Text(
//            "  Unread Messages  ",
//            color = Color.Gray,
//            fontSize = 12.sp
//        )
//        HorizontalDivider(
//            modifier = Modifier
//                .weight(1f)
//                .align(Alignment.CenterVertically),
//            color = Color.Gray.copy(alpha = 0.4f)
//        )
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GroupChatPreview() {
//    val navController = rememberNavController()
//
//    val dummyNotificationViewModel = NotificationViewModel()
//    val dummyUserViewModel = UserViewModel(UserModel())
//    val dummyChatViewModel = ChatViewModel(ChatModel(), UserModel(), TripModel())
//
//    MaterialTheme {
//        Scaffold(
//            topBar = {
//                TopBar(
//                    nvm = dummyNotificationViewModel,
//                    navController = navController,
//                    uvm = dummyUserViewModel
//                )
//            },
//            bottomBar = { BottomBar(navController) }
//        ) { innerPadding ->
//            GroupChat(
//                chatViewModel = dummyChatViewModel,
//                navController = navController,
//                modifier = Modifier.padding(innerPadding)
//            )
//        }
//    }
//}