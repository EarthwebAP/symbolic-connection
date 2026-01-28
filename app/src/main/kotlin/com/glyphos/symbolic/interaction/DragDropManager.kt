package com.glyphos.symbolic.interaction

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Drag-Drop Manager
 * Enables dragging artifacts (media, files, glyphs, thoughts) between spaces
 */
@Singleton
class DragDropManager @Inject constructor() {

    private val _activeDragItem = MutableStateFlow<DragItem?>(null)
    val activeDragItem: StateFlow<DragItem?> = _activeDragItem

    private val _dropTargets = MutableStateFlow<List<DropTarget>>(emptyList())
    val dropTargets: StateFlow<List<DropTarget>> = _dropTargets

    private val _dragHistory = MutableStateFlow<List<DragDropEvent>>(emptyList())
    val dragHistory: StateFlow<List<DragDropEvent>> = _dragHistory

    enum class ArtifactType {
        IMAGE,
        FILE,
        GLYPH,
        THOUGHT,
        VOICE_NOTE,
        VIDEO,
        TEXT_SNIPPET,
        PRESENCE_SNAPSHOT
    }

    enum class DropZoneType {
        CHAT_THREAD,
        BATCAVE_CANVAS,
        SECURE_ROOM,
        GLYPH_FIELD,
        PRESENCE_FIELD,
        RADIAL_MENU_SLOT,
        CONVERSATION_THREAD
    }

    data class DragItem(
        val id: String,
        val type: ArtifactType,
        val sourceId: String,
        val sourceZone: DropZoneType,
        val metadata: Map<String, String> = emptyMap(),
        val timestamp: Long = System.currentTimeMillis()
    )

    data class DropTarget(
        val id: String,
        val zoneType: DropZoneType,
        val accepted: List<ArtifactType>,
        val position: Pair<Float, Float> // x, y coordinates
    )

    data class DragDropEvent(
        val dragItemId: String,
        val sourceZone: DropZoneType,
        val targetZone: DropZoneType,
        val success: Boolean,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun startDrag(
        id: String,
        type: ArtifactType,
        sourceZone: DropZoneType,
        metadata: Map<String, String> = emptyMap()
    ) {
        _activeDragItem.value = DragItem(
            id = id,
            type = type,
            sourceId = sourceZone.name,
            sourceZone = sourceZone,
            metadata = metadata
        )
    }

    fun registerDropTarget(
        id: String,
        zoneType: DropZoneType,
        acceptedTypes: List<ArtifactType>,
        position: Pair<Float, Float>
    ) {
        val target = DropTarget(
            id = id,
            zoneType = zoneType,
            accepted = acceptedTypes,
            position = position
        )

        val currentTargets = _dropTargets.value.toMutableList()
        val existingIndex = currentTargets.indexOfFirst { it.id == id }

        if (existingIndex >= 0) {
            currentTargets[existingIndex] = target
        } else {
            currentTargets.add(target)
        }

        _dropTargets.value = currentTargets
    }

    fun unregisterDropTarget(id: String) {
        val currentTargets = _dropTargets.value.toMutableList()
        currentTargets.removeAll { it.id == id }
        _dropTargets.value = currentTargets
    }

    fun drop(targetId: String): Boolean {
        val dragItem = _activeDragItem.value ?: return false
        val target = _dropTargets.value.find { it.id == targetId } ?: return false

        val success = dragItem.type in target.accepted

        if (success) {
            recordEvent(
                DragDropEvent(
                    dragItemId = dragItem.id,
                    sourceZone = dragItem.sourceZone,
                    targetZone = target.zoneType,
                    success = true
                )
            )
        } else {
            recordEvent(
                DragDropEvent(
                    dragItemId = dragItem.id,
                    sourceZone = dragItem.sourceZone,
                    targetZone = target.zoneType,
                    success = false
                )
            )
        }

        _activeDragItem.value = null
        return success
    }

    fun cancelDrag() {
        _activeDragItem.value = null
    }

    fun getDragTargetCompatibility(itemType: ArtifactType): List<DropZoneType> {
        return _dropTargets.value
            .filter { itemType in it.accepted }
            .map { it.zoneType }
            .distinct()
    }

    private fun recordEvent(event: DragDropEvent) {
        val currentHistory = _dragHistory.value.toMutableList()
        currentHistory.add(event)

        // Keep last 200 events
        if (currentHistory.size > 200) {
            currentHistory.removeAt(0)
        }

        _dragHistory.value = currentHistory
    }

    fun getDropZoneInfo(zoneType: DropZoneType): DropZoneInfo {
        return when (zoneType) {
            DropZoneType.CHAT_THREAD -> DropZoneInfo(
                displayName = "Chat Thread",
                acceptedTypes = listOf(
                    ArtifactType.IMAGE,
                    ArtifactType.FILE,
                    ArtifactType.VOICE_NOTE,
                    ArtifactType.VIDEO,
                    ArtifactType.TEXT_SNIPPET
                ),
                color = 0xFF0099FF.toInt()
            )
            DropZoneType.BATCAVE_CANVAS -> DropZoneInfo(
                displayName = "Batcave Canvas",
                acceptedTypes = listOf(
                    ArtifactType.THOUGHT,
                    ArtifactType.GLYPH,
                    ArtifactType.VOICE_NOTE,
                    ArtifactType.IMAGE
                ),
                color = 0xFF1A1A1A.toInt()
            )
            DropZoneType.SECURE_ROOM -> DropZoneInfo(
                displayName = "Secure Room",
                acceptedTypes = listOf(
                    ArtifactType.IMAGE,
                    ArtifactType.FILE,
                    ArtifactType.VIDEO
                ),
                color = 0xFF008B8B.toInt()
            )
            DropZoneType.GLYPH_FIELD -> DropZoneInfo(
                displayName = "Glyph Field",
                acceptedTypes = listOf(
                    ArtifactType.GLYPH,
                    ArtifactType.IMAGE,
                    ArtifactType.THOUGHT
                ),
                color = 0xFF00FFFF.toInt()
            )
            DropZoneType.PRESENCE_FIELD -> DropZoneInfo(
                displayName = "Presence Field",
                acceptedTypes = listOf(
                    ArtifactType.PRESENCE_SNAPSHOT,
                    ArtifactType.GLYPH
                ),
                color = 0xFF00CED1.toInt()
            )
            DropZoneType.RADIAL_MENU_SLOT -> DropZoneInfo(
                displayName = "Menu Slot",
                acceptedTypes = listOf(
                    ArtifactType.GLYPH,
                    ArtifactType.VOICE_NOTE
                ),
                color = 0xFF20B2AA.toInt()
            )
            DropZoneType.CONVERSATION_THREAD -> DropZoneInfo(
                displayName = "Conversation",
                acceptedTypes = listOf(
                    ArtifactType.TEXT_SNIPPET,
                    ArtifactType.VOICE_NOTE,
                    ArtifactType.IMAGE
                ),
                color = 0xFF5F9EA0.toInt()
            )
        }
    }

    data class DropZoneInfo(
        val displayName: String,
        val acceptedTypes: List<ArtifactType>,
        val color: Int
    )
}
