package com.glyphos.symbolic.data

import com.glyphos.symbolic.core.contracts.Message
import com.glyphos.symbolic.core.models.GlyphIdentity
import java.io.Serializable

/**
 * Contact data model for WhatsApp-like functionality
 */
data class Contact(
    val userId: String,
    val displayName: String,
    val phoneNumber: String? = null,
    val personalGlyph: GlyphIdentity? = null,
    val lastMessageTime: Long? = null,
    val status: ContactStatus = ContactStatus.ACTIVE,
    val addedAt: Long = System.currentTimeMillis()
) : Serializable

enum class ContactStatus {
    ACTIVE,
    BLOCKED,
    ARCHIVED,
    DELETED
}

/**
 * Chat session between user and contact
 */
data class ChatSession(
    val chatId: String,
    val participantIds: List<String>,  // Always 2 for direct chat
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val mutedUntil: Long? = null,  // Null = not muted
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val archivedAt: Long? = null
) : Serializable

/**
 * Message with minimal metadata (full message in Firestore)
 */
data class MessageItem(
    val messageId: String,
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val deliveryStatus: DeliveryStatus = DeliveryStatus.SENT,
    val readAt: Long? = null,
    val mediaUrls: List<String> = emptyList(),
    val replyToId: String? = null
) : Serializable

enum class DeliveryStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}

/**
 * Call record
 */
data class CallRecord(
    val callId: String,
    val initiatorId: String,
    val recipientId: String,
    val callType: CallType,
    val startTime: Long,
    val endTime: Long? = null,
    val duration: Long = 0,
    val status: CallStatus = CallStatus.MISSED
) : Serializable

enum class CallType {
    VOICE,
    VIDEO
}

enum class CallStatus {
    INCOMING,
    OUTGOING,
    MISSED,
    DECLINED,
    COMPLETED,
    FAILED
}
