package com.glyphos.symbolic.ui.screens.contacts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glyphos.symbolic.data.ChatSession
import com.glyphos.symbolic.data.Contact
import com.glyphos.symbolic.data.ContactRepository
import com.glyphos.symbolic.data.PhoneContactsManager
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

    private val _importError = MutableStateFlow<String?>(null)
    val importError: StateFlow<String?> = _importError

    private val _importedCount = MutableStateFlow(0)
    val importedCount: StateFlow<Int> = _importedCount

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

    /**
     * Import contacts from device phone contacts
     * Requires READ_CONTACTS permission
     */
    fun importPhoneContacts(context: Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _importError.value = null
                _importedCount.value = 0

                val phoneContactsManager = PhoneContactsManager(context)
                val phoneContacts = phoneContactsManager.getPhoneContacts()

                if (phoneContacts.isEmpty()) {
                    _importError.value = "No contacts found or permission denied"
                    _isLoading.value = false
                    return@launch
                }

                // Add each phone contact to the app
                var successCount = 0
                phoneContacts.forEach { contact ->
                    try {
                        val result = contactRepository.addContact(contact)
                        if (result.isSuccess) {
                            successCount++
                        }
                    } catch (e: Exception) {
                        // Continue with next contact
                    }
                }

                _importedCount.value = successCount
                if (successCount > 0) {
                    _contacts.value = _contacts.value + phoneContacts.take(successCount)
                }
                loadChatSessions()
            } catch (e: SecurityException) {
                _importError.value = "READ_CONTACTS permission not granted. Please enable it in app settings."
            } catch (e: Exception) {
                _importError.value = "Error importing contacts: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Search phone contacts by name
     */
    fun searchPhoneContacts(context: Context, query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val phoneContactsManager = PhoneContactsManager(context)
                val results = phoneContactsManager.searchPhoneContacts(query)
                _contacts.value = results
            } catch (e: Exception) {
                _importError.value = "Error searching contacts: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
