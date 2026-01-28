package com.glyphos.symbolic.core.models

import java.io.Serializable
import kotlin.math.abs

/**
 * PHASE 0: Core Data Contracts
 *
 * These are the foundational data types for all Symbolic Connection modules.
 * All modules must reference these types for type-safe communication.
 */

// ============================================================================
// ENUMERATIONS - Presence & State
// ============================================================================

enum class PresenceMode {
    PRIVATE,      // Alone, maximum security
    CALM,         // Quiet presence, minimal interaction
    ALONE,        // Solo, receptive
    SOCIAL,       // Open to interaction
    DEEP_FOCUS    // Highly concentrated, do not disturb
}

enum class EmotionalTone {
    NEUTRAL,      // Baseline
    JOYFUL,       // Positive, open
    CALM,         // Peaceful, relaxed
    ANXIOUS,      // Alert, careful
    ENERGETIC     // Active, engaged
}

enum class FocusLevel {
    LOW,          // Easily distracted
    MEDIUM,       // Normal attention
    HIGH,         // Concentrated
    DEEP          // Flow state
}

enum class SocialContext {
    ALONE,        // Only user present
    WITH_ONE,     // One other person
    SMALL_GROUP,  // 2-5 others
    PUBLIC        // 6+ others
}

enum class GlowState {
    NONE,         // No glow
    SUBTLE,       // Faint glow
    DIM,          // Visible glow
    FULL,         // Bright glow
    DISCREET      // Hidden glow (only visible when focused)
}

enum class RoomType {
    STANDARD,     // Normal messaging room
    BATCAVE,      // Private room with AI companions
    SECURE_DIGITAL,  // Zero notifications, ephemeral
    CEREMONIAL    // Formal, presence-bound access
}

enum class ResonanceType {
    URGENCY,      // Immediate attention needed
    CURIOSITY,    // Inquiry or interest
    FAVOR,        // Request for help
    EMOTIONAL_PRESENCE  // Presence-only signal
}

enum class RitualType {
    BREATH_UNLOCK,
    WHISPER_COMMAND,
    GESTURE_REVEAL,
    CEREMONIAL_REQUEST,
    PRESENCE_CONTRACT,
    SYMBOLIC_PULSE
}

enum class DeliveryMode {
    IMMEDIATE,    // Send immediately
    DELAYED,      // Wait for recipient presence
    SCHEDULED,    // Send at specific time
    CONDITIONAL   // Send if condition met
}

// ============================================================================
// CORE DATA CLASSES - Presence & Identity
// ============================================================================

/**
 * Represents a user's current presence state
 * Used for presence-bound access control
 */
data class PresenceState(
    val mode: PresenceMode,
    val emotionalTone: EmotionalTone,
    val focusLevel: FocusLevel,
    val socialContext: SocialContext,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable {
    fun matches(required: PresenceState): Boolean {
        return mode == required.mode &&
               emotionalTone == required.emotionalTone &&
               focusLevel.ordinal >= required.focusLevel.ordinal &&
               socialContext.ordinal <= required.socialContext.ordinal
    }
}

/**
 * Semantic metrics for glyph characterization
 * Each metric ranges from 0-100
 */
data class SemanticMetrics(
    val power: Int,              // Computational intensity
    val complexity: Int,         // Parameter complexity
    val resonance: Int,          // Feature interaction strength
    val stability: Int,          // Gradient stability
    val connectivity: Int,       // Inter-layer connectivity
    val affinity: Int            // Semantic similarity
) : Serializable {
    init {
        require(power in 0..100) { "Power must be 0-100" }
        require(complexity in 0..100) { "Complexity must be 0-100" }
        require(resonance in 0..100) { "Resonance must be 0-100" }
        require(stability in 0..100) { "Stability must be 0-100" }
        require(connectivity in 0..100) { "Connectivity must be 0-100" }
        require(affinity in 0..100) { "Affinity must be 0-100" }
    }

    fun distanceTo(other: SemanticMetrics): Double {
        return Math.sqrt(
            (power - other.power).toDouble().pow(2) +
            (complexity - other.complexity).toDouble().pow(2) +
            (resonance - other.resonance).toDouble().pow(2) +
            (stability - other.stability).toDouble().pow(2) +
            (connectivity - other.connectivity).toDouble().pow(2) +
            (affinity - other.affinity).toDouble().pow(2)
        )
    }
}

/**
 * Glyph visual and semantic identity
 * Represents a compressed layer parameter encoding
 */
data class GlyphIdentity(
    val glyphId: String,                    // Unique identifier (001-600)
    val name: String = "UNKNOWN",           // Glyph name
    val visualData: ByteArray,              // Rendered visual representation
    val semanticMetrics: SemanticMetrics,   // 6D semantic vector
    val resonancePattern: ByteArray,        // 64D latent space encoding
    val glowState: GlowState = GlowState.NONE,
    val category: String = "unknown"        // Glyph category from GlyphCP
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GlyphIdentity) return false
        return glyphId == other.glyphId &&
               visualData.contentEquals(other.visualData) &&
               semanticMetrics == other.semanticMetrics
    }

    override fun hashCode(): Int {
        var result = glyphId.hashCode()
        result = 31 * result + visualData.contentHashCode()
        result = 31 * result + semanticMetrics.hashCode()
        return result
    }
}

