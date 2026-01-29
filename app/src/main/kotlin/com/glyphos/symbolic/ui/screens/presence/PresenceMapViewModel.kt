package com.glyphos.symbolic.ui.screens.presence

import androidx.lifecycle.ViewModel
import com.glyphos.symbolic.presence.PresenceEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Presence Map Screen ViewModel
 * Delegates to PresenceEngine
 */
@HiltViewModel
class PresenceMapViewModel @Inject constructor(
    val presenceEngine: PresenceEngine
) : ViewModel()
