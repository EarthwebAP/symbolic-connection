package com.glyphos.symbolic.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glyphos.symbolic.core.models.PresenceState
import com.glyphos.symbolic.core.models.PresenceMode
import com.glyphos.symbolic.core.models.EmotionalTone
import com.glyphos.symbolic.core.models.FocusLevel
import com.glyphos.symbolic.core.models.SocialContext
import com.glyphos.symbolic.core.models.SemanticMetrics
import com.glyphos.symbolic.core.models.GlyphIdentity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _userGlyph = MutableStateFlow<GlyphIdentity?>(null)
    val userGlyph: StateFlow<GlyphIdentity?> = _userGlyph.asStateFlow()

    private val _presence = MutableStateFlow<PresenceState?>(null)
    val presence: StateFlow<PresenceState?> = _presence.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                // Generate a default presence state
                val defaultPresence = PresenceState(
                    mode = PresenceMode.CALM,
                    emotionalTone = EmotionalTone.NEUTRAL,
                    focusLevel = FocusLevel.MEDIUM,
                    socialContext = SocialContext.ALONE
                )
                _presence.value = defaultPresence

                // Create a personal glyph
                val glyph = GlyphIdentity(
                    glyphId = "glyph-001",
                    name = "Personal Glyph",
                    visualData = ByteArray(256),
                    semanticMetrics = SemanticMetrics(
                        power = 50,
                        complexity = 50,
                        resonance = 50,
                        stability = 50,
                        connectivity = 50,
                        affinity = 50
                    ),
                    resonancePattern = ByteArray(64)
                )
                _userGlyph.value = glyph

                _isLoading.value = false
            } catch (e: Exception) {
                // If generation fails, create a minimal glyph
                val minimalGlyph = GlyphIdentity(
                    glyphId = "default-glyph",
                    name = "Default",
                    visualData = ByteArray(256),
                    semanticMetrics = SemanticMetrics(
                        power = 50,
                        complexity = 50,
                        resonance = 50,
                        stability = 50,
                        connectivity = 50,
                        affinity = 50
                    ),
                    resonancePattern = ByteArray(64)
                )
                _userGlyph.value = minimalGlyph
                _presence.value = PresenceState(
                    mode = PresenceMode.CALM,
                    emotionalTone = EmotionalTone.NEUTRAL,
                    focusLevel = FocusLevel.MEDIUM,
                    socialContext = SocialContext.ALONE
                )
                _isLoading.value = false
            }
        }
    }

    fun updatePresence(mode: PresenceMode, tone: EmotionalTone, focus: FocusLevel, social: SocialContext) {
        val newPresence = PresenceState(
            mode = mode,
            emotionalTone = tone,
            focusLevel = focus,
            socialContext = social
        )
        _presence.value = newPresence
    }
}
