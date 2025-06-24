package com.example.voyago.model

// Data Structure for Notification
data class NotificationModel(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "",
    val idLink: Int = -1,
    val read: Boolean = false
)
