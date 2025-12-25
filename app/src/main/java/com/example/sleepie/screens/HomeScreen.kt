package com.example.sleepie.ui.home

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sleepie.data.db.SleepSession
import com.example.sleepie.navigation.NavigationDestinations
import com.example.sleepie.ui.theme.SleepieTheme
import com.example.sleepie.viewModel.SleepViewModel
import com.example.sleepie.viewModel.SleepViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: SleepViewModel = viewModel(factory = SleepViewModelFactory(application))
    val lastSleepSession by viewModel.allSleepSessions.collectAsState(initial = null)

    Scaffold(
        topBar = {
            HomeScreenTopAppBar()
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        HomeScreenContent(
            navController = navController,
            lastSleepSession = lastSleepSession?.firstOrNull(),
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenTopAppBar() {
    TopAppBar(
        title = {
            Text(
                text = "Sleepie",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
        navigationIcon = {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = "Home",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    )
}

@Composable
private fun HomeScreenContent(
    navController: NavController,
    lastSleepSession: SleepSession?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SleepSummaryCard(lastSleepSession)
        NavigationButtons(navController = navController)
    }
}

@Composable
private fun SleepSummaryCard(lastSleepSession: SleepSession?) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Last Sleep Session",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (lastSleepSession != null) {
                Text(
                    text = lastSleepSession.duration,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Sleep Quality: ${lastSleepSession.quality}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            } else {
                Text(
                    text = "No sleep data yet",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun NavigationButtons(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = { navController.navigate(NavigationDestinations.ALARM) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 2.dp
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Alarm, contentDescription = "Set Alarm")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Set Alarm",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        OutlinedButton(
            onClick = { navController.navigate(NavigationDestinations.HISTORY) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
        ) {
             Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.History, contentDescription = "View Sleep History", tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "View Sleep History",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreview() {
    SleepieTheme(darkTheme = true) {
        HomeScreen(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SleepSummaryCardPreview() {
    SleepieTheme(darkTheme = true) {
        SleepSummaryCard(lastSleepSession = SleepSession(date = "Dec 23, 2025", duration = "7h 30m", quality = "Good"))
    }
}
