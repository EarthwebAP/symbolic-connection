package com.glyphos.symbolic.hardware

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PHASE 5: Reverse Charging Manager
 *
 * Wireless reverse charging (device as power source).
 * - Reverse charging capability detection
 * - Power output management
 * - Charging status monitoring
 */
class ReverseChargingManager(private val context: Context) {
    companion object {
        private const val TAG = "ReverseChargingManager"
    }

    private val _reverseChargingAvailable = MutableStateFlow(false)
    val reverseChargingAvailable: StateFlow<Boolean> = _reverseChargingAvailable.asStateFlow()

    private val _reverseChargingActive = MutableStateFlow(false)
    val reverseChargingActive: StateFlow<Boolean> = _reverseChargingActive.asStateFlow()

    private val _batteryLevel = MutableStateFlow(0f)
    val batteryLevel: StateFlow<Float> = _batteryLevel.asStateFlow()

    private val _outputPower = MutableStateFlow(0f)
    val outputPower: StateFlow<Float> = _outputPower.asStateFlow()

    init {
        _reverseChargingAvailable.value = isReverseChargingSupported()
    }

    fun startReverseCharging() {
        if (!_reverseChargingAvailable.value) {
            Log.w(TAG, "Reverse charging not available on this device")
            return
        }

        _reverseChargingActive.value = true
        _outputPower.value = 5f // Default 5W output

        Log.d(TAG, "Reverse charging started (${_outputPower.value}W)")
    }

    fun stopReverseCharging() {
        _reverseChargingActive.value = false
        _outputPower.value = 0f

        Log.d(TAG, "Reverse charging stopped")
    }

    fun setOutputPower(watts: Float) {
        if (watts < 0 || watts > 15) {
            Log.w(TAG, "Invalid output power: $watts W (must be 0-15W)")
            return
        }

        _outputPower.value = watts
        Log.d(TAG, "Output power set to: ${watts}W")
    }

    fun updateBatteryLevel() {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        if (intent != null) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            _batteryLevel.value = level.toFloat() / scale.toFloat()
        }
    }

    fun canReverseCharge(): Boolean {
        // Only if battery above 20% and reverse charging available
        return _reverseChargingAvailable.value && _batteryLevel.value > 0.2f
    }

    fun getStatistics(): ReverseChargingStatistics {
        return ReverseChargingStatistics(
            isAvailable = _reverseChargingAvailable.value,
            isActive = _reverseChargingActive.value,
            batteryLevel = _batteryLevel.value,
            outputPower = _outputPower.value,
            canCharge = canReverseCharge()
        )
    }

    fun getStatus(): String {
        val stats = getStatistics()
        return """
        Reverse Charging Status:
        - Available: ${stats.isAvailable}
        - Active: ${stats.isActive}
        - Battery: ${String.format("%.1f", stats.batteryLevel * 100)}%
        - Output: ${String.format("%.1f", stats.outputPower)}W
        - Can charge: ${stats.canCharge}
        """.trimIndent()
    }

    private fun isReverseChargingSupported(): Boolean {
        // Check device capabilities
        return try {
            val feature = "android.hardware.battery.wireless_reverse"
            context.packageManager?.hasSystemFeature(feature) ?: false
        } catch (e: Exception) {
            false
        }
    }
}

data class ReverseChargingStatistics(
    val isAvailable: Boolean,
    val isActive: Boolean,
    val batteryLevel: Float,
    val outputPower: Float,
    val canCharge: Boolean
)
