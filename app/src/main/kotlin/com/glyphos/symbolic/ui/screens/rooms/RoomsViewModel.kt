package com.glyphos.symbolic.ui.screens.rooms

import androidx.lifecycle.ViewModel
import com.glyphos.symbolic.core.models.Room
import com.glyphos.symbolic.core.models.RoomType
import com.glyphos.symbolic.core.models.SecurityProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RoomsViewModel @Inject constructor() : ViewModel() {

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    init {
        loadRooms()
    }

    private fun loadRooms() {
        val defaultSecurity = SecurityProfile(
            notificationsEnabled = true,
            loggingEnabled = true,
            exportAllowed = true,
            aiAccessible = true
        )

        val sampleRooms = listOf(
            Room(
                roomId = "room-1",
                name = "General Chat",
                type = RoomType.STANDARD,
                ownerId = "user-1",
                participants = listOf("user-1", "user-2"),
                securityProfile = defaultSecurity,
                createdAt = System.currentTimeMillis()
            ),
            Room(
                roomId = "room-2",
                name = "Deep Focus",
                type = RoomType.BATCAVE,
                ownerId = "user-1",
                participants = listOf("user-1"),
                securityProfile = defaultSecurity,
                createdAt = System.currentTimeMillis() - 86400000
            ),
            Room(
                roomId = "room-3",
                name = "Secure Exchange",
                type = RoomType.SECURE_DIGITAL,
                ownerId = "user-1",
                participants = listOf("user-1", "user-3"),
                securityProfile = SecurityProfile(
                    notificationsEnabled = false,
                    loggingEnabled = false,
                    exportAllowed = false,
                    aiAccessible = false
                ),
                createdAt = System.currentTimeMillis() - 172800000
            )
        )
        _rooms.value = sampleRooms
    }

    fun createRoom(name: String, type: RoomType) {
        val newRoom = Room(
            roomId = "room-${System.currentTimeMillis()}",
            name = name,
            type = type,
            ownerId = "user-1",
            participants = listOf("user-1"),
            securityProfile = SecurityProfile(
                notificationsEnabled = true,
                loggingEnabled = true,
                exportAllowed = true,
                aiAccessible = true
            ),
            createdAt = System.currentTimeMillis()
        )
        _rooms.value = _rooms.value + newRoom
    }
}