/**
 * Represents a single user in the system
 */
data class UserIdentity(
    val userId: String,                     // Unique user ID
    val displayName: String,                // Display name
    val personalGlyph: GlyphIdentity,       // User's personal glyph
    val presenceState: PresenceState,       // Current presence
    val encryptionKeyAlias: String,         // Android Keystore alias
    val createdAt: Long = System.currentTimeMillis(),
    val lineageMarkers: List<String> = emptyList()  // Identity ancestry
) : Serializable

// ============================================================================
// ENCRYPTION & SECURITY CONTRACTS
// ============================================================================

/**
 * Encrypted content with metadata
 */
data class EncryptedContent(
    val ciphertext: ByteArray,
    val keyAlias: String,
    val nonce: ByteArray,
    val encryptedAt: Long = System.currentTimeMillis()
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptedContent) return false
        return ciphertext.contentEquals(other.ciphertext) &&
               keyAlias == other.keyAlias &&
               nonce.contentEquals(other.nonce)
    }

    override fun hashCode(): Int {
        var result = ciphertext.contentHashCode()
        result = 31 * result + keyAlias.hashCode()
        result = 31 * result + nonce.contentHashCode()
        return result
    }
}

/**
 * Key shard for multi-key sharding
 */
data class KeyShard(
    val index: Int,
    val data: ByteArray,
    val requirement: ShardRequirement
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KeyShard) return false
        return index == other.index && data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + data.contentHashCode()
        return result
    }
}

enum class ShardRequirement {
    DEVICE_KEY,      // Requires device unlock
    PRESENCE_KEY,    // Requires matching presence
    BIOMETRIC_KEY    // Requires biometric auth
}

/**
 * Delivery profile for messages
 */
data class DeliveryProfile(
    val mode: DeliveryMode,
    val delayMs: Long = 0,
    val scheduledTime: Long? = null,
    val condition: String? = null
) : Serializable

// ============================================================================
// MESSAGING & COMMUNICATION
// ============================================================================

/**
 * Single message
 */
data class Message(
    val messageId: String,
    val senderId: String,
    val receiverId: String,
    val content: EncryptedContent,
    val deliveryProfile: DeliveryProfile,
    val presenceRequirements: PresenceState? = null,  // Presence-bound access
    val timestamp: Long = System.currentTimeMillis(),
    val expiryTime: Long? = null,                      // Time-sensitive expiry
    val readAt: Long? = null
) : Serializable

/**
 * Signal glyph - non-verbal communication via resonance
 */
