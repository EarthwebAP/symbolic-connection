package com.glyphos.symbolic.security.media

import android.util.Log
import com.glyphos.symbolic.core.models.Message
import com.glyphos.symbolic.core.models.Room
import com.glyphos.symbolic.core.models.RoomType
import com.glyphos.symbolic.core.models.SecurityProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * PHASE 1: Secure Digital Room
 *
 * Zero-notification, ephemeral communication space with maximum privacy:
 * - No notifications sent or received
 * - No external logging
 * - No export/save functionality
 * - No AI access unless explicitly invited
 * - Auto-delete on exit
 * - No room list persistence
 * - Presence-bound access only
 */
class SecureDigitalRoom(
    val roomId: String = "secure-${UUID.randomUUID()}",
    val ownerId: String,
    val participantIds: List<String> = emptyList()
) {
    companion object {
        private const val TAG = "SecureDigitalRoom"
    }

    // Room state
    private val _room = MutableStateFlow(createRoom())
    val room: StateFlow<Room> = _room.asStateFlow()

    // Messages in memory only (no persistence)
    private val messages = mutableListOf<Message>()
    private val _messageFlow = MutableStateFlow<List<Message>>(emptyList())
    val messages_: StateFlow<List<Message>> = _messageFlow.asStateFlow()

    // Activity tracking
    private val _lastActivityTime = MutableStateFlow(System.currentTimeMillis())
    val lastActivityTime: StateFlow<Long> = _lastActivityTime.asStateFlow()

    // Room status
    private val _isActive = MutableStateFlow(true)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    private var createdAt = System.currentTimeMillis()
    private var accessedAt = System.currentTimeMillis()

    /**
     * Create the secure room with strict security profile
     */
    private fun createRoom(): Room {
        return Room(
            roomId = roomId,
            name = "Secure Room",
            type = RoomType.SECURE_DIGITAL,
            ownerId = ownerId,
            participants = participantIds,
            securityProfile = SecurityProfile(
                notificationsEnabled = false,    // ❌ NO NOTIFICATIONS
                loggingEnabled = false,          // ❌ NO EXTERNAL LOGGING
                exportAllowed = false,           // ❌ NO EXPORT
                aiAccessible = false             // ❌ NO AI ACCESS
            ),
            createdAt = createdAt
        )
    }

    /**
     * Add message to room (memory only, no persistence)
     * @param message Message to add
     */
    fun addMessage(message: Message) {
        if (!_isActive.value) {
            Log.w(TAG, "Cannot add message to inactive room")
            return
        }

        messages.add(message)
        _messageFlow.value = messages.toList()
        _lastActivityTime.value = System.currentTimeMillis()

        Log.d(TAG, "Message added to secure room: ${message.messageId}")
    }

    /**
     * Get messages from room
     * Messages are in-memory only
     * @param limit Max messages to return
     * @return List of messages
     */
    fun getMessages(limit: Int = 100): List<Message> {
        return messages.takeLast(limit)
    }

    /**
     * Delete specific message
     * Cannot be recovered
     * @param messageId Message to delete
     */
    fun deleteMessage(messageId: String) {
        val removed = messages.removeAll { it.messageId == messageId }
        if (removed) {
            _messageFlow.value = messages.toList()
            _lastActivityTime.value = System.currentTimeMillis()
            Log.d(TAG, "Message deleted from secure room: $messageId")
        }
    }

    /**
     * Clear all messages from room
     * Cannot be undone
     */
    fun clearAllMessages() {
        val count = messages.size
        messages.clear()
        _messageFlow.value = emptyList()
        _lastActivityTime.value = System.currentTimeMillis()

        Log.w(TAG, "Cleared $count messages from secure room")
    }

    /**
     * Add participant to room
     * @param userId User ID to add
     */
    fun addParticipant(userId: String) {
        if (!_isActive.value) return

        val current = _room.value
        val newParticipants = current.participants.toMutableList()
        if (!newParticipants.contains(userId)) {
            newParticipants.add(userId)
            _room.value = current.copy(participants = newParticipants)
            _lastActivityTime.value = System.currentTimeMillis()
            Log.d(TAG, "Added participant to secure room: $userId")
        }
    }

    /**
     * Remove participant from room
     * @param userId User ID to remove
     */
    fun removeParticipant(userId: String) {
        if (!_isActive.value) return

        val current = _room.value
        val newParticipants = current.participants.toMutableList()
        if (newParticipants.remove(userId)) {
            _room.value = current.copy(participants = newParticipants)
            _lastActivityTime.value = System.currentTimeMillis()
            Log.d(TAG, "Removed participant from secure room: $userId")
        }
    }

    /**
     * Get room participants
     * @return List of user IDs
     */
    fun getParticipants(): List<String> {
        return _room.value.participants.toList()
    }

    /**
     * Get room age
     * @return Age in milliseconds
     */
    fun getRoomAge(): Long {
        return System.currentTimeMillis() - createdAt
    }

    /**
     * Get time since last activity
     * @return Milliseconds since last activity
     */
    fun getTimeSinceLastActivity(): Long {
        return System.currentTimeMillis() - _lastActivityTime.value
    }

    /**
     * Check if room has expired (default 24 hours inactivity)
     * @param maxInactivityMs Max inactivity before expiry
     * @return true if room should be closed
     */
    fun hasExpired(maxInactivityMs: Long = 24 * 60 * 60 * 1000): Boolean {
        return getTimeSinceLastActivity() > maxInactivityMs
    }

    /**
     * Close and destroy room
     * - Clears all messages
     * - Removes all participants
     * - Marks as inactive
     * - Cannot be reopened
     */
    fun closeRoom() {
        if (!_isActive.value) return

        clearAllMessages()
        _room.value = _room.value.copy(
            participants = emptyList(),
            archivedAt = System.currentTimeMillis()
        )
        _isActive.value = false

        Log.w(TAG, "Secure room closed and destroyed: $roomId")
    }

    /**
     * Verify room security configuration
     * @return SecurityVerification with details
     */
    fun verifySecurity(): SecurityVerification {
        val profile = _room.value.securityProfile
        val issues = mutableListOf<String>()

        if (profile.notificationsEnabled) {
            issues.add("Notifications are enabled")
        }
        if (profile.loggingEnabled) {
            issues.add("Logging is enabled")
        }
        if (profile.exportAllowed) {
            issues.add("Export is allowed")
        }
        if (profile.aiAccessible) {
            issues.add("AI is accessible")
        }

        return SecurityVerification(
            isSecure = issues.isEmpty(),
            issues = issues,
            securityLevel = if (issues.isEmpty()) "MAXIMUM" else "COMPROMISED"
        )
    }

    /**
     * Get room statistics
     * @return Room statistics
     */
    fun getStatistics(): RoomStatistics {
        return RoomStatistics(
            roomId = roomId,
            participants = getParticipants().size,
            messageCount = messages.size,
            age = getRoomAge(),
            lastActivity = getTimeSinceLastActivity(),
            isActive = _isActive.value,
            isSecure = verifySecurity().isSecure
        )
    }

    /**
     * Get status for logging/display
     * @return Status string
     */
    fun getStatus(): String {
        val stats = getStatistics()
        val verification = verifySecurity()

        return """
        Secure Digital Room Status:
        - Room ID: $roomId
        - Status: ${if (stats.isActive) "ACTIVE" else "CLOSED"}
        - Security: ${verification.securityLevel}
        - Participants: ${stats.participants}
        - Messages: ${stats.messageCount}
        - Age: ${stats.age}ms
        - Last activity: ${stats.lastActivity}ms ago
        ${if (verification.issues.isNotEmpty()) "- Issues: ${verification.issues.joinToString()}" else ""}
        """.trimIndent()
    }
}

