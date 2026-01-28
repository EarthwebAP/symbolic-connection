package com.glyphos.symbolic.identity.signal

import android.util.Log
import com.glyphos.symbolic.core.models.EncryptedContent
import com.glyphos.symbolic.core.models.GlyphIdentity
import com.glyphos.symbolic.core.models.GlowState
import com.glyphos.symbolic.core.models.PresenceState
import com.glyphos.symbolic.core.models.ResonanceType
import com.glyphos.symbolic.core.models.SignalGlyph
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * PHASE 2: Signal Glyph Manager
 *
 * Non-verbal, presence-aware communication via resonance signals.
 * Four types: Urgency, Curiosity, Favor, Emotional Presence.
 * Adaptive glow based on receiver's presence state.
 */
class SignalGlyphManager {
    companion object {
        private const val TAG = "SignalGlyphManager"
    }

    // Signal storage
    private val signals = mutableMapOf<String, SignalGlyph>()
    private val _signalFlow = MutableStateFlow<List<SignalGlyph>>(emptyList())
    val signals_: StateFlow<List<SignalGlyph>> = _signalFlow.asStateFlow()

    // Received signals per user
    private val userSignals = mutableMapOf<String, MutableList<SignalGlyph>>()

    /**
     * Send signal glyph from sender to receiver
     * @param senderId Sender user ID
     * @param receiverId Receiver user ID
     * @param resonanceType Type of signal (Urgency, Curiosity, Favor, Emotional)
     * @param glyph Glyph to send
     * @param hiddenMessage Optional encrypted message
     * @return SignalGlyph ID
     */
    suspend fun sendSignal(
        senderId: String,
        receiverId: String,
        resonanceType: ResonanceType,
        glyph: GlyphIdentity,
        hiddenMessage: EncryptedContent? = null
    ): String {
        val signalId = "sig-${UUID.randomUUID()}"

        val signal = SignalGlyph(
            signalId = signalId,
            senderId = senderId,
            receiverId = receiverId,
            resonanceType = resonanceType,
            glyphData = glyph,
            hiddenMessage = hiddenMessage,
            presenceAdaptiveGlow = GlowState.SUBTLE  // Default glow
        )

        signals[signalId] = signal
        userSignals.getOrPut(receiverId) { mutableListOf() }.add(signal)
        _signalFlow.value = signals.values.toList()

        Log.d(TAG, "Signal sent: $signalId ($resonanceType) from $senderId to $receiverId")
        return signalId
    }

    /**
     * Get signals received by user
     * @param userId Receiver user ID
     * @return List of signals
     */
    suspend fun getReceivedSignals(userId: String): List<SignalGlyph> {
        return userSignals[userId]?.toList() ?: emptyList()
    }

    /**
     * Get signals by type
     * @param userId Receiver user ID
     * @param type Resonance type
     * @return List of signals of that type
     */
    suspend fun getSignalsByType(userId: String, type: ResonanceType): List<SignalGlyph> {
        return getReceivedSignals(userId).filter { it.resonanceType == type }
    }

    /**
     * Get unread signals for user
     * @param userId User ID
     * @return List of signals with glow > NONE
     */
    suspend fun getUnreadSignals(userId: String): List<SignalGlyph> {
        return getReceivedSignals(userId).filter {
            it.presenceAdaptiveGlow != GlowState.NONE
        }
    }

    /**
     * Adapt signal glow based on receiver's presence
     * - PRIVATE: FULL glow (urgent, needs attention)
     * - CALM: DIM glow (less intrusive)
     * - ALONE: SUBTLE glow (available)
     * - SOCIAL: DISCREET glow (don't interrupt)
     * - DEEP_FOCUS: NONE (silent, no glow)
     *
     * @param signal Signal to adapt
     * @param presenceState Receiver's presence state
     * @return Adapted signal with appropriate glow
     */
    fun adaptGlyphGlow(
        signal: SignalGlyph,
        presenceState: PresenceState
    ): SignalGlyph {
        val adaptedGlow = when {
            presenceState.mode.name == "DEEP_FOCUS" -> GlowState.NONE
            presenceState.mode.name == "PRIVATE" -> GlowState.FULL
            presenceState.mode.name == "CALM" -> GlowState.DIM
            presenceState.mode.name == "ALONE" -> GlowState.SUBTLE
            presenceState.socialContext.ordinal > 1 -> GlowState.DISCREET
            else -> GlowState.SUBTLE
        }

        return signal.copy(presenceAdaptiveGlow = adaptedGlow)
    }

