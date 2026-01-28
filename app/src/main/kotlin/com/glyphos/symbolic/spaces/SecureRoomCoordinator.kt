package com.glyphos.symbolic.spaces

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
 * PHASE 3: Secure Room Coordinator
 *
 * Coordinates zero-notification ephemeral communication spaces.
 * - No external logging
 * - No notifications
 * - No export/save
 * - In-memory only (destroyed on close)
 * - Presence-bound access
 * - Auto-delete timer
 */
class SecureRoomCoordinator {
    companion object {
        private const val TAG = "SecureRoomCoordinator"
        private const val DEFAULT_ROOM_LIFETIME_MS = 24 * 60 * 60 * 1000L  // 24 hours
    }

    // Track active secure rooms
    private val activeRooms = mutableMapOf<String, SecureRoomSession>()
    private val _roomsFlow = MutableStateFlow<List<SecureRoomSession>>(emptyList())
    val activeRooms_: StateFlow<List<SecureRoomSession>> = _roomsFlow.asStateFlow()

    /**
     * Create new secure room
     * @param ownerUserId Owner user ID
     * @param participantIds Initial participants
     * @param lifetime Room lifetime in milliseconds
     * @return Room ID
     */
    suspend fun createSecureRoom(
        ownerUserId: String,
        participantIds: List<String>,
        lifetime: Long = DEFAULT_ROOM_LIFETIME_MS
    ): String {
        val roomId = "secure-${UUID.randomUUID()}"

        val room = Room(
            roomId = roomId,
            name = "Secure Room",
            type = RoomType.SECURE_DIGITAL,
            ownerId = ownerUserId,
            participants = participantIds,
            securityProfile = SecurityProfile(
                notificationsEnabled = false,
                loggingEnabled = false,
                exportAllowed = false,
                aiAccessible = false
            )
        )

        val session = SecureRoomSession(
            room = room,
            messages = mutableListOf(),
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + lifetime
        )

        activeRooms[roomId] = session
        updateRoomsFlow()

        Log.d(TAG, "Secure room created: $roomId")
        return roomId
    }

    /**
     * Get room
     * @param roomId Room ID
     * @return SecureRoomSession or null
     */
    suspend fun getRoom(roomId: String): SecureRoomSession? {
        return activeRooms[roomId]
    }

    /**
     * Add message to room
     * @param roomId Room ID
     * @param message Message to add
     * @return true if added
     */
    suspend fun addMessage(roomId: String, message: Message): Boolean {
        val session = activeRooms[roomId] ?: return false

        if (System.currentTimeMillis() > session.expiresAt) {
            closeRoom(roomId)
            return false
        }

        session.messages.add(message)
        updateRoomsFlow()

        Log.d(TAG, "Message added to room: $roomId")
        return true
    }

    /**
     * Get messages from room
     * @param roomId Room ID
     * @return List of messages
     */
    suspend fun getMessages(roomId: String): List<Message> {
        return activeRooms[roomId]?.messages?.toList() ?: emptyList()
    }

    /**
     * Add participant to room
     * @param roomId Room ID
     * @param userId User to add
     */
    suspend fun addParticipant(roomId: String, userId: String) {
        val session = activeRooms[roomId] ?: return

        val newParticipants = session.room.participants.toMutableList()
        if (!newParticipants.contains(userId)) {
            newParticipants.add(userId)
            activeRooms[roomId] = session.copy(
                room = session.room.copy(participants = newParticipants)
            )
            updateRoomsFlow()
            Log.d(TAG, "Participant added to room: $roomId")
        }
    }

    /**
     * Remove participant
     * @param roomId Room ID
     * @param userId User to remove
     */
    suspend fun removeParticipant(roomId: String, userId: String) {
        val session = activeRooms[roomId] ?: return

        val newParticipants = session.room.participants.toMutableList()
        if (newParticipants.remove(userId)) {
            activeRooms[roomId] = session.copy(
                room = session.room.copy(participants = newParticipants)
            )
            updateRoomsFlow()
            Log.d(TAG, "Participant removed from room: $roomId")
        }
    }

    /**
     * Close room permanently
     * @param roomId Room to close
     */
    suspend fun closeRoom(roomId: String) {
        activeRooms.remove(roomId)
        updateRoomsFlow()

        Log.w(TAG, "Room closed and destroyed: $roomId")
    }

