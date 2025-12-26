package com.example.sleepie.screens

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sleepie.MainActivity
import com.example.sleepie.broadcastReceiver.AlarmReceiver
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

private const val PREFS_NAME = "sleepie_alarm_prefs"
private const val KEY_ALARM_ACTIVE = "alarm_active"
private const val KEY_ALARM_TIME = "alarm_time"
private const val KEY_START_TIME = "start_time"
private const val TEST_MINUTES = 2L

private val DarkBackground = Color(0xFF121212)
private val AccentViolet = Color(0xFF7C4DFF)
private val ProgressViolet = Color(0xFF8E6CFF)
private val LightText = Color.White
private val MutedText = Color(0xFF9E9E9E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(navController: NavController) {

    val context = LocalContext.current
    val alarmManager =
        remember { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    val prefs =
        remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var alarmActive by remember { mutableStateOf(prefs.getBoolean(KEY_ALARM_ACTIVE, false)) }
    var alarmTime by remember { mutableStateOf(prefs.getLong(KEY_ALARM_TIME, 0L)) }
    var startTime by remember { mutableStateOf(prefs.getLong(KEY_START_TIME, 0L)) }
    var remainingTime by remember { mutableStateOf(0L) }

    LaunchedEffect(alarmActive) {
        if (alarmActive) {
            while (true) {
                remainingTime = (alarmTime - System.currentTimeMillis()).coerceAtLeast(0)
                if (remainingTime == 0L) break
                delay(1000)
            }
        }
    }

    fun cancelAlarm() {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
        prefs.edit().clear().apply()
        alarmActive = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Sleep Alarm",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = LightText
                )
            )
        },
        containerColor = DarkBackground
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(48.dp))

            if (!alarmActive) {

                // --- Idle State ---
                Text(
                    "Alarm will ring in",
                    color = MutedText,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    "$TEST_MINUTES minutes",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightText
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = {
                        val now = System.currentTimeMillis()
                        val triggerTime =
                            now + TimeUnit.MINUTES.toMillis(TEST_MINUTES)

                        val alarmIntent = Intent(
                            context,
                            AlarmReceiver::class.java
                        ).let {
                            PendingIntent.getBroadcast(
                                context,
                                0,
                                it,
                                PendingIntent.FLAG_IMMUTABLE
                            )
                        }

                        val showAppIntent = Intent(
                            context,
                            MainActivity::class.java
                        ).let {
                            PendingIntent.getActivity(
                                context,
                                1,
                                it,
                                PendingIntent.FLAG_IMMUTABLE
                            )
                        }

                        alarmManager.setAlarmClock(
                            AlarmManager.AlarmClockInfo(
                                triggerTime,
                                showAppIntent
                            ),
                            alarmIntent
                        )

                        prefs.edit().apply {
                            putBoolean(KEY_ALARM_ACTIVE, true)
                            putLong(KEY_START_TIME, now)
                            putLong(KEY_ALARM_TIME, triggerTime)
                            apply()
                        }

                        alarmActive = true
                        startTime = now
                        alarmTime = triggerTime
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentViolet
                    )
                ) {
                    Text(
                        "Start Sleep",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

            } else {

                // --- Active State ---
                val totalDuration = (alarmTime - startTime).coerceAtLeast(1)
                val progress = remainingTime.toFloat() / totalDuration

                Text(
                    "Sleeping…",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = LightText
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Canvas(Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            style = Stroke(22f)
                        )
                    }

                    Canvas(Modifier.fillMaxSize()) {
                        drawArc(
                            color = ProgressViolet,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(
                                width = 22f,
                                cap = StrokeCap.Round
                            )
                        )
                    }

                    Text(
                        formatRemainingTime(remainingTime),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightText
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val sessionStartTime =
                            prefs.getLong(KEY_START_TIME, System.currentTimeMillis())
                        cancelAlarm()
                        navController.navigate("summary/$sessionStartTime") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentViolet
                    )
                ) {
                    Text(
                        "I Woke Up",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { cancelAlarm() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        "Cancel Alarm",
                        color = MutedText
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun formatRemainingTime(ms: Long): String {
    if (ms <= 0) return "00:00"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return "%02d:%02d".format(minutes, seconds)
}
