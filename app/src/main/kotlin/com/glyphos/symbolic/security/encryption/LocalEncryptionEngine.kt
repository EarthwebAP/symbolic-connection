package com.glyphos.symbolic.security.encryption

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import com.glyphos.symbolic.core.models.EncryptedContent
import java.security.KeyPairGenerator
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * PHASE 1: Local Encryption Engine
 *
 * Manages encryption/decryption using Android Keystore.
 * All encryption happens locally on device - server only sees ciphertext.
 * Keys are stored in hardware-backed secure storage (if available).
 */
class LocalEncryptionEngine {
    companion object {
        private const val TAG = "LocalEncryptionEngine"
        private const val KEY_SIZE = 256
        private const val IV_SIZE = 12
        private const val TAG_SIZE = 128
        private const val ALGORITHM = "AES"
        private const val BLOCK_MODE = "GCM"
        private const val PADDING = "NoPadding"
        private val CIPHER_TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
        private const val KEY_STORE = "AndroidKeyStore"
    }

    private val keyStore: KeyStore = KeyStore.getInstance(KEY_STORE).apply { load(null) }

    /**
     * Generate a new encryption key and store in Android Keystore
     * @param alias Unique identifier for the key
     * @return true if key generation succeeded
     */
    fun generateKey(alias: String): Boolean {
        return try {
            // Check if key already exists
            if (keyStore.containsAlias(alias)) {
                Log.w(TAG, "Key with alias '$alias' already exists")
                true
            } else {
                val keyGenerator = KeyGenerator.getInstance(ALGORITHM, KEY_STORE)

                val spec = KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                ).apply {
                    setKeySize(KEY_SIZE)
                    setBlockModes(BLOCK_MODE)
                    setEncryptionPaddings(PADDING)
                    setRandomizedEncryptionRequired(true)

                    // Security: Require user authentication
                    setUserAuthenticationRequired(false)  // Can be set to true if biometric required

                    // Validity: Keys are valid indefinitely
                    setKeyValidityStart(java.util.Calendar.getInstance().time)
                }.build()

                keyGenerator.init(spec)
                val key = keyGenerator.generateKey()

                Log.d(TAG, "Generated new key: $alias")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating key: ${e.message}", e)
            false
        }
    }

    /**
     * Encrypt data using stored key
     * @param data Data to encrypt
     * @param keyAlias Keystore alias
     * @return EncryptedContent with ciphertext and nonce, or null on failure
     */
    fun encrypt(data: ByteArray, keyAlias: String): EncryptedContent? {
        return try {
            val key = keyStore.getKey(keyAlias, null) as? SecretKey
            if (key == null) {
                Log.e(TAG, "Key not found: $keyAlias")
                null
            } else {
                val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, key)

                val iv = cipher.iv  // GCM mode generates random IV
                val ciphertext = cipher.doFinal(data)

                EncryptedContent(
                    ciphertext = ciphertext,
                    keyAlias = keyAlias,
                    nonce = iv
                ).also {
                    Log.d(TAG, "Encrypted ${data.size} bytes with key: $keyAlias")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Encryption error: ${e.message}", e)
            null
        }
    }

    /**
     * Decrypt encrypted content using stored key
     * @param encrypted EncryptedContent with ciphertext and nonce
     * @param keyAlias Keystore alias (should match EncryptedContent.keyAlias)
     * @return Decrypted plaintext, or null on failure
     */
    fun decrypt(encrypted: EncryptedContent, keyAlias: String): ByteArray? {
        return try {
            // Verify key alias matches
            if (encrypted.keyAlias != keyAlias) {
                Log.e(TAG, "Key alias mismatch: expected ${encrypted.keyAlias}, got $keyAlias")
                null
            } else {
                val key = keyStore.getKey(keyAlias, null) as? SecretKey
                if (key == null) {
                    Log.e(TAG, "Key not found: $keyAlias")
                    null
                } else {
                    val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
                    val gcmSpec = GCMParameterSpec(TAG_SIZE, encrypted.nonce)
                    cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

                    val plaintext = cipher.doFinal(encrypted.ciphertext)
                    Log.d(TAG, "Decrypted ${encrypted.ciphertext.size} bytes with key: $keyAlias")
                    plaintext
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Decryption error: ${e.message}", e)
            null
        }
    }

    /**
     * Delete a key from Android Keystore
     * @param alias Key alias
     * @return true if key was deleted
     */
    fun deleteKey(alias: String): Boolean {
        return try {
            if (!keyStore.containsAlias(alias)) {
                true
            } else {
                keyStore.deleteEntry(alias)
                Log.d(TAG, "Deleted key: $alias")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting key: ${e.message}", e)
            false
        }
    }

    /**
     * Check if a key exists in keystore
     * @param alias Key alias
     * @return true if key exists
     */
    fun keyExists(alias: String): Boolean {
        return keyStore.containsAlias(alias)
    }

    /**
     * List all keys in keystore
     * @return List of key aliases
     */
    fun listKeys(): List<String> {
        return keyStore.aliases().toList()
    }

    /**
     * Re-encrypt data with a different key
     * Useful for key rotation
     * @param encrypted EncryptedContent with old key
     * @param oldKeyAlias Current key alias
     * @param newKeyAlias New key alias
     * @return EncryptedContent with new key, or null on failure
     */
    fun reencryptData(
        encrypted: EncryptedContent,
        oldKeyAlias: String,
        newKeyAlias: String
    ): EncryptedContent? {
        val plaintext = decrypt(encrypted, oldKeyAlias) ?: return null
        return encrypt(plaintext, newKeyAlias)
    }
}
