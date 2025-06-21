package com.example.voyago.model

// Data Structure for Notification
data class NotificationModel(
    val title: String = "",
    val body: String = "",
    val type: String = "",
    val idLink: Int = -1,
    val read: Boolean = false
)
