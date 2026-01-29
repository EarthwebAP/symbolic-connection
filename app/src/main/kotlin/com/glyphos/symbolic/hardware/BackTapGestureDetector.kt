package com.glyphos.symbolic.hardware

import android.content.Context
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * PHASE 5: Back-Tap Gesture Detector
 *
 * Back-of-device tap gesture detection for emergency/ritual activation.
 * - Single tap detection
 * - Double tap detection
 * - Tap pattern recognition
 * - Accelerometer-based detection
 */
class BackTapGestureDetector(context: Context) {
    companion object {
        private const val TAG = "BackTapGestureDetector"
        private const val TAP_THRESHOLD = 25f // Acceleration threshold
        private const val DOUBLE_TAP_TIMEOUT_MS = 500L
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    private val _lastTapTime = MutableStateFlow(0L)
    val lastTapTime: StateFlow<Long> = _lastTapTime.asStateFlow()

    private val _tapCount = MutableStateFlow(0)
    val tapCount: StateFlow<Int> = _tapCount.asStateFlow()

    private val _detectedGestures = MutableStateFlow<List<TapGesture>>(emptyList())
    val detectedGestures: StateFlow<List<TapGesture>> = _detectedGestures.asStateFlow()

    private val _gesturePatterns = MutableStateFlow<List<GesturePattern>>(emptyList())
    val gesturePatterns: StateFlow<List<GesturePattern>> = _gesturePatterns.asStateFlow()

    data class TapGesture(
        val id: String = UUID.randomUUID().toString(),
        val tapCount: Int,
        val timestamp: Long = System.currentTimeMillis(),
        val acceleration: Float = 0f,
        val patternMatched: String? = null
    )

    data class GesturePattern(
        val name: String,
        val tapSequence: List<Int>, // e.g., [1, 2] for single then double
        val timeoutMs: Long = 2000,
        val action: suspend () -> Unit = {}
    )

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type != android.hardware.Sensor.TYPE_ACCELEROMETER) return

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calculate total acceleration (magnitude)
            val acceleration = kotlin.math.sqrt((x*x + y*y + z*z).toDouble()).toFloat()

            // Check for tap (high acceleration on Z-axis typically indicates back tap)
            if (kotlin.math.abs(z) > TAP_THRESHOLD) {
                registerTap(acceleration)
            }
        }

        override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
    }

    fun startDetection() {
        if (accelerometer == null) {
            Log.w(TAG, "Accelerometer not available")
            return
        }

        _isActive.value = true
        sensorManager?.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        Log.d(TAG, "Back-tap gesture detection started")
    }

    fun stopDetection() {
        _isActive.value = false
        sensorManager?.unregisterListener(sensorListener)
        Log.d(TAG, "Back-tap gesture detection stopped")
    }

    private fun registerTap(acceleration: Float) {
        val currentTime = System.currentTimeMillis()
        val lastTime = _lastTapTime.value
        val timeSinceLastTap = currentTime - lastTime

        val isDoubleTap = timeSinceLastTap < DOUBLE_TAP_TIMEOUT_MS

        val newTapCount = if (isDoubleTap) _tapCount.value + 1 else 1

        _tapCount.value = newTapCount
        _lastTapTime.value = currentTime

        val gesture = TapGesture(
            tapCount = newTapCount,
            acceleration = acceleration
        )

        _detectedGestures.value = _detectedGestures.value + gesture

        Log.d(TAG, "Tap detected: count=$newTapCount, acceleration=${String.format("%.2f", acceleration)}")

        // Check for pattern matches
        checkPatternMatches()

        // Reset tap count after timeout
        if (!isDoubleTap) {
            _tapCount.value = 1
        }
    }

    private fun checkPatternMatches() {
        val patterns = _gesturePatterns.value
        val tapCount = _tapCount.value

        for (pattern in patterns) {
            if (pattern.tapSequence.last() == tapCount) {
                Log.d(TAG, "Gesture pattern matched: ${pattern.name}")
                // Pattern matched - action would be triggered here
            }
        }
    }

    fun registerPattern(
        name: String,
        tapSequence: List<Int>,
        action: suspend () -> Unit = {}
    ) {
        val pattern = GesturePattern(
            name = name,
            tapSequence = tapSequence,
            action = action
        )
        _gesturePatterns.value = _gesturePatterns.value + pattern
        Log.d(TAG, "Gesture pattern registered: $name")
    }

    fun registerEmergencySeal() {
        registerPattern(
            name = "Emergency Seal",
            tapSequence = listOf(1, 2), // Single tap then double tap
            action = { Log.d(TAG, "Emergency seal activated") }
        )
    }

    fun getRecentGestures(count: Int = 5): List<TapGesture> {
        return _detectedGestures.value.takeLast(count)
    }

    fun getStatistics(): BackTapStatistics {
        return BackTapStatistics(
            isActive = _isActive.value,
            totalTaps = _detectedGestures.value.size,
            patternsRegistered = _gesturePatterns.value.size,
            lastTapDelta = System.currentTimeMillis() - _lastTapTime.value,
            currentTapSequence = _tapCount.value
        )
    }

    fun getStatus(): String {
        val stats = getStatistics()
        return """
        Back-Tap Detector Status:
        - Active: ${stats.isActive}
        - Total taps: ${stats.totalTaps}
        - Patterns: ${stats.patternsRegistered}
        - Last tap: ${stats.lastTapDelta}ms ago
        - Current sequence: ${stats.currentTapSequence}
        """.trimIndent()
    }
}

data class BackTapStatistics(
    val isActive: Boolean,
    val totalTaps: Int,
    val patternsRegistered: Int,
    val lastTapDelta: Long,
    val currentTapSequence: Int
)
