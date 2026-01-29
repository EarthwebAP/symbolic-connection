package com.glyphos.symbolic.identity.signal

import android.util.Log
import com.glyphos.symbolic.core.models.GlowState
import com.glyphos.symbolic.core.models.PresenceState
import com.glyphos.symbolic.core.models.ResonanceType
import com.glyphos.symbolic.core.models.SignalGlyph

/**
 * PHASE 2: Presence-Adaptive Behavior
 *
 * Signal glyphs adapt their behavior and visual representation
 * based on the receiver's current presence state.
 *
 * Determines:
 * - Whether glyph is visible
 * - Glow intensity
 * - Notification style
 * - Time to reveal
 */
class PresenceAdaptiveBehavior {
    companion object {
        private const val TAG = "PresenceAdaptiveBehavior"
    }

    /**
     * Determine if signal should be visible to user
     * @param signal Signal glyph
     * @param presenceState User's current presence
     * @return true if signal should be visible
     */
    fun isSignalVisible(
        signal: SignalGlyph,
        presenceState: PresenceState
    ): Boolean {
        // DEEP_FOCUS: hide all signals
        if (presenceState.mode.name == "DEEP_FOCUS") {
            return false
        }

        // PRIVATE: show urgent signals only
        if (presenceState.mode.name == "PRIVATE") {
            return signal.resonanceType == ResonanceType.URGENCY
        }

        // All other modes: show all signals
        return true
    }

    /**
     * Determine glow state based on presence
     * @param signal Signal glyph
     * @param presenceState User's presence
     * @return Appropriate GlowState
     */
    fun determineGlowState(
        signal: SignalGlyph,
        presenceState: PresenceState
    ): GlowState {
        return when {
            // Deep focus: no glow (silent)
            presenceState.mode.name == "DEEP_FOCUS" -> GlowState.NONE

            // Private mode: full glow for urgent
            presenceState.mode.name == "PRIVATE" && signal.resonanceType == ResonanceType.URGENCY ->
                GlowState.FULL

            // Calm/alone: subtle/dim glow
            presenceState.mode.name in listOf("CALM", "ALONE") -> GlowState.SUBTLE

            // Social: discreet glow (don't interrupt conversation)
            presenceState.socialContext.ordinal > 1 -> GlowState.DISCREET

            // Default
            else -> GlowState.DIM
        }
    }

    /**
     * Determine notification delivery mode
     * @param signal Signal
     * @param presenceState Receiver's presence
     * @return NotificationMode
     */
    fun determineNotificationMode(
        signal: SignalGlyph,
        presenceState: PresenceState
    ): NotificationMode {
        return when {
            presenceState.mode.name == "DEEP_FOCUS" -> NotificationMode.SILENT
            presenceState.mode.name == "PRIVATE" && signal.resonanceType != ResonanceType.URGENCY ->
                NotificationMode.VIBRATION
            presenceState.socialContext.ordinal > 1 -> NotificationMode.SILENT
            signal.resonanceType == ResonanceType.URGENCY -> NotificationMode.SOUND_AND_VIBRATION
            else -> NotificationMode.VIBRATION
        }
    }

    /**
     * Determine delay before revealing signal
     * @param signal Signal
     * @param presenceState Receiver's presence
     * @return Milliseconds to delay reveal
     */
    fun determineRevealDelay(
        signal: SignalGlyph,
        presenceState: PresenceState
    ): Long {
        return when {
            signal.resonanceType == ResonanceType.URGENCY -> 0  // Immediate
            presenceState.mode.name == "DEEP_FOCUS" -> 30000    // 30 seconds
            presenceState.mode.name == "PRIVATE" -> 5000        // 5 seconds
            presenceState.mode.name == "CALM" -> 10000          // 10 seconds
            presenceState.focusLevel.ordinal > 2 -> 15000       // 15 seconds if focused
            else -> 3000                                         // 3 seconds default
        }
    }

