package com.glyphos.symbolic.hardware

import android.content.Context
import android.media.AudioRecord
import android.media.MediaRecorder
import android.view.Surface
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sqrt
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Breath Unlock Detection System
 * Combines multiple sensors for reliable breath-based unlock
 * - Microphone acoustic signature
 * - Camera fog detection
 * - Multi-signal validation
 */
@Singleton
class BreathDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _breathDetected = MutableStateFlow<Boolean>(false)
    val breathDetected: StateFlow<Boolean> = _breathDetected

    private val _confidence = MutableStateFlow<Double>(0.0)
    val confidence: StateFlow<Double> = _confidence

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private var audioRecord: AudioRecord? = null
    private var camera: Camera? = null

    fun startBreathDetection() {
        _isListening.value = true
        startMicrophoneDetection()
        // Camera fog detection would start here
    }

    fun stopBreathDetection() {
        _isListening.value = false
        stopMicrophoneDetection()
    }

    fun bindCamera(cameraProvider: ProcessCameraProvider, lifecycleOwner: LifecycleOwner) {
        try {
            val cameraSelector = CameraSelector.DEFAULT_FRONT

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(java.util.concurrent.Executors.newSingleThreadExecutor()) { imageProxy ->
                        analyzeFogPattern(imageProxy)
                        imageProxy.close()
                    }
                }

            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                imageAnalysis
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startMicrophoneDetection() {
        try {
            val sampleRate = 44100
            val channelConfig = android.media.AudioFormat.CHANNEL_IN_MONO
            val audioFormat = android.media.AudioFormat.ENCODING_PCM_16BIT
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            audioRecord?.startRecording()

            // Start detection loop
            detectBreathAcousticSignature()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopMicrophoneDetection() {
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    private fun detectBreathAcousticSignature() {
        Thread {
            val bufferSize = 4096
            val audioBuffer = ShortArray(bufferSize)

            while (_isListening.value) {
                try {
                    val readCount = audioRecord?.read(audioBuffer, 0, bufferSize) ?: 0
                    if (readCount > 0) {
                        val signature = analyzeAudioSignature(audioBuffer, readCount)
                        if (isBreathSignature(signature)) {
                            _confidence.value = signature.confidence
                            if (signature.confidence > 0.75) {
                                _breathDetected.value = true
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    private fun analyzeAudioSignature(audioBuffer: ShortArray, length: Int): AudioSignature {
        try {
            // Calculate RMS (loudness)
            var sumSquare = 0.0
            for (i in 0 until length) {
                val sample = audioBuffer[i].toDouble()
                sumSquare += sample * sample
            }
            val rms = sqrt(sumSquare / length)

            // Detect low-frequency turbulence (breath characteristic)
            val fft = FFTUtil.computeFFT(audioBuffer.copyOfRange(0, length))
            val lowFreqEnergy = fft.take(20).sum()  // Low freq bins
            val ratio = if (fft.sum() > 0) lowFreqEnergy / fft.sum() else 0.0

            // Check for non-voiced characteristics (breath vs voice)
            val voiceContent = detectVoiceContent(fft)

            return AudioSignature(
                rms = rms,
                lowFreqRatio = ratio,
                voiceContent = voiceContent,
                confidence = calculateConfidence(rms, ratio, voiceContent)
            )
        } catch (e: Exception) {
            return AudioSignature(0.0, 0.0, 0.0, 0.0)
        }
    }

    private fun isBreathSignature(signature: AudioSignature): Boolean {
        return signature.rms in 500.0..5000.0 &&  // Typical breath volume
               signature.lowFreqRatio > 0.4 &&     // Breath has low freq
               signature.voiceContent < 0.3        // Not predominantly voiced
    }

    private fun calculateConfidence(rms: Double, lowFreqRatio: Double, voiceContent: Double): Double {
        var confidence = 0.0

        // RMS score
        val rmsScore = when {
            rms in 500.0..5000.0 -> 0.3
            else -> 0.0
        }

        // Frequency score
        val freqScore = if (lowFreqRatio > 0.4) 0.3 else 0.0

        // Voice content score
        val voiceScore = if (voiceContent < 0.3) 0.4 else 0.0

        confidence = rmsScore + freqScore + voiceScore
        return confidence
    }

    private fun detectVoiceContent(fft: List<Double>): Double {
        // Harmonic content detection
        var harmonicEnergy = 0.0
        for (i in 100 until 200) {  // Voice fundamental range
            if (i < fft.size) {
                harmonicEnergy += fft[i]
            }
        }
        return if (fft.sum() > 0) harmonicEnergy / fft.sum() else 0.0
    }

    private fun analyzeFogPattern(imageProxy: androidx.camera.core.ImageProxy) {
        try {
            // Detect camera lens fog from breath
            // This would analyze brightness patterns typical of breath condensation
            val planes = imageProxy.planes
            if (planes.isNotEmpty()) {
                val buffer = planes[0].buffer
                val data = ByteArray(buffer.remaining())
                buffer.get(data)

                val brightness = calculateAverageBrightness(data)
                if (brightness < 100) {  // Foggy conditions
                    _confidence.value = (_confidence.value + 0.1).coerceAtMost(1.0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateAverageBrightness(data: ByteArray): Int {
        var sum = 0L
        for (i in data.indices step 4) {
            sum += (data[i].toInt() and 0xFF)
        }
        return (sum / (data.size / 4)).toInt()
    }

    data class AudioSignature(
        val rms: Double,
        val lowFreqRatio: Double,
        val voiceContent: Double,
        val confidence: Double
    )

    object FFTUtil {
        fun computeFFT(data: ShortArray): List<Double> {
            // Simplified FFT output for breath detection
            // In production, use a proper FFT library
            return (0..255).map { 0.0 }.toMutableList().apply {
                for (i in data.indices) {
                    val bin = (i * 256) / data.size
                    if (bin < size) {
                        this[bin] += (data[i].toDouble() * data[i].toDouble())
                    }
                }
            }
        }
    }
}
