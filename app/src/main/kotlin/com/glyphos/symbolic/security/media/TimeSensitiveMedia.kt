package com.glyphos.symbolic.security.media

import android.util.Log
import com.glyphos.symbolic.core.models.PresenceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

/**
 * PHASE 1: Time-Sensitive Media
 *
 * Messages and content automatically expire after a set duration.
 * Can also be restricted to specific presence windows.
 * Auto-deletes expired content on access.
 */
class TimeSensitiveMedia {
    companion object {
        private const val TAG = "TimeSensitiveMedia"
    }

    // Track all time-sensitive items
    private data class TimeSensitiveItem(
        val mediaId: String,
        val createdAt: Long,
        val expiryTime: Long,
        val allowedPresenceStates: List<PresenceState>? = null,
        val contentHash: String? = null
    )

    private val items = mutableMapOf<String, TimeSensitiveItem>()
    private val _expiredItems = MutableStateFlow<Set<String>>(emptySet())
    val expiredItems: StateFlow<Set<String>> = _expiredItems.asStateFlow()

    /**
     * Register media with expiry timer
     * @param mediaId Unique identifier
     * @param durationMs Duration in milliseconds
     * @param allowedPresence Optional presence states for visibility window
     */
    fun registerMedia(
        mediaId: String,
        durationMs: Long,
        allowedPresence: List<PresenceState>? = null
    ) {
        val expiryTime = System.currentTimeMillis() + durationMs
        items[mediaId] = TimeSensitiveItem(
            mediaId = mediaId,
            createdAt = System.currentTimeMillis(),
            expiryTime = expiryTime,
            allowedPresenceStates = allowedPresence
        )

        Log.d(TAG, "Registered media $mediaId, expires in ${durationMs}ms")
    }

    /**
     * Set expiry timer for existing media
     * @param mediaId Media identifier
     * @param durationMs Duration in milliseconds
     */
    fun setExpiry(mediaId: String, durationMs: Long) {
        val item = items[mediaId] ?: return
        val expiryTime = System.currentTimeMillis() + durationMs

        items[mediaId] = item.copy(expiryTime = expiryTime)
        Log.d(TAG, "Set expiry for $mediaId: ${durationMs}ms")
    }

    /**
     * Set presence-bound visibility window
     * Media only accessible during specific presence states
     * @param mediaId Media identifier
     * @param allowedStates List of allowed presence states
     */
    fun setPresenceWindow(
        mediaId: String,
        allowedStates: List<PresenceState>
    ) {
        val item = items[mediaId] ?: return
        items[mediaId] = item.copy(allowedPresenceStates = allowedStates)

        Log.d(TAG, "Set presence window for $mediaId: ${allowedStates.size} allowed states")
    }

    /**
     * Check if media is still accessible
     * @param mediaId Media identifier
     * @param currentPresence Current presence state (optional)
     * @return AccessibilityStatus with reason if not accessible
     */
    fun isAccessible(
        mediaId: String,
        currentPresence: PresenceState? = null
    ): AccessibilityStatus {
        val item = items[mediaId]
            ?: return AccessibilityStatus(
                accessible = false,
                reason = "Media not found"
            )

        // Check expiry
        val now = System.currentTimeMillis()
        if (now > item.expiryTime) {
            markExpired(mediaId)
            return AccessibilityStatus(
                accessible = false,
                reason = "Media has expired",
                expiredAt = item.expiryTime
            )
        }

        // Check presence window
        if (item.allowedPresenceStates != null && currentPresence != null) {
            val presenceMatches = item.allowedPresenceStates.any { required ->
                currentPresence.matches(required)
            }

            if (!presenceMatches) {
                return AccessibilityStatus(
                    accessible = false,
                    reason = "Current presence does not match visibility window"
                )
            }
        }

        val timeRemaining = item.expiryTime - now
        return AccessibilityStatus(
            accessible = true,
            timeRemaining = timeRemaining
        )
    }

    /**
     * Get time remaining for media
     * @param mediaId Media identifier
     * @return Time remaining in milliseconds, or 0 if expired
     */
    fun getTimeRemaining(mediaId: String): Long {
        val item = items[mediaId] ?: return 0
        val remaining = item.expiryTime - System.currentTimeMillis()
        return max(0, remaining)
    }

    /**
     * Get expiry time
     * @param mediaId Media identifier
     * @return Absolute timestamp when media expires
     */
    fun getExpiryTime(mediaId: String): Long? {
        return items[mediaId]?.expiryTime
    }

