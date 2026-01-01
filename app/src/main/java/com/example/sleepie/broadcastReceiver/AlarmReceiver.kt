package com.example.sleepie.broadcastReceiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.sleepie.SleepieApplication
import com.example.sleepie.services.AlarmService
import com.example.sleepie.services.WakeLockManager

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Acquire the WakeLock to ensure the CPU stays awake.
        WakeLockManager.acquire(context)

        // Get alarm label from the intent
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: ""

        // Create a notification channel (required for Android Oreo and above)
        createNotificationChannel(context)

        // Start the foreground service to show the notification and play the alarm.
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("ALARM_LABEL", alarmLabel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sleepie Alarm Channel"
            val descriptionText = "Channel for Sleepie alarms"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(SleepieApplication.ALARM_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
