package com.example.voyago.model

data class NotificationItem(
    val title: String = "",
    val body: String = "",
    val type: String = "",
    val idLink: Int = -1,
    val read: Boolean = false
)
