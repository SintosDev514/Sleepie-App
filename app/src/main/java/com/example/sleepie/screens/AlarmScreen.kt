package com.example.sleepie.screens

import android.app.*
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.sleepie.MainActivity
import com.example.sleepie.broadcastReceiver.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.*

/* -------------------- THEME -------------------- */

private val DarkBackground = Color(0xFF121212)
private val Accent = Color(0xFF7C4DFF)
private val LightText = Color.White
private val MutedText = Color(0xFF9E9E9E)

/* -------------------- SCREEN -------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmScreen(navController: NavController) {

    val context = LocalContext.current
    val alarmManager =
        remember { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }

    var calendar by remember { mutableStateOf(Calendar.getInstance()) }

    var repeatDaily by remember { mutableStateOf(false) }
    var enableLabel by remember { mutableStateOf(false) }
    var alarmLabel by remember { mutableStateOf("") }

    /* -------------------- SAVE ALARM -------------------- */

    fun saveAlarm() {
        val now = System.currentTimeMillis()

        // Prevent past alarms
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val triggerTime = calendar.timeInMillis

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_LABEL", if (enableLabel) alarmLabel else "")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            triggerTime.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showAppIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        if (repeatDaily) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, showAppIntent),
                pendingIntent
            )
        }

        // Save alarm details to SharedPreferences for the dashboard
        val prefs = context.getSharedPreferences("SleepiePrefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putLong("next_alarm_time", triggerTime)
            putString("next_alarm_label", alarmLabel)
            apply()
        }

        navController.popBackStack()
    }

    /* -------------------- UI -------------------- */

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Add Alarm", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = LightText
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(32.dp))

            TimeDatePicker(calendar) { calendar = it }

            Spacer(Modifier.height(32.dp))

            SettingRow("Add Label", enableLabel) {
                enableLabel = it
            }

            if (enableLabel) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = alarmLabel,
                    onValueChange = { alarmLabel = it },
                    label = { Text("Alarm Label") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))

            SettingRow("Repeat Daily", repeatDaily) {
                repeatDaily = it
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { saveAlarm() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent)
            ) {
                Text("Save Alarm", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

/* -------------------- TIME + DATE PICKER -------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDatePicker(
    calendar: Calendar,
    onUpdate: (Calendar) -> Unit
) {

    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val timeState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE)
    )

    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis
    )

    if (showTimePicker) {
        PickerDialog(
            title = "Select Time",
            onDismiss = { showTimePicker = false },
            onConfirm = {
                val newCal = calendar.clone() as Calendar
                newCal.set(Calendar.HOUR_OF_DAY, timeState.hour)
                newCal.set(Calendar.MINUTE, timeState.minute)
                onUpdate(newCal)
                showTimePicker = false
            }
        ) {
            TimePicker(state = timeState)
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { millis ->
                        // The millis from the date picker is for midnight UTC.
                        val selectedUtcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = millis
                        }
                        // Apply the selected year, month, and day to our existing
                        // calendar, which preserves the user's selected time and timezone.
                        val newCal = calendar.clone() as Calendar
                        newCal.set(Calendar.YEAR, selectedUtcCalendar.get(Calendar.YEAR))
                        newCal.set(Calendar.MONTH, selectedUtcCalendar.get(Calendar.MONTH))
                        newCal.set(Calendar.DAY_OF_MONTH, selectedUtcCalendar.get(Calendar.DAY_OF_MONTH))
                        onUpdate(newCal)
                    }
                    showDatePicker = false
                }) { Text("OK", color = Accent) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Accent)
                }
            }
        ) { DatePicker(state = dateState) }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        PickerCard("TIME", formatTime(calendar)) { showTimePicker = true }
        PickerCard("DATE", formatDate(calendar)) { showDatePicker = true }
    }
}

/* -------------------- COMPONENTS -------------------- */

@Composable
private fun PickerCard(title: String, value: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = MutedText, fontSize = 12.sp)
        Text(value, color = LightText, fontSize = 26.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = LightText, fontSize = 16.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/* -------------------- HELPERS -------------------- */

private fun formatTime(calendar: Calendar): String =
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)

private fun formatDate(calendar: Calendar): String =
    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(calendar.time)

@Composable
private fun PickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkBackground
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, color = LightText)
                Spacer(Modifier.height(12.dp))
                content()
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Accent) }
                    TextButton(onClick = onConfirm) { Text("OK", color = Accent) }
                }
            }
        }
    }
}
