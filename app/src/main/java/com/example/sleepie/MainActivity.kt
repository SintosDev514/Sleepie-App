package com.example.sleepie

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sleepie.navigation.BottomNavItem
import com.example.sleepie.navigation.SleepieNavGraph
import com.example.sleepie.ui.theme.SleepieTheme
import com.example.sleepie.weather.domain.weather.WeatherViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@ExperimentalPermissionsApi
class MainActivity : ComponentActivity() {

    private val weatherViewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SleepieTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        BottomAppBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route
                            val items = listOf(BottomNavItem.Home, BottomNavItem.Alarm, BottomNavItem.History)
                            items.forEach { item ->
                                NavigationBarItem(
                                    selected = currentRoute == item.route,
                                    onClick = { navController.navigate(item.route) },
                                    icon = item.icon,
                                    label = { Text(item.label) }
                                )
                            }
                        }
                    }
                ) { padding ->
                    SleepieNavGraph(navController, modifier = Modifier.padding(padding), weatherViewModel)
                }
            }
        }
    }
}
