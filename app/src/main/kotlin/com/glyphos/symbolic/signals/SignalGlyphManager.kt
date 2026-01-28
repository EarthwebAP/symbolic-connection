package com.glyphos.symbolic.signals

import com.glyphos.symbolic.core.contracts.EncryptedContent
import com.glyphos.symbolic.core.contracts.GlowState
import com.glyphos.symbolic.core.contracts.PersonalGlyph
import com.glyphos.symbolic.core.contracts.ResonanceType
import com.glyphos.symbolic.core.contracts.SignalGlyph
import com.glyphos.symbolic.core.contracts.UserId
import com.glyphos.symbolic.presence.PresenceEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Signal Glyph system for non-verbal, privacy-first communication
 */
@Singleton
class SignalGlyphManager @Inject constructor(
    private val presenceEngine: PresenceEngine
) {

    private val _incomingSignals = MutableStateFlow<List<SignalGlyph>>(emptyList())
    val incomingSignals: StateFlow<List<SignalGlyph>> = _incomingSignals

    private val _signalHistory = MutableStateFlow<Map<String, List<SignalGlyph>>>(emptyMap())
    val signalHistory: StateFlow<Map<String, List<SignalGlyph>>> = _signalHistory

    fun sendSignalGlyph(
        senderId: UserId,
        recipientId: UserId,
        resonanceType: ResonanceType,
        glyphData: PersonalGlyph,
        hiddenMessage: EncryptedContent? = null,
        partnerSafeMode: Boolean = false
    ): SignalGlyph {
        val signal = SignalGlyph(
            signalId = "signal-${System.currentTimeMillis()}",
            senderId = senderId,
            receiverId = recipientId,
            resonanceType = resonanceType,
            glyphData = glyphData,
            hiddenMessage = hiddenMessage,
            presenceAdaptiveGlow = determineGlowState(recipientId, partnerSafeMode),
            timestamp = System.currentTimeMillis(),
            partnerSafeMode = partnerSafeMode
        )

        // Add to incoming signals for recipient
        val current = _incomingSignals.value.toMutableList()
        current.add(signal)
        _incomingSignals.value = current

        // Log to history
        logSignal(signal)

        return signal
    }

    fun acknowledgeSignal(signalId: String): Boolean {
        val current = _incomingSignals.value.toMutableList()
        val removed = current.removeAll { it.signalId == signalId }
        _incomingSignals.value = current
        return removed
    }

    fun getResonanceDescription(resonanceType: ResonanceType): String {
        return when (resonanceType) {
            ResonanceType.URGENCY -> "Immediate attention needed"
            ResonanceType.CURIOSITY -> "Something interesting awaits"
            ResonanceType.FAVOR -> "Request for help"
            ResonanceType.EMOTIONAL_PRESENCE -> "Thinking of you"
        }
    }

    fun getResonanceColor(resonanceType: ResonanceType): String {
        return when (resonanceType) {
            ResonanceType.URGENCY -> "#FF3333"
            ResonanceType.CURIOSITY -> "#FFAA00"
            ResonanceType.FAVOR -> "#FFD700"
            ResonanceType.EMOTIONAL_PRESENCE -> "#00FFFF"
        }
    }

    fun getResonanceAnimation(resonanceType: ResonanceType): String {
        return when (resonanceType) {
            ResonanceType.URGENCY -> "pulse_sharp"
            ResonanceType.CURIOSITY -> "shimmer"
            ResonanceType.FAVOR -> "breathe"
            ResonanceType.EMOTIONAL_PRESENCE -> "harmonic_vibration"
        }
    }

    private fun determineGlowState(recipientId: UserId, partnerSafeMode: Boolean): GlowState {
        return when {
            partnerSafeMode -> GlowState.DISCREET
            presenceEngine.getPresenceState(recipientId)?.socialContext?.ordinal ?: 0 > 0 -> GlowState.DIM
            else -> GlowState.FULL
        }
    }

    private fun logSignal(signal: SignalGlyph) {
        val history = _signalHistory.value.toMutableMap()
        val key = "${signal.senderId.value}-${signal.receiverId.value}"
        val signals = history[key]?.toMutableList() ?: mutableListOf()

        signals.add(signal)

        // Keep only last 200 signals
        if (signals.size > 200) {
            signals.removeAt(0)
        }

        history[key] = signals
        _signalHistory.value = history
    }

    fun getSignalsFromUser(senderId: UserId): List<SignalGlyph> {
        return _incomingSignals.value.filter { it.senderId == senderId }
    }

    fun hasHiddenContent(signal: SignalGlyph): Boolean {
        return signal.hiddenMessage != null
    }

    fun isPrivacySafe(signal: SignalGlyph): Boolean {
        return signal.hiddenMessage?.ciphertext?.isNotEmpty() == true
    }
}
