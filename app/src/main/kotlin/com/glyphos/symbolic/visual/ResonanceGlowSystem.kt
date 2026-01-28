package com.glyphos.symbolic.visual

import android.util.Log
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sin

/**
 * PHASE 6: Resonance Glow System
 *
 * Visual glow effects for glyphs based on resonance state.
 * - Pulsing glow effects
 * - Resonance color mapping
 * - Energy wave visualization
 * - Dynamic intensity control
 */
class ResonanceGlowSystem {
    companion object {
        private const val TAG = "ResonanceGlowSystem"

        // Glow animation durations
        const val PULSE_DURATION_MS = 1500
        const val SHIMMER_DURATION_MS = 2000
        const val WAVE_DURATION_MS = 3000
    }

    enum class GlowPattern {
        NONE,
        SUBTLE_PULSE,    // Gentle pulsing at 0.3-0.6 alpha
        STEADY_GLOW,     // Constant glow at 0.5-0.8 alpha
        RAPID_PULSE,     // Fast pulsing at 0.4-1.0 alpha (active/urgent)
        SHIMMER,         // Shimmering effect like stars
        WAVE_RIPPLE      // Outward ripple waves
    }

    enum class ResonanceType {
        NONE,
        CURIOSITY,       // Light cyan
        URGENCY,         // Bright red/orange
        FAVOR,           // Warm yellow/gold
        EMOTIONAL,       // Purple/magenta
        PRESENCE,        // Blue (app default)
        RESONANCE_SPIKE  // White flash
    }

    data class GlowState(
        val pattern: GlowPattern = GlowPattern.NONE,
        val resonanceType: ResonanceType = ResonanceType.NONE,
        val intensity: Float = 0f,
        val color: Color = Color.Transparent,
        val duration: Long = 0L
    )

    private val _currentGlow = MutableStateFlow(GlowState())
    val currentGlow: StateFlow<GlowState> = _currentGlow.asStateFlow()

    private val _resonanceEnergy = MutableStateFlow(0f)
    val resonanceEnergy: StateFlow<Float> = _resonanceEnergy.asStateFlow()

    fun setGlowPattern(pattern: GlowPattern, resonance: ResonanceType = ResonanceType.PRESENCE) {
        val color = getResonanceColor(resonance)
        val intensity = getIntensityForPattern(pattern)

        _currentGlow.value = GlowState(
            pattern = pattern,
            resonanceType = resonance,
            intensity = intensity,
            color = color,
            duration = getDurationForPattern(pattern)
        )

        Log.d(TAG, "Glow pattern set: ${pattern.name} (${resonance.name}, intensity: $intensity)")
    }

    fun pulseResonance(energy: Float, type: ResonanceType = ResonanceType.PRESENCE) {
        _resonanceEnergy.value = energy

        val pattern = when {
            energy > 0.8f -> GlowPattern.RAPID_PULSE
            energy > 0.5f -> GlowPattern.STEADY_GLOW
            energy > 0.2f -> GlowPattern.SUBTLE_PULSE
            else -> GlowPattern.NONE
        }

        setGlowPattern(pattern, type)
    }

    fun triggerResonanceSpike(type: ResonanceType = ResonanceType.RESONANCE_SPIKE) {
        setGlowPattern(GlowPattern.RAPID_PULSE, type)
        Log.d(TAG, "Resonance spike triggered: ${type.name}")
    }

    fun startWaveRipple(energy: Float = 0.8f) {
        setGlowPattern(GlowPattern.WAVE_RIPPLE)
        _resonanceEnergy.value = energy
        Log.d(TAG, "Wave ripple started: energy=$energy")
    }

    fun clearGlow() {
        _currentGlow.value = GlowState()
        _resonanceEnergy.value = 0f
        Log.d(TAG, "Glow cleared")
    }

    private fun getResonanceColor(type: ResonanceType): Color {
        return when (type) {
            ResonanceType.NONE -> Color.Transparent
            ResonanceType.CURIOSITY -> Color(0xFF00DDFF)      // Cyan
            ResonanceType.URGENCY -> Color(0xFFFF4400)        // Red-orange
            ResonanceType.FAVOR -> Color(0xFFFFAA00)          // Gold
            ResonanceType.EMOTIONAL -> Color(0xFFDD00FF)      // Magenta
            ResonanceType.PRESENCE -> Color(0xFF0066FF)       // App blue
            ResonanceType.RESONANCE_SPIKE -> Color(0xFFFFFFFF) // White
        }
    }

