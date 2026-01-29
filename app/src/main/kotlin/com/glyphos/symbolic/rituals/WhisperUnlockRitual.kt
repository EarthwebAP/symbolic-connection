package com.glyphos.symbolic.rituals

import android.util.Log
import com.glyphos.symbolic.hardware.SoundRecognitionEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * PHASE 7: Whisper-to-Unlock Ritual
 *
 * Unlock resources with whispered words.
 * - Whisper detection and verification
 * - Voice-based biometric unlocking
 * - Passphrase system
 * - Ritual-specific voice unlock
 */
class WhisperUnlockRitual(private val soundEngine: SoundRecognitionEngine) {
    companion object {
        private const val TAG = "WhisperUnlockRitual"
        private const val MIN_WHISPER_CONFIDENCE = 0.7f
    }

    private val _unlockState = MutableStateFlow<UnlockState>(UnlockState.READY)
    val unlockState: StateFlow<UnlockState> = _unlockState.asStateFlow()

    private val _protectedResources = MutableStateFlow<List<ProtectedResource>>(emptyList())
    val protectedResources: StateFlow<List<ProtectedResource>> = _protectedResources.asStateFlow()

    private val _whisperedPhrases = MutableStateFlow<List<WhisperedPhrase>>(emptyList())
    val whisperedPhrases: StateFlow<List<WhisperedPhrase>> = _whisperedPhrases.asStateFlow()

    private val _unlockSessions = MutableStateFlow<List<UnlockSession>>(emptyList())
    val unlockSessions: StateFlow<List<UnlockSession>> = _unlockSessions.asStateFlow()

    enum class UnlockState {
        READY,
        LISTENING,
        PROCESSING,
        VERIFIED,
        UNLOCK_GRANTED,
        UNLOCK_DENIED
    }

    data class ProtectedResource(
        val id: String = UUID.randomUUID().toString(),
        val name: String,
        val resourceType: String,
        val passphrases: List<String>, // Multiple passphrases allowed
        val createdAt: Long = System.currentTimeMillis(),
        val unlockCount: Int = 0,
        val lastUnlockedAt: Long? = null
    )

    data class WhisperedPhrase(
        val id: String = UUID.randomUUID().toString(),
        val text: String,
        val timestamp: Long = System.currentTimeMillis(),
        val confidence: Float = 0f,
        val isWhisper: Boolean = false,
        val resourceId: String? = null
    )

    data class UnlockSession(
        val id: String = UUID.randomUUID().toString(),
        val resourceId: String,
        val startTime: Long = System.currentTimeMillis(),
        var endTime: Long? = null,
        val whisperedPhraseId: String? = null,
        val unlocked: Boolean = false,
        val attempts: Int = 0
    )

    suspend fun listenForWhisper(): WhisperedPhrase? {
        _unlockState.value = UnlockState.LISTENING

        return try {
            soundEngine.startListening()

            // Simulate whisper detection (in real implementation, process audio)
            val detectedSounds = soundEngine.detectedSounds.value
            val whisperSound = detectedSounds.lastOrNull { it.isWhisper }

            if (whisperSound != null && whisperSound.confidence >= MIN_WHISPER_CONFIDENCE) {
                _unlockState.value = UnlockState.PROCESSING

                val phrase = WhisperedPhrase(
                    text = "whispered-command",
                    confidence = whisperSound.confidence,
                    isWhisper = true
                )

                _whisperedPhrases.value = _whisperedPhrases.value + phrase

                Log.d(TAG, "Whisper detected: confidence=${String.format("%.2f", whisperSound.confidence)}")
                phrase
            } else {
                _unlockState.value = UnlockState.UNLOCK_DENIED
                Log.w(TAG, "Whisper detection failed or low confidence")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Whisper listening error", e)
            _unlockState.value = UnlockState.UNLOCK_DENIED
            null
        } finally {
            soundEngine.stopListening()
        }
    }

    suspend fun unlockResource(resourceId: String, phraseId: String?): Boolean {
        val resource = _protectedResources.value.firstOrNull { it.id == resourceId }
        if (resource == null) {
            Log.w(TAG, "Resource not found: $resourceId")
            _unlockState.value = UnlockState.UNLOCK_DENIED
            return false
        }

        val session = UnlockSession(
            resourceId = resourceId,
            whisperedPhraseId = phraseId
        )

        _unlockSessions.value = _unlockSessions.value + session

        // In real implementation, verify whispered phrase matches passphrase
        val unlocked = phraseId != null

        if (unlocked) {
            _unlockState.value = UnlockState.UNLOCK_GRANTED

            // Update resource stats
            _protectedResources.value = _protectedResources.value.map {
                if (it.id == resourceId) {
                    it.copy(
                        unlockCount = it.unlockCount + 1,
                        lastUnlockedAt = System.currentTimeMillis()
                    )
                } else {
                    it
                }
            }

            Log.d(TAG, "Resource unlocked: $resourceId")
        } else {
            _unlockState.value = UnlockState.UNLOCK_DENIED
            Log.w(TAG, "Failed to unlock resource: $resourceId")
        }

        return unlocked
    }

    fun protectResource(name: String, type: String, passphrases: List<String>): ProtectedResource {
        val resource = ProtectedResource(
            name = name,
            resourceType = type,
            passphrases = passphrases
        )

        _protectedResources.value = _protectedResources.value + resource
        Log.d(TAG, "Resource protected: $name with ${passphrases.size} passphrases")

        return resource
    }

    fun setPassphrases(resourceId: String, passphrases: List<String>) {
        _protectedResources.value = _protectedResources.value.map { resource ->
            if (resource.id == resourceId) {
                resource.copy(passphrases = passphrases)
            } else {
                resource
            }
        }

        Log.d(TAG, "Passphrases updated for resource: $resourceId")
    }

    fun getStatistics(): WhisperUnlockStatistics {
        val resources = _protectedResources.value
        val sessions = _unlockSessions.value

        return WhisperUnlockStatistics(
            protectedResources = resources.size,
            totalUnlocks = resources.sumOf { it.unlockCount },
            unlockSessions = sessions.size,
            successfulUnlocks = sessions.count { it.unlocked },
            failedAttempts = sessions.count { !it.unlocked }
        )
    }

    fun getStatus(): String {
        val stats = getStatistics()
        return """
        Whisper-to-Unlock Ritual Status:
        - State: ${_unlockState.value.name}
        - Protected resources: ${stats.protectedResources}
        - Total unlocks: ${stats.totalUnlocks}
        - Successful: ${stats.successfulUnlocks}
        - Failed: ${stats.failedAttempts}
        """.trimIndent()
    }
}

data class WhisperUnlockStatistics(
    val protectedResources: Int,
    val totalUnlocks: Int,
    val unlockSessions: Int,
    val successfulUnlocks: Int,
    val failedAttempts: Int
)
