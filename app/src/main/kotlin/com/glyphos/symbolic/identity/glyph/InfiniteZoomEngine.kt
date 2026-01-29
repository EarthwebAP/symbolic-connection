package com.glyphos.symbolic.identity.glyph

import com.glyphos.symbolic.core.contracts.CanvasElement
import com.glyphos.symbolic.core.contracts.ElementType
import com.glyphos.symbolic.core.contracts.EmbeddedContent
import com.glyphos.symbolic.core.contracts.GlyphMicroContent
import com.glyphos.symbolic.core.contracts.GlowState
import com.glyphos.symbolic.core.contracts.UserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Infinite Zoom Engine for Glyphs
 * Allows zooming to 30,000%+ into glyphs with micro-workspaces
 */
@Singleton
class InfiniteZoomEngine @Inject constructor() {

    private val _zoomLevels = MutableStateFlow<Map<String, GlyphMicroContent>>(emptyMap())
    val zoomLevels: StateFlow<Map<String, GlyphMicroContent>> = _zoomLevels

    private val _currentViewport = MutableStateFlow<Viewport?>(null)
    val currentViewport: StateFlow<Viewport?> = _currentViewport

    fun zoomIntoGlyph(glyphId: String, userId: UserId): GlyphMicroContent {
        var content = _zoomLevels.value[glyphId]

        if (content == null) {
            content = GlyphMicroContent(
                glyphId = glyphId,
                ownerId = userId,
                zoomLevel = 1.0,
                content = emptyList(),
                hasHiddenContent = false,
                resonanceGlow = GlowState.NONE
            )
            val map = _zoomLevels.value.toMutableMap()
            map[glyphId] = content
            _zoomLevels.value = map
        }

        return content
    }

    fun zoom(glyphId: String, factor: Double, maxZoom: Double = 30000.0): Double {
        val current = _zoomLevels.value[glyphId] ?: return 1.0
        val newZoom = (current.zoomLevel * factor).coerceIn(1.0, maxZoom)

        val updated = current.copy(zoomLevel = newZoom)
        val map = _zoomLevels.value.toMutableMap()
        map[glyphId] = updated
        _zoomLevels.value = map

        return newZoom
    }

    fun setViewport(glyphId: String, x: Double, y: Double) {
        _currentViewport.value = Viewport(
            glyphId = glyphId,
            x = x,
            y = y,
            timestamp = System.currentTimeMillis()
        )
    }

    fun attachContent(
        glyphId: String,
        element: CanvasElement
    ): Boolean {
        try {
            val content = _zoomLevels.value[glyphId] ?: return false
            val updated = content.copy(
                content = content.content.toMutableList().apply {
                    add(when (element.type) {
                        ElementType.NOTE -> EmbeddedContent.Note(
                            text = String(element.data),
                            timestamp = System.currentTimeMillis()
                        )
                        ElementType.FILE -> EmbeddedContent.File(
                            path = String(element.data),
                            encrypted = element.encrypted
                        )
                        ElementType.IMAGE -> EmbeddedContent.File(
                            path = String(element.data),
                            mimeType = "image/*",
                            encrypted = element.encrypted
                        )
                        ElementType.GLYPH -> EmbeddedContent.File(
                            path = String(element.data),
                            mimeType = "glyph"
                        )
                        ElementType.MICROTHREAD -> EmbeddedContent.MicroThread(
                            messages = emptyList()
                        )
                        ElementType.RESONANCE_PULSE -> EmbeddedContent.MessageRef(
                            messageId = String(element.data),
                            encryptedContent = com.glyphos.symbolic.core.contracts.EncryptedContent(
                                ciphertext = element.data,
                                keyAlias = "micro-${glyphId}",
                                nonce = ByteArray(12)
                            )
                        )
                    })
                },
                hasHiddenContent = true,
                resonanceGlow = GlowState.FULL
            )

            val map = _zoomLevels.value.toMutableMap()
            map[glyphId] = updated
            _zoomLevels.value = map
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun getContent(glyphId: String): List<EmbeddedContent> {
        return _zoomLevels.value[glyphId]?.content ?: emptyList()
    }

    fun hasContent(glyphId: String): Boolean {
        return _zoomLevels.value[glyphId]?.hasHiddenContent == true
    }

    fun getGlowState(glyphId: String): GlowState {
        return _zoomLevels.value[glyphId]?.resonanceGlow ?: GlowState.NONE
    }

    fun removeContent(glyphId: String, contentIndex: Int): Boolean {
        try {
            val content = _zoomLevels.value[glyphId] ?: return false
            val updated = content.copy(
                content = content.content.filterIndexed { i, _ -> i != contentIndex }
            )
            val map = _zoomLevels.value.toMutableMap()
            map[glyphId] = updated
            _zoomLevels.value = map
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun resetZoom(glyphId: String) {
        val content = _zoomLevels.value[glyphId] ?: return
        val updated = content.copy(zoomLevel = 1.0)
        val map = _zoomLevels.value.toMutableMap()
        map[glyphId] = updated
        _zoomLevels.value = map
    }

    data class Viewport(
        val glyphId: String,
        val x: Double,
        val y: Double,
        val timestamp: Long
    )
}