data class SignalGlyph(
    val signalId: String,
    val senderId: String,
    val receiverId: String,
    val resonanceType: ResonanceType,
    val glyphData: GlyphIdentity,
    val hiddenMessage: EncryptedContent? = null,       // Optional encrypted message
    val presenceAdaptiveGlow: GlowState = GlowState.NONE,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

// ============================================================================
// ROOMS & SPACES
// ============================================================================

/**
 * Security profile for a room
 */
data class SecurityProfile(
    val notificationsEnabled: Boolean = true,
    val loggingEnabled: Boolean = true,
    val exportAllowed: Boolean = true,
    val aiAccessible: Boolean = true,
    val presenceRequired: PresenceState? = null
) : Serializable

/**
 * A communication room or space
 */
data class Room(
    val roomId: String,
    val name: String,
    val type: RoomType,
    val ownerId: String,
    val participants: List<String> = emptyList(),  // User IDs
    val securityProfile: SecurityProfile,
    val createdAt: Long = System.currentTimeMillis(),
    val archivedAt: Long? = null
) : Serializable

/**
 * Batcave - private room with AI companions
 */
data class Batcave(
    val room: Room,
    val aiCompanions: List<String> = emptyList(),  // AI agent IDs
    val customTools: List<String> = emptyList()    // Custom tool IDs
) : Serializable {
    companion object {
        fun create(userId: String): Batcave {
            val room = Room(
                roomId = "batcave-$userId",
                name = "Batcave",
                type = RoomType.BATCAVE,
                ownerId = userId,
                securityProfile = SecurityProfile(
                    notificationsEnabled = true,
                    loggingEnabled = true,
                    exportAllowed = true,
                    aiAccessible = true
                )
            )
            return Batcave(room)
        }
    }
}

/**
 * Secure digital room - zero notifications, ephemeral
 */
data class SecureDigitalRoom(
    val room: Room
) : Serializable {
    companion object {
        fun create(userId: String, participantIds: List<String>): SecureDigitalRoom {
            val room = Room(
                roomId = "secure-${System.currentTimeMillis()}",
                name = "Secure Room",
                type = RoomType.SECURE_DIGITAL,
                ownerId = userId,
                participants = participantIds,
                securityProfile = SecurityProfile(
                    notificationsEnabled = false,
                    loggingEnabled = false,
                    exportAllowed = false,
                    aiAccessible = false
                )
            )
            return SecureDigitalRoom(room)
        }
    }
}

// ============================================================================
// RITUALS & EVENTS
// ============================================================================

/**
 * Ritual event triggered by user actions
 */
data class RitualEvent(
    val eventId: String,
    val type: RitualType,
    val triggeredBy: String,      // User ID
    val targetId: String? = null, // Room or message ID
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, Any> = emptyMap()
) : Serializable

/**
 * Presence contract - user agrees to presence state
 */
data class PresenceContract(
    val contractId: String,
    val userId: String,
    val requiredPresence: PresenceState,
    val duration: Long,           // Milliseconds
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + duration
) : Serializable

/**
 * Access grant for ceremonial requests
 */
data class AccessGrant(
    val grantId: String,
    val granteeId: String,
    val resources: List<String>,  // Resource IDs
    val expiresAt: Long,
    val revocable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

// ============================================================================
// CONTENT & WORKSPACE
// ============================================================================

/**
 * Content embedded in glyphs via microscope
 */
sealed class EmbeddedContent : Serializable {
    data class Note(
        val text: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : EmbeddedContent()

    data class File(
        val path: String,
        val encrypted: Boolean = true,
        val mimeType: String = ""
    ) : EmbeddedContent()

    data class MessageRef(
        val messageId: String,
        val encryptedContent: EncryptedContent
    ) : EmbeddedContent()

    data class MicroThread(
        val messages: List<String>,  // Message IDs
        val title: String = ""
    ) : EmbeddedContent()
}

// ============================================================================
// HELPER EXTENSIONS
// ============================================================================

fun <T : Number> T.pow(exponent: Int): Double {
    return Math.pow(this.toDouble(), exponent.toDouble())
}
