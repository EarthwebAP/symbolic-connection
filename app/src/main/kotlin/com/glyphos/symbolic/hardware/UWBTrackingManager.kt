package com.glyphos.symbolic.hardware

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * PHASE 5: UWB (Ultra-Wideband) Tracking Manager
 *
 * Ultra-wideband proximity detection and ranging.
 * - Presence proximity detection
 * - Presence-bound access triggers
 * - Dual-device proximity awareness
 * - Distance-aware interactions
 */
class UWBTrackingManager(context: Context) {
    companion object {
        private const val TAG = "UWBTrackingManager"
        private const val PROXIMITY_THRESHOLD_CM = 30f // 30cm for presence detection
        private const val INTIMATE_DISTANCE_CM = 45f
        private const val PERSONAL_DISTANCE_CM = 120f
    }

    private val _isAvailable = MutableStateFlow(isUWBAvailable(context))
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val _trackingActive = MutableStateFlow(false)
    val trackingActive: StateFlow<Boolean> = _trackingActive.asStateFlow()

    private val _proximityZone = MutableStateFlow<ProximityZone>(ProximityZone.FAR)
    val proximityZone: StateFlow<ProximityZone> = _proximityZone.asStateFlow()

    private val _distance = MutableStateFlow(0f)
    val distance: StateFlow<Float> = _distance.asStateFlow()

    private val _detectedDevices = MutableStateFlow<List<DetectedDevice>>(emptyList())
    val detectedDevices: StateFlow<List<DetectedDevice>> = _detectedDevices.asStateFlow()

    private val _proximityEvents = MutableStateFlow<List<ProximityEvent>>(emptyList())
    val proximityEvents: StateFlow<List<ProximityEvent>> = _proximityEvents.asStateFlow()

    enum class ProximityZone {
        INTIMATE,      // < 45cm
        PERSONAL,      // 45-120cm
        SOCIAL,        // 120-360cm
        FAR            // > 360cm
    }

    data class DetectedDevice(
        val id: String = UUID.randomUUID().toString(),
        val address: String,
        val distance: Float, // in cm
        val confidence: Float,
        val lastSeen: Long = System.currentTimeMillis(),
        val signalStrength: Float = 0f
    )

    data class ProximityEvent(
        val id: String = UUID.randomUUID().toString(),
        val deviceId: String,
        val timestamp: Long = System.currentTimeMillis(),
        val eventType: String, // "ENTERED", "EXITED", "DISTANCE_CHANGED"
        val distance: Float,
        val zone: ProximityZone
    )

    fun startTracking() {
        if (!_isAvailable.value) {
            Log.w(TAG, "UWB not available on this device")
            return
        }

        _trackingActive.value = true
        Log.d(TAG, "UWB tracking started")
    }

    fun stopTracking() {
        _trackingActive.value = false
        _proximityZone.value = ProximityZone.FAR
        _distance.value = 0f
        Log.d(TAG, "UWB tracking stopped")
    }

    suspend fun updateProximity(distanceCm: Float, deviceAddress: String? = null) {
        if (!_trackingActive.value) return

        _distance.value = distanceCm

        val zone = when {
            distanceCm < INTIMATE_DISTANCE_CM -> ProximityZone.INTIMATE
            distanceCm < PERSONAL_DISTANCE_CM -> ProximityZone.PERSONAL
            distanceCm < 360f -> ProximityZone.SOCIAL
            else -> ProximityZone.FAR
        }

        val previousZone = _proximityZone.value
        _proximityZone.value = zone

        // Record event if zone changed
        if (previousZone != zone) {
            val event = ProximityEvent(
                deviceId = deviceAddress ?: "unknown",
                eventType = when {
                    zone == ProximityZone.INTIMATE && previousZone != ProximityZone.INTIMATE -> "ENTERED_INTIMATE"
                    zone != ProximityZone.INTIMATE && previousZone == ProximityZone.INTIMATE -> "EXITED_INTIMATE"
                    else -> "ZONE_CHANGED"
                },
                distance = distanceCm,
                zone = zone
            )
            _proximityEvents.value = _proximityEvents.value + event

            Log.d(TAG, "Proximity zone changed to: $zone (${distanceCm}cm)")
        }

        // Trigger access if intimate proximity
        if (zone == ProximityZone.INTIMATE) {
            onIntimateProximityDetected(deviceAddress ?: "unknown")
        }
    }

    private fun onIntimateProximityDetected(deviceAddress: String) {
        Log.d(TAG, "Intimate proximity detected: $deviceAddress")
        // This can trigger presence-bound access unlocks
    }

    fun addDevice(address: String, distance: Float, confidence: Float) {
        val device = DetectedDevice(
            address = address,
            distance = distance,
            confidence = confidence
        )

        _detectedDevices.value = _detectedDevices.value
            .filter { it.address != address }
            .plus(device)

        Log.d(TAG, "Device detected: $address at ${distance}cm")
    }

    fun removeDevice(address: String) {
        _detectedDevices.value = _detectedDevices.value.filter { it.address != address }
        Log.d(TAG, "Device removed: $address")
    }

    fun getDeviceDistance(address: String): Float? {
        return _detectedDevices.value.firstOrNull { it.address == address }?.distance
    }

    fun isIntimateProximity(address: String? = null): Boolean {
        return if (address != null) {
            _detectedDevices.value.any {
                it.address == address && it.distance < INTIMATE_DISTANCE_CM
            }
        } else {
            _proximityZone.value == ProximityZone.INTIMATE
        }
    }

    fun getStatistics(): UWBStatistics {
        return UWBStatistics(
            isAvailable = _isAvailable.value,
            trackingActive = _trackingActive.value,
            devicesDetected = _detectedDevices.value.size,
            intimateProximityDetections = _proximityEvents.value.count {
                it.zone == ProximityZone.INTIMATE
            },
            currentZone = _proximityZone.value,
            currentDistance = _distance.value
        )
    }

    fun getStatus(): String {
        val stats = getStatistics()
        return """
        UWB Tracking Status:
        - Available: ${stats.isAvailable}
        - Active: ${stats.trackingActive}
        - Current zone: ${stats.currentZone}
        - Current distance: ${String.format("%.1f", stats.currentDistance)}cm
        - Devices: ${stats.devicesDetected}
        - Intimate detections: ${stats.intimateProximityDetections}
        """.trimIndent()
    }

    private fun isUWBAvailable(context: Context): Boolean {
        // In real implementation, check device capabilities
        return try {
            context.packageManager?.hasSystemFeature("android.hardware.uwb") ?: false
        } catch (e: Exception) {
            false
        }
    }
}

data class UWBStatistics(
    val isAvailable: Boolean,
    val trackingActive: Boolean,
    val devicesDetected: Int,
    val intimateProximityDetections: Int,
    val currentZone: UWBTrackingManager.ProximityZone,
    val currentDistance: Float
)
