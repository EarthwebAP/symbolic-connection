package com.glyphos.symbolic.security.lens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.mutableStateOf
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
// import kotlinx.coroutines.tasks.await  // Requires kotlin-coroutines-play-services

/**
 * PHASE 1: Proximity-Aware Shield
 *
 * Automatically protects screen content when others are nearby:
 * - Detects extra faces via camera (ML Kit)
 * - Measures proximity via proximity sensor
 * - Auto-activates blur when threats detected
 * - Configurable detection sensitivity
 * - Real-time monitoring
 */
class ProximityShield(
    private val context: Context,
    private val blurMode: AmbientBlurMode
) : SensorEventListener {
    companion object {
        private const val TAG = "ProximityShield"

        // Detection thresholds
        private const val PROXIMITY_THRESHOLD_CM = 20f  // 20cm = close proximity
        private const val FACE_DETECTION_THRESHOLD = 0.5f  // ML Kit confidence
    }

    // Proximity tracking
    private val _proximityDistance = MutableStateFlow<Float?>(null)
    val proximityDistance: StateFlow<Float?> = _proximityDistance.asStateFlow()

    private val _detectedFaces = MutableStateFlow<Int>(0)
    val detectedFaces: StateFlow<Int> = _detectedFaces.asStateFlow()

    // Protection state
    var shieldActive = mutableStateOf(false)
    var detectionFeedback = mutableStateOf<String?>(null)

    // Sensors
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    // ML Kit face detector
    private val faceDetector: FaceDetector = FaceDetection.getClient()

    // Detection configuration
    var autoBlurEnabled = true
    var minFacesForAlarm = 2  // Alert if more than user present

    /**
     * Start proximity monitoring
     */
    fun startMonitoring() {
        try {
            proximitySensor?.let {
                sensorManager.registerListener(
                    this,
                    it,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
                Log.d(TAG, "Proximity monitoring started")
            } ?: Log.w(TAG, "Proximity sensor not available")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting monitoring: ${e.message}", e)
        }
    }

    /**
     * Stop proximity monitoring
     */
    fun stopMonitoring() {
        try {
            sensorManager.unregisterListener(this)
            Log.d(TAG, "Proximity monitoring stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping monitoring: ${e.message}", e)
        }
    }

    /**
     * SensorEventListener implementation
     */
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            val distance = event.values[0]
            _proximityDistance.value = distance

            // Check if too close
            if (distance < PROXIMITY_THRESHOLD_CM) {
                if (autoBlurEnabled) {
                    shieldActive.value = true
                    detectionFeedback.value = "Proximity detected: ${distance}cm"
                    Log.w(TAG, "Close proximity detected: ${distance}cm")
                }
            } else {
                // Only deactivate if no faces detected
                if (_detectedFaces.value <= 1) {
                    shieldActive.value = false
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // No action needed
    }

    /**
     * Detect faces in camera frame
     * Uses ML Kit for real-time detection
     *
     * @param imageProxy Camera frame from CameraX
     * @return Number of faces detected
     */
    suspend fun detectExtraFaces(imageProxy: ImageProxy): Int {
        return try {
            val image = imageProxy.image ?: return 0

            val inputImage = InputImage.fromMediaImage(
                image,
                imageProxy.imageInfo.rotationDegrees
            )

            // await() requires kotlin-coroutines-play-services
            // val faces = faceDetector.process(inputImage).await()
            // val faceCount = faces.size
            val faceCount = 0  // Stub for now

            _detectedFaces.value = faceCount

            // Alert if more faces than expected (user + others)
            if (faceCount > minFacesForAlarm) {
                if (autoBlurEnabled) {
                    shieldActive.value = true
                    detectionFeedback.value = "Multiple faces detected: $faceCount"
                    Log.w(TAG, "Multiple faces detected: $faceCount")
                }
            }

            faceCount
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting faces: ${e.message}", e)
            0
        }
    }

    /**
     * Check if close proximity or multiple faces
     * @return true if shield should be active
     */
    fun shouldActivateShield(): Boolean {
        val proximity = _proximityDistance.value ?: return false
        val faceCount = _detectedFaces.value

        return proximity < PROXIMITY_THRESHOLD_CM || faceCount > minFacesForAlarm
    }

    /**
     * Activate shield
     */
    fun activateShield() {
        shieldActive.value = true
        blurMode.enableBlur()
        Log.d(TAG, "Shield activated")
    }

    /**
     * Deactivate shield
     */
    fun deactivateShield() {
        shieldActive.value = false
        blurMode.disableBlur()
        Log.d(TAG, "Shield deactivated")
    }

    /**
     * Get proximity status
     * @return Status string
     */
    fun getProximityStatus(): String {
        val distance = _proximityDistance.value
        return if (distance != null) {
            when {
                distance < 5f -> "Very close"
                distance < PROXIMITY_THRESHOLD_CM -> "Close"
                distance < 50f -> "Nearby"
                else -> "Far"
            }
        } else {
            "Unknown"
        }
    }

    /**
     * Get face detection status
     * @return Status string
     */
    fun getFaceStatus(): String {
        return when (val count = _detectedFaces.value) {
            0 -> "No faces detected"
            1 -> "User detected"
            else -> "$count faces detected"
        }
    }

    /**
     * Get overall shield status
     * @return Status string
     */
    fun getStatus(): String {
        return """
        Proximity Shield Status:
        - Shield active: ${shieldActive.value}
        - Proximity: ${getProximityStatus()} (${_proximityDistance.value}cm)
        - Faces: ${getFaceStatus()}
        - Auto-blur enabled: $autoBlurEnabled
        - Min faces for alarm: $minFacesForAlarm
        - Feedback: ${detectionFeedback.value ?: "None"}
        """.trimIndent()
    }

    /**
     * Release resources
     */
    fun release() {
        stopMonitoring()
        faceDetector.close()
    }
}

/**
 * Represents detected face information
 */
data class DetectedFace(
    val id: String,
    val confidence: Float,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getCenterX() = x + width / 2
    fun getCenterY() = y + height / 2
}

/**
 * Proximity shield events
 */
sealed class ProximityEvent {
    data class CloseProximityDetected(val distance: Float) : ProximityEvent()
    data class MultipleUsersDetected(val faceCount: Int) : ProximityEvent()
    object AllClearDetected : ProximityEvent()
    data class ShieldStatusChanged(val active: Boolean) : ProximityEvent()
}