    /**
     * Extend expiry time
     * @param mediaId Media identifier
     * @param additionalMs Additional milliseconds to add
     * @return New expiry time, or null if media not found
     */
    fun extendExpiry(mediaId: String, additionalMs: Long): Long? {
        val item = items[mediaId] ?: return null
        val newExpiryTime = item.expiryTime + additionalMs
        items[mediaId] = item.copy(expiryTime = newExpiryTime)

        Log.d(TAG, "Extended expiry for $mediaId by ${additionalMs}ms")
        return newExpiryTime
    }

    /**
     * Mark media as expired and remove from storage
     * @param mediaId Media identifier
     */
    fun markExpired(mediaId: String) {
        items.remove(mediaId)
        _expiredItems.value = _expiredItems.value + mediaId

        Log.d(TAG, "Marked media as expired: $mediaId")
    }

    /**
     * Cleanup all expired media
     * @return Number of items removed
     */
    suspend fun cleanupExpired(): Int {
        val now = System.currentTimeMillis()
        val expiredIds = items.filter { (_, item) -> now > item.expiryTime }.keys

        expiredIds.forEach { mediaId ->
            items.remove(mediaId)
            _expiredItems.value = _expiredItems.value + mediaId
        }

        if (expiredIds.isNotEmpty()) {
            Log.d(TAG, "Cleaned up ${expiredIds.size} expired items")
        }

        return expiredIds.size
    }

    /**
     * Get all active media
     * @return List of media identifiers
     */
    fun getActiveMedia(): List<String> {
        val now = System.currentTimeMillis()
        return items.filter { (_, item) -> now <= item.expiryTime }.keys.toList()
    }

    /**
     * Get media about to expire (within timeout window)
     * @param windowMs Time window in milliseconds (default 5 minutes)
     * @return List of media about to expire
     */
    fun getExpiringSoon(windowMs: Long = 5 * 60 * 1000): List<String> {
        val now = System.currentTimeMillis()
        return items.filter { (_, item) ->
            val remaining = item.expiryTime - now
            remaining in 0..windowMs
        }.keys.toList()
    }

    /**
     * Revoke media (immediate expiry)
     * @param mediaId Media identifier
     */
    fun revoke(mediaId: String) {
        if (items.containsKey(mediaId)) {
            markExpired(mediaId)
            Log.d(TAG, "Revoked media: $mediaId")
        }
    }

    /**
     * Get statistics
     * @return Statistics about time-sensitive media
     */
    fun getStatistics(): MediaStatistics {
        val now = System.currentTimeMillis()
        val active = items.count { (_, item) -> now <= item.expiryTime }
        val expired = items.size - active
        val expiring = getExpiringSoon().size

        val earliestExpiry = items.values.minOfOrNull { it.expiryTime }
        val latestExpiry = items.values.maxOfOrNull { it.expiryTime }

        return MediaStatistics(
            totalItems = items.size,
            activeItems = active,
            expiredItems = expired,
            expiringItems = expiring,
            earliestExpiryTime = earliestExpiry,
            latestExpiryTime = latestExpiry
        )
    }

    /**
     * Clear all media
     */
    fun clearAll() {
        items.clear()
        _expiredItems.value = emptySet()
        Log.d(TAG, "Cleared all media")
    }

    /**
     * Get status for display/logging
     * @return Status string
     */
    fun getStatus(): String {
        val stats = getStatistics()
        return """
        Time-Sensitive Media Status:
        - Total items: ${stats.totalItems}
        - Active items: ${stats.activeItems}
        - Expired items: ${stats.expiredItems}
        - Expiring soon: ${stats.expiringItems}
        """.trimIndent()
    }
}

/**
 * Result of accessibility check
 */
data class AccessibilityStatus(
    val accessible: Boolean,
    val reason: String? = null,
    val timeRemaining: Long? = null,
    val expiredAt: Long? = null
)

/**
 * Statistics about time-sensitive media
 */
data class MediaStatistics(
    val totalItems: Int,
    val activeItems: Int,
    val expiredItems: Int,
    val expiringItems: Int,
    val earliestExpiryTime: Long?,
    val latestExpiryTime: Long?
) {
    fun getOldestMediaAge(): Long? {
        return earliestExpiryTime?.let { System.currentTimeMillis() - it }
    }

    fun getNewestMediaAge(): Long? {
        return latestExpiryTime?.let { System.currentTimeMillis() - it }
    }
}

/**
 * Duration helper for readability
 */
object TimeDuration {
    fun seconds(n: Long) = n * 1000
    fun minutes(n: Long) = n * 60 * 1000
    fun hours(n: Long) = n * 60 * 60 * 1000
    fun days(n: Long) = n * 24 * 60 * 60 * 1000
    fun weeks(n: Long) = n * 7 * 24 * 60 * 60 * 1000
}
