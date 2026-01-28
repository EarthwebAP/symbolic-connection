package com.glyphos.symbolic.hardware

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * PHASE 5: Sound Recognition Engine
 *
 * Audio classification for environmental sound and speech detection.
 * - Environmental sound classification
 * - Whisper detection (for whisper-to-unlock ritual)
 * - Voice activity detection
 * - Sound event recognition
 */
class SoundRecognitionEngine(context: Context) {
    companion object {
        private const val TAG = "SoundRecognitionEngine"
    }

    private val _detectedSounds = MutableStateFlow<List<DetectedSound>>(emptyList())
    val detectedSounds: StateFlow<List<DetectedSound>> = _detectedSounds.asStateFlow()

    private val _lastSoundType = MutableStateFlow<SoundType?>(null)
    val lastSoundType: StateFlow<SoundType?> = _lastSoundType.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    enum class SoundType {
        SILENCE,
        SPEECH,
        WHISPER,
        MUSIC,
        NOISE,
        ALARM,
        DOOR_KNOCK,
        FOOTSTEPS,
        GLASS_BREAK,
        GUNSHOT,
        SHOUTING,
        CRYING,
        SNORING,
        COUGHING,
        LAUGHTER,
        UNKNOWN
    }

    data class DetectedSound(
        val id: String = UUID.randomUUID().toString(),
        val type: SoundType,
        val timestamp: Long = System.currentTimeMillis(),
        val confidence: Float = 0f,
        val frequency: Float = 0f, // Hz
        val duration: Long = 0L, // ms
        val isWhisper: Boolean = false
    )

    fun startListening() {
        _isListening.value = true
        Log.d(TAG, "Sound recognition listening started")
    }

    fun stopListening() {
        _isListening.value = false
        Log.d(TAG, "Sound recognition stopped")
    }

    suspend fun detectSound(audioData: ByteArray): DetectedSound? {
        if (!_isListening.value) return null

        // Analyze audio data
        val soundType = analyzeSoundType(audioData)
        val confidence = calculateConfidence(audioData, soundType)
        val frequency = extractFrequency(audioData)

        val isWhisper = soundType == SoundType.WHISPER ||
                       (soundType == SoundType.SPEECH && frequency < 1000f)

        val sound = DetectedSound(
            type = soundType,
            confidence = confidence,
            frequency = frequency,
            duration = audioData.size.toLong(),
            isWhisper = isWhisper
        )

        _detectedSounds.value = _detectedSounds.value + sound
        _lastSoundType.value = soundType

        Log.d(TAG, "Sound detected: $soundType (confidence: ${String.format("%.2f", confidence)})")

        if (isWhisper) {
            onWhisperDetected(sound)
        }

        return sound
    }

    private suspend fun onWhisperDetected(sound: DetectedSound) {
        Log.d(TAG, "Whisper detected - unlock ritual can be triggered")
    }

    fun getRecentSounds(count: Int = 20): List<DetectedSound> {
        return _detectedSounds.value.takeLast(count)
    }

    fun getSoundsByType(type: SoundType): List<DetectedSound> {
        return _detectedSounds.value.filter { it.type == type }
    }

    fun isWhisperActive(): Boolean {
        val recent = _detectedSounds.value.takeLast(1)
        return recent.any { it.isWhisper }
    }

    fun getStatistics(): SoundRecognitionStatistics {
        val sounds = _detectedSounds.value
        return SoundRecognitionStatistics(
            totalDetected = sounds.size,
            whisperCount = sounds.count { it.isWhisper },
            speechCount = sounds.count { it.type == SoundType.SPEECH },
            averageConfidence = if (sounds.isNotEmpty()) {
                sounds.map { it.confidence }.average().toFloat()
            } else 0f,
            isListening = _isListening.value
        )
    }

    fun getStatus(): String {
        val stats = getStatistics()
        return """
        Sound Recognition Status:
        - Listening: ${stats.isListening}
        - Total detected: ${stats.totalDetected}
        - Whispers: ${stats.whisperCount}
        - Speech: ${stats.speechCount}
        - Avg confidence: ${String.format("%.2f", stats.averageConfidence * 100)}%
        - Last sound: ${_lastSoundType.value}
        """.trimIndent()
    }

    private fun analyzeSoundType(audioData: ByteArray): SoundType {
        // In real implementation, use ML Kit Sound Classification or TensorFlow Lite model
        // For now, use heuristics based on frequency analysis
        val frequency = extractFrequency(audioData)

        return when {
            frequency < 100f -> SoundType.SILENCE
            frequency in 200f..400f -> SoundType.WHISPER
            frequency in 400f..2000f -> SoundType.SPEECH
            frequency in 2000f..4000f -> SoundType.MUSIC
            else -> SoundType.UNKNOWN
        }
    }

    private fun calculateConfidence(audioData: ByteArray, type: SoundType): Float {
        // Calculate confidence based on signal-to-noise ratio
        return if (audioData.isNotEmpty()) {
            val mean = audioData.map { it.toInt() }.average().toFloat()
            val variance = audioData.map { (it.toInt() - mean) * (it.toInt() - mean) }.average()
            val stdDev = kotlin.math.sqrt(variance.toDouble()).toFloat()

            // Normalize to 0-1 range
            val snr = stdDev / (kotlin.math.abs(mean) + 1f)
            kotlin.math.min(snr / 100f, 1f)
        } else {
            0f
        }
    }

    private fun extractFrequency(audioData: ByteArray): Float {
        // Simplified frequency extraction (real implementation would use FFT)
        return if (audioData.isNotEmpty()) {
            val rms = kotlin.math.sqrt(
                audioData.map { it.toDouble() * it.toDouble() }.average()
            ).toFloat()
            rms * 10f // Arbitrary scaling
        } else {
            0f
        }
    }
}

data class SoundRecognitionStatistics(
    val totalDetected: Int,
    val whisperCount: Int,
    val speechCount: Int,
    val averageConfidence: Float,
    val isListening: Boolean
)
