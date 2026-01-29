package com.glyphos.symbolic.identity.microscope

import android.util.Log
import com.glyphos.symbolic.core.models.EmbeddedContent
import com.glyphos.symbolic.core.models.GlyphIdentity
import com.glyphos.symbolic.core.models.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PHASE 2: Glyph Content Manager
 *
 * Manages content embedded in glyphs.
 * Stores notes, files, messages, and micro-threads inside glyph workspaces.
 */
class GlyphContentManager {
    companion object {
        private const val TAG = "GlyphContentManager"
        private const val MAX_ITEMS_PER_GLYPH = 100
    }

    // Glyph content storage: glyphId -> list of content
    private val contentStorage = mutableMapOf<String, MutableList<EmbeddedContent>>()

    // Observable content for each glyph
    private val contentFlows = mutableMapOf<String, MutableStateFlow<List<EmbeddedContent>>>()

    /**
     * Add content to a glyph
     * @param glyphId Glyph to add content to
     * @param content Content to add
     * @return true if added successfully
     */
    suspend fun addContent(glyphId: String, content: EmbeddedContent): Boolean {
        return try {
            val items = contentStorage.getOrPut(glyphId) { mutableListOf() }

            if (items.size >= MAX_ITEMS_PER_GLYPH) {
                Log.w(TAG, "Glyph $glyphId is full")
                return false
            }

            items.add(content)
            updateContentFlow(glyphId)

            Log.d(TAG, "Added content to glyph: $glyphId (${items.size} total)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding content: ${e.message}", e)
            false
        }
    }

    /**
     * Get all content from a glyph
     * @param glyphId Glyph to retrieve from
     * @return List of embedded content
     */
    suspend fun getContent(glyphId: String): List<EmbeddedContent> {
        return contentStorage[glyphId]?.toList() ?: emptyList()
    }

    /**
     * Get content count for glyph
     * @param glyphId Glyph to check
     * @return Number of items
     */
    suspend fun getContentCount(glyphId: String): Int {
        return contentStorage[glyphId]?.size ?: 0
    }

    /**
     * Delete specific content from glyph
     * @param glyphId Glyph containing content
     * @param index Index of content to delete
     * @return true if deleted
     */
    suspend fun deleteContent(glyphId: String, index: Int): Boolean {
        return try {
            val items = contentStorage[glyphId] ?: return false

            if (index < 0 || index >= items.size) {
                return false
            }

            items.removeAt(index)
            updateContentFlow(glyphId)

            Log.d(TAG, "Deleted content from glyph: $glyphId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting content: ${e.message}", e)
            false
        }
    }

    /**
     * Clear all content from glyph
     * @param glyphId Glyph to clear
     */
    suspend fun clearContent(glyphId: String) {
        contentStorage[glyphId]?.clear()
        updateContentFlow(glyphId)
        Log.d(TAG, "Cleared all content from glyph: $glyphId")
    }

    /**
     * Observe content changes for a glyph
     * @param glyphId Glyph to observe
     * @return StateFlow of content list
     */
    fun observeContent(glyphId: String): StateFlow<List<EmbeddedContent>> {
        return contentFlows.getOrPut(glyphId) {
            MutableStateFlow(contentStorage[glyphId]?.toList() ?: emptyList())
        }.asStateFlow()
    }

    /**
     * Add note to glyph
     * @param glyphId Glyph ID
     * @param text Note text
     * @return true if added
     */
    suspend fun addNote(glyphId: String, text: String): Boolean {
        return addContent(glyphId, EmbeddedContent.Note(text))
    }

    /**
     * Add file reference to glyph
     * @param glyphId Glyph ID
     * @param filePath File path
     * @param mimeType MIME type
     * @param encrypted Whether file is encrypted
     * @return true if added
     */
    suspend fun addFile(
        glyphId: String,
        filePath: String,
        mimeType: String = "",
        encrypted: Boolean = true
    ): Boolean {
        return addContent(
            glyphId,
            EmbeddedContent.File(filePath, encrypted, mimeType)
        )
    }

    /**
     * Add message to glyph
     * @param glyphId Glyph ID
     * @param messageId Message ID to reference
     * @param encryptedContent Encrypted message content
     * @return true if added
     */
    suspend fun addMessage(
        glyphId: String,
        messageId: String,
        encryptedContent: com.glyphos.symbolic.core.models.EncryptedContent
    ): Boolean {
        return addContent(
            glyphId,
            EmbeddedContent.MessageRef(messageId, encryptedContent)
        )
    }

