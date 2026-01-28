package com.glyphos.symbolic.core.data

import com.glyphos.symbolic.core.models.*
import kotlinx.coroutines.flow.Flow

/**
 * Core repository interfaces for data access
 *
 * PHASE 0: Data persistence contracts
 */

// ============================================================================
// USER & IDENTITY REPOSITORY
// ============================================================================

interface UserRepository {
    suspend fun createUser(displayName: String): UserIdentity
    suspend fun getUser(userId: String): UserIdentity?
    suspend fun updatePresence(userId: String, presence: PresenceState)
    suspend fun getCurrentUser(): UserIdentity?
    fun observeCurrentUser(): Flow<UserIdentity?>
    fun observePresence(userId: String): Flow<PresenceState>
}

// ============================================================================
// MESSAGE REPOSITORY
// ============================================================================

interface MessageRepository {
    suspend fun sendMessage(message: Message): Boolean
    suspend fun getMessage(messageId: String): Message?
    suspend fun getConversation(userId1: String, userId2: String): List<Message>
    suspend fun markAsRead(messageId: String)
    fun observeMessages(userId: String): Flow<List<Message>>
    fun observeIncomingSignals(userId: String): Flow<List<SignalGlyph>>
    suspend fun deleteMessage(messageId: String)
    suspend fun deleteExpiredMessages()
}

// ============================================================================
// ROOM REPOSITORY
// ============================================================================

interface RoomRepository {
    suspend fun createRoom(room: Room): Room
    suspend fun getRoom(roomId: String): Room?
    suspend fun updateRoom(room: Room)
    suspend fun deleteRoom(roomId: String)
    suspend fun getUserRooms(userId: String): List<Room>
    suspend fun addParticipant(roomId: String, userId: String)
    suspend fun removeParticipant(roomId: String, userId: String)
    fun observeRoom(roomId: String): Flow<Room?>
    fun observeRooms(userId: String): Flow<List<Room>>
}

// ============================================================================
// GLYPH REPOSITORY
// ============================================================================

interface GlyphRepository {
    suspend fun getGlyph(glyphId: String): GlyphIdentity?
    suspend fun searchGlyphs(query: String): List<GlyphIdentity>
    suspend fun getGlyphsByCategory(category: String): List<GlyphIdentity>
    suspend fun getNearestGlyph(metrics: SemanticMetrics): GlyphIdentity?
    suspend fun getAllGlyphs(): List<GlyphIdentity>
    fun observeGlyph(glyphId: String): Flow<GlyphIdentity?>
}

// ============================================================================
// RITUAL REPOSITORY
// ============================================================================

interface RitualRepository {
    suspend fun recordRitual(event: RitualEvent)
    suspend fun getRitualHistory(userId: String, limit: Int = 50): List<RitualEvent>
    fun observeRituals(userId: String): Flow<RitualEvent>
    suspend fun createPresenceContract(contract: PresenceContract)
    suspend fun getPresenceContracts(userId: String): List<PresenceContract>
    suspend fun expireExpiredContracts()
}

// ============================================================================
// ACCESS GRANT REPOSITORY
// ============================================================================

interface AccessRepository {
    suspend fun createGrant(grant: AccessGrant): AccessGrant
    suspend fun getGrant(grantId: String): AccessGrant?
    suspend fun revokeGrant(grantId: String)
    suspend fun getUserGrants(userId: String): List<AccessGrant>
    suspend fun getGrantedResources(userId: String): List<String>
    suspend fun expireExpiredGrants()
}

// ============================================================================
// EMBEDDED CONTENT REPOSITORY
// ============================================================================

interface ContentRepository {
    suspend fun addContent(glyphId: String, content: EmbeddedContent)
    suspend fun getContent(glyphId: String): List<EmbeddedContent>
    suspend fun deleteContent(glyphId: String, contentIndex: Int)
    suspend fun clearContent(glyphId: String)
    fun observeContent(glyphId: String): Flow<List<EmbeddedContent>>
}

// ============================================================================
// ENCRYPTION REPOSITORY
// ============================================================================

interface EncryptionRepository {
    suspend fun encrypt(data: ByteArray, keyAlias: String): EncryptedContent
    suspend fun decrypt(encrypted: EncryptedContent, keyAlias: String): ByteArray?
    suspend fun generateKeyPair(alias: String): Boolean
    suspend fun deleteKey(alias: String): Boolean
    fun keyExists(alias: String): Boolean
    suspend fun shardKey(keyAlias: String, numShards: Int): List<KeyShard>
    suspend fun reassembleKey(shards: List<KeyShard>): ByteArray?
}
