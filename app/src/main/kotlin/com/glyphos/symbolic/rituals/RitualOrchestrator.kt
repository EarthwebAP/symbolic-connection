package com.glyphos.symbolic.rituals

import android.util.Log
import com.glyphos.symbolic.core.models.PresenceState
import com.glyphos.symbolic.security.lens.BreathUnlock
import com.glyphos.symbolic.hardware.SoundRecognitionEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PHASE 7: Ritual Orchestrator
 *
 * Central coordinator for all ritual systems.
 * - Ritual lifecycle management
 * - Cross-ritual coordination
 * - Ritual state synchronization
 * - Symbolic behavior triggering
 */
class RitualOrchestrator(
    private val breathUnlock: BreathUnlock,
    private val soundEngine: SoundRecognitionEngine
) {
    companion object {
        private const val TAG = "RitualOrchestrator"
    }

    // Initialize all ritual systems
    val breathRevealRitual = BreathRevealRitual(breathUnlock)
    val whisperUnlockRitual = WhisperUnlockRitual(soundEngine)
    val objectAccessRitual = ObjectBasedAccessRitual()
    val presenceContractSystem = PresenceContractSystem()
    val symbolicPulseSystem = SymbolicPulseSystem()

    private val _currentRitual = MutableStateFlow<ActiveRitual?>(null)
    val currentRitual: StateFlow<ActiveRitual?> = _currentRitual.asStateFlow()

    private val _ritualHistory = MutableStateFlow<List<RitualExecution>>(emptyList())
    val ritualHistory: StateFlow<List<RitualExecution>> = _ritualHistory.asStateFlow()

    private val _orchestrationMode = MutableStateFlow<OrchestrationMode>(OrchestrationMode.IDLE)
    val orchestrationMode: StateFlow<OrchestrationMode> = _orchestrationMode.asStateFlow()

    enum class RitualType {
        BREATH_REVEAL,
        WHISPER_UNLOCK,
        OBJECT_ACCESS,
        PRESENCE_CONTRACT,
        SYMBOLIC_PULSE
    }

    enum class OrchestrationMode {
        IDLE,
        BREATH_RITUAL,
        WHISPER_RITUAL,
        OBJECT_RITUAL,
        CONTRACT_ACTIVATION,
        PULSE_BROADCAST,
        MULTI_RITUAL
    }

    data class ActiveRitual(
        val type: RitualType,
        val startTime: Long = System.currentTimeMillis(),
        var endTime: Long? = null,
        val status: RitualStatus = RitualStatus.ACTIVE
    )

    enum class RitualStatus {
        ACTIVE, PAUSED, COMPLETED, FAILED
    }

    data class RitualExecution(
        val ritualType: RitualType,
        val startTime: Long,
        val endTime: Long,
        val successful: Boolean,
        val details: String? = null
    )

    suspend fun startBreathRevealRitual(): Boolean {
        _orchestrationMode.value = OrchestrationMode.BREATH_RITUAL
        _currentRitual.value = ActiveRitual(RitualType.BREATH_REVEAL)

        return try {
            val success = breathRevealRitual.startBreathDetection()

            if (success) {
                Log.d(TAG, "Breath-Reveal ritual started successfully")
            } else {
                _orchestrationMode.value = OrchestrationMode.IDLE
                Log.w(TAG, "Breath-Reveal ritual failed")
            }

            success
        } catch (e: Exception) {
            Log.e(TAG, "Error starting Breath-Reveal ritual", e)
            _orchestrationMode.value = OrchestrationMode.IDLE
            false
        }
    }

    suspend fun startWhisperUnlockRitual(resourceId: String): Boolean {
        _orchestrationMode.value = OrchestrationMode.WHISPER_RITUAL
        _currentRitual.value = ActiveRitual(RitualType.WHISPER_UNLOCK)

        return try {
            val whisper = whisperUnlockRitual.listenForWhisper()
            if (whisper != null) {
                val unlocked = whisperUnlockRitual.unlockResource(resourceId, whisper.id)
                Log.d(TAG, "Whisper-Unlock ritual: ${if (unlocked) "SUCCESS" else "FAILED"}")
                unlocked
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting Whisper-Unlock ritual", e)
            false
        } finally {
            _orchestrationMode.value = OrchestrationMode.IDLE
        }
    }

    suspend fun startObjectAccessRitual(resourceId: String): Boolean {
        _orchestrationMode.value = OrchestrationMode.OBJECT_RITUAL
        _currentRitual.value = ActiveRitual(RitualType.OBJECT_ACCESS)

        return try {
            val detectedObject = objectAccessRitual.scanForObject()
            if (detectedObject != null) {
                val granted = objectAccessRitual.verifyAndGrantAccess(resourceId, detectedObject.id)
                Log.d(TAG, "Object-Access ritual: ${if (granted) "SUCCESS" else "FAILED"}")
                granted
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting Object-Access ritual", e)
            false
        } finally {
            _orchestrationMode.value = OrchestrationMode.IDLE
        }
    }

    suspend fun activatePresenceContract(
        contractId: String,
        currentPresence: PresenceState
    ): Boolean {
        _orchestrationMode.value = OrchestrationMode.CONTRACT_ACTIVATION
        _currentRitual.value = ActiveRitual(RitualType.PRESENCE_CONTRACT)

        return try {
            val activated = presenceContractSystem.activateContractOnPresence(contractId, currentPresence)
            Log.d(TAG, "Presence Contract: ${if (activated) "ACTIVATED" else "DENIED"}")
            activated
        } catch (e: Exception) {
            Log.e(TAG, "Error activating Presence Contract", e)
            false
        } finally {
            _orchestrationMode.value = OrchestrationMode.IDLE
        }
    }

    suspend fun broadcastSymbolicPulse(
        senderId: String,
        pulseType: SymbolicPulseSystem.PulseType,
        energy: Float = 0.5f
    ) {
        _orchestrationMode.value = OrchestrationMode.PULSE_BROADCAST
        _currentRitual.value = ActiveRitual(RitualType.SYMBOLIC_PULSE)

        try {
            symbolicPulseSystem.emitPulse(
                senderId = senderId,
                pulseType = pulseType,
                energy = energy
            )
            Log.d(TAG, "Symbolic Pulse broadcast: ${pulseType.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error broadcasting Symbolic Pulse", e)
        } finally {
            _orchestrationMode.value = OrchestrationMode.IDLE
        }
    }

    suspend fun endCurrentRitual() {
        val ritual = _currentRitual.value ?: return

        _currentRitual.value = ritual.copy(
            endTime = System.currentTimeMillis(),
            status = RitualStatus.COMPLETED
        )

        _ritualHistory.value = _ritualHistory.value + RitualExecution(
            ritualType = ritual.type,
            startTime = ritual.startTime,
            endTime = ritual.endTime ?: System.currentTimeMillis(),
            successful = true
        )

        _orchestrationMode.value = OrchestrationMode.IDLE
        Log.d(TAG, "Ritual completed: ${ritual.type.name}")
    }

    fun getStatus(): String {
        val stats = getStatistics()
        return """
        Ritual Orchestrator Status:
        - Mode: ${_orchestrationMode.value.name}
        - Active ritual: ${_currentRitual.value?.type?.name ?: "None"}
        - Total executions: ${stats.totalRituals}
        - Success rate: ${String.format("%.1f", stats.successRate * 100)}%
        - Breath reveals: ${stats.breathRevealCount}
        - Whisper unlocks: ${stats.whisperUnlockCount}
        - Object accesses: ${stats.objectAccessCount}
        - Contracts: ${stats.activeContracts}
        - Pulses: ${stats.totalPulses}
        """.trimIndent()
    }

    private fun getStatistics(): OrchestratorStatistics {
        return OrchestratorStatistics(
            totalRituals = _ritualHistory.value.size,
            successRate = if (_ritualHistory.value.isNotEmpty()) {
                _ritualHistory.value.count { it.successful }.toFloat() / _ritualHistory.value.size
            } else 0f,
            breathRevealCount = breathRevealRitual.getStatistics().totalSessions,
            whisperUnlockCount = whisperUnlockRitual.getStatistics().unlockSessions,
            objectAccessCount = objectAccessRitual.getStatistics().accessSessions,
            activeContracts = presenceContractSystem.getStatistics().activeContracts,
            totalPulses = symbolicPulseSystem.getStatistics().activePulses
        )
    }
}

data class OrchestratorStatistics(
    val totalRituals: Int,
    val successRate: Float,
    val breathRevealCount: Int,
    val whisperUnlockCount: Int,
    val objectAccessCount: Int,
    val activeContracts: Int,
    val totalPulses: Int
)
