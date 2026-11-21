package com.aether.player.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.aether.player.R

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    companion object {
        const val CHANNEL_ID = "aether_playback_channel"
        const val NOTIFICATION_ID = 1001
        var currentMediaSession: MediaSession? = null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return currentMediaSession
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "AETHER Playback"
            val descriptionText = "Control de reproducción en segundo plano"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        currentMediaSession = null
        super.onDestroy()
    }
}
