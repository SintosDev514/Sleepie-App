package com.example.sleepie.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
) {

    object Home : BottomNavItem(
        route = "home",
        label = "Home",
        icon = {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = "Home"
            )
        }
    )

    object Alarm : BottomNavItem(
        route = "alarm",
        label = "Alarm",
        icon = {
            Text("⏰")
        }
    )

    object History : BottomNavItem(
        route = "history",
        label = "History",
        icon = {
            Icon(
                imageVector = Icons.Filled.History,
                contentDescription = "History"
            )
        }
    )
}
