package com.glyphos.symbolic.rituals

import android.util.Log
import com.glyphos.symbolic.security.lens.BreathUnlock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * PHASE 7: Breath-to-Reveal Ritual
 *
 * Unlock hidden content with human breath detection.
 * - Breath detection (audio + visual)
 * - Hidden content reveal
 * - Biometric authentication
 * - Ritual logging
 */
class BreathRevealRitual(private val breathUnlock: BreathUnlock) {
    companion object {
        private const val TAG = "BreathRevealRitual"
    }

    private val _revealState = MutableStateFlow<RevealState>(RevealState.WAITING)
    val revealState: StateFlow<RevealState> = _revealState.asStateFlow()

    private val _hiddenContent = MutableStateFlow<List<HiddenContent>>(emptyList())
    val hiddenContent: StateFlow<List<HiddenContent>> = _hiddenContent.asStateFlow()

    private val _ritualSessions = MutableStateFlow<List<RitualSession>>(emptyList())
    val ritualSessions: StateFlow<List<RitualSession>> = _ritualSessions.asStateFlow()

    enum class RevealState {
        WAITING,
        LISTENING,
        DETECTING_BREATH,
        VERIFIED,
        CONTENT_REVEALED,
        FAILED
    }

    data class HiddenContent(
        val id: String = UUID.randomUUID().toString(),
        val content: ByteArray,
        val contentType: String, // "text", "image", "document", "message"
        val createdAt: Long = System.currentTimeMillis(),
        val lastAccessedBy: String? = null,
        val accessCount: Int = 0,
        val requiresBreathEachTime: Boolean = true
    )

    data class RitualSession(
        val id: String = UUID.randomUUID().toString(),
        val startTime: Long = System.currentTimeMillis(),
        var endTime: Long? = null,
        val breathDetected: Boolean = false,
        val contentsRevealed: List<String> = emptyList(),
        val successCount: Int = 0,
        val failureCount: Int = 0
    )

    suspend fun startBreathDetection(): Boolean {
        _revealState.value = RevealState.LISTENING

        return try {
            _revealState.value = RevealState.DETECTING_BREATH

            // Use BreathUnlock to detect breath
            val audioDetected = breathUnlock.detectBreathAudio()
            // Visual detection requires camera integration - skip for now
            val visualDetected = false  // breathUnlock.detectBreathVisual()

            if (audioDetected || visualDetected) {
                _revealState.value = RevealState.VERIFIED
                Log.d(TAG, "Breath verified (audio: $audioDetected, visual: $visualDetected)")
                true
            } else {
                _revealState.value = RevealState.FAILED
                Log.w(TAG, "Breath detection failed")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Breath detection error", e)
            _revealState.value = RevealState.FAILED
            false
        }
    }

    suspend fun revealContent(contentId: String): ByteArray? {
        if (_revealState.value != RevealState.VERIFIED) {
            Log.w(TAG, "Cannot reveal content - breath not verified")
            return null
        }

        val content = _hiddenContent.value.firstOrNull { it.id == contentId }
        if (content == null) {
            Log.w(TAG, "Content not found: $contentId")
            return null
        }

        _revealState.value = RevealState.CONTENT_REVEALED

        Log.d(TAG, "Content revealed: $contentId")
        return content.content
    }

    fun hideContent(content: ByteArray, contentType: String): HiddenContent {
        val hidden = HiddenContent(
            content = content,
            contentType = contentType
        )

        _hiddenContent.value = _hiddenContent.value + hidden
        Log.d(TAG, "Content hidden: ${hidden.id}")

        return hidden
    }

    fun registerHiddenContent(contentId: String, description: String) {
        Log.d(TAG, "Registered hidden content: $contentId - $description")
    }

    suspend fun endSession() {
        val sessions = _ritualSessions.value
        val currentSession = sessions.lastOrNull()

        if (currentSession != null && currentSession.endTime == null) {
            val updatedSession = currentSession.copy(
                endTime = System.currentTimeMillis()
            )
            _ritualSessions.value = _ritualSessions.value
                .dropLast(1)
                .plus(updatedSession)

            Log.d(TAG, "Breath reveal ritual session ended")
        }

        _revealState.value = RevealState.WAITING
    }

    fun getStatistics(): BreathRevealStatistics {
        val sessions = _ritualSessions.value
        return BreathRevealStatistics(
            totalSessions = sessions.size,
            successfulSessions = sessions.count { it.breathDetected },
            contentsHidden = _hiddenContent.value.size,
            totalAccesses = _hiddenContent.value.sumOf { it.accessCount }
        )
    }

    fun getStatus(): String {
        val stats = getStatistics()
        return """
        Breath-to-Reveal Ritual Status:
        - State: ${_revealState.value.name}
        - Hidden contents: ${stats.contentsHidden}
        - Sessions: ${stats.totalSessions}
        - Successful: ${stats.successfulSessions}
        - Total reveals: ${stats.totalAccesses}
        """.trimIndent()
    }
}

data class BreathRevealStatistics(
    val totalSessions: Int,
    val successfulSessions: Int,
    val contentsHidden: Int,
    val totalAccesses: Int
)
