package com.example.sleepie.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.sleepie.services.AlarmService
import com.example.sleepie.services.WakeLockManager

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Acquire the WakeLock to ensure the CPU stays awake.
        WakeLockManager.acquire(context)

        // Start the foreground service to show the notification and play the alarm.
        val serviceIntent = Intent(context, AlarmService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
