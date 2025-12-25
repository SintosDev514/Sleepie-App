package com.example.sleepie.screens

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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
private val DarkViolet = Color(0xFF3700B3)
private val ProgressViolet = Color(0xFF6200EE)
private val LightText = Color.White
private val LightGrayText = Color(0xFFB0B0B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen() {
    val context = LocalContext.current
    val alarmManager = remember { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var alarmActive by remember { mutableStateOf(prefs.getBoolean(KEY_ALARM_ACTIVE, false)) }
    var alarmTime by remember { mutableStateOf(prefs.getLong(KEY_ALARM_TIME, 0L)) }
    var startTime by remember { mutableStateOf(prefs.getLong(KEY_START_TIME, 0L)) }
    var remainingTime by remember { mutableStateOf(0L) }

    var showPermissionDialog by remember { mutableStateOf(false) }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { /* The button's onClick logic will re-run the checks */ }
    )

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* The button's onClick logic will re-run the checks */ }
    )

    LaunchedEffect(key1 = alarmActive) {
        if (alarmActive) {
            while (true) {
                remainingTime = (alarmTime - System.currentTimeMillis()).coerceAtLeast(0)
                if (remainingTime == 0L) break
                delay(1000)
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sleep Alarm") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkViolet, titleContentColor = LightText)) },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!alarmActive) {
                Text("Alarm will ring in", color = LightGrayText)
                Spacer(modifier = Modifier.height(8.dp))
                Text("$TEST_MINUTES minutes", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = LightText)
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ProgressViolet, contentColor = LightText),
                    onClick = {
                        val hasExactAlarmPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms() else true
                        val hasNotificationPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED else true

                        when {
                            !hasExactAlarmPerm -> showPermissionDialog = true
                            !hasNotificationPerm -> notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            else -> {
                                val now = System.currentTimeMillis()
                                val triggerTime = now + TimeUnit.MINUTES.toMillis(TEST_MINUTES)
                                val alarmIntent = Intent(context, AlarmReceiver::class.java).let { PendingIntent.getBroadcast(context, 0, it, PendingIntent.FLAG_IMMUTABLE) }
                                val showAppIntent = Intent(context, MainActivity::class.java).let { PendingIntent.getActivity(context, 1, it, PendingIntent.FLAG_IMMUTABLE) }

                                alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerTime, showAppIntent), alarmIntent)

                                with(prefs.edit()) {
                                    putBoolean(KEY_ALARM_ACTIVE, true)
                                    putLong(KEY_START_TIME, now)
                                    putLong(KEY_ALARM_TIME, triggerTime)
                                    apply()
                                }
                                alarmActive = true
                                startTime = now
                                alarmTime = triggerTime
                            }
                        }
                    }
                ) { Text("Start 2-Minute Test Alarm") }
            } else {
                val totalDuration = (alarmTime - startTime).coerceAtLeast(1)
                val progress = remainingTime.toFloat() / totalDuration

                Text("Alarm Active", fontSize = 22.sp, color = LightText)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Ends in:", color = LightGrayText)
                Spacer(modifier = Modifier.height(24.dp))
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) { drawCircle(Color.Black.copy(alpha = 0.3f), style = Stroke(20f)) }
                    Canvas(modifier = Modifier.fillMaxSize()) { drawArc(ProgressViolet, -90f, 360f * progress, false, style = Stroke(20f, cap = StrokeCap.Round)) }
                    Text(formatRemainingTime(remainingTime), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = LightText)
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        val intent = Intent(context, AlarmReceiver::class.java)
                        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                        alarmManager.cancel(pendingIntent)
                        prefs.edit().clear().apply()
                        alarmActive = false
                    }
                ) { Text("Cancel Alarm", color = Color.White) }
            }
        }
    }

    if (showPermissionDialog) {
        PermissionDialog(
            onDismiss = { showPermissionDialog = false },
            title = "Permission Required",
            text = "To set reliable alarms, Sleepie needs the 'Alarms & reminders' permission. Please find and enable it for Sleepie in your system settings.",
            onConfirm = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply { data = Uri.parse("package:${context.packageName}") }
                    settingsLauncher.launch(intent)
                }
            }
        )
    }
}

@Composable
private fun PermissionDialog(onDismiss: () -> Unit, title: String, text: String, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { Button(onClick = { onConfirm(); onDismiss() }) { Text("Open Settings") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Done") } }
    )
}

private fun formatRemainingTime(ms: Long): String {
    if (ms <= 0) return "00:00"
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
}
