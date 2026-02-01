package com.glyphos.symbolic.data

import java.io.Serializable

/**
 * Cipher message embedded in infinite zoom glyph
 * Message is hidden at specific zoom depth coordinates
 */
data class CipherMessage(
    val messageId: String,
    val glyphId: String,
    val senderId: String,
    val recipientId: String,
    val plaintext: String,
    val zoomDepth: Float,  // Zoom level at which message appears (1.0 - 30000.0)
    val xCoordinate: Double,  // X position in zoom space
    val yCoordinate: Double,  // Y position in zoom space
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
    val isRevealed: Boolean = false  // Whether user has extracted the message
) : Serializable

/**
 * Cipher encoder/decoder for GLyphIX
 */
object CipherCodec {
    fun encode(plaintext: String, seed: Long = System.currentTimeMillis()): String {
        // XOR cipher with seed-based key for simplicity
        // In production, use AES-256-GCM
        val bytes = plaintext.toByteArray()
        val key = seed.toString().toByteArray()

        val encoded = bytes.mapIndexed { i, byte ->
            (byte.toInt() xor key[i % key.size].toInt()).toByte()
        }.toByteArray()

        return android.util.Base64.encodeToString(encoded, android.util.Base64.DEFAULT)
    }

    fun decode(ciphertext: String, seed: Long): String? {
        return try {
            val encoded = android.util.Base64.decode(ciphertext, android.util.Base64.DEFAULT)
            val key = seed.toString().toByteArray()

            val decoded = encoded.mapIndexed { i, byte ->
                (byte.toInt() xor key[i % key.size].toInt()).toByte()
            }.toByteArray()

            String(decoded)
        } catch (e: Exception) {
            null
        }
    }

    fun generateCipherSeed(): Long = System.currentTimeMillis()
}
