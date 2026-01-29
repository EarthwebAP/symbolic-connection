package com.glyphos.symbolic.ui.screens.rooms

import androidx.lifecycle.ViewModel
import com.glyphos.symbolic.core.models.Message
import com.glyphos.symbolic.core.models.EncryptedContent
import com.glyphos.symbolic.core.models.DeliveryProfile
import com.glyphos.symbolic.core.models.DeliveryMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import java.util.UUID

data class MessageItem(
    val id: String,
    val senderId: String,
    val content: String,
    val timestamp: Long
)

@HiltViewModel
class RoomDetailViewModel @Inject constructor() : ViewModel() {

    private val _messages = MutableStateFlow<List<MessageItem>>(emptyList())
    val messages: StateFlow<List<MessageItem>> = _messages.asStateFlow()

    private val _newMessage = MutableStateFlow("")
    val newMessage: StateFlow<String> = _newMessage.asStateFlow()

    init {
        loadMessages()
    }

    private fun loadMessages() {
        val sampleMessages = listOf(
            MessageItem(
                id = "msg-1",
                senderId = "user-2",
                content = "Hey, how are you?",
                timestamp = System.currentTimeMillis() - 3600000
            ),
            MessageItem(
                id = "msg-2",
                senderId = "user-1",
                content = "Good! Working on something interesting.",
                timestamp = System.currentTimeMillis() - 1800000
            ),
            MessageItem(
                id = "msg-3",
                senderId = "user-2",
                content = "Nice! Tell me more",
                timestamp = System.currentTimeMillis() - 900000
            )
        )
        _messages.value = sampleMessages
    }

    fun updateNewMessage(text: String) {
        _newMessage.value = text
    }

    fun sendMessage() {
        if (_newMessage.value.isBlank()) return

        val newMsg = MessageItem(
            id = UUID.randomUUID().toString(),
            senderId = "user-1",
            content = _newMessage.value,
            timestamp = System.currentTimeMillis()
        )

        _messages.value = _messages.value + newMsg
        _newMessage.value = ""
    }
}
