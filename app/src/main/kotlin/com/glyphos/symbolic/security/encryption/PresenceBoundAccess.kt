package com.glyphos.symbolic.security.encryption

import android.util.Log
import com.glyphos.symbolic.core.models.Message
import com.glyphos.symbolic.core.models.PresenceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PHASE 1: Presence-Bound Access Control
 *
 * Enforces access control based on current presence state.
 * Messages and content can only be decrypted if user's current
 * presence state matches the required presence profile.
 */
class PresenceBoundAccess(
    private val encryptionEngine: LocalEncryptionEngine
) {
    companion object {
        private const val TAG = "PresenceBoundAccess"
    }

    // Current presence state - can be updated externally
    private val _presenceState = MutableStateFlow<PresenceState?>(null)
    val presenceState: StateFlow<PresenceState?> = _presenceState.asStateFlow()

    // Access attempt log
    private val accessLog = mutableListOf<AccessAttempt>()

    /**
     * Update current presence state
     * @param presence New presence state
     */
    fun updatePresence(presence: PresenceState) {
        _presenceState.value = presence
        Log.d(TAG, "Presence updated: mode=${presence.mode}, tone=${presence.emotionalTone}")
    }

    /**
     * Check if current presence matches required profile
     * @param requiredPresence Required presence state
     * @return true if current presence satisfies requirements
     */
    fun canAccess(requiredPresence: PresenceState?): Boolean {
        // No requirement = always accessible
        if (requiredPresence == null) return true

        val current = _presenceState.value ?: return false

        val matches = current.matches(requiredPresence)
        Log.d(TAG, "Access check: current=$current, required=$requiredPresence, matches=$matches")
        return matches
    }

    /**
     * Decrypt message only if presence matches
     * @param message Message to decrypt
     * @param keyAlias Keystore alias
     * @return Decrypted content, or null if access denied
     */
    fun decryptIfPresenceMatches(message: Message, keyAlias: String): ByteArray? {
        val canAccess = canAccess(message.presenceRequirements)

        if (!canAccess) {
            logAccessDenied(
                messageId = message.messageId,
                reason = "Presence mismatch"
            )
            Log.w(TAG, "Access denied: presence mismatch for message ${message.messageId}")
            return null
        }

        logAccessGranted(messageId = message.messageId)

        return encryptionEngine.decrypt(message.content, keyAlias).also {
            if (it == null) {
                Log.e(TAG, "Decryption failed for message ${message.messageId}")
            } else {
                Log.d(TAG, "Message decrypted successfully")
            }
        }
    }

    /**
     * Check if a resource can be accessed
     * @param requiredPresence Required presence for access
     * @param resourceId Resource identifier
     * @return true if resource is accessible
     */
    fun canAccessResource(
        requiredPresence: PresenceState?,
        resourceId: String
    ): Boolean {
        val accessible = canAccess(requiredPresence)
        if (!accessible) {
            logAccessDenied(
                messageId = resourceId,
                reason = "Presence requirement not met"
            )
        } else {
            logAccessGranted(resourceId)
        }
        return accessible
    }

    /**
     * Get access denial reason
     * @param required Required presence
     * @param current Current presence
     * @return Reason string
     */
    fun getAccessDenialReason(required: PresenceState, current: PresenceState?): String {
        if (current == null) return "No presence state set"

        val reasons = mutableListOf<String>()

        if (required.mode != current.mode) {
            reasons.add("Mode mismatch: need ${required.mode}, have ${current.mode}")
        }

        if (required.emotionalTone != current.emotionalTone) {
            reasons.add("Emotional tone mismatch: need ${required.emotionalTone}, have ${current.emotionalTone}")
        }

        if (required.focusLevel.ordinal > current.focusLevel.ordinal) {
            reasons.add("Focus level too low: need ${required.focusLevel}, have ${current.focusLevel}")
        }

        if (required.socialContext.ordinal > current.socialContext.ordinal) {
            reasons.add("Social context incompatible: need ${required.socialContext}, have ${current.socialContext}")
        }

        return reasons.joinToString("; ")
    }

    /**
     * Get current access summary
     * @return String describing current access state
     */
    fun getAccessSummary(): String {
        val current = _presenceState.value
        return if (current != null) {
            """
            Current Presence:
            - Mode: ${current.mode}
            - Emotional Tone: ${current.emotionalTone}
            - Focus Level: ${current.focusLevel}
            - Social Context: ${current.socialContext}
            - Timestamp: ${current.timestamp}
            """.trimIndent()
        } else {
            "No presence state set"
        }
    }

    /**
     * Get access log
     * @param limit Max entries to return
     * @return List of access attempts
     */
    fun getAccessLog(limit: Int = 100): List<AccessAttempt> {
        return accessLog.takeLast(limit)
    }

    /**
     * Clear access log
     */
    fun clearAccessLog() {
        accessLog.clear()
    }

    // ========================================================================
    // PRIVATE HELPERS
    // ========================================================================

    private fun logAccessGranted(messageId: String) {
        accessLog.add(
            AccessAttempt(
                resourceId = messageId,
                granted = true,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private fun logAccessDenied(messageId: String, reason: String) {
        accessLog.add(
            AccessAttempt(
                resourceId = messageId,
                granted = false,
                reason = reason,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}

/**
 * Record of an access attempt
 */
data class AccessAttempt(
    val resourceId: String,
    val granted: Boolean,
    val reason: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