    /**
     * Get room age in milliseconds
     * @param roomId Room ID
     * @return Age or 0
     */
    suspend fun getRoomAge(roomId: String): Long {
        val session = activeRooms[roomId] ?: return 0
        return System.currentTimeMillis() - session.createdAt
    }

    /**
     * Get time until expiry
     * @param roomId Room ID
     * @return Milliseconds until expiry, or 0 if expired
     */
    suspend fun getTimeToExpiry(roomId: String): Long {
        val session = activeRooms[roomId] ?: return 0
        val remaining = session.expiresAt - System.currentTimeMillis()
        return if (remaining > 0) remaining else 0
    }

    /**
     * Check if room has expired
     * @param roomId Room ID
     * @return true if expired
     */
    suspend fun isExpired(roomId: String): Boolean {
        val session = activeRooms[roomId] ?: return true
        return System.currentTimeMillis() > session.expiresAt
    }

    /**
     * Cleanup expired rooms
     * @return Number of rooms removed
     */
    suspend fun cleanupExpired(): Int {
        val now = System.currentTimeMillis()
        val expiredIds = activeRooms.filter { (_, session) ->
            now > session.expiresAt
        }.keys

        expiredIds.forEach { roomId ->
            activeRooms.remove(roomId)
        }

        if (expiredIds.isNotEmpty()) {
            updateRoomsFlow()
            Log.d(TAG, "Cleaned up ${expiredIds.size} expired rooms")
        }

        return expiredIds.size
    }

    /**
     * Extend room lifetime
     * @param roomId Room ID
     * @param additionalMs Additional milliseconds
     * @return New expiry time, or 0 if room not found
     */
    suspend fun extendRoomLifetime(roomId: String, additionalMs: Long): Long {
        val session = activeRooms[roomId] ?: return 0

        val newExpiryTime = session.expiresAt + additionalMs
        activeRooms[roomId] = session.copy(expiresAt = newExpiryTime)
        updateRoomsFlow()

        Log.d(TAG, "Extended room lifetime: $roomId")
        return newExpiryTime
    }

    /**
     * Get all active rooms
     * @return List of room sessions
     */
    suspend fun getActiveRooms(): List<SecureRoomSession> {
        return activeRooms.values.toList()
    }

    /**
     * Get rooms owned by user
     * @param userId Owner user ID
     * @return List of rooms
     */
    suspend fun getUserRooms(userId: String): List<SecureRoomSession> {
        return activeRooms.values.filter { it.room.ownerId == userId }.toList()
    }

    /**
     * Get statistics
     * @return RoomStatistics
     */
    suspend fun getStatistics(): RoomStatistics {
        val totalRooms = activeRooms.size
        val totalMessages = activeRooms.values.sumOf { it.messages.size }
        val totalParticipants = activeRooms.values.flatMap { it.room.participants }.distinct().size

        return RoomStatistics(
            totalRooms = totalRooms,
            totalMessages = totalMessages,
            totalParticipants = totalParticipants,
            averageRoomAge = if (totalRooms > 0) {
                activeRooms.values.map { System.currentTimeMillis() - it.createdAt }.average().toLong()
            } else 0L
        )
    }

    /**
     * Verify room security configuration
     * @param roomId Room ID
     * @return true if room is properly secured
     */
    suspend fun verifyRoomSecurity(roomId: String): Boolean {
        val session = activeRooms[roomId] ?: return false
        val profile = session.room.securityProfile

        return !profile.notificationsEnabled &&
               !profile.loggingEnabled &&
               !profile.exportAllowed &&
               !profile.aiAccessible
    }

    /**
     * Update rooms flow
     */
    private fun updateRoomsFlow() {
        _roomsFlow.value = activeRooms.values.toList()
    }

    /**
     * Get status
     * @return Status string
     */
    suspend fun getStatus(): String {
        val stats = getStatistics()
        return """
        Secure Room Coordinator Status:
        - Active rooms: ${stats.totalRooms}
        - Total messages: ${stats.totalMessages}
        - Unique participants: ${stats.totalParticipants}
        - Average room age: ${stats.averageRoomAge}ms
        """.trimIndent()
    }
}

data class SecureRoomSession(
    val room: Room,
    val messages: MutableList<Message>,
    val createdAt: Long,
    val expiresAt: Long
)

data class RoomStatistics(
    val totalRooms: Int,
    val totalMessages: Int,
    val totalParticipants: Int,
    val averageRoomAge: Long
)
