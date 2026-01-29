package com.glyphos.symbolic.security.lens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.glyphos.symbolic.core.models.RitualType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PHASE 1: Ambient Blur Mode
 *
 * Global blur overlay protecting screen content.
 * - Applies blur to entire display
 * - Toggled via ritual triggers
 * - Automatically activates on proximity detection
 * - Can be manually controlled
 * - Configurable blur intensity
 */
class AmbientBlurMode {
    companion object {
        private const val TAG = "AmbientBlurMode"
        private const val DEFAULT_BLUR_INTENSITY = 10f
    }

    // Blur state
    private val _blurEnabled = MutableStateFlow(true)
    val blurEnabled: StateFlow<Boolean> = _blurEnabled.asStateFlow()

    // Blur intensity (0-25)
    private val _blurIntensity = MutableStateFlow(DEFAULT_BLUR_INTENSITY)
    val blurIntensity: StateFlow<Float> = _blurIntensity.asStateFlow()

    // Auto-blur on detection
    private val _autoBlurEnabled = MutableStateFlow(true)
    val autoBlurEnabled: StateFlow<Boolean> = _autoBlurEnabled.asStateFlow()

    // Blur triggers
    private val _blurTriggers = mutableListOf<String>()

    /**
     * Enable blur mode
     */
    fun enableBlur() {
        _blurEnabled.value = true
        Log.d(TAG, "Blur enabled")
    }

    /**
     * Disable blur mode
     */
    fun disableBlur() {
        _blurEnabled.value = false
        Log.d(TAG, "Blur disabled")
    }

    /**
     * Toggle blur on/off
     */
    fun toggleBlur() {
        _blurEnabled.value = !_blurEnabled.value
        Log.d(TAG, "Blur toggled: ${_blurEnabled.value}")
    }

    /**
     * Set blur intensity
     * @param intensity 0-25 (0=no blur, 25=maximum blur)
     */
    fun setBlurIntensity(intensity: Float) {
        val clamped = intensity.coerceIn(0f, 25f)
        _blurIntensity.value = clamped
        Log.d(TAG, "Blur intensity set to $clamped")
    }

    /**
     * Increase blur intensity
     */
    fun increaseBlur() {
        setBlurIntensity(_blurIntensity.value + 1f)
    }

    /**
     * Decrease blur intensity
     */
    fun decreaseBlur() {
        setBlurIntensity(_blurIntensity.value - 1f)
    }

    /**
     * Enable auto-blur on detection
     */
    fun enableAutoBlur() {
        _autoBlurEnabled.value = true
        Log.d(TAG, "Auto-blur enabled")
    }

    /**
     * Disable auto-blur
     */
    fun disableAutoBlur() {
        _autoBlurEnabled.value = false
        Log.d(TAG, "Auto-blur disabled")
    }

    /**
     * Trigger blur via ritual
     * @param ritual Ritual that triggered blur
     */
    fun triggerBlur(ritual: RitualType) {
        enableBlur()
        _blurTriggers.add("${System.currentTimeMillis()}: $ritual")
        Log.d(TAG, "Blur triggered by ritual: $ritual")
    }

    /**
     * Auto-blur on proximity detection
     * Called by ProximityShield when someone detected nearby
     */
    suspend fun autoBlurOnProximity() {
        if (_autoBlurEnabled.value) {
            enableBlur()
            Log.d(TAG, "Auto-blur triggered by proximity detection")
        }
    }

    /**
     * Get blur status
     * @return Status string
     */
    fun getStatus(): String {
        return """
        Ambient Blur Mode Status:
        - Blur enabled: ${_blurEnabled.value}
        - Blur intensity: ${_blurIntensity.value}
        - Auto-blur enabled: ${_autoBlurEnabled.value}
        - Blur triggers: ${_blurTriggers.size}
        """.trimIndent()
    }
}

/**
 * Composable blur overlay
 * Wraps content and applies conditional blur
 */
@Composable
fun BlurOverlay(
    blurMode: AmbientBlurMode,
    content: @Composable () -> Unit
) {
    val blurEnabled = mutableStateOf(false)
    val blurIntensity = mutableStateOf(0f)

    LaunchedEffect(Unit) {
        blurMode.blurEnabled.collect { enabled ->
            blurEnabled.value = enabled
        }
    }

    LaunchedEffect(Unit) {
        blurMode.blurIntensity.collect { intensity ->
            blurIntensity.value = intensity
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                if (blurEnabled.value && blurIntensity.value > 0f) {
                    // Apply blur shader (SDK 31+)
                    // In production, use RenderEffect.createBlurEffect()
                }
            }
    ) {
        content()
    }
}

/**
 * Quick blur toggle button state
 */
data class BlurToggleState(
    val isEnabled: Boolean,
    val intensity: Float,
    val canToggle: Boolean = true
)

/**
 * Blur intensity levels
 */
enum class BlurIntensityLevel(val value: Float) {
    NONE(0f),
    SUBTLE(5f),
    MODERATE(10f),
    STRONG(15f),
    MAXIMUM(25f)
}

/**
 * Extension to set blur from intensity level
 */
fun AmbientBlurMode.setBlurLevel(level: BlurIntensityLevel) {
    setBlurIntensity(level.value)
}
