package com.glyphos.symbolic.hardware

import android.content.Context
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Voice-Activated Routine Engine
 * Recognizes commands and triggers actions
 */
@Singleton
class VoiceRoutineEngine @Inject constructor(
    private val context: Context
) {

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _recognizedCommand = MutableStateFlow<VoiceCommand?>(null)
    val recognizedCommand: StateFlow<VoiceCommand?> = _recognizedCommand

    private val _confidence = MutableStateFlow(0.0f)
    val confidence: StateFlow<Float> = _confidence

    private var speechRecognizer: SpeechRecognizer? = null

    enum class VoiceCommand {
        SHIFT_FOCUS,
        SHIFT_EXPANSIVE,
        SHIFT_REFLECTIVE,
        OPEN_BATCAVE,
        SEND_SIGNAL,
        EMERGENCY_SEAL,
        ENTER_SEALED_MODE,
        EXIT_SEALED_MODE,
        RECORD_THOUGHT,
        SUMMON_AI,
        ENTER_SECURE_ROOM,
        UNKNOWN
    }

    fun startListening() {
        _isListening.value = true
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: android.os.Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    _isListening.value = false
                }

                override fun onResults(results: android.os.Bundle?) {
                    results?.let {
                        val matches = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val scores = it.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

                        if (!matches.isNullOrEmpty() && !scores.isNullOrEmpty()) {
                            val command = parseVoiceCommand(matches[0])
                            _recognizedCommand.value = command
                            _confidence.value = scores[0]
                        }
                    }
                    _isListening.value = false
                }

                override fun onPartialResults(partialResults: android.os.Bundle?) {}
                override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
            })

            val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
        }
    }

    fun stopListening() {
        _isListening.value = false
        speechRecognizer?.stopListening()
    }

    fun release() {
        speechRecognizer?.destroy()
    }

    private fun parseVoiceCommand(text: String): VoiceCommand {
        val normalized = text.lowercase()

        return when {
            normalized.contains("focus") -> VoiceCommand.SHIFT_FOCUS
            normalized.contains("expand") -> VoiceCommand.SHIFT_EXPANSIVE
            normalized.contains("reflect") -> VoiceCommand.SHIFT_REFLECTIVE
            normalized.contains("batcave") -> VoiceCommand.OPEN_BATCAVE
            normalized.contains("signal") -> VoiceCommand.SEND_SIGNAL
            normalized.contains("seal") -> VoiceCommand.EMERGENCY_SEAL
            normalized.contains("sealed") -> VoiceCommand.ENTER_SEALED_MODE
            normalized.contains("unseal") -> VoiceCommand.EXIT_SEALED_MODE
            normalized.contains("thought") -> VoiceCommand.RECORD_THOUGHT
            normalized.contains("ai") -> VoiceCommand.SUMMON_AI
            normalized.contains("secure") -> VoiceCommand.ENTER_SECURE_ROOM
            else -> VoiceCommand.UNKNOWN
        }
    }

    fun getCommandDescription(command: VoiceCommand): String {
        return when (command) {
            VoiceCommand.SHIFT_FOCUS -> "Entering deep focus mode"
            VoiceCommand.SHIFT_EXPANSIVE -> "Entering expansive mode"
            VoiceCommand.SHIFT_REFLECTIVE -> "Entering reflective mode"
            VoiceCommand.OPEN_BATCAVE -> "Opening Batcave"
            VoiceCommand.SEND_SIGNAL -> "Sending signal glyph"
            VoiceCommand.EMERGENCY_SEAL -> "Emergency seal activated"
            VoiceCommand.ENTER_SEALED_MODE -> "Sealed mode activated"
            VoiceCommand.EXIT_SEALED_MODE -> "Sealed mode deactivated"
            VoiceCommand.RECORD_THOUGHT -> "Recording thought"
            VoiceCommand.SUMMON_AI -> "Summoning AI companion"
            VoiceCommand.ENTER_SECURE_ROOM -> "Entering secure room"
            VoiceCommand.UNKNOWN -> "Command not recognized"
        }
    }
}
