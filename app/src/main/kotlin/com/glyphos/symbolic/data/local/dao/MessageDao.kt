package com.glyphos.symbolic.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.glyphos.symbolic.data.local.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE messageId = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE roomId = :roomId ORDER BY timestamp ASC")
    fun getMessagesByRoom(roomId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE roomId = :roomId AND timestamp > :since ORDER BY timestamp ASC")
    suspend fun getMessagesSince(roomId: String, since: Long): List<MessageEntity>

    @Query("UPDATE messages SET readAt = :readTime WHERE messageId = :messageId")
    suspend fun markMessageAsRead(messageId: String, readTime: Long)

    @Query("DELETE FROM messages WHERE roomId = :roomId")
    suspend fun deleteRoomMessages(roomId: String)

    @Query("SELECT COUNT(*) FROM messages WHERE roomId = :roomId")
    suspend fun getMessageCount(roomId: String): Int
}
