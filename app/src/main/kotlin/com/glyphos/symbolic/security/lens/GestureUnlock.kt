package com.glyphos.symbolic.security.lens

import android.content.Context
import android.media.AudioRecord
import android.media.MediaRecorder
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * PHASE 1: Gesture & Object Unlock
 *
 * Multi-method authentication:
 * - Finger trace patterns (gestures)
 * - Sound cues (humming, whispers)
 * - NFC object recognition
 * - Visual markers/barcodes
 */
class GestureUnlock(
    private val context: Context
) {
    companion object {
        private const val TAG = "GestureUnlock"

        // Gesture detection
        private const val MIN_GESTURE_LENGTH = 100f
        private const val GESTURE_THRESHOLD_POINTS = 10

        // Sound detection
        private const val WHISPER_FREQUENCY_MIN = 1000f  // Hz
        private const val WHISPER_FREQUENCY_MAX = 4000f  // Hz
        private const val WHISPER_THRESHOLD = 0.2f
    }

    // Gesture tracking
    var gestureTracing = mutableStateOf(false)
    var detectedGesture = mutableStateOf<GesturePattern?>(null)
    var detectionFeedback = mutableStateOf<String?>(null)

    private var currentGesturePoints = mutableListOf<Offset>()

    /**
     * Represents a detected gesture pattern
     */
    data class GesturePattern(
        val points: List<Offset>,
        val length: Float,
        val complexity: Float,  // 0-1
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun getDirection(): String {
            if (points.size < 2) return "INVALID"

            val dx = points.last().x - points.first().x
            val dy = points.last().y - points.first().y

            return when {
                abs(dx) > abs(dy) -> if (dx > 0) "RIGHT" else "LEFT"
                else -> if (dy > 0) "DOWN" else "UP"
            }
        }

        fun getShape(): String {
            return when {
                length < 200 -> "SMALL"
                length < 500 -> "MEDIUM"
                else -> "LARGE"
            }
        }
    }

    /**
     * Start tracking finger gesture
     * @param startPoint Initial touch point
     */
    fun startGestureTrace(startPoint: Offset) {
        gestureTracing.value = true
        currentGesturePoints.clear()
        currentGesturePoints.add(startPoint)
        Log.d(TAG, "Gesture trace started at $startPoint")
    }

    /**
     * Update gesture with new touch point
     * @param point New touch point
     */
    fun updateGestureTrace(point: Offset) {
        if (!gestureTracing.value) return

        // Only add if point is far enough from last point
        if (currentGesturePoints.isNotEmpty()) {
            val lastPoint = currentGesturePoints.last()
            val distance = sqrt(
                (point.x - lastPoint.x).powInt(2) + (point.y - lastPoint.y).powInt(2)
            )
            if (distance < 5f) return  // Minimum point distance
        }

        currentGesturePoints.add(point)
    }

    /**
     * Complete gesture trace and detect pattern
     * @return GesturePattern if valid
     */
    fun endGestureTrace(): GesturePattern? {
        if (!gestureTracing.value) return null
        gestureTracing.value = false

        if (currentGesturePoints.size < GESTURE_THRESHOLD_POINTS) {
            Log.d(TAG, "Gesture too short: ${currentGesturePoints.size} points")
            return null
        }

        // Calculate gesture metrics
        val length = calculateGestureLength()
        if (length < MIN_GESTURE_LENGTH) {
            Log.d(TAG, "Gesture too short: $length px")
            return null
        }

        val complexity = calculateComplexity()

        val pattern = GesturePattern(
            points = currentGesturePoints.toList(),
            length = length,
            complexity = complexity
        )

        detectedGesture.value = pattern
        detectionFeedback.value = "Gesture detected: ${pattern.getDirection()} ${pattern.getShape()}"

        Log.d(TAG, "Gesture detected: ${pattern.getDirection()} (length=$length, complexity=$complexity)")
        return pattern
    }

    /**
     * Calculate total gesture length
     */
    private fun calculateGestureLength(): Float {
        var length = 0f
        for (i in 0 until currentGesturePoints.size - 1) {
            val p1 = currentGesturePoints[i]
            val p2 = currentGesturePoints[i + 1]
            length += sqrt((p2.x - p1.x).powInt(2) + (p2.y - p1.y).powInt(2))
        }
        return length
    }

    /**
     * Calculate gesture complexity (curviness)
     * Straight line = 0, curved/complex = 1
     */
    private fun calculateComplexity(): Float {
        if (currentGesturePoints.size < 3) return 0f

        var totalTurning = 0f

        for (i in 1 until currentGesturePoints.size - 1) {
            val p1 = currentGesturePoints[i - 1]
            val p2 = currentGesturePoints[i]
            val p3 = currentGesturePoints[i + 1]

            val v1 = Offset(p2.x - p1.x, p2.y - p1.y)
            val v2 = Offset(p3.x - p2.x, p3.y - p2.y)

            // Calculate angle between vectors
            val dot = v1.x * v2.x + v1.y * v2.y
            val det = v1.x * v2.y - v1.y * v2.x
            val angle = kotlin.math.atan2(det.toDouble(), dot.toDouble())

            totalTurning += abs(angle).toFloat()
        }

        return (totalTurning / currentGesturePoints.size).coerceIn(0f, 1f)
    }

    /**
     * Detect sound cue (hum/whisper)
     * @param audioBuffer Audio samples
     * @return true if sound cue detected
     */
    fun detectSoundCue(audioBuffer: ShortArray): Boolean {
        return try {
            // Analyze frequency spectrum for whisper pattern
            val spectrum = computeFrequencySpectrum(audioBuffer)

            // Look for energy in whisper frequency range
            val minBin = (WHISPER_FREQUENCY_MIN * audioBuffer.size / 44100).toInt()
            val maxBin = (WHISPER_FREQUENCY_MAX * audioBuffer.size / 44100).toInt()

            var whisperEnergy = 0f
            var totalEnergy = 0f

            for (i in spectrum.indices) {
                totalEnergy += spectrum[i]
                if (i in minBin..maxBin) {
                    whisperEnergy += spectrum[i]
                }
            }

            val whisperRatio = if (totalEnergy > 0) whisperEnergy / totalEnergy else 0f
            val detected = whisperRatio > WHISPER_THRESHOLD

            if (detected) {
                detectionFeedback.value = "Sound cue detected"
                Log.d(TAG, "Sound cue detected: ratio=$whisperRatio")
            }

            detected
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting sound cue: ${e.message}")
            false
        }
    }

    /**
     * Simplified frequency spectrum
     */
    private fun computeFrequencySpectrum(buffer: ShortArray): FloatArray {
        val spectrum = FloatArray(buffer.size / 2)
        for (i in spectrum.indices) {
            var sum = 0f
            for (j in 0 until 32) {
                val idx = (i * 32 + j).coerceIn(buffer.indices)
                sum += buffer[idx].toFloat()
            }
            spectrum[i] = abs(sum) / 32f
        }
        return spectrum
    }

    /**
     * Detect NFC tag (object-based unlock)
     * @param tag NFC tag scanned by device
     * @return true if authorized tag detected
     */
    fun detectObject(tag: Tag?): Boolean {
        return try {
            if (tag == null) return false

            // Get tag ID
            val tagId = tag.id.joinToString("") { "%02x".format(it) }

            // In production, compare against authorized list
            val isAuthorized = authorizedTags.contains(tagId)

            if (isAuthorized) {
                detectionFeedback.value = "Authorized object detected"
                Log.d(TAG, "Authorized NFC tag detected: $tagId")
            } else {
                Log.w(TAG, "Unauthorized NFC tag: $tagId")
            }

            isAuthorized
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting object: ${e.message}")
            false
        }
    }

    /**
     * Detect visual marker (QR code or barcode)
     * @param imageProxy Camera frame
     * @return true if authorized marker detected
     */
    fun detectVisualMarker(imageProxy: ImageProxy): Boolean {
        // In production, use ML Kit for barcode/QR detection
        // This is simplified implementation
        return try {
            // Would process image to detect QR/barcode
            // Then validate against authorized list
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting visual marker: ${e.message}")
            false
        }
    }

    /**
     * Authorized NFC tag IDs
     */
    private val authorizedTags = mutableSetOf<String>()

    /**
     * Register authorized NFC tag
     * @param tagId Hex-encoded tag ID
     */
    fun authorizeTag(tagId: String) {
        authorizedTags.add(tagId)
        Log.d(TAG, "Authorized tag: $tagId")
    }

    /**
     * Remove authorized tag
     * @param tagId Hex-encoded tag ID
     */
    fun revokeTag(tagId: String) {
        authorizedTags.remove(tagId)
        Log.d(TAG, "Revoked tag: $tagId")
    }

    /**
     * Get status
     * @return Status string
     */
    fun getStatus(): String {
        return """
        Gesture Unlock Status:
        - Gesture tracing: ${gestureTracing.value}
        - Current gesture points: ${currentGesturePoints.size}
        - Detected gesture: ${detectedGesture.value?.getDirection()} ${detectedGesture.value?.getShape()}
        - Authorized tags: ${authorizedTags.size}
        - Feedback: ${detectionFeedback.value ?: "None"}
        """.trimIndent()
    }
}

/**
 * Composable for visualizing gesture trace
 */
@Composable
fun GestureTraceVisualizer(
    gesture: GestureUnlock.GesturePattern?,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        gesture?.points?.let { points ->
            // Draw points
            for (point in points) {
                drawCircle(
                    color = Color.Cyan,
                    radius = 3f,
                    center = point
                )
            }

            // Draw lines connecting points
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = Color.Cyan,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 2f
                )
            }

            // Draw start and end markers
            drawCircle(
                color = Color.Green,
                radius = 5f,
                center = points.first()
            )
            drawCircle(
                color = Color.Red,
                radius = 5f,
                center = points.last()
            )
        }
    }
}

/**
 * Helper extension
 */
private fun Float.powInt(exp: Int): Float {
    return Math.pow(this.toDouble(), exp.toDouble()).toFloat()
}
