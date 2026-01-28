package com.glyphos.symbolic.interaction

import com.glyphos.symbolic.core.contracts.ActionCategory
import com.glyphos.symbolic.core.contracts.RadialMenuAction
import com.glyphos.symbolic.core.contracts.UserId
import com.glyphos.symbolic.presence.PresenceEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 6-Slot Radial Menu System
 * Press anywhere → menu opens with context-adaptive actions
 */
@Singleton
class RadialMenuSystem @Inject constructor(
    private val presenceEngine: PresenceEngine
) {

    private val _isMenuOpen = MutableStateFlow(false)
    val isMenuOpen: StateFlow<Boolean> = _isMenuOpen

    private val _currentActions = MutableStateFlow<List<RadialMenuAction>>(emptyList())
    val currentActions: StateFlow<List<RadialMenuAction>> = _currentActions

    private val _selectedAction = MutableStateFlow<RadialMenuAction?>(null)
    val selectedAction: StateFlow<RadialMenuAction?> = _selectedAction

    private val allActions = mapOf(
        "shift_focus" to RadialMenuAction(
            actionId = "shift_focus",
            label = "Deep Focus",
            icon = "ic_focus",
            category = ActionCategory.PRESENCE
        ),
        "shift_expansive" to RadialMenuAction(
            actionId = "shift_expansive",
            label = "Expansive",
            icon = "ic_expand",
            category = ActionCategory.PRESENCE
        ),
        "shift_reflective" to RadialMenuAction(
            actionId = "shift_reflective",
            label = "Reflective",
            icon = "ic_reflect",
            category = ActionCategory.PRESENCE
        ),
        "drop_glyph" to RadialMenuAction(
            actionId = "drop_glyph",
            label = "Drop Glyph",
            icon = "ic_glyph",
            category = ActionCategory.SYMBOLIC
        ),
        "send_signal" to RadialMenuAction(
            actionId = "send_signal",
            label = "Signal",
            icon = "ic_signal",
            category = ActionCategory.COMMUNICATION
        ),
        "open_batcave" to RadialMenuAction(
            actionId = "open_batcave",
            label = "Batcave",
            icon = "ic_batcave",
            category = ActionCategory.SPATIAL
        ),
        "new_room" to RadialMenuAction(
            actionId = "new_room",
            label = "New Room",
            icon = "ic_room",
            category = ActionCategory.SPATIAL
        ),
        "ceremonial_request" to RadialMenuAction(
            actionId = "ceremonial_request",
            label = "Ceremonial",
            icon = "ic_ritual",
            category = ActionCategory.RITUAL
        ),
        "start_recording" to RadialMenuAction(
            actionId = "start_recording",
            label = "Record",
            icon = "ic_record",
            category = ActionCategory.HARDWARE
        ),
        "document_scan" to RadialMenuAction(
            actionId = "document_scan",
            label = "Scan",
            icon = "ic_scan",
            category = ActionCategory.HARDWARE
        ),
        "voice_command" to RadialMenuAction(
            actionId = "voice_command",
            label = "Voice",
            icon = "ic_voice",
            category = ActionCategory.HARDWARE
        ),
        "toggle_blur" to RadialMenuAction(
            actionId = "toggle_blur",
            label = "Blur",
            icon = "ic_blur",
            category = ActionCategory.UTILITY
        ),
        "app_disguise" to RadialMenuAction(
            actionId = "app_disguise",
            label = "Disguise",
            icon = "ic_disguise",
            category = ActionCategory.UTILITY
        ),
        "emergency_seal" to RadialMenuAction(
            actionId = "emergency_seal",
            label = "Seal",
            icon = "ic_seal",
            category = ActionCategory.UTILITY
        ),
        "presence_map" to RadialMenuAction(
            actionId = "presence_map",
            label = "Presence",
            icon = "ic_presence",
            category = ActionCategory.PRESENCE
        ),
        "drag_drop" to RadialMenuAction(
            actionId = "drag_drop",
            label = "Drag",
            icon = "ic_drag",
            category = ActionCategory.COMMUNICATION
        ),
        "infinite_zoom" to RadialMenuAction(
            actionId = "infinite_zoom",
            label = "Zoom",
            icon = "ic_zoom",
            category = ActionCategory.SYMBOLIC
        ),
        "secure_room" to RadialMenuAction(
            actionId = "secure_room",
            label = "Secure",
            icon = "ic_secure",
            category = ActionCategory.SPATIAL
        ),
        "presence_sync" to RadialMenuAction(
            actionId = "presence_sync",
            label = "Sync",
            icon = "ic_sync",
            category = ActionCategory.PRESENCE
        ),
        "emoji_pulse" to RadialMenuAction(
            actionId = "emoji_pulse",
            label = "Pulse",
            icon = "ic_pulse",
            category = ActionCategory.COMMUNICATION
        )
    )

    fun openMenu(userId: UserId) {
        val contextualActions = selectContextualActions(userId)
        _currentActions.value = contextualActions
        _isMenuOpen.value = true
    }

    fun closeMenu() {
        _isMenuOpen.value = false
        _selectedAction.value = null
    }

    fun selectAction(actionId: String) {
        val action = allActions[actionId]
        if (action != null) {
            _selectedAction.value = action
        }
    }

    fun executeAction(actionId: String): Boolean {
        return try {
            val action = allActions[actionId] ?: return false
            // Action execution happens in UI layer
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun selectContextualActions(userId: UserId): List<RadialMenuAction> {
        val presence = presenceEngine.getPresenceState(userId)
        val defaultSlots = 6

        // Adaptive selection based on context
        return if (presence != null) {
            when {
                // Deep focus mode - minimal interruption
                presence.cognitive.name.contains("DEEP") -> listOf(
                    allActions["emergency_seal"] ?: return emptyList(),
                    allActions["toggle_blur"] ?: return emptyList(),
                    allActions["shift_expansive"] ?: return emptyList(),
                    allActions["app_disguise"] ?: return emptyList(),
                    allActions["send_signal"] ?: return emptyList(),
                    allActions["voice_command"] ?: return emptyList()
                )

                // Expansive/collaborative mode
                presence.cognitive.name.contains("EXPANSIVE") -> listOf(
                    allActions["drop_glyph"] ?: return emptyList(),
                    allActions["new_room"] ?: return emptyList(),
                    allActions["send_signal"] ?: return emptyList(),
                    allActions["presence_map"] ?: return emptyList(),
                    allActions["drag_drop"] ?: return emptyList(),
                    allActions["emoji_pulse"] ?: return emptyList()
                )

                // Reflective mode
                presence.cognitive.name.contains("REFLECTIVE") -> listOf(
                    allActions["open_batcave"] ?: return emptyList(),
                    allActions["infinite_zoom"] ?: return emptyList(),
                    allActions["document_scan"] ?: return emptyList(),
                    allActions["start_recording"] ?: return emptyList(),
                    allActions["presence_sync"] ?: return emptyList(),
                    allActions["shift_focus"] ?: return emptyList()
                )

                else -> getDefaultActions(defaultSlots)
            }
        } else {
            getDefaultActions(defaultSlots)
        }
    }

    private fun getDefaultActions(count: Int): List<RadialMenuAction> {
        return allActions.values.take(count).toList()
    }

    fun getActionsByCategory(category: ActionCategory): List<RadialMenuAction> {
        return allActions.values.filter { it.category == category }
    }

    fun search(query: String): List<RadialMenuAction> {
        return allActions.values.filter {
            it.label.contains(query, ignoreCase = true) ||
            it.actionId.contains(query, ignoreCase = true)
        }
    }
}
