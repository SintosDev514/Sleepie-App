package com.example.sleepie.services

import android.content.Context
import android.os.PowerManager

object WakeLockManager {
    private var wakeLock: PowerManager.WakeLock? = null

    fun acquire(context: Context) {
        if (wakeLock?.isHeld == true) return
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Sleepie:AlarmWakeLock").apply {
            setReferenceCounted(false)
            acquire(60 * 1000L /* 1-minute safety timeout */)
        }
    }

    fun release() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }
}
