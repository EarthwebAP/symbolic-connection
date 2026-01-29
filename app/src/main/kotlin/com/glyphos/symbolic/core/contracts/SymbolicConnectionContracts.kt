package com.glyphos.symbolic.core.contracts

import java.io.Serializable

// ============================================================================
// 1. IDENTITY CONTRACTS
// ============================================================================

data class UserId(val value: String) : Serializable

data class UserIdentity(
    val userId: UserId,
    val displayName: String,
    val personalGlyph: PersonalGlyph,
    val presenceState: PresenceState,
    val securityKeys: SecurityKeys,
    val createdAt: Long = System.currentTimeMillis(),
    val lineageMarkers: List<String> = emptyList()
) : Serializable

data class PersonalGlyph(
    val glyphId: String,
    val visualData: ByteArray,
    val resonancePattern: ByteArray,
    val glowState: GlowState = GlowState.NONE,
    val createdAt: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PersonalGlyph) return false
        return glyphId == other.glyphId && visualData.contentEquals(other.visualData)
    }

    override fun hashCode(): Int {
        var result = glyphId.hashCode()
        result = 31 * result + visualData.contentHashCode()
        return result
    }
}

enum class GlowState {
    NONE, SUBTLE, DIM, FULL, DISCREET
}

// ============================================================================
// 2. PRESENCE CONTRACTS
// ============================================================================

data class PresenceState(
    val cognitive: CognitiveMode,
    val emotional: EmotionalTone,
    val intent: IntentVector,
    val socialContext: SocialContext,
    val bandwidth: BandwidthLevel,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable {
    fun matches(required: PresenceState): Boolean {
        return cognitive == required.cognitive &&
               emotional == required.emotional &&
               intent.intersects(required.intent) &&
               socialContext.ordinal >= required.socialContext.ordinal &&
               bandwidth.ordinal >= required.bandwidth.ordinal
    }
}

enum class CognitiveMode {
    DEEP_FOCUS,
    LIGHT_ATTENTION,
    DRIFTING,
    ABSORBING,
    HIGH_BANDWIDTH_CREATIVE,
    LOW_BANDWIDTH_RECOVERY
}

enum class EmotionalTone {
    GROUNDED,
    EXPANSIVE,
    EXPLORATORY,
    PROTECTIVE,
    RECEPTIVE,
    CHARGED,
    REFLECTIVE,
    CONNECTIVE
}

data class IntentVector(
    val openToCollaboration: Boolean = false,
    val openToListening: Boolean = false,
    val openToDebate: Boolean = false,
    val openToPlay: Boolean = false,
    val openToSilence: Boolean = false,
    val openToCoCreation: Boolean = false,
    val openToDeepWorkParallel: Boolean = false,
    val customIntents: Map<String, Boolean> = emptyMap()
) : Serializable {
    fun intersects(other: IntentVector): Boolean {
        return (openToCollaboration == other.openToCollaboration) &&
               (openToListening == other.openToListening) &&
               (openToDebate == other.openToDebate)
    }
}

enum class SocialContext {
    ALONE, WITH_ONE, SMALL_GROUP, PUBLIC
}

enum class BandwidthLevel {
    CRITICAL_LOW, LOW, MEDIUM, HIGH, MAXIMUM
}

data class PresenceSignature(
    val userId: UserId,
    val state: PresenceState,
    val lastUpdated: Long = System.currentTimeMillis(),
    val resonanceFrequency: Double = 0.0
) : Serializable

// ============================================================================
// 3. COMMUNICATION CONTRACTS
// ============================================================================

data class Message(
    val messageId: String,
    val senderId: UserId,
    val receiverId: UserId,
    val content: EncryptedContent,
    val contentType: MessageContentType = MessageContentType.TEXT,
    val deliveryProfile: DeliveryProfile,
    val presenceRequirements: PresenceState? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val expiryTime: Long? = null,
    val readAt: Long? = null,
    val mediaUrls: List<String> = emptyList()
) : Serializable

enum class MessageContentType {
    TEXT, IMAGE, VIDEO, AUDIO, FILE, GLYPH, MIXED
}

data class EncryptedContent(
    val ciphertext: ByteArray,
    val keyAlias: String,
    val nonce: ByteArray,
    val encryptedAt: Long = System.currentTimeMillis(),
    val algorithm: String = "AES/GCM"
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptedContent) return false
        return ciphertext.contentEquals(other.ciphertext) && keyAlias == other.keyAlias
    }

    override fun hashCode(): Int {
        var result = ciphertext.contentHashCode()
        result = 31 * result + keyAlias.hashCode()
        return result
    }
}

