package com.glyphos.symbolic.interaction

import android.util.Log
import com.glyphos.symbolic.core.models.DeliveryMode
import com.glyphos.symbolic.core.models.Message
import com.glyphos.symbolic.core.models.PresenceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PHASE 4: Quiet Message Protocol
 *
 * Sender-controlled message delivery with presence awareness.
 * Controls:
 * - Delivery tone (sound, chime, vibration, silent)
 * - Delivery mode (immediate, delayed, scheduled, conditional)
 * - Presence-bound reveal
 * - Notification suppression
 */
class QuietMessageProtocol {
    companion object {
        private const val TAG = "QuietMessageProtocol"
    }

    enum class DeliveryTone {
        SILENT,        // No notification
        VIBRATION,     // Vibration only
        SUBTLE_CHIME,  // Soft sound
        ALERT          // Bold sound + vibration
    }

    /**
     * Message with delivery preferences
     */
    data class QuietMessage(
        val message: Message,
        val tone: DeliveryTone,
        val suppressNotification: Boolean,
        val revealCondition: RevealCondition? = null,
        val sentAt: Long = System.currentTimeMillis()
    )

    sealed class RevealCondition {
        data class PresenceMatch(val requiredPresence: PresenceState) : RevealCondition()
        data class TimedDelay(val delayMs: Long) : RevealCondition()
        object OnOpen : RevealCondition()
    }

    private val _pendingMessages = MutableStateFlow<List<QuietMessage>>(emptyList())
    val pendingMessages: StateFlow<List<QuietMessage>> = _pendingMessages.asStateFlow()

    private val _deliveredMessages = MutableStateFlow<List<QuietMessage>>(emptyList())
    val deliveredMessages: StateFlow<List<QuietMessage>> = _deliveredMessages.asStateFlow()

    /**
     * Send quiet message with delivery preferences
     * @param message Message to send
     * @param tone Delivery tone
     * @param suppress Suppress notification
     * @param condition Optional reveal condition
     */
    suspend fun sendQuietMessage(
        message: Message,
        tone: DeliveryTone = DeliveryTone.VIBRATION,
        suppress: Boolean = false,
        condition: RevealCondition? = null
    ): QuietMessage {
        val quietMsg = QuietMessage(
            message = message,
            tone = tone,
            suppressNotification = suppress,
            revealCondition = condition
        )

        _pendingMessages.value = _pendingMessages.value + quietMsg

        Log.d(TAG, "Quiet message sent: ${message.messageId} (tone=$tone, suppress=$suppress)")
        return quietMsg
    }

    /**
     * Determine if message should be silent based on sender's tone choice
     * @param quietMsg Quiet message
     * @param receiverPresence Receiver's current presence (optional context)
     * @return true if notification should be suppressed
     */
    fun shouldBeSilent(
        quietMsg: QuietMessage,
        receiverPresence: PresenceState? = null
    ): Boolean {
        return when {
            quietMsg.suppressNotification -> true
            quietMsg.tone == DeliveryTone.SILENT -> true
            receiverPresence?.mode?.name == "DEEP_FOCUS" -> true
            else -> false
        }
    }

    /**
     * Get notification audio/vibration for message
     * @param quietMsg Quiet message
     * @return NotificationMode
     */
    fun getNotificationMode(quietMsg: QuietMessage): NotificationMode {
        return when (quietMsg.tone) {
            DeliveryTone.SILENT -> NotificationMode.SILENT
            DeliveryTone.VIBRATION -> NotificationMode.VIBRATION
            DeliveryTone.SUBTLE_CHIME -> NotificationMode.SOUND_ONLY
            DeliveryTone.ALERT -> NotificationMode.SOUND_AND_VIBRATION
        }
    }

