package com.glyphos.symbolic.data

import com.glyphos.symbolic.core.contracts.EncryptedContent
import com.glyphos.symbolic.core.contracts.Message
import com.glyphos.symbolic.core.contracts.UserId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseMessagingService @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun sendMessage(message: Message): Result<String> {
        return try {
            val chatId = generateChatId(message.senderId.value, message.receiverId.value)
            val messageMap = mapOf(
                "messageId" to message.messageId,
                "senderId" to message.senderId.value,
                "receiverId" to message.receiverId.value,
                "contentCiphertext" to message.content.ciphertext,
                "contentKeyAlias" to message.content.keyAlias,
                "contentNonce" to message.content.nonce,
                "contentAlgorithm" to message.content.algorithm,
                "contentType" to message.contentType.name,
                "timestamp" to message.timestamp,
                "expiryTime" to message.expiryTime,
                "readAt" to message.readAt,
                "deliveryMode" to message.deliveryProfile.mode.name,
                "mediaUrls" to message.mediaUrls
            )
            firestore
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .document(message.messageId)
                .set(messageMap)
                .await()
            Result.success(message.messageId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMessagesStream(
        userId1: UserId,
        userId2: UserId,
        limit: Int = 50
    ): Flow<List<MessageItem>> = flow {
        try {
            val chatId = generateChatId(userId1.value, userId2.value)
            firestore
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        throw error
                    }
                    val messages = snapshot?.documents?.map { doc ->
                        MessageItem(
                            messageId = doc.getString("messageId") ?: "",
                            senderId = doc.getString("senderId") ?: "",
                            content = doc.getString("contentCiphertext") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0,
                            deliveryStatus = DeliveryStatus.valueOf(
                                doc.getString("deliveryMode") ?: "SENT"
                            ),
                            readAt = doc.getLong("readAt"),
                            mediaUrls = (doc.get("mediaUrls") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                        )
                    } ?: emptyList()
                }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun markAsRead(messageId: String, userId1: UserId, userId2: UserId): Result<Unit> {
        return try {
            val chatId = generateChatId(userId1.value, userId2.value)
            firestore
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .update("readAt", System.currentTimeMillis())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMessage(messageId: String, userId1: UserId, userId2: UserId): Result<Unit> {
        return try {
            val chatId = generateChatId(userId1.value, userId2.value)
            firestore
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChatSessions(userId: UserId, limit: Int = 50): Flow<List<ChatSession>> = flow {
        try {
            firestore
                .collection("users")
                .document(userId.value)
                .collection("chatSessions")
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        throw error
                    }
                    val sessions = snapshot?.documents?.map { doc ->
                        ChatSession(
                            chatId = doc.getId(),
                            participantIds = (doc.get("participantIds") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                            unreadCount = doc.getLong("unreadCount")?.toInt() ?: 0,
                            isPinned = doc.getBoolean("isPinned") ?: false,
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    } ?: emptyList()
                }
        } catch (e: Exception) {
            throw e
        }
    }

    private fun generateChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "$userId1-$userId2" else "$userId2-$userId1"
    }
}