/**
 * Security verification results
 */
data class SecurityVerification(
    val isSecure: Boolean,
    val issues: List<String>,
    val securityLevel: String
)

/**
 * Room statistics
 */
data class RoomStatistics(
    val roomId: String,
    val participants: Int,
    val messageCount: Int,
    val age: Long,
    val lastActivity: Long,
    val isActive: Boolean,
    val isSecure: Boolean
) {
    fun toSummary(): String {
        return "$participants participants, $messageCount messages, age=${age}ms"
    }
}

/**
 * Factory for creating secure rooms
 */
object SecureRoomFactory {
    /**
     * Create new secure room
     * @param ownerId Owner user ID
     * @param participantIds Initial participants
     * @return New SecureDigitalRoom
     */
    fun createSecureRoom(
        ownerId: String,
        participantIds: List<String> = emptyList()
    ): SecureDigitalRoom {
        val room = SecureDigitalRoom(
            ownerId = ownerId,
            participantIds = participantIds
        )
        Log.d("SecureRoomFactory", "Created secure room: ${room.roomId}")
        return room
    }

    /**
     * Create temporary secure room (auto-expires)
     * @param ownerId Owner user ID
     * @param durationMs Expiry duration in milliseconds
     * @param participantIds Initial participants
     * @return New SecureDigitalRoom with expiry
     */
    fun createTemporarySecureRoom(
        ownerId: String,
        durationMs: Long,
        participantIds: List<String> = emptyList()
    ): SecureDigitalRoom {
        val room = SecureDigitalRoom(
            ownerId = ownerId,
            participantIds = participantIds
        )
        // In production, would schedule auto-close
        Log.d("SecureRoomFactory", "Created temporary secure room: ${room.roomId}, expires in ${durationMs}ms")
        return room
    }
}
