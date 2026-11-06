package com.bearinmind.equalizer314.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bearinmind.equalizer314.MainActivity
import com.bearinmind.equalizer314.dsp.AudioDspEngine
import com.bearinmind.equalizer314.model.AudioHardwareCapabilities

class EqService : Service() {

    private val binder = LocalBinder()
    lateinit var dspEngine: AudioDspEngine
        private set

    private var isEngineOn = true
    private var currentPresetName = "Flat (Neutral)"

    inner class LocalBinder : Binder() {
        fun getService(): EqService = this@EqService
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        dspEngine = AudioDspEngine(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE_POWER -> {
                isEngineOn = !isEngineOn
                dspEngine.setPower(isEngineOn)
                updateNotification()
            }
            ACTION_STOP_SERVICE -> {
                stopForeground(true)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        startInForeground()
        return START_STICKY
    }

    private fun startInForeground() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val toggleIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, EqService::class.java).apply { action = ACTION_TOGGLE_POWER },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val statusText = if (isEngineOn) "Processing Active • $currentPresetName" else "Bypassed / Idle"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AudioFX Studio")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .addAction(
                if (isEngineOn) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isEngineOn) "Bypass" else "Power On",
                toggleIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun updateStatus(presetName: String, isPowered: Boolean) {
        currentPresetName = presetName
        isEngineOn = isPowered
        updateNotification()
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AudioFX Studio Engine",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time status of system-wide audio effects engine"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        dspEngine.release()
    }

    companion object {
        const val CHANNEL_ID = "audiofx_service_channel"
        const val NOTIFICATION_ID = 3141

        const val ACTION_TOGGLE_POWER = "com.bearinmind.equalizer314.ACTION_TOGGLE_POWER"
        const val ACTION_STOP_SERVICE = "com.bearinmind.equalizer314.ACTION_STOP_SERVICE"

        var instance: EqService? = null
            private set
    }
}