    private fun getIntensityForPattern(pattern: GlowPattern): Float {
        return when (pattern) {
            GlowPattern.NONE -> 0f
            GlowPattern.SUBTLE_PULSE -> 0.45f
            GlowPattern.STEADY_GLOW -> 0.65f
            GlowPattern.RAPID_PULSE -> 0.7f
            GlowPattern.SHIMMER -> 0.55f
            GlowPattern.WAVE_RIPPLE -> 0.8f
        }
    }

    private fun getDurationForPattern(pattern: GlowPattern): Long {
        return when (pattern) {
            GlowPattern.SUBTLE_PULSE -> PULSE_DURATION_MS.toLong()
            GlowPattern.RAPID_PULSE -> (PULSE_DURATION_MS / 2).toLong()
            GlowPattern.SHIMMER -> SHIMMER_DURATION_MS.toLong()
            GlowPattern.WAVE_RIPPLE -> WAVE_DURATION_MS.toLong()
            else -> 0L
        }
    }

    fun getStatus(): String {
        val glow = _currentGlow.value
        return """
        Resonance Glow System Status:
        - Pattern: ${glow.pattern.name}
        - Type: ${glow.resonanceType.name}
        - Intensity: ${String.format("%.2f", glow.intensity)}
        - Energy: ${String.format("%.2f", _resonanceEnergy.value)}
        - Color: RGB(${glow.color.red * 255}, ${glow.color.green * 255}, ${glow.color.blue * 255})
        """.trimIndent()
    }
}

/**
 * Composable for rendering glyph with resonance glow
 */
@Composable
fun GlyphWithResonanceGlow(
    glowSystem: ResonanceGlowSystem,
    modifier: Modifier = Modifier,
    size: Float = 64f,
    content: @Composable () -> Unit
) {
    val glow = glowSystem.currentGlow.value
    val infiniteTransition = rememberInfiniteTransition(label = "resonanceGlow")

    val alpha by when (glow.pattern) {
        ResonanceGlowSystem.GlowPattern.SUBTLE_PULSE -> {
            infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.6f,
                animationSpec = InfiniteRepeatableSpec(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "subtlePulse"
            )
        }
        ResonanceGlowSystem.GlowPattern.RAPID_PULSE -> {
            infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1.0f,
                animationSpec = InfiniteRepeatableSpec(
                    animation = tween(750, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "rapidPulse"
            )
        }
        ResonanceGlowSystem.GlowPattern.STEADY_GLOW -> {
            mutableStateOf(0.65f)
        }
        else -> mutableStateOf(0f)
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .alpha(1f - alpha * 0.3f),
        contentAlignment = Alignment.Center
    ) {
        // Background glow
        if (alpha > 0f) {
            Canvas(modifier = Modifier.size(size.dp)) {
                drawCircle(
                    color = glow.color.copy(alpha = alpha * 0.5f),
                    radius = size.dp.toPx() * 0.6f
                )
            }
        }

        // Content
        content()
    }
}

/**
 * Composable for wave ripple effect
 */
@Composable
fun WaveRippleGlow(
    glowSystem: ResonanceGlowSystem,
    modifier: Modifier = Modifier,
    centerSize: Float = 64f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveRipple")

    val waveRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(ResonanceGlowSystem.WAVE_DURATION_MS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveRadius"
    )

    val glow = glowSystem.currentGlow.value

    Canvas(modifier = modifier.size((centerSize + 400).dp)) {
        // Draw multiple expanding rings
        for (i in 0..2) {
            val radius = waveRadius + (i * 50)
            val alpha = (1f - (radius / (centerSize + 400))).coerceIn(0f, 1f)

            drawCircle(
                color = glow.color.copy(alpha = alpha * 0.3f),
                radius = radius,
                center = Offset(size.width / 2, size.height / 2)
            )
        }
    }
}