    /**
     * Reveal hidden message in signal
     * @param signalId Signal ID
     * @param keyAlias Keystore alias for decryption
     * @param decryptFunction Function to decrypt content
     * @return Decrypted message, or null
     */
    suspend fun revealMessage(
        signalId: String,
        keyAlias: String,
        decryptFunction: (EncryptedContent, String) -> ByteArray?
    ): ByteArray? {
        val signal = signals[signalId] ?: return null
        val encrypted = signal.hiddenMessage ?: return null

        val plaintext = decryptFunction(encrypted, keyAlias)

        if (plaintext != null) {
            Log.d(TAG, "Message revealed for signal: $signalId")
        }

        return plaintext
    }

    /**
     * Mark signal as read (remove glow)
     * @param signalId Signal ID
     */
    suspend fun markAsRead(signalId: String) {
        val signal = signals[signalId] ?: return

        signals[signalId] = signal.copy(presenceAdaptiveGlow = GlowState.NONE)
        _signalFlow.value = signals.values.toList()

        Log.d(TAG, "Signal marked as read: $signalId")
    }

    /**
     * Delete signal
     * @param signalId Signal ID
     */
    suspend fun deleteSignal(signalId: String) {
        signals.remove(signalId)
        _signalFlow.value = signals.values.toList()

        Log.d(TAG, "Signal deleted: $signalId")
    }

    /**
     * Clear all signals for a user
     * @param userId User ID
     */
    suspend fun clearUserSignals(userId: String) {
        userSignals[userId]?.let { signals_list ->
            signals_list.forEach { signals.remove(it.signalId) }
            signals_list.clear()
        }

        _signalFlow.value = signals.values.toList()
        Log.d(TAG, "Cleared all signals for user: $userId")
    }

    /**
     * Get signal statistics
     * @return SignalStatistics
     */
    suspend fun getStatistics(): SignalStatistics {
        val totalSignals = signals.size
        val unreadSignals = signals.count { (_, signal) ->
            signal.presenceAdaptiveGlow != GlowState.NONE
        }

        val byType = ResonanceType.values().associate { type ->
            type to signals.count { (_, signal) -> signal.resonanceType == type }
        }

        val byGlow = GlowState.values().associate { glow ->
            glow to signals.count { (_, signal) -> signal.presenceAdaptiveGlow == glow }
        }

        return SignalStatistics(
            totalSignals = totalSignals,
            unreadSignals = unreadSignals,
            byType = byType,
            byGlow = byGlow
        )
    }

    /**
     * Get status
     * @return Status string
     */
    suspend fun getStatus(): String {
        val stats = getStatistics()
        return """
        Signal Glyph Manager Status:
        - Total signals: ${stats.totalSignals}
        - Unread signals: ${stats.unreadSignals}
        - By type: ${stats.byType}
        - By glow: ${stats.byGlow}
        """.trimIndent()
    }
}

data class SignalStatistics(
    val totalSignals: Int,
    val unreadSignals: Int,
    val byType: Map<ResonanceType, Int>,
    val byGlow: Map<GlowState, Int>
)

/**
 * Signal glyph UI state
 */
data class SignalGlyphUIState(
    val glyphId: String,
    val senderName: String,
    val resonanceType: ResonanceType,
    val glowState: GlowState,
    val hasHiddenMessage: Boolean,
    val createdAt: Long
) {
    fun getGlyphIcon(): String = when (resonanceType) {
        ResonanceType.URGENCY -> "⚡"
        ResonanceType.CURIOSITY -> "❓"
        ResonanceType.FAVOR -> "🙏"
        ResonanceType.EMOTIONAL_PRESENCE -> "💜"
    }

    fun getGlyphColor(): String = when (resonanceType) {
        ResonanceType.URGENCY -> "Red"
        ResonanceType.CURIOSITY -> "Blue"
        ResonanceType.FAVOR -> "Yellow"
        ResonanceType.EMOTIONAL_PRESENCE -> "Magenta"
    }

    fun getGlyphBrightness(): Float = when (glowState) {
        GlowState.NONE -> 0.3f
        GlowState.SUBTLE -> 0.6f
        GlowState.DIM -> 0.75f
        GlowState.FULL -> 1.0f
        GlowState.DISCREET -> 0.45f
    }
}
