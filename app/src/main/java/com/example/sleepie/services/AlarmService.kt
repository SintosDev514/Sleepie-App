package com.example.sleepie.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.sleepie.SleepieApplication
import com.example.sleepie.screens.AlarmRingingActivity

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: android.os.Vibrator? = null
    private var playCount = 0

    companion object {
        fun stopService(context: Context) {
            val intent = Intent(context, AlarmService::class.java)
            context.stopService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Release the WakeLock that was acquired by the AlarmReceiver.
        WakeLockManager.release()

        // --- Notification and Full-Screen Intent ---
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, SleepieApplication.ALARM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Wake Up!")
            .setContentText("Your alarm is ringing.")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .addAction(0, "Open Wake-up Screen", fullScreenPendingIntent)
            .build()

        startForeground(1001, notification)

        // --- VIBRATION ---
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as android.os.Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 1000), 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 500, 1000), 0)
        }

        // --- RINGTONE (10-play loop) ---
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (alarmUri != null) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@AlarmService, alarmUri)
                    setOnPreparedListener { start() }
                    setOnCompletionListener {
                        playCount++
                        if (playCount < 10) {
                            seekTo(0)
                            start()
                        } else {
                            stopSelf()
                        }
                    }
                    prepareAsync()
                }
            } else {
                stopSelf()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
