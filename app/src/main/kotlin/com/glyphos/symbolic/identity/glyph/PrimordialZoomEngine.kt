package com.glyphos.symbolic.identity.glyph

import com.glyphos.symbolic.core.contracts.CanvasElement
import com.glyphos.symbolic.core.contracts.ElementType
import com.glyphos.symbolic.core.contracts.EmbeddedContent
import com.glyphos.symbolic.core.contracts.GlyphMicroContent
import com.glyphos.symbolic.core.contracts.GlowState
import com.glyphos.symbolic.core.contracts.UserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Primordial Zoom Engine
 * Wave-based, field-based zoom using harmonic frequencies
 * No pixelation - infinite mathematical resolution
 * Uses GlyphOS primordial coordinate system
 */
@Singleton
class PrimordialZoomEngine @Inject constructor() {

    private val _glyphFields = MutableStateFlow<Map<String, HarmonicField>>(emptyMap())
    val glyphFields: StateFlow<Map<String, HarmonicField>> = _glyphFields

    private val _zoomFrequency = MutableStateFlow<Map<String, Double>>(emptyMap())
    val zoomFrequency: StateFlow<Map<String, Double>> = _zoomFrequency

    private val _currentViewport = MutableStateFlow<Viewport?>(null)
    val currentViewport: StateFlow<Viewport?> = _currentViewport

    fun initializeGlyphField(glyphId: String, userId: UserId, baseFrequency: Double = 440.0): HarmonicField {
        val field = HarmonicField(
            glyphId = glyphId,
            baseFrequency = baseFrequency,
            harmonics = generateHarmonicStack(baseFrequency),
            zoomLevel = 1.0,
            content = emptyMap(),
            resonanceState = GlowState.NONE
        )

        val map = _glyphFields.value.toMutableMap()
        map[glyphId] = field
        _glyphFields.value = map

        val freq = _zoomFrequency.value.toMutableMap()
        freq[glyphId] = baseFrequency
        _zoomFrequency.value = freq

        return field
    }

    fun zoomToFrequency(glyphId: String, targetFrequency: Double, maxFrequency: Double = 20000.0): Double {
        val field = _glyphFields.value[glyphId] ?: return 1.0
        val clampedFrequency = targetFrequency.coerceIn(field.baseFrequency, maxFrequency)

        // Calculate zoom ratio as harmonic relationship
        val zoomRatio = clampedFrequency / field.baseFrequency

        val updated = field.copy(
            zoomLevel = zoomRatio,
            harmonics = generateHarmonicStack(clampedFrequency)
        )

        val map = _glyphFields.value.toMutableMap()
        map[glyphId] = updated
        _glyphFields.value = map

        val freq = _zoomFrequency.value.toMutableMap()
        freq[glyphId] = clampedFrequency
        _zoomFrequency.value = freq

        return zoomRatio
    }

    fun attachContentToField(
        glyphId: String,
        element: CanvasElement,
        fieldPosition: FieldPosition = FieldPosition(0.0, 0.0, 0.0)
    ): Boolean {
        try {
            val field = _glyphFields.value[glyphId] ?: return false

            val embeddedContent = when (element.type) {
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
                    mimeType = "glyph/primordial"
                )
                ElementType.MICROTHREAD -> EmbeddedContent.MicroThread(
                    messages = emptyList(),
                    title = "Micro-thread at ${fieldPosition.x},${fieldPosition.y}"
                )
                ElementType.RESONANCE_PULSE -> EmbeddedContent.SymbolicPulse(
                    pulseId = element.elementId,
                    resonance = com.glyphos.symbolic.core.contracts.ResonanceType.EMOTIONAL_PRESENCE
                )
            }

            val updated = field.copy(
                content = field.content.toMutableMap().apply {
                    put(element.elementId, FieldContent(
                        content = embeddedContent,
                        position = fieldPosition,
                        frequency = calculateFieldFrequency(fieldPosition, field),
                        timestamp = System.currentTimeMillis()
                    ))
                },
                resonanceState = GlowState.FULL
            )

            val map = _glyphFields.value.toMutableMap()
            map[glyphId] = updated
            _glyphFields.value = map

            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun navigateField(glyphId: String, x: Double, y: Double, z: Double) {
        _currentViewport.value = Viewport(
            glyphId = glyphId,
            x = x,
            y = y,
            z = z,
            timestamp = System.currentTimeMillis()
        )
    }

    fun getContentAtPosition(glyphId: String, x: Double, y: Double): List<FieldContent> {
        val field = _glyphFields.value[glyphId] ?: return emptyList()

        return field.content.values.filter { content ->
            val distance = sqrt(
                (content.position.x - x).pow(2) +
                (content.position.y - y).pow(2)
            )
            distance < 1.0  // Within harmonic reach
        }
    }

    fun getResonanceAtPosition(glyphId: String, x: Double, y: Double): Double {
        val field = _glyphFields.value[glyphId] ?: return 0.0

        var totalResonance = 0.0
        field.content.values.forEach { content ->
            val distance = sqrt(
                (content.position.x - x).pow(2) +
                (content.position.y - y).pow(2)
            )
            if (distance < 2.0) {
                totalResonance += (1.0 - (distance / 2.0)) * (content.frequency / 1000.0)
            }
        }

        return totalResonance.coerceIn(0.0, 1.0)
    }

    fun getAllContent(glyphId: String): Map<String, FieldContent> {
        return _glyphFields.value[glyphId]?.content ?: emptyMap()
    }

    fun getGlyphFieldStats(glyphId: String): FieldStats? {
        val field = _glyphFields.value[glyphId] ?: return null
        return FieldStats(
            glyphId = glyphId,
            baseFrequency = field.baseFrequency,
            currentZoomFrequency = _zoomFrequency.value[glyphId] ?: field.baseFrequency,
            zoomLevel = field.zoomLevel,
            contentCount = field.content.size,
            harmonicCount = field.harmonics.size,
            resonanceState = field.resonanceState
        )
    }

    private fun generateHarmonicStack(baseFrequency: Double, harmonicCount: Int = 8): List<Double> {
        return (1..harmonicCount).map { harmonic ->
            baseFrequency * harmonic
        }
    }

    private fun calculateFieldFrequency(position: FieldPosition, field: HarmonicField): Double {
        // Calculate frequency based on position in field
        val distance = sqrt(position.x.pow(2) + position.y.pow(2) + position.z.pow(2))
        val angle = kotlin.math.atan2(position.y, position.x)

        // Harmonic modulation based on position
        val modulation = 1.0 + (0.5 * sin(angle)) + (0.3 * cos(distance))
        return field.baseFrequency * modulation
    }

    data class HarmonicField(
        val glyphId: String,
        val baseFrequency: Double,
        val harmonics: List<Double>,
        val zoomLevel: Double,
        val content: Map<String, FieldContent>,
        val resonanceState: GlowState
    )

    data class FieldContent(
        val content: EmbeddedContent,
        val position: FieldPosition,
        val frequency: Double,
        val timestamp: Long
    )

    data class FieldPosition(
        val x: Double,
        val y: Double,
        val z: Double
    )

    data class Viewport(
        val glyphId: String,
        val x: Double,
        val y: Double,
        val z: Double,
        val timestamp: Long
    )

    data class FieldStats(
        val glyphId: String,
        val baseFrequency: Double,
        val currentZoomFrequency: Double,
        val zoomLevel: Double,
        val contentCount: Int,
        val harmonicCount: Int,
        val resonanceState: GlowState
    )
}
