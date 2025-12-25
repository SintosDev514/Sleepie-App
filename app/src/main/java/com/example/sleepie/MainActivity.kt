package com.example.sleepie

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.util.Consumer
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sleepie.navigation.BottomNavItem
import com.example.sleepie.navigation.SleepieNavGraph

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            var newIntent by remember { mutableStateOf(intent) }
            DisposableEffect(Unit) {
                val listener = Consumer<Intent> { intent ->
                    newIntent = intent
                }
                addOnNewIntentListener(listener)
                onDispose { removeOnNewIntentListener(listener) }
            }

            LaunchedEffect(newIntent) {
                newIntent?.getStringExtra("navigate_to")?.let {
                    navController.navigate(it)
                    // Prevent re-navigation on configuration changes
                    newIntent?.removeExtra("navigate_to")
                }
            }

            Scaffold(
                bottomBar = {
                    val items = listOf(
                        BottomNavItem.Home,
                        BottomNavItem.Alarm,
                        BottomNavItem.History,
                    )
                    if (currentRoute in items.map { it.route }) {
                        NavigationBar {
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { screen.icon() },
                                    label = { Text(screen.label) },
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                SleepieNavGraph(navController = navController, modifier = Modifier.padding(innerPadding))
            }
        }
    }
}
