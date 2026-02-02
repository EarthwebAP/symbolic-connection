package com.glyphos.symbolic.data

import java.io.Serializable

/**
 * Invoked Content Type
 * Defines what type of content is invoked inside the glyph
 */
enum class InvokedContentType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO
}

/**
 * Primordial Cipher Message
 * Message/media embedded in harmonic field at specific frequency and 3D coordinates
 * Uses GlyphOS primordial coordinate system for infinite resolution
 */
data class CipherMessage(
    val messageId: String,
    val glyphId: String,
    val senderId: String,
    val recipientId: String,
    // Content
    val plaintext: String,  // Text content (if type is TEXT)
    val contentType: InvokedContentType = InvokedContentType.TEXT,
    val mediaUrl: String? = null,  // URL/path for IMAGE, VIDEO, AUDIO
    val mediaMimeType: String? = null,  // MIME type for media
    val mediaThumbnail: String? = null,  // Base64 thumbnail for preview
    // Harmonic embedding coordinates
    val embeddingFrequency: Double,  // Harmonic frequency (440Hz - 20,000Hz range)
    val xCoordinate: Double,  // 3D field position X
    val yCoordinate: Double,  // 3D field position Y
    val zCoordinate: Double,  // 3D field position Z (depth in harmonic field)
    val baseFrequency: Double,  // Base frequency of the glyph (usually 440Hz)
    val encryptedData: String,  // Base64 encoded cipher
    val timestamp: Long,
    val deliveryStatus: DeliveryStatus = DeliveryStatus.SENT,
    val readAt: Long? = null
) : Serializable

/**
 * Cipher message metadata for display in chat
 */
data class CipherMessageItem(
    val messageId: String,
    val senderId: String,
    val senderName: String,
    val timestamp: Long,
    val deliveryStatus: DeliveryStatus = DeliveryStatus.SENT,
    val glyphPreviewId: String,  // Glyph containing the cipher
    val isRevealed: Boolean = false,  // Whether user has extracted the message
    val embeddingFrequency: Double  // For display purposes
) : Serializable

/**
 * Primordial Cipher Codec - Harmonic-based encryption
 * Encrypts messages using harmonic frequency modulation
 */
object CipherCodec {
    fun encode(plaintext: String, frequency: Double, seed: Long = System.currentTimeMillis()): String {
        // Use frequency as part of key derivation
        val frequencyKey = (frequency * 1000).toLong()
        val combinedSeed = seed xor frequencyKey

        val bytes = plaintext.toByteArray()
        val key = combinedSeed.toString().toByteArray()

        val encoded = bytes.mapIndexed { i, byte ->
            (byte.toInt() xor key[i % key.size].toInt()).toByte()
        }.toByteArray()

        return android.util.Base64.encodeToString(encoded, android.util.Base64.DEFAULT)
    }

    fun decode(ciphertext: String, frequency: Double, seed: Long): String? {
        return try {
            val frequencyKey = (frequency * 1000).toLong()
            val combinedSeed = seed xor frequencyKey

            val encoded = android.util.Base64.decode(ciphertext, android.util.Base64.DEFAULT)
            val key = combinedSeed.toString().toByteArray()

            val decoded = encoded.mapIndexed { i, byte ->
                (byte.toInt() xor key[i % key.size].toInt()).toByte()
            }.toByteArray()

            String(decoded)
        } catch (e: Exception) {
            null
        }
    }

    fun generateCipherSeed(): Long = System.currentTimeMillis()

    /**
     * Calculate harmonic frequency for embedding
     * Harmonics range from baseFrequency to baseFrequency * 8
     */
    fun calculateEmbeddingFrequency(baseFrequency: Double, harmonicLevel: Int): Double {
        return baseFrequency * harmonicLevel.coerceIn(1, 8)
    }

    /**
     * Generate 3D field position within harmonic field
     * x, y in [-1, 1] (field plane)
     * z represents depth in harmonic structure
     */
    fun generateFieldPosition(frequency: Double, baseFrequency: Double): Triple<Double, Double, Double> {
        val harmonicRatio = frequency / baseFrequency
        val angle = (frequency % 360.0) * Math.PI / 180.0
        val radius = (harmonicRatio - 1.0).coerceIn(0.0, 1.0)

        val x = (radius * kotlin.math.cos(angle)).coerceIn(-1.0, 1.0)
        val y = (radius * kotlin.math.sin(angle)).coerceIn(-1.0, 1.0)
        val z = (harmonicRatio / 8.0).coerceIn(0.0, 1.0)  // Normalized depth

        return Triple(x, y, z)
    }
}
