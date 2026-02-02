package com.glyphos.symbolic.service

import android.app.Service
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.glyphos.symbolic.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * ConnectionStatusService
 *
 * Foreground service that shows a persistent connection status indicator
 * in the Android status bar (top of screen near WiFi signal).
 *
 * Shows:
 * - 🟢 When connected to Glyph007 server
 * - 🔴 When disconnected
 */
@AndroidEntryPoint
class ConnectionStatusService : Service() {

  companion object {
    const val CHANNEL_ID = "glyph_connection_channel"
    const val NOTIFICATION_ID = 1
    const val ACTION_CONNECTED = "com.glyphos.symbolic.CONNECTED"
    const val ACTION_DISCONNECTED = "com.glyphos.symbolic.DISCONNECTED"
  }

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_CONNECTED -> showConnectedNotification()
      ACTION_DISCONNECTED -> showDisconnectedNotification()
      else -> showDisconnectedNotification()
    }
    return START_STICKY
  }

  override fun onBind(intent: Intent?): IBinder? = null

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Glyph007 Connection",
        NotificationManager.IMPORTANCE_LOW // Minimal notification importance
      )
      channel.description = "Shows connection status to Glyph007 server"
      channel.enableVibration(false)
      channel.enableLights(false)
      channel.setShowBadge(true)

      val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
      manager.createNotificationChannel(channel)
    }
  }

  private fun showConnectedNotification() {
    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("Glyph007")
      .setContentText("Connected")
      .setSmallIcon(android.R.drawable.ic_dialog_info) // System drawable (will be colored green)
      .setOngoing(true) // Persistent - cannot be swiped away
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .setShowWhen(false)
      .setAutoCancel(false)
      .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
      .build()

    startForeground(NOTIFICATION_ID, notification)
  }

  private fun showDisconnectedNotification() {
    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("Glyph007")
      .setContentText("Disconnected")
      .setSmallIcon(android.R.drawable.ic_dialog_alert) // System drawable (warning icon)
      .setOngoing(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .setShowWhen(false)
      .setAutoCancel(false)
      .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
      .build()

    startForeground(NOTIFICATION_ID, notification)
  }
}
