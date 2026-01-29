package com.glyphos.symbolic.di

import android.content.Context
import com.glyphos.symbolic.calling.CallManager
import com.glyphos.symbolic.data.ContactRepository
import com.glyphos.symbolic.data.FirebaseMessagingService
import com.glyphos.symbolic.data.MessageRepository
import com.glyphos.symbolic.hardware.BackTapGestureManager
import com.glyphos.symbolic.hardware.BreathDetector
import com.glyphos.symbolic.hardware.EnvironmentalSensorsEngine
import com.glyphos.symbolic.hardware.GlyphScannerEngine
import com.glyphos.symbolic.hardware.UWBProximityTracker
import com.glyphos.symbolic.hardware.VoiceRoutineEngine
import com.glyphos.symbolic.identity.glyph.PrimordialZoomEngine
import com.glyphos.symbolic.integration.SettingsIntegration
import com.glyphos.symbolic.interaction.DragDropManager
import com.glyphos.symbolic.interaction.RadialMenuSystem
import com.glyphos.symbolic.rituals.RitualOrchestrator
import com.glyphos.symbolic.security.EmergencySeal
import com.glyphos.symbolic.security.lens.GesturePatternUnlock
import com.glyphos.symbolic.presence.PresenceEngine
import com.glyphos.symbolic.security.SovereignSecurityEngine
import com.glyphos.symbolic.security.disguise.AppDisguiseManager
import com.glyphos.symbolic.service.SymbolicConnectionService
import com.glyphos.symbolic.signals.SignalGlyphManager
import com.glyphos.symbolic.spaces.BatcaveRoomManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }

    @Singleton
    @Provides
    fun provideFirebaseMessagingService(firestore: FirebaseFirestore): FirebaseMessagingService {
        return FirebaseMessagingService(firestore)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideMessageRepository(): MessageRepository {
        return MessageRepository()
    }

    @Singleton
    @Provides
    fun provideContactRepository(): ContactRepository {
        return ContactRepository()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object PresenceModule {

    @Singleton
    @Provides
    fun providePresenceEngine(): PresenceEngine {
        return PresenceEngine()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object SignalModule {

    @Singleton
    @Provides
    fun provideSignalGlyphManager(presenceEngine: PresenceEngine): SignalGlyphManager {
        return SignalGlyphManager(presenceEngine)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object GlyphModule {

    @Singleton
    @Provides
    fun providePrimordialZoomEngine(): PrimordialZoomEngine {
        return PrimordialZoomEngine()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Singleton
    @Provides
    fun provideSovereignSecurityEngine(
        @ApplicationContext context: Context
    ): SovereignSecurityEngine {
        return SovereignSecurityEngine(context)
    }

    @Singleton
    @Provides
    fun provideAppDisguiseManager(
        @ApplicationContext context: Context
    ): AppDisguiseManager {
        return AppDisguiseManager(context)
    }

    @Singleton
    @Provides
    fun provideEmergencySeal(): EmergencySeal {
        return EmergencySeal()
    }

    @Singleton
    @Provides
    fun provideGesturePatternUnlock(): GesturePatternUnlock {
        return GesturePatternUnlock()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object HardwareModule {

    @Singleton
    @Provides
    fun provideBreathDetector(
        @ApplicationContext context: Context
    ): BreathDetector {
        return BreathDetector(context)
    }

    @Singleton
    @Provides
    fun provideBackTapGestureManager(
        @ApplicationContext context: Context
    ): BackTapGestureManager {
        return BackTapGestureManager(context)
    }

    @Singleton
    @Provides
    fun provideGlyphScannerEngine(
        @ApplicationContext context: Context
    ): GlyphScannerEngine {
        return GlyphScannerEngine(context)
    }

    @Singleton
    @Provides
    fun provideUWBProximityTracker(
        @ApplicationContext context: Context
    ): UWBProximityTracker {
        return UWBProximityTracker(context)
    }

    @Singleton
    @Provides
    fun provideVoiceRoutineEngine(
        @ApplicationContext context: Context
    ): VoiceRoutineEngine {
        return VoiceRoutineEngine(context)
    }

    @Singleton
    @Provides
    fun provideEnvironmentalSensorsEngine(
        @ApplicationContext context: Context
    ): EnvironmentalSensorsEngine {
        return EnvironmentalSensorsEngine(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object InteractionModule {

    @Singleton
    @Provides
    fun provideRadialMenuSystem(presenceEngine: PresenceEngine): RadialMenuSystem {
        return RadialMenuSystem(presenceEngine)
    }

    @Singleton
    @Provides
    fun provideDragDropManager(): DragDropManager {
        return DragDropManager()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RitualModule {

    @Singleton
    @Provides
    fun provideRitualOrchestrator(
        presenceEngine: PresenceEngine
    ): RitualOrchestrator {
        return RitualOrchestrator(presenceEngine)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object SpaceModule {

    @Singleton
    @Provides
    fun provideBatcaveRoomManager(): BatcaveRoomManager {
        return BatcaveRoomManager()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object CallingModule {

    @Singleton
    @Provides
    fun provideCallManager(
        @ApplicationContext context: Context
    ): CallManager {
        return CallManager(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object IntegrationModule {

    @Singleton
    @Provides
    fun provideSettingsIntegration(
        @ApplicationContext context: Context
    ): SettingsIntegration {
        return SettingsIntegration(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Singleton
    @Provides
    fun provideSymbolicConnectionService(
        @ApplicationContext context: Context,
        presenceEngine: PresenceEngine,
        signalGlyphManager: SignalGlyphManager,
        primordialZoomEngine: PrimordialZoomEngine,
        sovereignSecurityEngine: SovereignSecurityEngine,
        breathDetector: BreathDetector,
        backTapGestureManager: BackTapGestureManager,
        glyphScannerEngine: GlyphScannerEngine,
        uwbProximityTracker: UWBProximityTracker,
        voiceRoutineEngine: VoiceRoutineEngine,
        environmentalSensorsEngine: EnvironmentalSensorsEngine,
        radialMenuSystem: RadialMenuSystem,
        dragDropManager: DragDropManager,
        batcaveRoomManager: BatcaveRoomManager,
        appDisguiseManager: AppDisguiseManager,
        emergencySeal: EmergencySeal,
        gesturePatternUnlock: GesturePatternUnlock,
        callManager: CallManager,
        settingsIntegration: SettingsIntegration,
        ritualOrchestrator: RitualOrchestrator
    ): SymbolicConnectionService {
        return SymbolicConnectionService(
            context = context,
            presenceEngine = presenceEngine,
            signalGlyphManager = signalGlyphManager,
            primordialZoomEngine = primordialZoomEngine,
            sovereignSecurityEngine = sovereignSecurityEngine,
            breathDetector = breathDetector,
            backTapGestureManager = backTapGestureManager,
            glyphScannerEngine = glyphScannerEngine,
            uwbProximityTracker = uwbProximityTracker,
            voiceRoutineEngine = voiceRoutineEngine,
            environmentalSensorsEngine = environmentalSensorsEngine,
            radialMenuSystem = radialMenuSystem,
            dragDropManager = dragDropManager,
            batcaveRoomManager = batcaveRoomManager,
            appDisguiseManager = appDisguiseManager,
            emergencySeal = emergencySeal,
            gesturePatternUnlock = gesturePatternUnlock,
            callManager = callManager,
            settingsIntegration = settingsIntegration,
            ritualOrchestrator = ritualOrchestrator
        )
    }
}