    /**
     * Add micro-thread to glyph
     * @param glyphId Glyph ID
     * @param messages List of message IDs
     * @param title Thread title
     * @return true if added
     */
    suspend fun addMicroThread(
        glyphId: String,
        messages: List<String>,
        title: String = ""
    ): Boolean {
        return addContent(
            glyphId,
            EmbeddedContent.MicroThread(messages, title)
        )
    }

    /**
     * Get notes from glyph
     * @param glyphId Glyph ID
     * @return List of notes
     */
    suspend fun getNotes(glyphId: String): List<EmbeddedContent.Note> {
        return getContent(glyphId).filterIsInstance<EmbeddedContent.Note>()
    }

    /**
     * Get files from glyph
     * @param glyphId Glyph ID
     * @return List of files
     */
    suspend fun getFiles(glyphId: String): List<EmbeddedContent.File> {
        return getContent(glyphId).filterIsInstance<EmbeddedContent.File>()
    }

    /**
     * Get messages from glyph
     * @param glyphId Glyph ID
     * @return List of message references
     */
    suspend fun getMessages(glyphId: String): List<EmbeddedContent.MessageRef> {
        return getContent(glyphId).filterIsInstance<EmbeddedContent.MessageRef>()
    }

    /**
     * Get micro-threads from glyph
     * @param glyphId Glyph ID
     * @return List of threads
     */
    suspend fun getThreads(glyphId: String): List<EmbeddedContent.MicroThread> {
        return getContent(glyphId).filterIsInstance<EmbeddedContent.MicroThread>()
    }

    /**
     * Search content by text
     * @param glyphId Glyph ID
     * @param query Search query
     * @return List of matching content
     */
    suspend fun searchContent(glyphId: String, query: String): List<EmbeddedContent> {
        return getContent(glyphId).filter { content ->
            when (content) {
                is EmbeddedContent.Note -> content.text.contains(query, ignoreCase = true)
                is EmbeddedContent.File -> content.path.contains(query, ignoreCase = true)
                is EmbeddedContent.MessageRef -> content.messageId.contains(query, ignoreCase = true)
                is EmbeddedContent.MicroThread -> content.title.contains(query, ignoreCase = true)
            }
        }
    }

    /**
     * Get all glyphs with content
     * @return List of glyph IDs
     */
    suspend fun getGlyphsWithContent(): List<String> {
        return contentStorage.filter { it.value.isNotEmpty() }.keys.toList()
    }

    /**
     * Get total content across all glyphs
     * @return Total count
     */
    suspend fun getTotalContentCount(): Int {
        return contentStorage.values.sumOf { it.size }
    }

    /**
     * Get statistics
     * @return ContentStatistics
     */
    suspend fun getStatistics(): ContentStatistics {
        val totalGlyphs = contentStorage.size
        val totalItems = getTotalContentCount()
        val glyphsWithContent = getGlyphsWithContent().size

        val notes = contentStorage.values.flatten().filterIsInstance<EmbeddedContent.Note>().size
        val files = contentStorage.values.flatten().filterIsInstance<EmbeddedContent.File>().size
        val messages = contentStorage.values.flatten().filterIsInstance<EmbeddedContent.MessageRef>().size
        val threads = contentStorage.values.flatten().filterIsInstance<EmbeddedContent.MicroThread>().size

        return ContentStatistics(
            totalGlyphs = totalGlyphs,
            glyphsWithContent = glyphsWithContent,
            totalItems = totalItems,
            notes = notes,
            files = files,
            messages = messages,
            threads = threads
        )
    }

    /**
     * Update content flow for a glyph
     */
    private fun updateContentFlow(glyphId: String) {
        val flow = contentFlows.getOrPut(glyphId) {
            MutableStateFlow(emptyList())
        }
        flow.value = contentStorage[glyphId]?.toList() ?: emptyList()
    }

    /**
     * Clear all storage (dangerous operation)
     */
    suspend fun clearAll() {
        contentStorage.clear()
        contentFlows.clear()
        Log.w(TAG, "Cleared all glyph content storage")
    }
}

data class ContentStatistics(
    val totalGlyphs: Int,
    val glyphsWithContent: Int,
    val totalItems: Int,
    val notes: Int,
    val files: Int,
    val messages: Int,
    val threads: Int
) {
    fun summary(): String {
        return """
        Glyph Content Statistics:
        - Total glyphs: $totalGlyphs
        - Glyphs with content: $glyphsWithContent
        - Total items: $totalItems
        - Notes: $notes
        - Files: $files
        - Messages: $messages
        - Threads: $threads
        """.trimIndent()
    }
}
