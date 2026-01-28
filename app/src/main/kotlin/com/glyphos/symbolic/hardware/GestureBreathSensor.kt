package com.glyphos.symbolic.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

/**
 * Gesture & Breath Sensing Module
 * Captures phone sensor data for:
 * - Hand gestures (accelerometer + gyroscope)
 * - Breath patterns (camera face detection + proximity sensor)
 * - Movement patterns (accelerometer)
 */
class GestureBreathSensor(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    // Sensor data
    private var accelX = 0f
    private var accelY = 0f
    private var accelZ = 0f
    private var gyroX = 0f
    private var gyroY = 0f
    private var gyroZ = 0f

    // Gesture detection state
    private var gestureBuffer = mutableListOf<FloatArray>()
    private var breathBuffer = mutableListOf<Float>()
    private var lastUpdateTime = 0L

    // Back-tap detection (2 taps)
    private var lastTapTime = 0L
    private var tapCount = 0

    // Callbacks
    var onGestureDetected: ((String) -> Unit)? = null
    var onBreathDetected: ((String) -> Unit)? = null

    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val proximityLamp: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    /**
     * Start sensor monitoring
     */
    fun startMonitoring() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        proximityLamp?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    /**
     * Stop sensor monitoring
     */
    fun stopMonitoring() {
        sensorManager.unregisterListener(this)
        cameraExecutor.shutdown()
    }

    override fun onSensorChanged(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelX = event.values[0]
                accelY = event.values[1]
                accelZ = event.values[2]

                // Detect gestures from acceleration patterns
                if (currentTime - lastUpdateTime > 100) {
                    detectGestures()
                    lastUpdateTime = currentTime
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                gyroX = event.values[0]
                gyroY = event.values[1]
                gyroZ = event.values[2]

                // Detect circular/rotational patterns (for unlock gestures)
                detectRotationalPatterns()
            }

            Sensor.TYPE_PROXIMITY -> {
                // Proximity sensor can detect breath patterns and hand proximity
                val proximity = event.values[0]
                breathBuffer.add(proximity)

                if (breathBuffer.size > 50) {
                    detectBreathPattern()
                    breathBuffer.clear()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Accuracy changes
    }

    private fun detectGestures() {
        val magnitude = kotlin.math.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ)
        val currentTime = System.currentTimeMillis()

        // **BACK-TAP DETECTION (2 taps on back of phone)**
        // Back of phone = positive Z-axis acceleration
        if (accelZ > 25 && magnitude > 25) {
            val timeSinceLastTap = currentTime - lastTapTime

            if (timeSinceLastTap < 500) {
                // Second tap within 500ms
                tapCount++
                if (tapCount == 2) {
                    onGestureDetected?.invoke("BACK_TAP_2X")
                    tapCount = 0
                }
            } else {
                // New tap sequence
                tapCount = 1
            }

            lastTapTime = currentTime
            return
        }

        // Reset tap counter if too much time passed
        if (currentTime - lastTapTime > 1000) {
            tapCount = 0
        }

        // Detect shake (high acceleration)
        if (magnitude > 30) {
            onGestureDetected?.invoke("SHAKE")
            return
        }

        // Detect swipe up
        if (accelY < -15 && abs(accelX) < 5) {
            onGestureDetected?.invoke("SWIPE_UP")
            return
        }

        // Detect swipe down
        if (accelY > 15 && abs(accelX) < 5) {
            onGestureDetected?.invoke("SWIPE_DOWN")
            return
        }

        // Detect swipe left
        if (accelX < -15 && abs(accelY) < 5) {
            onGestureDetected?.invoke("SWIPE_LEFT")
            return
        }

        // Detect swipe right
        if (accelX > 15 && abs(accelY) < 5) {
            onGestureDetected?.invoke("SWIPE_RIGHT")
            return
        }

        // Detect tap (quick spike in one direction)
        if (magnitude > 25 && magnitude < 35) {
            onGestureDetected?.invoke("TAP")
        }
    }

    private fun detectRotationalPatterns() {
        val rotationMagnitude = kotlin.math.sqrt(gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ)

        // Detect rotation (for pattern unlock)
        if (rotationMagnitude > 2.0) {
            onGestureDetected?.invoke("ROTATION")
        }

        // Detect figure-8 pattern (complex gesture)
        if (rotationMagnitude > 3.0 && gyroY > 1.5) {
            onGestureDetected?.invoke("FIGURE_8")
        }
    }

    private fun detectBreathPattern() {
        if (breathBuffer.isEmpty()) return

        // Analyze breath oscillation pattern
        val minProximity = breathBuffer.minOrNull() ?: 0f
        val maxProximity = breathBuffer.maxOrNull() ?: 0f
        val variation = maxProximity - minProximity

        // Breath creates proximity oscillation (hand moving away/toward sensor)
        if (variation > 1.0) {
            // Count oscillations (breath cycles)
            var oscillations = 0
            for (i in 1 until breathBuffer.size - 1) {
                val prev = breathBuffer[i - 1]
                val curr = breathBuffer[i]
                val next = breathBuffer[i + 1]

                if ((curr > prev && curr > next) || (curr < prev && curr < next)) {
                    oscillations++
                }
            }

            // Normal breath is 1-2 cycles per second
            if (oscillations > 3) {
                onBreathDetected?.invoke("BREATH_DETECTED")
                return
            }
        }
    }

    /**
     * Get current sensor values for debugging/display
     */
    fun getCurrentSensorData(): Map<String, Float> {
        return mapOf(
            "accelX" to accelX,
            "accelY" to accelY,
            "accelZ" to accelZ,
            "gyroX" to gyroX,
            "gyroY" to gyroY,
            "gyroZ" to gyroZ
        )
    }

    /**
     * Get gesture detection status
     */
    fun isGestureDetectionWorking(): Boolean {
        return accelerometer != null && gyroscope != null
    }

    /**
     * Get breath detection status
     */
    fun isBreathDetectionWorking(): Boolean {
        return proximityLamp != null
    }
}
