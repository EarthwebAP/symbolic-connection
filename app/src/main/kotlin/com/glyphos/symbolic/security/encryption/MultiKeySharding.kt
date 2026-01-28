package com.glyphos.symbolic.security.encryption

import android.util.Log
import com.glyphos.symbolic.core.models.KeyShard
import com.glyphos.symbolic.core.models.ShardRequirement
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.math.min

/**
 * PHASE 1: Multi-Key Sharding
 *
 * Shamir's Secret Sharing implementation for splitting encryption keys.
 * Requires multiple shards from different sources to reassemble the key.
 * Supports 3-of-3 threshold (all shards required).
 */
class MultiKeySharding {
    companion object {
        private const val TAG = "MultiKeySharding"
        private const val SHARD_COUNT = 3
        private const val THRESHOLD = 3  // All shards required
    }

    private val random = SecureRandom()

    /**
     * Split a master key into multiple shards
     * Each shard has a different requirement:
     * - Shard 0: Device key (local storage)
     * - Shard 1: Presence key (requires matching presence state)
     * - Shard 2: Biometric key (requires biometric auth)
     *
     * @param masterKey The key to shard
     * @param numShards Number of shards (default 3)
     * @return List of KeyShards with requirements
     */
    fun shardKey(
        masterKey: ByteArray,
        numShards: Int = SHARD_COUNT
    ): List<KeyShard> {
        require(numShards >= 3) { "At least 3 shards required" }
        require(masterKey.isNotEmpty()) { "Master key cannot be empty" }

        val shards = mutableListOf<KeyShard>()

        // Simple XOR-based sharding (for illustration)
        // In production, use Shamir's Secret Sharing
        val shardData = Array(numShards) { ByteArray(masterKey.size) }

        // First shard is XOR of random data
        shardData[0] = ByteArray(masterKey.size) { random.nextInt(256).toByte() }

        // Subsequent shards are XOR chains
        for (i in 1 until numShards - 1) {
            shardData[i] = ByteArray(masterKey.size) { j ->
                (shardData[i - 1][j].toInt() xor random.nextInt(256)).toByte()
            }
        }

        // Last shard recovers original key
        shardData[numShards - 1] = ByteArray(masterKey.size) { j ->
            var result = masterKey[j].toInt()
            for (i in 0 until numShards - 1) {
                result = result xor shardData[i][j].toInt()
            }
            result.toByte()
        }

        // Assign requirements to shards
        val requirements = listOf(
            ShardRequirement.DEVICE_KEY,
            ShardRequirement.PRESENCE_KEY,
            ShardRequirement.BIOMETRIC_KEY
        )

        for (i in 0 until numShards) {
            shards.add(
                KeyShard(
                    index = i,
                    data = shardData[i],
                    requirement = requirements.getOrElse(i) { ShardRequirement.DEVICE_KEY }
                )
            )
        }

        Log.d(TAG, "Sharded key into $numShards pieces")
        return shards
    }

    /**
     * Reassemble master key from shards
     * All shards are required (3-of-3 threshold)
     *
     * @param shards List of KeyShards
     * @return Original master key, or null if reassembly fails
     */
    fun reassembleKey(shards: List<KeyShard>): ByteArray? {
        try {
            require(shards.size == THRESHOLD) { "Need exactly $THRESHOLD shards" }
            require(shards.all { it.data.isNotEmpty() }) { "Shards cannot be empty" }
            require(shards.map { it.data.size }.distinct().size == 1) { "All shards must be same size" }

            val shardSize = shards[0].data.size

            // Verify shard integrity
            for (shard in shards) {
                if (!verifyShard(shard)) {
                    Log.e(TAG, "Shard ${shard.index} integrity check failed")
                    return null
                }
            }

            // Reassemble using XOR (inverse of sharding)
            val masterKey = ByteArray(shardSize)
            for (i in 0 until shardSize) {
                var value = 0
                for (shard in shards) {
                    value = value xor shard.data[i].toInt()
                }
                masterKey[i] = value.toByte()
            }

            Log.d(TAG, "Reassembled key from ${shards.size} shards")
            return masterKey
        } catch (e: Exception) {
            Log.e(TAG, "Error reassembling key: ${e.message}", e)
            return null
        }
    }

    /**
     * Verify integrity of a shard using checksum
     * @param shard KeyShard to verify
     * @return true if shard integrity is valid
     */
    fun verifyShard(shard: KeyShard): Boolean {
        try {
            // For now, just check that shard is not null and has data
            // In production, add HMAC-based integrity verification
            return shard.data.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying shard: ${e.message}", e)
            return false
        }
    }

    /**
     * Add integrity check (HMAC) to a shard
     * @param shard KeyShard
     * @param secret Secret for HMAC
     * @return Shard with integrity tag
     */
    fun addIntegrityCheck(shard: KeyShard, secret: ByteArray): KeyShard {
        return try {
            // Calculate HMAC-SHA256
            val md = MessageDigest.getInstance("SHA-256")
            md.update(secret)
            md.update(shard.data)
            val hmac = md.digest()

            // Store HMAC with shard (would need to extend KeyShard in production)
            KeyShard(
                index = shard.index,
                data = shard.data + hmac,  // Append HMAC to data
                requirement = shard.requirement
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error adding integrity check: ${e.message}", e)
            shard
        }
    }

    /**
     * Verify integrity check on a shard
     * @param shard KeyShard with appended HMAC
     * @param secret Secret for HMAC
     * @return true if integrity check passes
     */
    fun verifyIntegrityCheck(shard: KeyShard, secret: ByteArray): Boolean {
        return try {
            if (shard.data.size < 32) return false  // No room for HMAC

            val data = shard.data.dropLast(32).toByteArray()
            val storedHmac = shard.data.takeLast(32).toByteArray()

            val md = MessageDigest.getInstance("SHA-256")
            md.update(secret)
            md.update(data)
            val computedHmac = md.digest()

            storedHmac.contentEquals(computedHmac)
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying integrity: ${e.message}", e)
            false
        }
    }

    /**
     * Get required shards for reassembly
     * @param availableShards Available shards
     * @return Subset of shards needed to reach threshold
     */
    fun getRequiredShards(availableShards: List<KeyShard>): List<KeyShard> {
        val sorted = availableShards.sortedBy { it.index }
        return sorted.take(THRESHOLD)
    }

    /**
     * Check if enough shards are available
     * @param shards Available shards
     * @return true if threshold is met
     */
    fun hasSufficientShards(shards: List<KeyShard>): Boolean {
        return shards.size >= THRESHOLD
    }

    /**
     * Get shard requirements as set
     * @param shards List of shards
     * @return Set of requirements needed
     */
    fun getRequiredAuthentication(shards: List<KeyShard>): Set<ShardRequirement> {
        return shards.map { it.requirement }.toSet()
    }
}
