package com.glyphos.symbolic.hardware

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * PHASE 5: Voice Activation Engine
 *
 * Speech recognition for ritual phrases and voice commands.
 * - Ritual phrase recognition (Breath-to-reveal, Whisper-to-unlock)
 * - Real-time speech input handling
 * - Audio level detection
 * - Command interpretation
 */
class VoiceActivationEngine(private val context: Context) {
    companion object {
        private const val TAG = "VoiceActivationEngine"
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private val _recognitionState = MutableStateFlow<RecognitionState>(RecognitionState.IDLE)
    val recognitionState: StateFlow<RecognitionState> = _recognitionState.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _commands = MutableStateFlow<List<VoiceCommand>>(emptyList())
    val commands: StateFlow<List<VoiceCommand>> = _commands.asStateFlow()

    enum class RecognitionState {
        IDLE, LISTENING, PROCESSING, RECOGNIZED, ERROR
    }

    enum class RitualPhrase(val text: String, val triggers: List<String>) {
        BREATH_TO_REVEAL(
            "Reveal by breath",
            listOf("breath reveal", "show on breath", "reveal breath")
        ),
        WHISPER_TO_UNLOCK(
            "Unlock by whisper",
            listOf("whisper unlock", "whisper to unlock", "unlock whisper")
        ),
        SUMMON_AI(
            "Summon AI companion",
            listOf("summon ai", "call companion", "ai assist")
        ),
        EMIT_PULSE(
            "Emit presence pulse",
            listOf("emit pulse", "send pulse", "broadcast presence")
        ),
        DROP_MARKER(
            "Drop symbolic marker",
            listOf("drop marker", "mark location", "place marker")
        );

        fun matches(text: String): Boolean {
            return triggers.any { trigger ->
                text.lowercase().contains(trigger.lowercase())
            }
        }
    }

    data class VoiceCommand(
        val id: String,
        val text: String,
        val confidence: Float,
        val timestamp: Long,
        val ritualPhrase: RitualPhrase? = null,
        val isConfirmed: Boolean = false
    )

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            setupRecognitionListener()
        }
    }

    fun startListening() {
        if (speechRecognizer == null) return

        _recognitionState.value = RecognitionState.LISTENING
        _audioLevel.value = 0f

        val intent = Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(android.speech.RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        speechRecognizer?.startListening(intent)
        Log.d(TAG, "Voice listening started")
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _recognitionState.value = RecognitionState.IDLE
        Log.d(TAG, "Voice listening stopped")
    }

    suspend fun recognizeRitualPhrase(): RitualPhrase? {
        startListening()
        // In real implementation, wait for recognition to complete
        return RitualPhrase.values().firstOrNull { phrase ->
            phrase.matches(_recognizedText.value)
        }
    }

    fun registerCustomCommand(command: String, action: suspend () -> Unit) {
        val voiceCmd = VoiceCommand(
            id = "cmd-${System.currentTimeMillis()}",
            text = command,
            confidence = 1f,
            timestamp = System.currentTimeMillis()
        )
        _commands.value = _commands.value + voiceCmd
        Log.d(TAG, "Custom command registered: $command")
    }

    fun getStatus(): String {
        return """
        Voice Activation Status:
        - State: ${_recognitionState.value}
        - Last recognized: ${_recognizedText.value}
        - Audio level: ${_audioLevel.value}
        - Commands: ${_commands.value.size}
        """.trimIndent()
    }

    private fun setupRecognitionListener() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _recognitionState.value = RecognitionState.LISTENING
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "Speech beginning detected")
            }

            override fun onRmsChanged(rmsdB: Float) {
                _audioLevel.value = (rmsdB + 5) / 35f // Normalize to 0-1
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Handle audio buffer
            }

            override fun onEndOfSpeech() {
                _recognitionState.value = RecognitionState.PROCESSING
            }

            override fun onError(error: Int) {
                _recognitionState.value = RecognitionState.ERROR
                Log.e(TAG, "Recognition error: $error")
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(
                    android.speech.SpeechRecognizer.RESULTS_RECOGNITION
                ) ?: emptyList()

                if (matches.isNotEmpty()) {
                    _recognizedText.value = matches[0]
                    _recognitionState.value = RecognitionState.RECOGNIZED

                    // Check for ritual phrases
                    val ritual = RitualPhrase.values().firstOrNull { it.matches(matches[0]) }
                    if (ritual != null) {
                        Log.d(TAG, "Ritual phrase recognized: ${ritual.text}")
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Handle partial results
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Handle other events
            }
        })
    }

    fun release() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