    /**
     * Check if message should be revealed based on condition
     * @param quietMsg Message to check
     * @param currentPresence Current presence state (if applicable)
     * @return true if message should be revealed
     */
    fun canRevealMessage(
        quietMsg: QuietMessage,
        currentPresence: PresenceState? = null
    ): Boolean {
        val condition = quietMsg.revealCondition ?: return true

        return when (condition) {
            is RevealCondition.PresenceMatch ->
                currentPresence?.matches(condition.requiredPresence) ?: false
            is RevealCondition.TimedDelay ->
                System.currentTimeMillis() >= quietMsg.sentAt + condition.delayMs
            RevealCondition.OnOpen -> true
        }
    }

    /**
     * Deliver pending messages that meet their conditions
     * @param receiverPresence Current receiver presence
     * @return List of messages ready to deliver
     */
    suspend fun deliverReadyMessages(receiverPresence: PresenceState): List<QuietMessage> {
        val toDeliver = mutableListOf<QuietMessage>()

        for (msg in _pendingMessages.value) {
            if (canRevealMessage(msg, receiverPresence)) {
                toDeliver.add(msg)
            }
        }

        // Move delivered messages
        _pendingMessages.value = _pendingMessages.value.filter { it !in toDeliver }
        _deliveredMessages.value = _deliveredMessages.value + toDeliver

        if (toDeliver.isNotEmpty()) {
            Log.d(TAG, "Delivered ${toDeliver.size} messages")
        }

        return toDeliver
    }

    /**
     * Get pending messages for specific condition type
     * @param conditionType Type of condition
     * @return List of messages
     */
    suspend fun getPendingByCondition(conditionType: String): List<QuietMessage> {
        return _pendingMessages.value.filter { msg ->
            val condition = msg.revealCondition
            condition?.javaClass?.simpleName == conditionType
        }
    }

    /**
     * Cancel pending message
     * @param messageId Message ID to cancel
     * @return true if cancelled
     */
    suspend fun cancelPendingMessage(messageId: String): Boolean {
        val found = _pendingMessages.value.any { it.message.messageId == messageId }
        if (found) {
            _pendingMessages.value = _pendingMessages.value.filter {
                it.message.messageId != messageId
            }
            Log.d(TAG, "Cancelled pending message: $messageId")
        }
        return found
    }

    /**
     * Get all messages (pending + delivered)
     * @return List of all quiet messages
     */
    suspend fun getAllMessages(): List<QuietMessage> {
        return _pendingMessages.value + _deliveredMessages.value
    }

    /**
     * Clear delivered messages
     */
    suspend fun clearDelivered() {
        _deliveredMessages.value = emptyList()
        Log.d(TAG, "Cleared delivered messages")
    }

    /**
     * Get statistics
     * @return MessageStatistics
     */
    suspend fun getStatistics(): MessageStatistics {
        val pending = _pendingMessages.value.size
        val delivered = _deliveredMessages.value.size

        val toneBreakdown = getAllMessages().groupingBy { it.tone }.eachCount()
        val silent = getAllMessages().count { shouldBeSilent(it) }

        return MessageStatistics(
            pendingCount = pending,
            deliveredCount = delivered,
            totalCount = pending + delivered,
            silentCount = silent,
            toneBreakdown = toneBreakdown
        )
    }

    /**
     * Get status
     */
    suspend fun getStatus(): String {
        val stats = getStatistics()
        return """
        Quiet Message Protocol Status:
        - Pending: ${stats.pendingCount}
        - Delivered: ${stats.deliveredCount}
        - Silent: ${stats.silentCount}
        - By tone: ${stats.toneBreakdown}
        """.trimIndent()
    }
}

enum class NotificationMode {
    SILENT,
    VIBRATION,
    SOUND_ONLY,
    SOUND_AND_VIBRATION
}

data class MessageStatistics(
    val pendingCount: Int,
    val deliveredCount: Int,
    val totalCount: Int,
    val silentCount: Int,
    val toneBreakdown: Map<QuietMessageProtocol.DeliveryTone, Int>
)
