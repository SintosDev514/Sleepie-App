package com.example.sleepie.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.sleepie.screens.AddAlarmScreen
import com.example.sleepie.screens.HistoryScreen
import com.example.sleepie.screens.HomeScreen
import com.example.sleepie.screens.SummaryScreen
import com.example.sleepie.weather.domain.weather.WeatherViewModel

@Composable
fun SleepieNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    weatherViewModel: WeatherViewModel
) {
    NavHost(
        navController = navController,
        startDestination = NavigationDestinations.HOME,
        modifier = modifier
    ) {
        composable(NavigationDestinations.HOME) { HomeScreen(navController, weatherViewModel) }
        composable(NavigationDestinations.ALARM) { AddAlarmScreen(navController) }
        composable(NavigationDestinations.HISTORY) { HistoryScreen(navController) }

        composable(
            route = "summary/{startTime}",
            arguments = listOf(navArgument("startTime") { type = NavType.LongType })
        ) { backStackEntry ->
            val startTime = backStackEntry.arguments?.getLong("startTime")
            if (startTime != null && startTime > 0) {
                SummaryScreen(navController, startTime = startTime)
            } else {
                navController.navigate(NavigationDestinations.HOME) {
                    popUpTo(NavigationDestinations.HOME) { inclusive = true }
                }
            }
        }
    }
}
