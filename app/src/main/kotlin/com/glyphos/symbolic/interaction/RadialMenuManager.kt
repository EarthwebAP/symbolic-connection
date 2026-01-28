package com.glyphos.symbolic.interaction

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * PHASE 4: Radial Menu Manager
 *
 * 6-slot context-aware action wheel.
 * Tap anywhere on screen to open menu.
 * Drag to select action.
 * Actions adapt based on current context (room, presence, content).
 */
class RadialMenuManager {
    companion object {
        private const val TAG = "RadialMenuManager"
        const val SLOT_COUNT = 6
        const val RADIUS = 100f
    }

    enum class MenuAction(val displayName: String, val icon: String) {
        SHIFT_MODE("Shift Mode", "🔄"),
        EMIT_PULSE("Emit Pulse", "📡"),
        DROP_MARKER("Drop Marker", "🎯"),
        START_MESSAGE("New Message", "💬"),
        OPEN_ZOOM("Infinite Zoom", "🔍"),
        SUMMON_AI("Summon AI", "🤖")
    }

    private val _isOpen = mutableStateOf(false)
    val isOpen = _isOpen

    private val _selectedAction = MutableStateFlow<MenuAction?>(null)
    val selectedAction: StateFlow<MenuAction?> = _selectedAction.asStateFlow()

    private var selectedSlot = -1

    /**
     * Open menu at tap location
     * @param tapLocation X, Y coordinates
     */
    fun openMenu(tapLocation: Offset) {
        _isOpen.value = true
        selectedSlot = -1
        Log.d(TAG, "Menu opened at $tapLocation")
    }

    /**
     * Close menu
     */
    fun closeMenu() {
        _isOpen.value = false
        selectedSlot = -1
    }

    /**
     * Handle slot selection based on angle
     * @param angle Angle from center (radians)
     * @return Selected action
     */
    fun selectSlotFromAngle(angle: Float): MenuAction? {
        // Normalize angle to 0-360
        val normalizedAngle = ((angle * 180 / Math.PI).toFloat() + 360) % 360

        // Divide into 6 slots (60 degrees each)
        val slotSize = 360f / SLOT_COUNT
        selectedSlot = ((normalizedAngle + slotSize / 2) / slotSize).toInt() % SLOT_COUNT

        val actions = MenuAction.values()
        val selected = if (selectedSlot in actions.indices) actions[selectedSlot] else null

        if (selected != null) {
            _selectedAction.value = selected
            Log.d(TAG, "Selected action: ${selected.displayName}")
        }

        return selected
    }

    /**
     * Execute selected action
     * @return true if action executed
     */
    suspend fun executeAction(): Boolean {
        val action = _selectedAction.value ?: return false

        Log.d(TAG, "Executing action: ${action.displayName}")

        when (action) {
            MenuAction.SHIFT_MODE -> handleShiftMode()
            MenuAction.EMIT_PULSE -> handleEmitPulse()
            MenuAction.DROP_MARKER -> handleDropMarker()
            MenuAction.START_MESSAGE -> handleStartMessage()
            MenuAction.OPEN_ZOOM -> handleOpenZoom()
            MenuAction.SUMMON_AI -> handleSummonAI()
        }

        closeMenu()
        return true
    }

    private suspend fun handleShiftMode() {
        Log.d(TAG, "Shifting cognitive mode...")
    }

    private suspend fun handleEmitPulse() {
        Log.d(TAG, "Emitting presence pulse...")
    }

    private suspend fun handleDropMarker() {
        Log.d(TAG, "Dropping glyph marker...")
    }

    private suspend fun handleStartMessage() {
        Log.d(TAG, "Starting symbolic message...")
    }

    private suspend fun handleOpenZoom() {
        Log.d(TAG, "Opening infinite zoom...")
    }

    private suspend fun handleSummonAI() {
        Log.d(TAG, "Summoning AI companion...")
    }

    /**
     * Get menu status
     */
    fun getStatus(): String {
        return """
        Radial Menu Status:
        - Open: ${_isOpen.value}
        - Selected: ${_selectedAction.value?.displayName ?: "None"}
        - Selected slot: $selectedSlot
        """.trimIndent()
    }
}

/**
 * Composable for radial menu visualization
 */
@Composable
fun RadialMenu(
    manager: RadialMenuManager,
    modifier: Modifier = Modifier
) {
    val isOpen = manager.isOpen
    val selectedAction = mutableStateOf<RadialMenuManager.MenuAction?>(null)

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Tap to open menu
                // Drag to select action
            },
        contentAlignment = Alignment.Center
    ) {
        if (isOpen.value) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = size.center

                // Draw 6 slots
                for (i in 0 until 6) {
                    val angle = (i * 360f / 6) * Math.PI / 180f

                    val x = center.x + RadialMenuManager.RADIUS * cos(angle).toFloat()
                    val y = center.y + RadialMenuManager.RADIUS * sin(angle).toFloat()

                    // Draw slot circle
                    drawCircle(
                        color = if (i == selectedAction.value?.ordinal) Color.Cyan else Color.Gray,
                        radius = 30f,
                        center = Offset(x, y)
                    )
                }

                // Draw center circle
                drawCircle(
                    color = Color.Magenta,
                    radius = 20f,
                    center = center
                )
            }
        }
    }
}
