package com.example.sleepie.screens.home

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sleepie.broadcastReceiver.AlarmReceiver
import com.example.sleepie.data.db.SleepSession
import com.example.sleepie.navigation.NavigationDestinations
import com.example.sleepie.viewModel.SleepViewModel
import com.example.sleepie.viewModel.SleepViewModelFactory
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: SleepViewModel =
        viewModel(factory = SleepViewModelFactory(application))

    val sleepSessions by viewModel.allSleepSessions.collectAsState(initial = emptyList())
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

    // Refresh when returning to the screen
    LaunchedEffect(navController.currentBackStackEntry) {
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            GreetingSection()

            if (nextAlarmTime > System.currentTimeMillis()) {
                AlarmDashboardCard(
                    alarmSetTime = startTime,
                    alarmTime = nextAlarmTime,
                    alarmLabel = nextAlarmLabel,
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
                    }
                )
            }

            SleepSummaryCard(lastSleep)

        }
    }
}

/* ---------------- ALARM CARD ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmDashboardCard(
    alarmSetTime: Long,
    alarmTime: Long,
    alarmLabel: String,
    onCancel: () -> Unit
) {
    var remainingTime by remember { mutableStateOf(alarmTime - System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (remainingTime > 0) {
            delay(1000)
            remainingTime = alarmTime - System.currentTimeMillis()
        }
    }

    val hours = (remainingTime / (1000 * 60 * 60)) % 24
    val minutes = (remainingTime / (1000 * 60)) % 60
    val seconds = (remainingTime / 1000) % 60

    val countdownText = when {
        hours > 0 -> String.format("%dh %02dm", hours, minutes)
        minutes > 0 -> String.format("%dm %02ds", minutes, seconds)
        else -> String.format("%ds", seconds)
    }

    val totalDuration = (alarmTime - alarmSetTime).coerceAtLeast(1)
    val progress = (remainingTime.toFloat() / totalDuration).coerceIn(0f, 1f)

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text("Next Alarm", fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        SimpleDateFormat("hh:mm a", Locale.getDefault())
                            .format(Date(alarmTime)),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (alarmLabel.isNotEmpty()) {
                        Text(alarmLabel)
                    }
                }

                CircularTimeProgress(
                    countdownText = countdownText,
                    progress = progress,
                    progressColor = primaryColor,
                    trackColor = trackColor
                )
            }

            TextButton(onClick = onCancel) {
                Text("Cancel Alarm")
            }
        }
    }
}

/* ---------------- CIRCULAR PROGRESS ---------------- */

@Composable
private fun CircularTimeProgress(
    countdownText: String,
    progress: Float,
    progressColor: Color,
    trackColor: Color
) {
    Box(
        modifier = Modifier.size(90.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.12f

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    stroke,
                    cap = StrokeCap.Round
                )
            )

            drawArc(
                color = progressColor,
                startAngle = 270f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    stroke,
                    cap = StrokeCap.Round
                )
            )
        }

        Text(countdownText, fontWeight = FontWeight.Bold)
    }
}

/* ---------------- OTHER UI ---------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar() {
    TopAppBar(
        title = { Text("Sleepie", fontWeight = FontWeight.Bold) }
    )
}

@Composable
private fun GreetingSection() {
    Column {
        Text("Good Evening 🌙", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Track your sleep better",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepSummaryCard(lastSleep: SleepSession?) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Last Sleep", fontWeight = FontWeight.SemiBold)

            if (lastSleep != null) {
                Text(lastSleep.duration, fontWeight = FontWeight.Bold)
                AssistChip(
                    onClick = {},
                    label = { Text("Quality: ${lastSleep.quality}") },
                    leadingIcon = {
                        Icon(Icons.Filled.Star, contentDescription = null)
                    }
                )
            } else {
                Text("No data yet")
            }
        }
    }
}
