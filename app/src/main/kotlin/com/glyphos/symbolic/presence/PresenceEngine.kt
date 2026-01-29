package com.glyphos.symbolic.presence

import com.glyphos.symbolic.core.contracts.BandwidthLevel
import com.glyphos.symbolic.core.contracts.CognitiveMode
import com.glyphos.symbolic.core.contracts.EmotionalTone
import com.glyphos.symbolic.core.contracts.IntentVector
import com.glyphos.symbolic.core.contracts.PresenceSignature
import com.glyphos.symbolic.core.contracts.PresenceState
import com.glyphos.symbolic.core.contracts.SocialContext
import com.glyphos.symbolic.core.contracts.UserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cognitive-Emotional Presence Engine
 * Manages 3-dimensional presence: cognitive + emotional + intent
 */
@Singleton
class PresenceEngine @Inject constructor() {

    private val _userPresence = MutableStateFlow<Map<String, PresenceState>>(emptyMap())
    val userPresence: StateFlow<Map<String, PresenceState>> = _userPresence

    private val _presenceHistory = MutableStateFlow<Map<String, List<PresenceSignature>>>(emptyMap())
    val presenceHistory: StateFlow<Map<String, List<PresenceSignature>>> = _presenceHistory

    fun setPresenceState(userId: UserId, state: PresenceState) {
        val current = _userPresence.value.toMutableMap()
        current[userId.value] = state
        _userPresence.value = current

        // Log to history
        logPresenceChange(userId, state)
    }

    fun getPresenceState(userId: UserId): PresenceState? {
        return _userPresence.value[userId.value]
    }

    fun getPresenceSignature(userId: UserId): PresenceSignature? {
        val state = getPresenceState(userId) ?: return null
        return PresenceSignature(
            userId = userId,
            state = state,
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun isPresenceCompatible(user1Id: UserId, user2Id: UserId): Boolean {
        val state1 = getPresenceState(user1Id) ?: return false
        val state2 = getPresenceState(user2Id) ?: return false
        return state1.matches(state2)
    }

    fun canUserReceiveUrgent(userId: UserId): Boolean {
        val state = getPresenceState(userId) ?: return false
        return state.cognitive != CognitiveMode.DEEP_FOCUS &&
               state.cognitive != CognitiveMode.LOW_BANDWIDTH_RECOVERY
    }

    fun canUserReceiveDelicate(userId: UserId): Boolean {
        val state = getPresenceState(userId) ?: return false
        return state.cognitive != CognitiveMode.DEEP_FOCUS &&
               state.emotional != EmotionalTone.PROTECTIVE &&
               state.bandwidth != BandwidthLevel.CRITICAL_LOW
    }

    private fun logPresenceChange(userId: UserId, state: PresenceState) {
        val history = _presenceHistory.value.toMutableMap()
        val userHistory = history[userId.value]?.toMutableList() ?: mutableListOf()

        userHistory.add(
            PresenceSignature(
                userId = userId,
                state = state,
                lastUpdated = System.currentTimeMillis()
            )
        )

        // Keep only last 100 entries
        if (userHistory.size > 100) {
            userHistory.removeAt(0)
        }

        history[userId.value] = userHistory
        _presenceHistory.value = history
    }

    fun shiftCognitiveMode(userId: UserId, mode: CognitiveMode) {
        val current = getPresenceState(userId) ?: return
        setPresenceState(userId, current.copy(cognitive = mode))
    }

    fun shiftEmotionalTone(userId: UserId, tone: EmotionalTone) {
        val current = getPresenceState(userId) ?: return
        setPresenceState(userId, current.copy(emotional = tone))
    }

    fun updateIntentVector(userId: UserId, intent: IntentVector) {
        val current = getPresenceState(userId) ?: return
        setPresenceState(userId, current.copy(intent = intent))
    }

    fun setSocialContext(userId: UserId, context: SocialContext) {
        val current = getPresenceState(userId) ?: return
        setPresenceState(userId, current.copy(socialContext = context))
    }

    fun setBandwidth(userId: UserId, bandwidth: BandwidthLevel) {
        val current = getPresenceState(userId) ?: return
        setPresenceState(userId, current.copy(bandwidth = bandwidth))
    }

    fun getResonanceFrequency(userId1: UserId, userId2: UserId): Double {
        val state1 = getPresenceState(userId1) ?: return 0.0
        val state2 = getPresenceState(userId2) ?: return 0.0

        var frequency = 0.0

        // Cognitive alignment
        if (state1.cognitive == state2.cognitive) frequency += 0.2

        // Emotional harmony
        if (state1.emotional == state2.emotional) frequency += 0.2

        // Intent compatibility
        if (state1.intent.intersects(state2.intent)) frequency += 0.2

        // Social context alignment
        if (state1.socialContext == state2.socialContext) frequency += 0.2

        // Bandwidth match
        if (state1.bandwidth == state2.bandwidth) frequency += 0.2

        return frequency
    }
}
