package com.example.voyago.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.voyago.activities.BottomBar
import com.example.voyago.activities.TopBar
import com.example.voyago.model.ChatMessage
import com.example.voyago.model.ChatModel
import com.example.voyago.model.TripModel
import com.example.voyago.model.UserModel
import com.example.voyago.viewmodel.ChatViewModel
import com.example.voyago.viewmodel.NotificationViewModel
import com.example.voyago.viewmodel.UserViewModel


@Composable
fun SingleChat(
    recipientName: String,
    recipientHandle: String,
    chatViewModel: ChatViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                recipientName,
                "Hey, are we still on for tonight?",
                "June 18, 2025",
                "9:15 AM",
                false
            ),
            ChatMessage("You", "Yep! 7 PM works?", "June 18, 2025", "9:16 AM", true),
            ChatMessage(recipientName, "Perfect. See you then!", "June 18, 2025", "9:17 AM", false)
        )
    }

    var firstUnreadIndex by remember { mutableIntStateOf(2) }
    var showUnreadSeparator by remember { mutableStateOf(true) }
    var autoScrollToUnread by remember { mutableStateOf(true) }
    var inputText by remember { mutableStateOf("") }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            showUnreadSeparator = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3E5F5))
    ) {
        TopBarPrivate(recipientName, recipientHandle, onBlockUser = {})

        ChatMessagesView(
            messages = messages,
            firstUnreadIndex = firstUnreadIndex,
            showUnreadSeparator = showUnreadSeparator,
            autoScrollToUnread = autoScrollToUnread,
            onUnreadSeparatorPassed = { showUnreadSeparator = false },
            modifier = Modifier.weight(1f)
        )

        if (showEmojiPicker) {
            EmojiPicker(
                onEmojiSelected = {
                    inputText += it
                    showEmojiPicker = false
                },
                onDismiss = { showEmojiPicker = false }
            )
        }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                /*coroutineScope.launch {
                    viewModel.sendImageMessage(it, context)
                }*/
            }
            showAttachmentMenu = false
        }

        if (showAttachmentMenu) {
            AttachmentMenu(
                onImageClick = { imagePickerLauncher.launch("image/*") },
                onFileClick = { /* Implement file upload */ },
                onDismiss = { showAttachmentMenu = false }
            )
        }
    }
}

@Composable
fun TopBarPrivate(
    recipientName: String,
    recipientHandle: String,
    onBlockUser: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF7E57C2))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = "User Avatar",
            tint = Color.White,
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFF5E35B1), CircleShape)
                .padding(4.dp)
        )
        // Spacer to add space between the avatar and text
        Spacer(modifier = Modifier.width(12.dp))
        // Column to display recipient's name and handle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = recipientName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = recipientHandle,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
        }

        // Add the "More" button with dropdown menu for "Block User"
        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.White
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Block User") },
                    onClick = {
                        expanded = false
                        onBlockUser()
                    }
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun SingleChatPreview() {
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
            SingleChat(
                chatViewModel = dummyChatViewModel,
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                recipientName = "Alice",
                recipientHandle = "@alice_w"
            )
        }
    }
}