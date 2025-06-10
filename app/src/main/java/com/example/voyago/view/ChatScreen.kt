package com.example.voyago.view

import android.util.Log
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.voyago.model.ChatMessage
import com.example.voyago.model.MessageType
import com.example.voyago.model.User
import com.example.voyago.viewmodel.ChatRoomFactory
import com.example.voyago.viewmodel.ChatRoomViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.material3.Icon


@Composable
fun ChatRoomScreen(
    viewModel: ChatRoomViewModel = viewModel(factory = ChatRoomFactory)
) {
    val uiState by viewModel.uiState.collectAsState()  // 确保这里使用正确的 State

    // 处理错误显示
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // 显示错误提示，你可以根据需要实现Toast或Snackbar
            Log.e("ChatRoom", error)
            viewModel.clearError()
        }
    }

    // 处理错误显示
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // 显示错误提示，你可以根据需要实现Toast或Snackbar
            Log.e("ChatRoom", error)
            viewModel.clearError()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 在线用户显示
        OnlineUsersSection(
            users = uiState.onlineUsers,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 聊天消息列表
        ChatMessagesList(
            messages = uiState.messages,
            currentUserId = uiState.currentUser?.id ?: 0,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 消息输入框
        MessageInputSection(
            message = uiState.currentMessage,
            onMessageChange = viewModel::updateMessage,
            onSendClick = viewModel::sendMessage,
            isLoading = uiState.isLoading,
            enabled = uiState.currentUser != null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineUsersSection(
    users: List<User>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                     Icon(
                              imageVector        = Icons.Default.People,
                              contentDescription = "Online User",
                             tint               = Color.Green
                                    )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Online User (${users.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(users) { user ->
                    OnlineUserChip(user = user)
                }
            }
        }
    }
}

@Composable
fun OnlineUserChip(user: User) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.padding(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 用户头像 (如果有的话)
            if (!user.profilePictureUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = user.profilePictureUrl,
                    contentDescription = "头像",
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(6.dp))
            } else {
                // 在线状态指示器
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.Green, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = user.username,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun ChatMessagesList(
    messages: List<ChatMessage>,
    currentUserId: Int,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // 自动滚动到最新消息
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                ChatMessageItem(
                    message = message,
                    isOwnMessage = message.senderId == currentUserId
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isOwnMessage: Boolean
) {
    val isSystemMessage = message.type == MessageType.SYSTEM

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage && !isSystemMessage) {
            Arrangement.End
        } else if (isSystemMessage) {
            Arrangement.Center
        } else {
            Arrangement.Start
        }
    ) {
        if (isSystemMessage) {
            // 系统消息样式
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text(
                    text = message.message,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // 普通消息样式
            Column(
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                if (!isOwnMessage) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
                    ) {
                        // 显示发送者头像 (如果有)
                        if (!message.senderProfileUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = message.senderProfileUrl,
                                contentDescription = "发送者头像",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                        bottomEnd = if (isOwnMessage) 4.dp else 16.dp
                    ),
                    color = if (isOwnMessage) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = message.message,
                            color = if (isOwnMessage) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault())
                                .format(Date(message.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOwnMessage) {
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageInputSection(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("输入消息...") },
                maxLines = 3,
                enabled = enabled && !isLoading,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { if (enabled) onSendClick() }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSendClick,
                enabled = message.trim().isNotEmpty() && !isLoading && enabled
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "发送",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}