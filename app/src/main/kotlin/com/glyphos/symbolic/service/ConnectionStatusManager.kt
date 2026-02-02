package com.glyphos.symbolic.service

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ConnectionStatusManager
 *
 * Manages the connection status indicator service.
 * Starts/stops the foreground service based on Socket.IO connection state.
 */
@Singleton
class ConnectionStatusManager @Inject constructor(
  @ApplicationContext private val context: Context
) {

  fun setConnected(isConnected: Boolean) {
    val intent = Intent(context, ConnectionStatusService::class.java).apply {
      action = if (isConnected) {
        ConnectionStatusService.ACTION_CONNECTED
      } else {
        ConnectionStatusService.ACTION_DISCONNECTED
      }
    }

    // Start the foreground service
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      ContextCompat.startForegroundService(context, intent)
    } else {
      context.startService(intent)
    }
  }

  fun stopConnectionService() {
    val intent = Intent(context, ConnectionStatusService::class.java)
    context.stopService(intent)
  }
}
