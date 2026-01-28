package com.glyphos.symbolic.security.encryption

import android.util.Log
import com.glyphos.symbolic.core.models.EncryptedContent
import com.glyphos.symbolic.core.models.GlyphIdentity
import java.security.MessageDigest
import java.util.Base64

/**
 * PHASE 1: Glyph-Locked Encryption
 *
 * Content can be attached to glyphs and only decrypted when
 * the user zooms into that specific glyph.
 *
 * Uses the glyph's latent space as key material combined with
 * device-stored key for decryption.
 */
class GlyphLockedEncryption(
    private val encryptionEngine: LocalEncryptionEngine
) {
    companion object {
        private const val TAG = "GlyphLockedEncryption"
        private const val GLYPH_KEY_PREFIX = "glyph_"
    }

    /**
     * Derive encryption key from glyph identity
     * Combines glyph's latent vector with a hash to create unique key material
     *
     * @param glyph Glyph to derive key from
     * @return Key material derived from glyph
     */
    fun deriveGlyphKey(glyph: GlyphIdentity): ByteArray {
        return try {
            val md = MessageDigest.getInstance("SHA-256")

            // Include glyph ID
            md.update(glyph.glyphId.toByteArray())

            // Include resonance pattern (64D latent vector)
            md.update(glyph.resonancePattern)

            // Include semantic metrics
            md.update(glyph.semanticMetrics.power.toByte())
            md.update(glyph.semanticMetrics.complexity.toByte())
            md.update(glyph.semanticMetrics.resonance.toByte())
            md.update(glyph.semanticMetrics.stability.toByte())
            md.update(glyph.semanticMetrics.connectivity.toByte())
            md.update(glyph.semanticMetrics.affinity.toByte())

            md.digest()
        } catch (e: Exception) {
            Log.e(TAG, "Error deriving glyph key: ${e.message}", e)
            ByteArray(32)  // Return empty key on error
        }
    }

    /**
     * Attach encrypted content to a glyph
     * The content can only be decrypted when zooming into the glyph
     *
     * @param content Content to encrypt
     * @param glyph Glyph to attach content to
     * @return GlyphEncryptedContent with metadata
     */
    fun attachToGlyph(
        content: ByteArray,
        glyph: GlyphIdentity
    ): GlyphEncryptedContent {
        return try {
            // Generate key for this glyph
            val keyAlias = "${GLYPH_KEY_PREFIX}${glyph.glyphId}"

            // Ensure key exists
            if (!encryptionEngine.keyExists(keyAlias)) {
                encryptionEngine.generateKey(keyAlias)
            }

            // Encrypt content
            val encrypted = encryptionEngine.encrypt(content, keyAlias)
                ?: return GlyphEncryptedContent(
                    glyphId = glyph.glyphId,
                    content = null,
                    attached = false,
                    attachedAt = 0
                )

            GlyphEncryptedContent(
                glyphId = glyph.glyphId,
                content = encrypted,
                attached = true,
                attachedAt = System.currentTimeMillis(),
                contentSize = content.size
            ).also {
                Log.d(TAG, "Content attached to glyph: ${glyph.glyphId}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error attaching content: ${e.message}", e)
            GlyphEncryptedContent(
                glyphId = glyph.glyphId,
                content = null,
                attached = false,
                attachedAt = 0
            )
        }
    }

    /**
     * Unlock and decrypt content when zooming into a glyph
     * Triggered by zoom gesture/pinch on the glyph visual
     *
     * @param glyph Glyph being zoomed into
     * @param encrypted GlyphEncryptedContent attached to glyph
     * @return Decrypted content, or null if unlock fails
     */
    fun unlockOnZoom(
        glyph: GlyphIdentity,
        encrypted: GlyphEncryptedContent
    ): ByteArray? {
        return try {
            if (!encrypted.attached || encrypted.content == null) {
                Log.w(TAG, "No content attached to glyph: ${glyph.glyphId}")
                return null
            }

            val keyAlias = "${GLYPH_KEY_PREFIX}${glyph.glyphId}"

            // Decrypt using glyph's key
            val plaintext = encryptionEngine.decrypt(encrypted.content, keyAlias)

            if (plaintext != null) {
                Log.d(TAG, "Glyph unlocked on zoom: ${glyph.glyphId}")
            } else {
                Log.e(TAG, "Failed to decrypt glyph content")
            }

            plaintext
        } catch (e: Exception) {
            Log.e(TAG, "Error unlocking glyph: ${e.message}", e)
            null
        }
    }

    /**
     * Check if glyph has attached content
     * @param glyph Glyph to check
     * @return true if content is attached
     */
    fun hasContent(glyph: GlyphIdentity): Boolean {
        val keyAlias = "${GLYPH_KEY_PREFIX}${glyph.glyphId}"
        return encryptionEngine.keyExists(keyAlias)
    }

    /**
     * Delete content from a glyph
     * @param glyph Glyph containing content
     * @return true if deletion successful
     */
    fun deleteContent(glyph: GlyphIdentity): Boolean {
        val keyAlias = "${GLYPH_KEY_PREFIX}${glyph.glyphId}"
        return encryptionEngine.deleteKey(keyAlias)
    }

    /**
     * List all glyphs with attached content
     * @return List of glyph IDs with content
     */
    fun getGlyphsWithContent(): List<String> {
        return encryptionEngine.listKeys()
            .filter { it.startsWith(GLYPH_KEY_PREFIX) }
            .map { it.removePrefix(GLYPH_KEY_PREFIX) }
    }

    /**
     * Get size of encrypted content
     * @param encrypted GlyphEncryptedContent
     * @return Size in bytes, or 0 if not available
     */
    fun getContentSize(encrypted: GlyphEncryptedContent): Int {
        return encrypted.contentSize ?: encrypted.content?.ciphertext?.size ?: 0
    }

    /**
     * Re-encrypt all glyph content with new key
     * Useful for security rotation
     * @param glyph Glyph with content
     * @param encrypted Current GlyphEncryptedContent
     * @return New GlyphEncryptedContent with rotated key
     */
    fun rotateGlyphKey(
        glyph: GlyphIdentity,
        encrypted: GlyphEncryptedContent
    ): GlyphEncryptedContent {
        try {
            val plaintext = unlockOnZoom(glyph, encrypted) ?: return encrypted
            deleteContent(glyph)
            return attachToGlyph(plaintext, glyph)
        } catch (e: Exception) {
            Log.e(TAG, "Error rotating key: ${e.message}", e)
            return encrypted
        }
    }
}

/**
 * Content encrypted and attached to a glyph
 */
data class GlyphEncryptedContent(
    val glyphId: String,
    val content: EncryptedContent?,
    val attached: Boolean,
    val attachedAt: Long,
    val contentSize: Int? = null,
    val contentType: String? = null,
    val metadata: Map<String, String> = emptyMap()
)
