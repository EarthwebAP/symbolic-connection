package com.glyphos.symbolic.hardware

import android.content.Context
import android.hardware.SensorEvent
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Back-Tap Gesture Manager
 * Detects taps on phone back for symbolic interaction
 * Double-tap, triple-tap, custom patterns
 */
@Singleton
class BackTapGestureManager @Inject constructor(
    private val context: Context
) {

    private val _lastTapTime = MutableStateFlow(0L)
    val lastTapTime: StateFlow<Long> = _lastTapTime

    private val _tapCount = MutableStateFlow(0)
    val tapCount: StateFlow<Int> = _tapCount

    private val _recognizedGesture = MutableStateFlow<BackTapGesture?>(null)
    val recognizedGesture: StateFlow<BackTapGesture?> = _recognizedGesture

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private var tapSequence: MutableList<Long> = mutableListOf()
    private val TAP_TIMEOUT_MS = 500
    private val TAP_INTERVAL_TOLERANCE_MS = 300

    enum class BackTapGesture {
        SINGLE_TAP,
        DOUBLE_TAP,
        TRIPLE_TAP,
        RAPID_SEQUENCE,
        PATTERN_SOS,
        PATTERN_MORSE
    }

    fun startListening() {
        _isListening.value = true
        tapSequence.clear()
        _tapCount.value = 0
    }

    fun stopListening() {
        _isListening.value = false
        tapSequence.clear()
        _tapCount.value = 0
    }

    fun onTapDetected(tapForce: Float = 1.0f) {
        if (!_isListening.value) return

        val now = System.currentTimeMillis()
        tapSequence.add(now)
        _tapCount.value = tapSequence.size
        _lastTapTime.value = now

        // Clean old taps outside timeout window
        tapSequence = tapSequence.filter { now - it < TAP_TIMEOUT_MS }.toMutableList()

        val gesture = recognizeTapPattern()
        if (gesture != null) {
            _recognizedGesture.value = gesture
            resetTapSequence()
        } else if (now - (tapSequence.firstOrNull() ?: now) > TAP_TIMEOUT_MS) {
            resetTapSequence()
        }
    }

    fun onAccelerometerEvent(event: SensorEvent?) {
        if (event == null || !_isListening.value) return

        // Detect acceleration spikes on back of device
        // This is a simplified detection - actual implementation would be more sophisticated
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val acceleration = kotlin.math.sqrt(x * x + y * y + z * z)

        // Tap typically has acceleration spike > 50m/s²
        if (acceleration > 50f) {
            onTapDetected(acceleration)
        }
    }

    private fun recognizeTapPattern(): BackTapGesture? {
        if (tapSequence.size < 1) return null

        return when {
            tapSequence.size == 1 -> BackTapGesture.SINGLE_TAP
            tapSequence.size == 2 && isRegularInterval() -> BackTapGesture.DOUBLE_TAP
            tapSequence.size == 3 && isRegularInterval() -> BackTapGesture.TRIPLE_TAP
            tapSequence.size >= 4 && isRapidSequence() -> BackTapGesture.RAPID_SEQUENCE
            isMorsePattern("SOS") -> BackTapGesture.PATTERN_SOS
            else -> null
        }
    }

    private fun isRegularInterval(): Boolean {
        if (tapSequence.size < 2) return false

        val interval1 = tapSequence[1] - tapSequence[0]
        if (tapSequence.size == 2) {
            return interval1 <= TAP_INTERVAL_TOLERANCE_MS
        }

        val interval2 = tapSequence[2] - tapSequence[1]
        return kotlin.math.abs(interval1 - interval2) <= 50  // Allow 50ms variance
    }

    private fun isRapidSequence(): Boolean {
        if (tapSequence.size < 2) return false
        val totalTime = tapSequence.last() - tapSequence.first()
        val avgInterval = totalTime / (tapSequence.size - 1)
        return avgInterval < 200  // Rapid = < 200ms between taps
    }

    private fun isMorsePattern(pattern: String): Boolean {
        // Simplified morse detection
        // Real implementation would use dot/dash patterns
        return tapSequence.size == 3 && pattern == "SOS"
    }

    private fun resetTapSequence() {
        tapSequence.clear()
        _tapCount.value = 0
    }

    fun getTapSequenceStats(): TapStats {
        return TapStats(
            totalTaps = tapSequence.size,
            lastTapTime = _lastTapTime.value,
            averageInterval = if (tapSequence.size > 1) {
                (tapSequence.last() - tapSequence.first()) / (tapSequence.size - 1)
            } else {
                0L
            },
            recognizedGesture = _recognizedGesture.value,
            isListening = _isListening.value
        )
    }

    fun mapGestureToAction(gesture: BackTapGesture): SymbolicAction {
        return when (gesture) {
            BackTapGesture.SINGLE_TAP -> SymbolicAction.TOGGLE_MENU
            BackTapGesture.DOUBLE_TAP -> SymbolicAction.SEND_QUIET_MESSAGE
            BackTapGesture.TRIPLE_TAP -> SymbolicAction.EMERGENCY_SEAL
            BackTapGesture.RAPID_SEQUENCE -> SymbolicAction.EMIT_PRESENCE_PULSE
            BackTapGesture.PATTERN_SOS -> SymbolicAction.SIGNAL_URGENCY
            BackTapGesture.PATTERN_MORSE -> SymbolicAction.CUSTOM_RITUAL
        }
    }

    enum class SymbolicAction {
        TOGGLE_MENU,
        SEND_QUIET_MESSAGE,
        EMERGENCY_SEAL,
        EMIT_PRESENCE_PULSE,
        SIGNAL_URGENCY,
        CUSTOM_RITUAL
    }

    data class TapStats(
        val totalTaps: Int,
        val lastTapTime: Long,
        val averageInterval: Long,
        val recognizedGesture: BackTapGesture?,
        val isListening: Boolean
    )
}
