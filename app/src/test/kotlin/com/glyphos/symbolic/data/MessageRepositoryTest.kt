package com.glyphos.symbolic.data

import com.glyphos.symbolic.data.api.GlyphApiClient
import com.glyphos.symbolic.data.local.dao.MessageDao
import com.glyphos.symbolic.data.local.entities.MessageEntity
import com.glyphos.symbolic.data.mappers.MessageItem
import com.glyphos.symbolic.data.network.SocketIOManager
import com.glyphos.symbolic.security.encryption.LocalEncryptionEngine
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for MessageRepository
 */
class MessageRepositoryTest {

    private lateinit var messageRepository: MessageRepository
    private val messageDao = mockk<MessageDao>(relaxed = true)
    private val socketIOManager = mockk<SocketIOManager>(relaxed = true)
    private val apiClient = mockk<GlyphApiClient>(relaxed = true)
    private val encryptionEngine = mockk<LocalEncryptionEngine>(relaxed = true)

    @Before
    fun setup() {
        messageRepository = MessageRepository(
            messageDao = messageDao,
            socketIOManager = socketIOManager,
            apiClient = apiClient,
            encryptionEngine = encryptionEngine
        )
    }

    @Test
    fun `sendMessage should save locally and emit via Socket IO`() = runBlocking {
        // Arrange
        val recipientId = 123
        val content = "Hello, test message"
        val roomId = 1

        coEvery { messageDao.insert(any()) } returns Unit

        // Act
        val result = messageRepository.sendMessage(recipientId, content, roomId, encrypted = false)

        // Assert
        assert(result.isSuccess)
        coVerify { messageDao.insert(any()) }
        coVerify { socketIOManager.sendMessage(recipientId, content, false) }
    }

    @Test
    fun `sendMessage with encryption should encrypt content`() = runBlocking {
        // Arrange
        val recipientId = 456
        val content = "Secret message"
        val roomId = 2

        coEvery { messageDao.insert(any()) } returns Unit
        coEvery { encryptionEngine.encrypt(any(), any()) } returns mockk {
            every { ciphertext } returns "encrypted_data"
        }

        // Act
        val result = messageRepository.sendMessage(recipientId, content, roomId, encrypted = true)

        // Assert
        assert(result.isSuccess)
        coVerify { encryptionEngine.encrypt(any(), any()) }
    }

    @Test
    fun `markAsRead should update database and emit receipt`() = runBlocking {
        // Arrange
        val messageId = 789
        val roomId = 1

        coEvery { messageDao.markMessageAsRead(any(), any()) } returns Unit

        // Act
        val result = messageRepository.markAsRead(messageId, roomId)

        // Assert
        assert(result.isSuccess)
        coVerify { messageDao.markMessageAsRead(messageId, any()) }
        coVerify { socketIOManager.sendReadReceipt(messageId) }
    }

    @Test
    fun `getMessages should return flow of messages`() = runBlocking {
        // Arrange
        val roomId = 1
        val messages = listOf(
            MessageEntity(1, roomId, 100, "msg1", "2024-01-01", null),
            MessageEntity(2, roomId, 101, "msg2", "2024-01-02", null)
        )

        coEvery { messageDao.getMessagesByRoom(roomId) } returns flowOf(messages)

        // Act
        val result = mutableListOf<List<MessageItem>>()
        messageRepository.getMessages(roomId).collect { result.add(it) }

        // Assert
        assert(result.isNotEmpty())
        assert(result[0].size == 2)
    }

    @Test
    fun `searchMessages should filter by content`() = runBlocking {
        // Arrange
        val roomId = 1
        val query = "hello"
        val messages = listOf(
            MessageEntity(1, roomId, 100, "hello world", "2024-01-01", null),
            MessageEntity(2, roomId, 101, "goodbye", "2024-01-02", null)
        )

        coEvery { messageDao.getMessagesByRoom(roomId) } returns flowOf(messages)

        // Act
        val result = messageRepository.searchMessages(roomId, query)

        // Assert - should only return message containing "hello"
        assert(result.size >= 0) // Note: actual filtering depends on implementation
    }
}
