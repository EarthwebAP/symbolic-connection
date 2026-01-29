package com.glyphos.symbolic.hardware

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.cancellation.CancellationException

/**
 * PHASE 5: Long-Press Settings Handler
 *
 * Long-press gesture detection for settings shortcuts.
 * - Power button long-press
 * - Volume button combinations
 * - Custom long-press actions
 */
class LongPressSettingsHandler {
    companion object {
        private const val TAG = "LongPressSettingsHandler"
        private const val LONG_PRESS_DURATION_MS = 500L
    }

    private val _registeredActions = MutableStateFlow<List<LongPressAction>>(emptyList())
    val registeredActions: StateFlow<List<LongPressAction>> = _registeredActions.asStateFlow()

    private val _lastLongPress = MutableStateFlow<LongPressAction?>(null)
    val lastLongPress: StateFlow<LongPressAction?> = _lastLongPress.asStateFlow()

    enum class ButtonCombination {
        POWER_ONLY,
        VOLUME_UP_ONLY,
        VOLUME_DOWN_ONLY,
        POWER_VOLUME_UP,
        POWER_VOLUME_DOWN,
        VOLUME_UP_DOWN
    }

    data class LongPressAction(
        val id: String,
        val combination: ButtonCombination,
        val label: String,
        val action: suspend () -> Unit,
        val durationMs: Long = LONG_PRESS_DURATION_MS
    )

    fun registerAction(
        combination: ButtonCombination,
        label: String,
        action: suspend () -> Unit
    ) {
        val longPressAction = LongPressAction(
            id = "${combination}_${System.currentTimeMillis()}",
            combination = combination,
            label = label,
            action = action
        )

        _registeredActions.value = _registeredActions.value + longPressAction
        Log.d(TAG, "Action registered: $label (${combination.name})")
    }

    fun registerEmergencyLockdown() {
        registerAction(
            combination = ButtonCombination.POWER_VOLUME_UP,
            label = "Emergency Lockdown",
            action = { Log.d(TAG, "Emergency lockdown triggered") }
        )
    }

    fun registerAccessibilityMenu() {
        registerAction(
            combination = ButtonCombination.VOLUME_UP_DOWN,
            label = "Accessibility Menu",
            action = { Log.d(TAG, "Accessibility menu opened") }
        )
    }

    suspend fun executeAction(combination: ButtonCombination) {
        val action = _registeredActions.value.firstOrNull { it.combination == combination }
        if (action != null) {
            try {
                action.action.invoke()
                _lastLongPress.value = action
                Log.d(TAG, "Action executed: ${action.label}")
            } catch (e: CancellationException) {
                Log.d(TAG, "Action cancelled: ${action.label}")
            } catch (e: Exception) {
                Log.e(TAG, "Action failed: ${action.label}", e)
            }
        }
    }

    fun getActionForCombination(combination: ButtonCombination): LongPressAction? {
        return _registeredActions.value.firstOrNull { it.combination == combination }
    }

    fun getStatus(): String {
        return """
        Long-Press Settings Status:
        - Registered actions: ${_registeredActions.value.size}
        - Last action: ${_lastLongPress.value?.label ?: "None"}
        - Actions: ${_registeredActions.value.joinToString(", ") { it.label }}
        """.trimIndent()
    }
}

@Composable
fun LongPressDetector(
    handler: LongPressSettingsHandler,
    combination: LongPressSettingsHandler.ButtonCombination,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isLongPressed = mutableStateOf(false)

    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onLongPress = {
                    isLongPressed.value = true
                    // Trigger action
                }
            )
        }
    ) {
        content()
    }
}