data class DeliveryProfile(
    val mode: DeliveryMode,
    val delayMs: Long = 0,
    val scheduledTime: Long? = null,
    val condition: String? = null,
    val quietMode: QuietMode? = null
) : Serializable

enum class DeliveryMode {
    IMMEDIATE, DELAYED, SCHEDULED, CONDITIONAL, PRESENCE_BOUND
}

data class QuietMode(
    val sound: Boolean = false,
    val tone: Boolean = false,
    val beep: Boolean = false,
    val vibration: Boolean = false,
    val silent: Boolean = true,
    val resonancePulse: Boolean = false,
    val delayedReveal: Long? = null
) : Serializable

data class SignalGlyph(
    val signalId: String,
    val senderId: UserId,
    val receiverId: UserId,
    val resonanceType: ResonanceType,
    val glyphData: PersonalGlyph,
    val hiddenMessage: EncryptedContent? = null,
    val presenceAdaptiveGlow: GlowState = GlowState.NONE,
    val timestamp: Long = System.currentTimeMillis(),
    val partnerSafeMode: Boolean = false
) : Serializable

enum class ResonanceType {
    URGENCY, CURIOSITY, FAVOR, EMOTIONAL_PRESENCE
}

data class CallRecord(
    val callId: String,
    val initiatorId: UserId,
    val recipientId: UserId,
    val callType: CallType,
    val startTime: Long,
    val endTime: Long? = null,
    val duration: Long = 0,
    val status: CallStatus = CallStatus.MISSED,
    val encryptionKey: String? = null
) : Serializable

enum class CallType {
    VOICE, VIDEO
}

enum class CallStatus {
    INCOMING, OUTGOING, RINGING, ACTIVE, MISSED, DECLINED, COMPLETED, FAILED
}

// ============================================================================
// 4. SECURITY CONTRACTS
// ============================================================================

data class SecurityKeys(
    val deviceKey: ByteArray,
    val presenceKey: ByteArray,
    val biometricKey: ByteArray,
    val keyShards: List<KeyShard> = emptyList(),
    val keyAlias: String = ""
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SecurityKeys) return false
        return deviceKey.contentEquals(other.deviceKey)
    }

    override fun hashCode(): Int {
        return deviceKey.contentHashCode()
    }
}

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
        return 31 * index + data.contentHashCode()
    }
}

enum class ShardRequirement {
    DEVICE_KEY, PRESENCE_KEY, BIOMETRIC_KEY, BREATH_UNLOCK, GESTURE_UNLOCK
}

data class AccessGrant(
    val grantId: String,
    val granteeId: UserId,
    val resources: List<String>,
    val expiresAt: Long,
    val revocable: Boolean = true,
    val presenceBound: PresenceState? = null,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

data class ViewOnlyMedia(
    val mediaId: String,
    val ownerId: UserId,
    val viewerId: UserId,
    val encryptedContent: EncryptedContent,
    val noExport: Boolean = true,
    val noScreenshot: Boolean = true,
    val noSave: Boolean = true,
    val noCopy: Boolean = true,
    val expiresAt: Long? = null,
    val accessedAt: Long? = null
) : Serializable

data class SovereignMediaProtocol(
    val multiKeyShard: KeyShard,
    val noServerDecryption: Boolean = true,
    val zeroMetadataLeakage: Boolean = true,
    val glyphLocked: Boolean = false,
    val messageOwnership: Boolean = true
) : Serializable

// ============================================================================
// 5. SPACE CONTRACTS
// ============================================================================

data class Room(
    val roomId: String,
    val name: String,
    val type: RoomType,
    val ownerId: UserId,
    val participants: List<UserId> = emptyList(),
    val securityProfile: SecurityProfile,
    val presenceRequirement: PresenceState? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val archivedAt: Long? = null
) : Serializable

enum class RoomType {
    STANDARD, BATCAVE, SECURE_DIGITAL, CEREMONIAL
}

data class SecurityProfile(
    val notificationsEnabled: Boolean = true,
    val loggingEnabled: Boolean = true,
    val exportAllowed: Boolean = true,
    val aiAccessible: Boolean = true,
    val screenshotAllowed: Boolean = true,
    val copyAllowed: Boolean = true,
    val presenceRequired: PresenceState? = null
) : Serializable

data class Batcave(
    val room: Room,
    val aiCompanions: List<String> = emptyList(),
    val customTools: List<String> = emptyList(),
    val sealedMode: Boolean = true,
    val infiniteCanvas: InfiniteCanvas? = null
) : Serializable

data class InfiniteCanvas(
    val canvasId: String,
    val zoom: Double = 1.0,
    val maxZoom: Double = 30000.0,
    val elements: Map<String, CanvasElement> = emptyMap(),
    val viewportX: Double = 0.0,
    val viewportY: Double = 0.0
) : Serializable

data class CanvasElement(
    val elementId: String,
    val type: ElementType,
    val x: Double,
    val y: Double,
    val z: Double,
    val data: ByteArray,
    val encrypted: Boolean = true
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CanvasElement) return false
        return elementId == other.elementId && data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        return 31 * elementId.hashCode() + data.contentHashCode()
    }
}

