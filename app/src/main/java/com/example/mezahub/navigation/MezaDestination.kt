package com.example.mezahub.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class MezaDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    LISTEN(route = "listen", label = "Listen", icon = Icons.Filled.Mic),
    HISTORY(route = "history", label = "History", icon = Icons.Filled.History),
    SETTINGS(route = "settings", label = "Settings", icon = Icons.Filled.Settings),
}
