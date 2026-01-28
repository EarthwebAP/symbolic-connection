package com.glyphos.symbolic.security.lens

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import kotlin.math.abs

/**
 * PHASE 1: Breath Unlock
 *
 * Detects breath via microphone and camera for unlocking secure content.
 * - Audio analysis: frequency spectrum for breath pattern
 * - Visual analysis: fog pattern on camera lens (proximity detection)
 * - Multi-signal detection: either audio OR visual breath triggers unlock
 * - Configurable sensitivity
 * - Real-time feedback
 */
class BreathUnlock(
    private val context: Context
) {
    companion object {
        private const val TAG = "BreathUnlock"

        // Audio parameters
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 4096
        private const val CHANNEL_CONFIG = android.media.AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = android.media.AudioFormat.ENCODING_PCM_16BIT

        // Breath detection parameters
        private const val BREATH_FREQUENCY_MIN = 0.2f  // Hz (12 breaths/min)
        private const val BREATH_FREQUENCY_MAX = 0.5f  // Hz (30 breaths/min)
        private const val BREATH_THRESHOLD = 0.3f      // Normalized amplitude
        private const val ANALYSIS_WINDOW_MS = 2000    // 2 second window
    }

    // Detection states
    var isBreathDetectionEnabled = mutableStateOf(true)
    var breathDetected = mutableStateOf(false)
    var detectionFeedback = mutableStateOf<String?>(null)

    // Audio recording
    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    // Breath signature tracking
    private data class BreathSignature(
        val timestamp: Long,
        val amplitude: Float,
        val frequency: Float,
        val quality: Float // 0-1 confidence
    )

    private val recentSignatures = mutableListOf<BreathSignature>()

    /**
     * Start listening for breath via microphone
     * @return true if audio recording started successfully
     */
    fun startAudioDetection(): Boolean {
        return try {
            // Calculate buffer size
            val minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            )
            val bufferSize = maxOf(minBufferSize, BUFFER_SIZE)

            // Create audio record
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            audioRecord?.apply {
                startRecording()
                isRecording = true
            }

            Log.d(TAG, "Audio detection started")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting audio detection: ${e.message}", e)
            false
        }
    }

    /**
     * Stop listening for breath via microphone
     */
    fun stopAudioDetection() {
        try {
            audioRecord?.apply {
                if (isRecording) {
                    stop()
                    isRecording = false
                }
                release()
            }
            audioRecord = null
            Log.d(TAG, "Audio detection stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio detection: ${e.message}", e)
        }
    }

    /**
     * Analyze audio buffer for breath pattern
     * @param buffer Audio samples
     * @return true if breath pattern detected
     */
    private fun analyzeBreathAudio(buffer: ShortArray): Boolean {
        try {
            // Convert to frequency domain using simplified FFT
            val spectrum = computeFrequencySpectrum(buffer)

            // Look for energy in breath frequency range
            val breathBandStart = (BREATH_FREQUENCY_MIN * buffer.size / SAMPLE_RATE).toInt()
            val breathBandEnd = (BREATH_FREQUENCY_MAX * buffer.size / SAMPLE_RATE).toInt()

            var breathEnergy = 0f
            var totalEnergy = 0f

            for (i in spectrum.indices) {
                totalEnergy += spectrum[i]
                if (i in breathBandStart..breathBandEnd) {
                    breathEnergy += spectrum[i]
                }
            }

            val breathRatio = if (totalEnergy > 0) breathEnergy / totalEnergy else 0f

            // Detect if breath ratio exceeds threshold
            val detected = breathRatio > BREATH_THRESHOLD

            if (detected) {
                recordBreathSignature(
                    amplitude = breathRatio,
                    frequency = BREATH_FREQUENCY_MAX  // Simplified
                )
            }

            return detected
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing breath: ${e.message}")
            return false
        }
    }

    /**
     * Simplified frequency spectrum calculation
     * In production, use proper FFT library
     */
    private fun computeFrequencySpectrum(buffer: ShortArray): FloatArray {
        val spectrum = FloatArray(buffer.size / 2)

        for (i in spectrum.indices) {
            var sum = 0f
            for (j in 0 until 32) {  // Simple windowing
                val idx = (i * 32 + j).coerceIn(buffer.indices)
                sum += buffer[idx].toFloat()
            }
            spectrum[i] = abs(sum) / 32f
        }

        return spectrum
    }

    /**
     * Record breath signature for pattern matching
     * @param amplitude Breath amplitude (0-1)
     * @param frequency Breath frequency (Hz)
     */
    private fun recordBreathSignature(amplitude: Float, frequency: Float) {
        val quality = (amplitude * frequency / BREATH_FREQUENCY_MAX).coerceIn(0f, 1f)

        recentSignatures.add(
            BreathSignature(
                timestamp = System.currentTimeMillis(),
                amplitude = amplitude,
                frequency = frequency,
                quality = quality
            )
        )

        // Keep only recent signatures (2 second window)
        val cutoff = System.currentTimeMillis() - ANALYSIS_WINDOW_MS
        recentSignatures.removeAll { it.timestamp < cutoff }
    }

    /**
     * Analyze recent breath signatures for pattern
     * @return Confidence level (0-1) that valid breath detected
     */
    fun analyzeBreathPattern(): Float {
        if (recentSignatures.isEmpty()) return 0f

        val avgQuality = recentSignatures.map { it.quality }.average().toFloat()
        val signatureCount = recentSignatures.size

        // More signatures = higher confidence
        val countConfidence = (signatureCount.toFloat() / 5f).coerceIn(0f, 1f)

        return (avgQuality + countConfidence) / 2f
    }

    /**
     * Check if breath was detected via audio
     * @return true if breath pattern matches
     */
    suspend fun detectBreathAudio(): Boolean {
        return try {
            if (!isBreathDetectionEnabled.value) return false

            val audioRecord = this.audioRecord ?: return false

            // Read audio samples
            val buffer = ShortArray(BUFFER_SIZE)
            val read = audioRecord.read(buffer, 0, buffer.size)

            if (read > 0) {
                val detected = analyzeBreathAudio(buffer)
                if (detected) {
                    detectionFeedback.value = "Audio breath detected"
                    Log.d(TAG, "Audio breath detected")
                }
                detected
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting audio breath: ${e.message}")
            false
        }
    }

    /**
     * Detect breath via visual fog on camera
     * Analyzes for moisture/fog patterns indicating exhalation
     * @param imageProxy Camera frame from CameraX
     * @return true if fog pattern detected
     */
    fun detectBreathVisual(imageProxy: ImageProxy): Boolean {
        return try {
            val image = imageProxy.image ?: return false

            // Analyze image for fog patterns (moisture)
            // Fog appears as increased brightness variance
            val width = image.width
            val height = image.height

            val yPlane = image.planes[0]
            val yPixelStride = yPlane.pixelStride
            val ySize = yPlane.buffer.remaining()
            val yData = ByteArray(ySize)
            yPlane.buffer.get(yData)

            // Sample central region (close to lens)
            val centerX = width / 2
            val centerY = height / 2
            val sampleSize = 100

            var variance = 0f
            var mean = 0f

            // Calculate mean
            for (i in 0 until sampleSize) {
                val x = (centerX + (i % 10) - 5).coerceIn(0, width - 1)
                val y = (centerY + (i / 10) - 5).coerceIn(0, height - 1)
                val idx = y * width + x
                mean += yData[idx].toFloat()
            }
            mean /= sampleSize

            // Calculate variance
            for (i in 0 until sampleSize) {
                val x = (centerX + (i % 10) - 5).coerceIn(0, width - 1)
                val y = (centerY + (i / 10) - 5).coerceIn(0, height - 1)
                val idx = y * width + x
                val diff = yData[idx].toFloat() - mean
                variance += diff * diff
            }
            variance /= sampleSize

            // High variance = fog/moisture
            val fogDetected = variance > 1000  // Threshold (empirical)

            if (fogDetected) {
                detectionFeedback.value = "Visual breath detected"
                Log.d(TAG, "Visual breath detected: variance=$variance")
            }

            fogDetected
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting visual breath: ${e.message}")
            false
        }
    }

    /**
     * Multi-signal breath detection
     * Returns true if EITHER audio OR visual breath detected
     * @param audioBuffer Optional audio buffer
     * @param imageProxy Optional camera frame
     * @return true if breath detected
     */
    suspend fun detectBreath(
        audioBuffer: ShortArray? = null,
        imageProxy: ImageProxy? = null
    ): Boolean {
        if (!isBreathDetectionEnabled.value) return false

        var audioDetected = false
        var visualDetected = false

        // Audio detection
        if (audioBuffer != null) {
            audioDetected = analyzeBreathAudio(audioBuffer)
        }

        // Visual detection
        if (imageProxy != null) {
            visualDetected = detectBreathVisual(imageProxy)
        }

        val detected = audioDetected || visualDetected
        if (detected) {
            breathDetected.value = true
        }

        return detected
    }

    /**
     * Require both audio AND visual confirmation
     * More restrictive, higher security
     * @return true if both signals detect breath
     */
    suspend fun detectBreathStrict(
        audioBuffer: ShortArray,
        imageProxy: ImageProxy
    ): Boolean {
        val audioDetected = analyzeBreathAudio(audioBuffer)
        val visualDetected = detectBreathVisual(imageProxy)

        val detected = audioDetected && visualDetected
        if (detected) {
            breathDetected.value = true
            detectionFeedback.value = "Breath confirmed (audio+visual)"
        }

        return detected
    }

    /**
     * Reset breath detection state
     */
    fun reset() {
        breathDetected.value = false
        recentSignatures.clear()
        detectionFeedback.value = null
    }

    /**
     * Get detection status
     * @return Status string
     */
    fun getStatus(): String {
        val confidence = analyzeBreathPattern()
        return """
        Breath Unlock Status:
        - Detection enabled: ${isBreathDetectionEnabled.value}
        - Breath detected: ${breathDetected.value}
        - Audio recording: $isRecording
        - Recent signatures: ${recentSignatures.size}
        - Confidence: $confidence
        - Feedback: ${detectionFeedback.value ?: "None"}
        """.trimIndent()
    }
}
