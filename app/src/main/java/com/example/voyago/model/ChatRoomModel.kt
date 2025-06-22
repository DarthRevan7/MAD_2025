package com.example.voyago.model

import com.google.firebase.Timestamp


data class ChatRoom(
    val id: String = "", // Firestore document ID (e.g., "1", "2", ...)
    val type: String = "private", // "private" or "group"
    val participants: List<Int> = emptyList(), // List IDs
    val name: String = "", // Optional for group chats
    val lastMessage: String = "", // Last message content
    val usersNotRead: List<String> = emptyList() // Users who haven't read last message
)

data class FirebaseChatMessage(
    val id: String = "", // Firestore document ID
    val senderId: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val type: String = "TEXT", // "TEXT", "IMAGE", etc.
    val imageUrl: String? = null
)