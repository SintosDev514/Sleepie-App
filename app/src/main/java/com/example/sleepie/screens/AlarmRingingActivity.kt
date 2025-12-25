package com.example.sleepie.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sleepie.MainActivity
import com.example.sleepie.services.AlarmService
import com.example.sleepie.ui.theme.SleepieTheme

class AlarmRingingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SleepieTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Wake Up!", style = MaterialTheme.typography.displayLarge)
                    Button(
                        onClick = {
                            // Stop the alarm service (ringtone and vibration)
                            AlarmService.stopService(this@AlarmRingingActivity)

                            // Get the start time from SharedPreferences
                            val prefs = getSharedPreferences("sleepie_alarm_prefs", Context.MODE_PRIVATE)
                            val startTime = prefs.getLong("start_time", System.currentTimeMillis())
                            prefs.edit().clear().apply()

                            // Start the main app and navigate to the summary screen
                            val intent = Intent(this@AlarmRingingActivity, MainActivity::class.java).apply {
                                putExtra("navigate_to", "summary/$startTime")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            finish()
                        },
                        modifier = Modifier.padding(top = 32.dp)
                    ) {
                        Text("Turn Off Alarm")
                    }
                }
            }
        }
    }
}
