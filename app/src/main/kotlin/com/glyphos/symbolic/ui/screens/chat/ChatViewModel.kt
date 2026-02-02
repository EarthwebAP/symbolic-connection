package com.glyphos.symbolic.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glyphos.symbolic.data.ChatSession
import com.glyphos.symbolic.data.CipherCodec
import com.glyphos.symbolic.data.CipherMessage
import com.glyphos.symbolic.data.CipherMessageItem
import com.glyphos.symbolic.data.ContactRepository
import com.glyphos.symbolic.data.DeliveryStatus
import com.glyphos.symbolic.data.MessageItem
import com.glyphos.symbolic.data.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<MessageItem>>(emptyList())
    val messages: StateFlow<List<MessageItem>> = _messages

    private val _cipherMessages = MutableStateFlow<List<CipherMessageItem>>(emptyList())
    val cipherMessages: StateFlow<List<CipherMessageItem>> = _cipherMessages

    private val _cipherStorage = MutableStateFlow<Map<String, CipherMessage>>(emptyMap())
    val cipherStorage: StateFlow<Map<String, CipherMessage>> = _cipherStorage

    private val _newMessage = MutableStateFlow("")
    val newMessage: StateFlow<String> = _newMessage

    private val _chatSession = MutableStateFlow<ChatSession?>(null)
    val chatSession: StateFlow<ChatSession?> = _chatSession

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _typingUsers = MutableStateFlow<List<String>>(emptyList())
    val typingUsers: StateFlow<List<String>> = _typingUsers

    fun loadChatSession(chatId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val session = contactRepository.getChatSession(chatId)
            _chatSession.value = session
            _isLoading.value = false

            // Load messages
            if (session != null) {
                messageRepository.getMessages(chatId).collect { msgs ->
                    _messages.value = msgs
                }
            }
        }
    }

    fun updateNewMessage(text: String) {
        _newMessage.value = text
    }

    fun sendMessage() {
        val chatId = _chatSession.value?.chatId ?: return
        val text = _newMessage.value.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            // TODO: Create Message object and send
            _newMessage.value = ""
            // Message will appear in stream
        }
    }

    fun markAsRead(messageId: String) {
        val chatId = _chatSession.value?.chatId ?: return
        viewModelScope.launch {
            messageRepository.markAsRead(messageId, chatId)
        }
    }

    fun deleteMessage(messageId: String) {
        val chatId = _chatSession.value?.chatId ?: return
        viewModelScope.launch {
            messageRepository.deleteMessage(messageId, chatId)
        }
    }

    fun searchMessages(query: String) {
        val chatId = _chatSession.value?.chatId ?: return
        viewModelScope.launch {
            val results = messageRepository.searchMessages(chatId, query)
            _messages.value = results
        }
    }

    fun setTypingIndicator(isTyping: Boolean) {
        viewModelScope.launch {
            // TODO: Broadcast typing indicator to recipient
        }
    }

    fun sendCipherMessage(cipher: CipherMessage) {
        viewModelScope.launch {
            // Store cipher in local map
            val storage = _cipherStorage.value.toMutableMap()
            storage[cipher.messageId] = cipher
            _cipherStorage.value = storage

            // Create cipher message item for display
            val item = CipherMessageItem(
                messageId = cipher.messageId,
                senderId = cipher.senderId,
                senderName = "You",
                timestamp = cipher.timestamp,
                deliveryStatus = DeliveryStatus.SENT,
                glyphPreviewId = cipher.glyphId,
                isRevealed = false,
                embeddingFrequency = cipher.embeddingFrequency
            )

            // Add to cipher messages list
            val currentCiphers = _cipherMessages.value.toMutableList()
            currentCiphers.add(item)
            _cipherMessages.value = currentCiphers
        }
    }

    fun receiveCipherMessage(cipher: CipherMessage, senderName: String) {
        viewModelScope.launch {
            // Store cipher
            val storage = _cipherStorage.value.toMutableMap()
            storage[cipher.messageId] = cipher
            _cipherStorage.value = storage

            // Create cipher message item for display
            val item = CipherMessageItem(
                messageId = cipher.messageId,
                senderId = cipher.senderId,
                senderName = senderName,
                timestamp = cipher.timestamp,
                deliveryStatus = DeliveryStatus.DELIVERED,
                glyphPreviewId = cipher.glyphId,
                isRevealed = false,
                embeddingFrequency = cipher.embeddingFrequency
            )

            val currentCiphers = _cipherMessages.value.toMutableList()
            currentCiphers.add(item)
            _cipherMessages.value = currentCiphers
        }
    }

    fun decryptCipher(cipherId: String): String? {
        val cipher = _cipherStorage.value[cipherId] ?: return null
        // Decrypt with harmonic frequency and timestamp as seed
        return CipherCodec.decode(cipher.encryptedData, cipher.embeddingFrequency, cipher.timestamp)
    }

    fun deleteCipher(cipherId: String) {
        viewModelScope.launch {
            val ciphers = _cipherMessages.value.toMutableList()
            ciphers.removeAll { it.messageId == cipherId }
            _cipherMessages.value = ciphers

            val storage = _cipherStorage.value.toMutableMap()
            storage.remove(cipherId)
            _cipherStorage.value = storage
        }
    }
}