enum class ElementType {
    NOTE, FILE, IMAGE, GLYPH, MICROTHREAD, RESONANCE_PULSE
}

data class SecureDigitalRoom(
    val room: Room,
    val ephemeral: Boolean = true,
    val autoDeleteAfter: Long? = null
) : Serializable {
    companion object {
        fun create(userId: UserId, participantIds: List<UserId>): SecureDigitalRoom {
            return SecureDigitalRoom(
                room = Room(
                    roomId = "secure-${System.currentTimeMillis()}",
                    name = "Secure Room",
                    type = RoomType.SECURE_DIGITAL,
                    ownerId = userId,
                    participants = participantIds,
                    securityProfile = SecurityProfile(
                        notificationsEnabled = false,
                        loggingEnabled = false,
                        exportAllowed = false,
                        aiAccessible = false,
                        screenshotAllowed = false,
                        copyAllowed = false
                    )
                ),
                ephemeral = true,
                autoDeleteAfter = 24 * 60 * 60 * 1000  // 24 hours
            )
        }
    }
}

data class GlyphMicroContent(
    val glyphId: String,
    val ownerId: UserId,
    val zoomLevel: Double = 1.0,
    val content: List<EmbeddedContent> = emptyList(),
    val hasHiddenContent: Boolean = false,
    val resonanceGlow: GlowState = GlowState.NONE
) : Serializable

sealed class EmbeddedContent : Serializable {
    data class Note(
        val text: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : EmbeddedContent()

    data class File(
        val path: String,
        val mimeType: String = "",
        val encrypted: Boolean = true,
        val viewOnly: Boolean = true
    ) : EmbeddedContent()

    data class MessageRef(
        val messageId: String,
        val encryptedContent: EncryptedContent
    ) : EmbeddedContent()

    data class MicroThread(
        val messages: List<String>,
        val title: String = ""
    ) : EmbeddedContent()

    data class SymbolicPulse(
        val pulseId: String,
        val resonance: ResonanceType
    ) : EmbeddedContent()
}

// ============================================================================
// 6. RITUAL CONTRACTS
// ============================================================================

data class RitualEvent(
    val eventId: String,
    val type: RitualType,
    val triggeredBy: UserId,
    val targetId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, Any> = emptyMap()
) : Serializable

enum class RitualType {
    BREATH_UNLOCK,
    GESTURE_REVEAL,
    BACK_TAP_SIGNAL,
    WHISPER_COMMAND,
    CEREMONIAL_REQUEST,
    PRESENCE_SYNC,
    SYMBOLIC_PULSE,
    GLYPH_CREATION,
    ROOM_MANIFESTATION
}

data class CeremonialConnection(
    val connectionId: String,
    val initiatorId: UserId,
    val recipientId: UserId,
    val grantedResources: List<String>,
    val grantedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long,
    val revocable: Boolean = true,
    val presenceBound: PresenceState? = null
) : Serializable

data class PresenceContract(
    val contractId: String,
    val userId: UserId,
    val requiredPresence: PresenceState,
    val duration: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + duration
) : Serializable

data class SymbolicPulse(
    val pulseId: String,
    val senderId: UserId,
    val recipientId: UserId,
    val resonance: ResonanceType,
    val intensity: Double = 1.0,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

data class BreathUnlockRitual(
    val rituaId: String,
    val userId: UserId,
    val micSignature: ByteArray? = null,
    val cameraFogSignature: ByteArray? = null,
    val airflowSignature: ByteArray? = null,
    val confidence: Double = 0.0
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BreathUnlockRitual) return false
        return rituaId == other.rituaId
    }

