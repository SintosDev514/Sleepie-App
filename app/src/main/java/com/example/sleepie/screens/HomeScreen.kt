package com.example.sleepie.screens

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sleepie.broadcastReceiver.AlarmReceiver
import com.example.sleepie.data.db.SleepSession
import com.example.sleepie.viewModel.SleepViewModel
import com.example.sleepie.viewModel.SleepViewModelFactory
import com.example.sleepie.weather.domain.weather.WeatherCard
import com.example.sleepie.weather.domain.weather.WeatherViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/* ---------------- HOME SCREEN ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    weatherViewModel: WeatherViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val sleepViewModel: SleepViewModel =
        viewModel(factory = SleepViewModelFactory(application))

    val sleepSessions by sleepViewModel.allSleepSessions.collectAsState(initial = emptyList())
    val lastSleep = sleepSessions.firstOrNull()

    val prefs = remember {
        context.getSharedPreferences("SleepiePrefs", Context.MODE_PRIVATE)
    }

    var nextAlarmTime by remember {
        mutableLongStateOf(prefs.getLong("next_alarm_time", 0L))
    }

    var nextAlarmLabel by remember {
        mutableStateOf(prefs.getString("next_alarm_label", "") ?: "")
    }

    var startTime by remember {
        mutableLongStateOf(prefs.getLong("start_time", 0L))
    }

    val hasAlarm = nextAlarmTime > System.currentTimeMillis()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            weatherViewModel.loadWeatherInfo()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(navController.currentBackStackEntry) {
        // Refresh data when returning to the screen
        nextAlarmTime = prefs.getLong("next_alarm_time", 0L)
        nextAlarmLabel = prefs.getString("next_alarm_label", "") ?: ""
        startTime = prefs.getLong("start_time", 0L)
    }

    Scaffold(
        topBar = { HomeTopBar() }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {

            GreetingSection()

            WeatherCard(
                state = weatherViewModel.state,
                onRefresh = { weatherViewModel.loadWeatherInfo() }
            )

            AlarmDashboardCard(
                alarmTime = nextAlarmTime,
                alarmLabel = nextAlarmLabel,
                hasAlarm = hasAlarm,
                onCancel = {
                    val alarmManager =
                        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                    val intent = Intent(context, AlarmReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        nextAlarmTime.toInt(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    alarmManager.cancel(pendingIntent)

                    prefs.edit {
                        remove("next_alarm_time")
                        remove("next_alarm_label")
                    }

                    nextAlarmTime = 0L
                    nextAlarmLabel = ""
                }
            )

            val sleepTrend = remember(sleepSessions) {
                calculateSleepTrend(sleepSessions)
            }

            SleepStatsRow(lastSleep, sleepTrend)

            if (startTime > 0) {
                Button(
                    onClick = { /* Already sleeping */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    enabled = false
                ) {
                    Text("Sleeping...", fontWeight = FontWeight.SemiBold)
                }
            } else {
                Button(
                    onClick = {
                        val newStartTime = System.currentTimeMillis()
                        prefs.edit {
                            putLong("start_time", newStartTime)
                        }
                        startTime = newStartTime
                    },
                    enabled = hasAlarm, // ✅ FIX: Only enable if there's an alarm
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Filled.Bedtime, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Start Sleep", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

/* ---------------- HELPERS ---------------- */

private fun calculateSleepTrend(sessions: List<SleepSession>): String {
    if (sessions.size < 2) return "--"

    val lastDuration = sessions[0].endTime - sessions[0].startTime
    val previousSessions = sessions.drop(1)
    val averageDuration = previousSessions.map { it.endTime - it.startTime }.average()

    return when {
        lastDuration > averageDuration * 1.1 -> "Improving"
        lastDuration < averageDuration * 0.9 -> "Declining"
        else -> "Stable"
    }
}


/* ---------------- TOP BAR ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Sleepie",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    )
}

/* ---------------- GREETING ---------------- */

@Composable
private fun GreetingSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Good Evening",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Wind down and prepare for rest",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


/* ---------------- ALARM CARD ---------------- */

@Composable
private fun AlarmDashboardCard(
    alarmTime: Long,
    alarmLabel: String,
    hasAlarm: Boolean,
    onCancel: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp)
    ) {
        if (hasAlarm) {
            // UI for when an alarm is set
            var remainingTime by remember { mutableStateOf(alarmTime - System.currentTimeMillis()) }
            val totalTime = remember(alarmTime) {
                (alarmTime - System.currentTimeMillis()).coerceAtLeast(1L)
            }

            LaunchedEffect(alarmTime) {
                while (remainingTime > 0) {
                    delay(1000)
                    remainingTime = alarmTime - System.currentTimeMillis()
                }
            }

            val progress = (remainingTime.toFloat() / totalTime).coerceIn(0f, 1f)

            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Next Alarm",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(alarmTime)),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (alarmLabel.isNotBlank()) {
                        Text(
                            text = alarmLabel,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    TextButton(onClick = onCancel) {
                        Text("Cancel alarm")
                    }
                }

                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val indicatorColor = MaterialTheme.colorScheme.primary
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant

                    CircularProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxSize(),
                        color = indicatorColor,
                        trackColor = trackColor,
                        strokeWidth = 10.dp,
                        strokeCap = StrokeCap.Round
                    )

                    val hours = (remainingTime / (1000 * 60 * 60)) % 24
                    val minutes = (remainingTime / (1000 * 60)) % 60
                    val seconds = (remainingTime / 1000) % 60

                    val displayTime = when {
                        hours > 0 -> String.format("%dh %02dm", hours, minutes)
                        minutes > 0 -> String.format("%dm %02ds", minutes, seconds)
                        else -> String.format("%ds", seconds.coerceAtLeast(0))
                    }

                    Text(
                        text = displayTime,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // UI for when no alarm is set
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Icon(
                    Icons.Filled.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "No alarm scheduled",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/* ---------------- STATS ---------------- */

@Composable
private fun SleepStatsRow(lastSleep: SleepSession?, trend: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard("Duration", lastSleep?.duration ?: "--", Icons.Filled.Schedule)
        StatCard("Quality", lastSleep?.quality ?: "--", Icons.Filled.Star)

    }
}

@Composable
private fun RowScope.StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ElevatedCard(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
