package com.example.voyago.model

import androidx.compose.ui.graphics.vector.ImageVector

// Data structure for the elements in the navigation bottom bar
data class NavItem(
    val label: String,
    val icon: ImageVector,
    val rootRoute: String,
    val startRoute: String
)