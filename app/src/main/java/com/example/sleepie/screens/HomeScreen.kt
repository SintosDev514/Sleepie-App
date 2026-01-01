package com.example.sleepie.screens

import androidx.compose.material3.MaterialTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    val hasAlarm = nextAlarmTime > System.currentTimeMillis()

    /* -------- LOCATION PERMISSIONS (NO ACCOMPANIST) -------- */

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
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    Scaffold(
        topBar = { HomeTopBar() }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            GreetingSection()

            WeatherCard(
                state = weatherViewModel.state,
                onRefresh = { weatherViewModel.loadWeatherInfo() }
            )

            SleepScoreCard(lastSleep)

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

            SleepStatsRow(lastSleep)
        }
    }
}

/* ---------------- TOP BAR ---------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar() {
    TopAppBar(
        title = { Text("Sleepie", fontWeight = FontWeight.Bold) }
    )
}

/* ---------------- GREETING ---------------- */

@Composable
private fun GreetingSection() {
    Column {
        Text(
            "Good Evening 🌙",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Here’s your sleep overview",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* ---------------- SLEEP SCORE ---------------- */

@Composable
private fun SleepScoreCard(lastSleep: SleepSession?) {

    val hasData = lastSleep != null

    val score = when (lastSleep?.quality) {
        "Excellent" -> 92
        "Good" -> 80
        "Fair" -> 65
        else -> 0
    }

    val progress by animateFloatAsState(
        targetValue = if (hasData) score / 100f else 0f,
        label = "SleepScore"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasData)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Last Sleep", fontWeight = FontWeight.SemiBold)

            Spacer(Modifier.height(12.dp))

            CircularScoreProgress(progress, score, hasData)

            Spacer(Modifier.height(12.dp))

            Text(
                if (hasData) lastSleep!!.quality else "No sleep recorded yet",
                fontWeight = FontWeight.Bold
            )
        }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Text("Next Alarm", fontWeight = FontWeight.SemiBold)

            if (hasAlarm) {

                Text(
                    SimpleDateFormat("hh:mm a", Locale.getDefault())
                        .format(Date(alarmTime)),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )

                if (alarmLabel.isNotEmpty()) {
                    Text(alarmLabel)
                }

                TextButton(onClick = onCancel) {
                    Text("Cancel Alarm")
                }

            } else {

                Icon(
                    Icons.Filled.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp)
                )

                Text("No alarm set yet", fontWeight = FontWeight.Medium)
            }
        }
    }
}

/* ---------------- STATS ---------------- */

@Composable
private fun SleepStatsRow(lastSleep: SleepSession?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatCard("Duration", lastSleep?.duration ?: "--", Icons.Filled.Schedule)
        StatCard("Quality", lastSleep?.quality ?: "--", Icons.Filled.Star)
        StatCard("Consistency", "Stable", Icons.AutoMirrored.Filled.ShowChart)
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.width(110.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.Bold)
            Text(
                title,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

/* ---------------- CIRCULAR PROGRESS ---------------- */

@Composable
private fun CircularScoreProgress(
    progress: Float,
    score: Int,
    hasData: Boolean
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier.size(130.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {

            drawArc(
                color = Color.LightGray.copy(alpha = 0.3f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(16f)
            )

            if (hasData) {
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        16f,
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        Text(
            if (hasData) "$score" else "--",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
    }
}
