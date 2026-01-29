package com.glyphos.symbolic.ui.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glyphos.symbolic.data.ChatSession
import com.glyphos.symbolic.data.Contact
import com.glyphos.symbolic.data.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts

    private val _chatSessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val chatSessions: StateFlow<List<ChatSession>> = _chatSessions

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadChatSessions()
    }

    private fun loadChatSessions() {
        viewModelScope.launch {
            _isLoading.value = true
            // TODO: Get current user ID from auth
            val userId = "current-user-id"
            contactRepository.getAllChatSessions(userId).collect { sessions ->
                _chatSessions.value = sessions.sortedByDescending { it.lastMessage?.timestamp ?: 0 }
            }
            _isLoading.value = false
        }
    }

    fun loadAllContacts() {
        viewModelScope.launch {
            contactRepository.getAllContacts().collect { contactList ->
                _contacts.value = contactList
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchContacts(query: String) {
        viewModelScope.launch {
            val results = contactRepository.searchContacts(query)
            _contacts.value = results
        }
    }

    fun blockContact(userId: String) {
        viewModelScope.launch {
            contactRepository.blockContact(userId)
        }
    }

    fun unblockContact(userId: String) {
        viewModelScope.launch {
            contactRepository.unblockContact(userId)
        }
    }

    fun deleteContact(userId: String) {
        viewModelScope.launch {
            contactRepository.deleteContact(userId)
        }
    }

    fun archiveChat(chatId: String) {
        viewModelScope.launch {
            // TODO: Archive chat
        }
    }

    fun pinChat(chatId: String) {
        viewModelScope.launch {
            // TODO: Pin chat
        }
    }

    fun muteChat(chatId: String, durationMs: Long) {
        viewModelScope.launch {
            // TODO: Mute chat
        }
    }
}
