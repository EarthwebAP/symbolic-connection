package com.glyphos.symbolic.hardware

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PHASE 5: Antenna Manager
 *
 * Network connectivity and antenna switching.
 * - Cellular/WiFi switching
 * - Signal strength monitoring
 * - Network type detection
 * - Emergency fallback modes
 */
class AntennaManager(private val context: Context) {
    companion object {
        private const val TAG = "AntennaManager"
    }

    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    private val _currentNetworkType = MutableStateFlow<NetworkType>(NetworkType.UNKNOWN)
    val currentNetworkType: StateFlow<NetworkType> = _currentNetworkType.asStateFlow()

    private val _signalStrength = MutableStateFlow(0f)
    val signalStrength: StateFlow<Float> = _signalStrength.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _emergencyMode = MutableStateFlow(false)
    val emergencyMode: StateFlow<Boolean> = _emergencyMode.asStateFlow()

    enum class NetworkType {
        CELLULAR_2G,
        CELLULAR_3G,
        CELLULAR_4G,
        CELLULAR_5G,
        WIFI,
        SATELLITE,
        EMERGENCY_BACKUP,
        UNKNOWN
    }

    fun updateNetworkType(): NetworkType {
        val type = detectNetworkType()
        _currentNetworkType.value = type
        Log.d(TAG, "Network type detected: ${type.name}")
        return type
    }

    fun updateSignalStrength(strength: Float) {
        // Normalize to 0-1 range
        _signalStrength.value = kotlin.math.min(kotlin.math.max(strength, 0f), 1f)
        _isConnected.value = strength > 0.3f

        Log.d(TAG, "Signal strength: ${String.format("%.2f", _signalStrength.value * 100)}%")
    }

    fun switchToWiFi() {
        _currentNetworkType.value = NetworkType.WIFI
        Log.d(TAG, "Switched to WiFi")
    }

    fun switchToCellular() {
        updateNetworkType()
        Log.d(TAG, "Switched to cellular")
    }

    fun enableEmergencyMode() {
        _emergencyMode.value = true
        Log.w(TAG, "Emergency network mode enabled - reduced data usage")
    }

    fun disableEmergencyMode() {
        _emergencyMode.value = false
        Log.d(TAG, "Emergency network mode disabled")
    }

    fun requestEmergencyBandwidth() {
        if (!_emergencyMode.value) {
            enableEmergencyMode()
        }
        Log.d(TAG, "Emergency bandwidth requested")
    }

    fun getPreferredNetwork(): NetworkType {
        return when {
            _emergencyMode.value -> NetworkType.EMERGENCY_BACKUP
            _currentNetworkType.value == NetworkType.WIFI -> NetworkType.WIFI
            _currentNetworkType.value.name.startsWith("CELLULAR") -> _currentNetworkType.value
            else -> NetworkType.UNKNOWN
        }
    }

    fun isHighSpeedNetwork(): Boolean {
        return _currentNetworkType.value in listOf(
            NetworkType.CELLULAR_4G,
            NetworkType.CELLULAR_5G,
            NetworkType.WIFI
        )
    }

    fun getStatistics(): AntennaStatistics {
        return AntennaStatistics(
            currentNetworkType = _currentNetworkType.value,
            signalStrength = _signalStrength.value,
            isConnected = _isConnected.value,
            emergencyMode = _emergencyMode.value,
            isHighSpeed = isHighSpeedNetwork()
        )
    }

    fun getStatus(): String {
        val stats = getStatistics()
        return """
        Antenna Manager Status:
        - Network: ${stats.currentNetworkType.name}
        - Signal: ${String.format("%.0f", stats.signalStrength * 100)}%
        - Connected: ${stats.isConnected}
        - Emergency: ${stats.emergencyMode}
        - High-speed: ${stats.isHighSpeed}
        """.trimIndent()
    }

    private fun detectNetworkType(): NetworkType {
        return try {
            @Suppress("DEPRECATION")
            when (telephonyManager?.networkType) {
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_IDEN,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT -> NetworkType.CELLULAR_2G

                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_EVDO_B,
                TelephonyManager.NETWORK_TYPE_EHRPD,
                TelephonyManager.NETWORK_TYPE_HSPAP -> NetworkType.CELLULAR_3G

                TelephonyManager.NETWORK_TYPE_LTE -> NetworkType.CELLULAR_4G

                TelephonyManager.NETWORK_TYPE_NR -> NetworkType.CELLULAR_5G

                else -> NetworkType.UNKNOWN
            }
        } catch (e: Exception) {
            NetworkType.UNKNOWN
        }
    }
}

data class AntennaStatistics(
    val currentNetworkType: AntennaManager.NetworkType,
    val signalStrength: Float,
    val isConnected: Boolean,
    val emergencyMode: Boolean,
    val isHighSpeed: Boolean
)
