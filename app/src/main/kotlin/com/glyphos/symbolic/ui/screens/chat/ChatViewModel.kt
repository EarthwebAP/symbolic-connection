package com.glyphos.symbolic.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glyphos.symbolic.data.ChatSession
import com.glyphos.symbolic.data.ContactRepository
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
}