    override fun hashCode(): Int {
        return rituaId.hashCode()
    }
}

data class GestureUnlockRitual(
    val ritualId: String,
    val userId: UserId,
    val gestureType: GestureType,
    val gestureData: ByteArray,
    val confidence: Double = 0.0
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GestureUnlockRitual) return false
        return ritualId == other.ritualId && gestureData.contentEquals(other.gestureData)
    }

    override fun hashCode(): Int {
        return 31 * ritualId.hashCode() + gestureData.contentHashCode()
    }
}

enum class GestureType {
    BACK_TAP_DOUBLE,
    BACK_TAP_TRIPLE,
    AIR_SWIPE,
    SYMBOL_TRACE,
    PROXIMITY_WAVE,
    TOUCH_PATTERN
}

// ============================================================================
// 7. HARDWARE & INTERACTION CONTRACTS
// ============================================================================

data class AdaptiveLensProtection(
    val userId: UserId,
    val blurMode: BlurMode = BlurMode.AMBIENT,
    val proximityAware: Boolean = true,
    val breathDetectionEnabled: Boolean = true,
    val unlockMethods: List<UnlockMethod> = listOf(UnlockMethod.BREATH_ACTIVATION),
    val presenceBoundReveal: Boolean = true,
    val decoyInterface: Boolean = false
) : Serializable

enum class BlurMode {
    NONE, AMBIENT, PROXIMITY_AWARE, FULL_ENCRYPT
}

enum class UnlockMethod {
    FINGER_GESTURE,
    BREATH_ACTIVATION,
    SOUND_CUE,
    OBJECT_RECOGNITION,
    PRESENCE_SIGNATURE
}

data class HardwareSensor(
    val sensorId: String,
    val type: SensorType,
    val enabled: Boolean = true,
    val data: ByteArray = byteArrayOf(),
    val timestamp: Long = System.currentTimeMillis()
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HardwareSensor) return false
        return sensorId == other.sensorId && data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        return 31 * sensorId.hashCode() + data.contentHashCode()
    }
}

enum class SensorType {
    BREATH_DETECTOR,
    BACK_TAP,
    UWB_TRACKING,
    VOICE_RECOGNITION,
    DOCUMENT_SCANNER,
    SOUND_RECOGNITION,
    BAROMETER,
    AMBIENT_LIGHT,
    MACRO_LENS,
    PROXIMITY_SENSOR
}

data class RadialMenuAction(
    val actionId: String,
    val label: String,
    val icon: String,
    val category: ActionCategory,
    val contextual: Boolean = false,
    val execute: suspend () -> Unit = {}
) : Serializable

enum class ActionCategory {
    PRESENCE,
    SYMBOLIC,
    COMMUNICATION,
    SPATIAL,
    RITUAL,
    HARDWARE,
    UTILITY
}

data class DragDropArtifact(
    val artifactId: String,
    val type: ArtifactType,
    val data: ByteArray,
    val metadata: Map<String, String> = emptyMap(),
    val encrypted: Boolean = true
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DragDropArtifact) return false
        return artifactId == other.artifactId && data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        return 31 * artifactId.hashCode() + data.contentHashCode()
    }
}

enum class ArtifactType {
    IMAGE, FILE, PHRASE, VOICE_NOTE, GLYPH, AI_AGENT, ROOM, PRESENCE
}

data class AppDisguise(
    val disguiseId: String,
    val userId: UserId,
    val activeDisguise: DisguiseType = DisguiseType.NONE,
    val unlockGesture: GestureType? = null,
    val hiddenAccessible: Boolean = false
) : Serializable

enum class DisguiseType {
    NONE, CALCULATOR, NOTES, SETTINGS, MESSAGING_APP
}
