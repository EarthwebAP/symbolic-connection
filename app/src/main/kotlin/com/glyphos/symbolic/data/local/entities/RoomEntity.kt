package com.glyphos.symbolic.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.glyphos.symbolic.core.models.RoomType

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey
    val roomId: String,
    val name: String,
    val type: String,  // RoomType.name
    val ownerId: String,
    val participantCount: Int,
    val createdAt: Long,
    val lastMessageAt: Long? = null
)
