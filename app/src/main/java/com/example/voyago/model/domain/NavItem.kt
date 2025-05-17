package com.example.voyago.model.domain

import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val rootRoute: String,
    val startRoute: String
)