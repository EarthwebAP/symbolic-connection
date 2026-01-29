package com.glyphos.symbolic.spaces

import com.glyphos.symbolic.core.contracts.Batcave
import com.glyphos.symbolic.core.contracts.BandwidthLevel
import com.glyphos.symbolic.core.contracts.CognitiveMode
import com.glyphos.symbolic.core.contracts.EmotionalTone
import com.glyphos.symbolic.core.contracts.InfiniteCanvas
import com.glyphos.symbolic.core.contracts.IntentVector
import com.glyphos.symbolic.core.contracts.PresenceState
import com.glyphos.symbolic.core.contracts.Room
import com.glyphos.symbolic.core.contracts.RoomType
import com.glyphos.symbolic.core.contracts.SecurityProfile
import com.glyphos.symbolic.core.contracts.SocialContext
import com.glyphos.symbolic.core.contracts.UserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Batcave Room Manager
 * Private cognitive sanctuary with no interruptions
 */
@Singleton
class BatcaveRoomManager @Inject constructor() {

    private val _batcaves = MutableStateFlow<Map<String, Batcave>>(emptyMap())
    val batcaves: StateFlow<Map<String, Batcave>> = _batcaves

    private val _activeBatcave = MutableStateFlow<Batcave?>(null)
    val activeBatcave: StateFlow<Batcave?> = _activeBatcave

    private val _sealedMode = MutableStateFlow(false)
    val sealedMode: StateFlow<Boolean> = _sealedMode

    private val _notificationsBlocked = MutableStateFlow(false)
    val notificationsBlocked: StateFlow<Boolean> = _notificationsBlocked

    fun createBatcave(userId: UserId): Batcave {
        val room = Room(
            roomId = "batcave-${userId.value}",
            name = "Batcave",
            type = RoomType.BATCAVE,
            ownerId = userId,
            participants = listOf(userId),
            securityProfile = SecurityProfile(
                notificationsEnabled = false,
                loggingEnabled = false,
                exportAllowed = false,
                aiAccessible = true,
                screenshotAllowed = false,
                copyAllowed = false
            )
        )

        val canvas = InfiniteCanvas(
            canvasId = "canvas-${userId.value}",
            zoom = 1.0,
            maxZoom = 30000.0,
            elements = emptyMap(),
            viewportX = 0.0,
            viewportY = 0.0
        )

        val batcave = Batcave(
            room = room,
            aiCompanions = emptyList(),
            customTools = emptyList(),
            sealedMode = true,
            infiniteCanvas = canvas
        )

        val map = _batcaves.value.toMutableMap()
        map[room.roomId] = batcave
        _batcaves.value = map

        return batcave
    }

    fun enterBatcave(userId: UserId) {
        val batcaves = _batcaves.value
        val batcave = batcaves.values.find { it.room.ownerId == userId }

        if (batcave != null) {
            _activeBatcave.value = batcave
            _sealedMode.value = true
            _notificationsBlocked.value = true
        }
    }

    fun exitBatcave() {
        _activeBatcave.value = null
        _sealedMode.value = false
        _notificationsBlocked.value = false
    }

    fun inviteAICompanion(batcaveId: String, aiId: String): Boolean {
        try {
            val batcave = _batcaves.value[batcaveId] ?: return false
            val updated = batcave.copy(
                aiCompanions = batcave.aiCompanions.toMutableList().apply { add(aiId) }
            )
            val map = _batcaves.value.toMutableMap()
            map[batcaveId] = updated
            _batcaves.value = map

            if (_activeBatcave.value?.room?.roomId == batcaveId) {
                _activeBatcave.value = updated
            }

            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun removeAICompanion(batcaveId: String, aiId: String): Boolean {
        try {
            val batcave = _batcaves.value[batcaveId] ?: return false
            val updated = batcave.copy(
                aiCompanions = batcave.aiCompanions.filter { it != aiId }
            )
            val map = _batcaves.value.toMutableMap()
            map[batcaveId] = updated
            _batcaves.value = map

            if (_activeBatcave.value?.room?.roomId == batcaveId) {
                _activeBatcave.value = updated
            }

            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun isInSealedMode(): Boolean {
        return _sealedMode.value
    }

    fun blockAllNotifications() {
        _notificationsBlocked.value = true
    }

    fun allowNotifications() {
        _notificationsBlocked.value = false
    }

    fun recordThought(batcaveId: String, thought: String, encrypted: Boolean = true): Boolean {
        // TODO: Store encrypted thought in canvas
        return true
    }

    fun getInfiniteCanvas(batcaveId: String): InfiniteCanvas? {
        return _batcaves.value[batcaveId]?.infiniteCanvas
    }

    fun canAccessBatcave(userId: UserId, batcaveId: String): Boolean {
        val batcave = _batcaves.value[batcaveId] ?: return false
        return batcave.room.ownerId == userId
    }

    fun getBatcaveStats(batcaveId: String): BatcaveStats? {
        val batcave = _batcaves.value[batcaveId] ?: return null
        return BatcaveStats(
            batcaveId = batcaveId,
            aiCompanionCount = batcave.aiCompanions.size,
            customToolCount = batcave.customTools.size,
            canvasElements = batcave.infiniteCanvas?.elements?.size ?: 0,
            sealedMode = batcave.sealedMode,
            createdAt = batcave.room.createdAt
        )
    }

    data class BatcaveStats(
        val batcaveId: String,
        val aiCompanionCount: Int,
        val customToolCount: Int,
        val canvasElements: Int,
        val sealedMode: Boolean,
        val createdAt: Long
    )
}
