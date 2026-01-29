package com.glyphos.symbolic.ui.screens.presence

import androidx.lifecycle.ViewModel
import com.glyphos.symbolic.core.models.PresenceState
import com.glyphos.symbolic.core.models.PresenceMode
import com.glyphos.symbolic.core.models.EmotionalTone
import com.glyphos.symbolic.core.models.FocusLevel
import com.glyphos.symbolic.core.models.SocialContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PresenceViewModel @Inject constructor() : ViewModel() {

    private val _currentPresence = MutableStateFlow(
        PresenceState(
            mode = PresenceMode.CALM,
            emotionalTone = EmotionalTone.NEUTRAL,
            focusLevel = FocusLevel.MEDIUM,
            socialContext = SocialContext.ALONE
        )
    )
    val currentPresence: StateFlow<PresenceState> = _currentPresence.asStateFlow()

    fun updateMode(mode: PresenceMode) {
        _currentPresence.value = _currentPresence.value.copy(mode = mode)
    }

    fun updateTone(tone: EmotionalTone) {
        _currentPresence.value = _currentPresence.value.copy(emotionalTone = tone)
    }

    fun updateFocus(focus: FocusLevel) {
        _currentPresence.value = _currentPresence.value.copy(focusLevel = focus)
    }

    fun updateSocial(context: SocialContext) {
        _currentPresence.value = _currentPresence.value.copy(socialContext = context)
    }

    fun save() {
        // TODO: Persist to database or API
    }
}