    /**
     * Calculate signal prominence (0-1) for UI rendering
     * Higher = more prominent in UI
     * @param signal Signal
     * @param presenceState Receiver's presence
     * @return Prominence factor
     */
    fun calculateProminence(
        signal: SignalGlyph,
        presenceState: PresenceState
    ): Float {
        val baseProminence = when (signal.resonanceType) {
            ResonanceType.URGENCY -> 1.0f
            ResonanceType.FAVOR -> 0.7f
            ResonanceType.CURIOSITY -> 0.5f
            ResonanceType.EMOTIONAL_PRESENCE -> 0.4f
        }

        val presenceFactor = when {
            presenceState.mode.name == "DEEP_FOCUS" -> 0.1f
            presenceState.mode.name == "PRIVATE" -> 0.8f
            presenceState.mode.name == "CALM" -> 0.6f
            presenceState.socialContext.ordinal > 1 -> 0.3f
            else -> 0.5f
        }

        return (baseProminence * presenceFactor).coerceIn(0f, 1f)
    }

    /**
     * Determine animation style for signal
     * @param signal Signal
     * @param presenceState Presence
     * @return AnimationStyle
     */
    fun determineAnimationStyle(
        signal: SignalGlyph,
        presenceState: PresenceState
    ): AnimationStyle {
        return when {
            signal.resonanceType == ResonanceType.URGENCY ->
                AnimationStyle.RAPID_PULSE
            presenceState.emotionalTone.name == "CALM" ->
                AnimationStyle.GENTLE_FLOAT
            presenceState.socialContext.ordinal > 1 ->
                AnimationStyle.SUBTLE_SHIMMER
            else -> AnimationStyle.STEADY_GLOW
        }
    }

    /**
     * Adapt signal for display based on presence
     * @param signal Original signal
     * @param presenceState Receiver's presence
     * @return Adapted signal with behavior settings
     */
    fun adaptSignalForPresence(
        signal: SignalGlyph,
        presenceState: PresenceState
    ): AdaptedSignal {
        return AdaptedSignal(
            signal = signal,
            visible = isSignalVisible(signal, presenceState),
            glowState = determineGlowState(signal, presenceState),
            notificationMode = determineNotificationMode(signal, presenceState),
            revealDelay = determineRevealDelay(signal, presenceState),
            prominence = calculateProminence(signal, presenceState),
            animationStyle = determineAnimationStyle(signal, presenceState),
            timestamp = System.currentTimeMillis()
        ).also {
            Log.d(TAG, "Signal adapted for $presenceState: $signal")
        }
    }

    /**
     * Check if signal should trigger notification sound
     * @param presenceState Receiver's presence
     * @return true if sound should play
     */
    fun shouldPlaySound(presenceState: PresenceState): Boolean {
        return presenceState.mode.name !in listOf("DEEP_FOCUS", "PRIVATE") &&
               presenceState.socialContext.ordinal <= 1
    }

    /**
     * Check if signal should trigger vibration
     * @param presenceState Receiver's presence
     * @return true if vibration should trigger
     */
    fun shouldVibrate(presenceState: PresenceState): Boolean {
        return presenceState.mode.name !in listOf("DEEP_FOCUS")
    }
}

enum class NotificationMode {
    SILENT,                    // No notification
    VIBRATION,                 // Vibration only
    SOUND_ONLY,               // Sound only
    SOUND_AND_VIBRATION       // Both
}

enum class AnimationStyle {
    RAPID_PULSE,              // Fast pulsing for urgency
    STEADY_GLOW,              // Constant glow
    GENTLE_FLOAT,             // Soft floating motion
    SUBTLE_SHIMMER            // Barely noticeable shimmer
}

/**
 * Signal with presence-based adaptations applied
 */
data class AdaptedSignal(
    val signal: SignalGlyph,
    val visible: Boolean,
    val glowState: GlowState,
    val notificationMode: NotificationMode,
    val revealDelay: Long,
    val prominence: Float,
    val animationStyle: AnimationStyle,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun shouldNotify(): Boolean = notificationMode != NotificationMode.SILENT

    fun getDisplayName(): String = "${signal.glyphData.name} (${signal.resonanceType})"

    fun getDescription(): String {
        return when (signal.resonanceType) {
            ResonanceType.URGENCY -> "Urgent signal from ${signal.senderId}"
            ResonanceType.CURIOSITY -> "${signal.senderId} is curious"
            ResonanceType.FAVOR -> "${signal.senderId} is asking for help"
            ResonanceType.EMOTIONAL_PRESENCE -> "${signal.senderId} is thinking of you"
        }
    }
}
