package com.glyphos.symbolic.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.glyphos.symbolic.data.local.entities.RoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity)

    @Delete
    suspend fun deleteRoom(room: RoomEntity)

    @Query("SELECT * FROM rooms WHERE roomId = :roomId")
    suspend fun getRoomById(roomId: String): RoomEntity?

    @Query("SELECT * FROM rooms WHERE ownerId = :ownerId ORDER BY lastMessageAt DESC")
    fun getRoomsByOwner(ownerId: String): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms ORDER BY lastMessageAt DESC")
    fun getAllRooms(): Flow<List<RoomEntity>>

    @Query("UPDATE rooms SET lastMessageAt = :timestamp WHERE roomId = :roomId")
    suspend fun updateLastMessageTime(roomId: String, timestamp: Long)

    @Query("DELETE FROM rooms WHERE roomId = :roomId")
    suspend fun deleteRoomById(roomId: String)
}
