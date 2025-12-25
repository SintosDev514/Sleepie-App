package com.example.sleepie.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.sleepie.screens.AlarmScreen
import com.example.sleepie.screens.HistoryScreen
import com.example.sleepie.screens.SummaryScreen
import com.example.sleepie.ui.home.HomeScreen

@Composable
fun SleepieNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = NavigationDestinations.HOME,
        modifier = modifier
    ) {
        composable(NavigationDestinations.HOME) { HomeScreen(navController) }
        composable(NavigationDestinations.ALARM) { AlarmScreen() }
        composable(NavigationDestinations.HISTORY) { HistoryScreen(navController) }

        composable(
            route = "summary/{startTime}",
            arguments = listOf(navArgument("startTime") { type = NavType.LongType })
        ) { backStackEntry ->
            val startTime = backStackEntry.arguments?.getLong("startTime") ?: System.currentTimeMillis()
            SummaryScreen(navController, startTime = startTime)
        }
    }
}
