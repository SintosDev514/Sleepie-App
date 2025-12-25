package com.example.sleepie.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import com.example.sleepie.services.AlarmService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This WakeLock is the critical fix. It prevents the CPU from sleeping
        // between the receiver being called and the service starting.
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Sleepie:AlarmWakeLock")
        wakeLock.acquire(60 * 1000L /* 1 minute timeout */)

        // Start the foreground service to show the notification and play the alarm
        val serviceIntent = Intent(context, AlarmService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // The WakeLock is automatically released after the timeout, but by then
        // the AlarmService will have acquired its own foreground service lock.
    }
}
