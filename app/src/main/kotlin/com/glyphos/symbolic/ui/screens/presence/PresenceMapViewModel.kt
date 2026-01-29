package com.glyphos.symbolic.ui.screens.presence

import androidx.lifecycle.ViewModel
import com.glyphos.symbolic.presence.PresenceEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Presence Map Screen ViewModel
 * Delegates to PresenceEngine
 */
@HiltViewModel
class PresenceMapViewModel @Inject constructor(
    val presenceEngine: PresenceEngine
) : ViewModel() {
    // Stub properties for compilation
    private val _userPresence = MutableStateFlow<String?>(null)
    val userPresence: StateFlow<String?> = _userPresence.asStateFlow()
}
