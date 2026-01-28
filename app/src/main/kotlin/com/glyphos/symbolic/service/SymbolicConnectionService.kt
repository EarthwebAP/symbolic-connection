package com.glyphos.symbolic.service

import android.content.Context
import com.glyphos.symbolic.calling.CallManager
import com.glyphos.symbolic.core.contracts.BandwidthLevel
import com.glyphos.symbolic.core.contracts.CognitiveMode
import com.glyphos.symbolic.core.contracts.EmotionalTone
import com.glyphos.symbolic.core.contracts.IntentVector
import com.glyphos.symbolic.core.contracts.PresenceState
import com.glyphos.symbolic.core.contracts.SocialContext
import com.glyphos.symbolic.core.contracts.UserId
import com.glyphos.symbolic.hardware.BackTapGestureManager
import com.glyphos.symbolic.hardware.BreathDetector
import com.glyphos.symbolic.hardware.EnvironmentalSensorsEngine
import com.glyphos.symbolic.hardware.GlyphScannerEngine
import com.glyphos.symbolic.hardware.UWBProximityTracker
import com.glyphos.symbolic.hardware.VoiceRoutineEngine
import com.glyphos.symbolic.interaction.DragDropManager
import com.glyphos.symbolic.security.EmergencySeal
import com.glyphos.symbolic.security.lens.GesturePatternUnlock
import com.glyphos.symbolic.identity.glyph.PrimordialZoomEngine
import com.glyphos.symbolic.integration.SettingsIntegration
import com.glyphos.symbolic.interaction.RadialMenuSystem
import com.glyphos.symbolic.presence.PresenceEngine
import com.glyphos.symbolic.rituals.RitualOrchestrator
import com.glyphos.symbolic.security.SovereignSecurityEngine
import com.glyphos.symbolic.security.disguise.AppDisguiseManager
import com.glyphos.symbolic.signals.SignalGlyphManager
import com.glyphos.symbolic.spaces.BatcaveRoomManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Symbolic Connection Service
 * Central orchestrator that initializes and manages all subsystems
 */
@Singleton
class SymbolicConnectionService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val presenceEngine: PresenceEngine,
    private val signalGlyphManager: SignalGlyphManager,
    private val primordialZoomEngine: PrimordialZoomEngine,
    private val sovereignSecurityEngine: SovereignSecurityEngine,
    private val breathDetector: BreathDetector,
    private val backTapGestureManager: BackTapGestureManager,
    private val glyphScannerEngine: GlyphScannerEngine,
    private val uwbProximityTracker: UWBProximityTracker,
    private val voiceRoutineEngine: VoiceRoutineEngine,
    private val environmentalSensorsEngine: EnvironmentalSensorsEngine,
    private val radialMenuSystem: RadialMenuSystem,
    private val dragDropManager: DragDropManager,
    private val batcaveRoomManager: BatcaveRoomManager,
    private val appDisguiseManager: AppDisguiseManager,
    private val emergencySeal: EmergencySeal,
    private val gesturePatternUnlock: GesturePatternUnlock,
    private val callManager: CallManager,
    private val settingsIntegration: SettingsIntegration,
    private val ritualOrchestrator: RitualOrchestrator
) {

    private var isInitialized = false
    private var currentUserId: UserId? = null

    fun initialize(userId: UserId) {
        if (isInitialized) return

        currentUserId = userId

        // Initialize presence engine with default state
        val defaultPresence = PresenceState(
            cognitive = CognitiveMode.LIGHT_ATTENTION,
            emotional = EmotionalTone.RECEPTIVE,
            intent = IntentVector(
                openToCollaboration = true,
                openToListening = true,
                openToSilence = true
            ),
            socialContext = SocialContext.ALONE,
            bandwidth = BandwidthLevel.MEDIUM
        )
        presenceEngine.setPresenceState(userId, defaultPresence)

        // Initialize security
        sovereignSecurityEngine.generateSecurityKeys(userId)

        // Initialize batcave
        batcaveRoomManager.createBatcave(userId)

        // Start hardware detection
        breathDetector.startBreathDetection()
        backTapGestureManager.startListening()

        // Initialize radial menu
        radialMenuSystem.openMenu(userId)

        isInitialized = true
    }

    fun shutdown() {
        if (!isInitialized) return

        breathDetector.stopBreathDetection()
        backTapGestureManager.stopListening()

        isInitialized = false
    }

    fun getCurrentUserId(): UserId? = currentUserId

    fun isInitialized(): Boolean = isInitialized

    // Presence management
    fun setPresenceState(state: PresenceState) {
        currentUserId?.let { presenceEngine.setPresenceState(it, state) }
    }

    fun shiftCognitiveMode(mode: CognitiveMode) {
        currentUserId?.let { presenceEngine.shiftCognitiveMode(it, mode) }
    }

    fun enterBatcave() {
        currentUserId?.let { batcaveRoomManager.enterBatcave(it) }
    }

    fun exitBatcave() {
        batcaveRoomManager.exitBatcave()
    }

    fun isBatcaveActive(): Boolean {
        return batcaveRoomManager.isInSealedMode()
    }

    // Signal system
    fun getSignalGlyphManager(): SignalGlyphManager = signalGlyphManager

    // Hardware
    fun getBreathDetector(): BreathDetector = breathDetector
    fun getBackTapGestureManager(): BackTapGestureManager = backTapGestureManager
    fun getGlyphScanner(): GlyphScannerEngine = glyphScannerEngine
    fun getUWBProximityTracker(): UWBProximityTracker = uwbProximityTracker
    fun getVoiceRoutineEngine(): VoiceRoutineEngine = voiceRoutineEngine
    fun getEnvironmentalSensorsEngine(): EnvironmentalSensorsEngine = environmentalSensorsEngine

    // Zoom
    fun getPrimordialZoomEngine(): PrimordialZoomEngine = primordialZoomEngine

    // Security
    fun getSovereignSecurityEngine(): SovereignSecurityEngine = sovereignSecurityEngine
    fun getAppDisguiseManager(): AppDisguiseManager = appDisguiseManager
    fun getEmergencySeal(): EmergencySeal = emergencySeal
    fun getGesturePatternUnlock(): GesturePatternUnlock = gesturePatternUnlock

    // Interaction
    fun getRadialMenuSystem(): RadialMenuSystem = radialMenuSystem
    fun getDragDropManager(): DragDropManager = dragDropManager

    // Rooms
    fun getBatcaveManager(): BatcaveRoomManager = batcaveRoomManager

    // Calling
    fun getCallManager(): CallManager = callManager

    // Settings
    fun getSettingsIntegration(): SettingsIntegration = settingsIntegration

    // Rituals
    fun getRitualOrchestrator(): RitualOrchestrator = ritualOrchestrator
}
