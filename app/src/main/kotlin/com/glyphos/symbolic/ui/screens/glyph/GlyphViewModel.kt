package com.glyphos.symbolic.ui.screens.glyph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glyphos.symbolic.core.models.GlyphIdentity
import com.glyphos.symbolic.core.models.SemanticMetrics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Simple data class for embedded content display
data class EmbeddedContentItem(
    val id: String,
    val type: String,
    val title: String,
    val data: String
)

@HiltViewModel
class GlyphViewModel @Inject constructor() : ViewModel() {

    private val _userGlyph = MutableStateFlow<GlyphIdentity?>(null)
    val userGlyph: StateFlow<GlyphIdentity?> = _userGlyph.asStateFlow()

    private val _embeddedContent = MutableStateFlow<List<EmbeddedContentItem>>(emptyList())
    val embeddedContent: StateFlow<List<EmbeddedContentItem>> = _embeddedContent.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadGlyph()
    }

    private fun loadGlyph() {
        viewModelScope.launch {
            try {
                val glyph = GlyphIdentity(
                    glyphId = "glyph-detail",
                    name = "Personal Glyph",
                    visualData = ByteArray(256),
                    semanticMetrics = SemanticMetrics(
                        power = 60,
                        complexity = 70,
                        resonance = 50,
                        stability = 80,
                        connectivity = 65,
                        affinity = 55
                    ),
                    resonancePattern = ByteArray(64)
                )
                _userGlyph.value = glyph

                // Create some sample embedded content items
                val content = listOf(
                    EmbeddedContentItem(
                        id = "content-1",
                        type = "note",
                        title = "Thoughts",
                        data = "Your personal notes and thoughts"
                    ),
                    EmbeddedContentItem(
                        id = "content-2",
                        type = "memory",
                        title = "Memories",
                        data = "Important moments"
                    ),
                    EmbeddedContentItem(
                        id = "content-3",
                        type = "intention",
                        title = "Intentions",
                        data = "Future aspirations"
                    )
                )
                _embeddedContent.value = content

                _isLoading.value = false
            } catch (e: Exception) {
                val minimalGlyph = GlyphIdentity(
                    glyphId = "glyph-backup",
                    name = "Backup Glyph",
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
                _embeddedContent.value = emptyList()
                _isLoading.value = false
            }
        }
    }
}
