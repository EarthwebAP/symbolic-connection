package com.glyphos.symbolic.rituals

import android.util.Log
import com.glyphos.symbolic.core.models.PresenceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * PHASE 7: Symbolic Pulse System
 *
 * Non-verbal presence signals through symbolic pulses.
 * - Heartbeat-like presence pulses
 * - Energy/resonance broadcast
 * - Presence announcement
 * - Network-wide symbolic communication
 */
class SymbolicPulseSystem {
    companion object {
        private const val TAG = "SymbolicPulseSystem"
    }

    private val _pulses = MutableStateFlow<List<SymbolicPulse>>(emptyList())
    val pulses: StateFlow<List<SymbolicPulse>> = _pulses.asStateFlow()

    private val _receivedPulses = MutableStateFlow<List<SymbolicPulse>>(emptyList())
    val receivedPulses: StateFlow<List<SymbolicPulse>> = _receivedPulses.asStateFlow()

    private val _currentResonance = MutableStateFlow(0f)
    val currentResonance: StateFlow<Float> = _currentResonance.asStateFlow()

    private val _activeChannels = MutableStateFlow<List<String>>(emptyList())
    val activeChannels: StateFlow<List<String>> = _activeChannels.asStateFlow()

    enum class PulseType {
        HEARTBEAT,       // Regular presence pulse
        ENERGY_SURGE,    // High-energy signal
        RESONANCE_CALL,  // Calling for resonance response
        PRESENCE_ECHO,   // Echo of another's pulse
        HARMONIC_TONE,   // Harmonious alignment
        ALARM_PULSE      // Emergency/urgent signal
    }

    data class SymbolicPulse(
        val id: String = UUID.randomUUID().toString(),
        val senderId: String,
        val pulseType: PulseType,
        val timestamp: Long = System.currentTimeMillis(),
        val energy: Float = 0.5f, // 0-1 scale
        val presenceState: PresenceState? = null,
        val targetAudience: List<String> = emptyList(), // Empty = broadcast
        val ttl: Long = 10000L // Time to live in milliseconds
    ) {
        fun isExpired(): Boolean {
            return System.currentTimeMillis() > timestamp + ttl
        }

        fun getCurrentEnergy(): Float {
            val age = System.currentTimeMillis() - timestamp
            val decay = (age.toFloat() / ttl.toFloat()).coerceIn(0f, 1f)
            return energy * (1f - decay)
        }
    }

    suspend fun emitPulse(
        senderId: String,
        pulseType: PulseType,
        energy: Float = 0.5f,
        presenceState: PresenceState? = null,
        targetAudience: List<String> = emptyList()
    ): SymbolicPulse {
        val pulse = SymbolicPulse(
            senderId = senderId,
            pulseType = pulseType,
            energy = energy.coerceIn(0f, 1f),
            presenceState = presenceState,
            targetAudience = targetAudience
        )

        _pulses.value = _pulses.value + pulse

        // Update resonance based on pulse
        updateResonance(energy)

        Log.d(TAG, "Pulse emitted: ${pulseType.name} (energy: $energy)")
        return pulse
    }

    suspend fun receivePulse(pulse: SymbolicPulse) {
        if (pulse.isExpired()) {
            Log.d(TAG, "Pulse expired: ${pulse.id}")
            return
        }

        // Check if pulse is targeted at us
        val isTargeted = pulse.targetAudience.isEmpty() || pulse.targetAudience.contains("broadcast")

        if (isTargeted) {
            _receivedPulses.value = _receivedPulses.value + pulse

            // Update resonance from received pulse
            updateResonance(pulse.getCurrentEnergy())

            Log.d(TAG, "Pulse received from ${pulse.senderId}: ${pulse.pulseType.name}")

            // Auto-respond to resonance calls
            if (pulse.pulseType == PulseType.RESONANCE_CALL) {
                emitPulse(
                    senderId = "self",
                    pulseType = PulseType.PRESENCE_ECHO,
                    energy = pulse.getCurrentEnergy() * 0.8f,
                    targetAudience = listOf(pulse.senderId)
                )
            }
        }
    }

    private fun updateResonance(energy: Float) {
        // Simple resonance calculation: average of recent pulses
        val recentPulses = (_pulses.value + _receivedPulses.value)
            .filter { !it.isExpired() }
            .takeLast(10)

        if (recentPulses.isNotEmpty()) {
            _currentResonance.value = recentPulses.map { it.getCurrentEnergy() }.average().toFloat()
        }
    }

    fun openChannel(channelId: String) {
        _activeChannels.value = _activeChannels.value + channelId
        Log.d(TAG, "Opened channel: $channelId")
    }

    fun closeChannel(channelId: String) {
        _activeChannels.value = _activeChannels.value.filter { it != channelId }
        Log.d(TAG, "Closed channel: $channelId")
    }

    fun getActivePulses(): List<SymbolicPulse> {
        return (_pulses.value + _receivedPulses.value)
            .filter { !it.isExpired() }
            .sortedByDescending { it.timestamp }
    }

    fun getPulsesByType(type: PulseType): List<SymbolicPulse> {
        return getActivePulses().filter { it.pulseType == type }
    }

    fun getHighEnergyPulses(threshold: Float = 0.7f): List<SymbolicPulse> {
        return getActivePulses().filter { it.getCurrentEnergy() > threshold }
    }

    fun cleanupExpiredPulses() {
        _pulses.value = _pulses.value.filter { !it.isExpired() }
        _receivedPulses.value = _receivedPulses.value.filter { !it.isExpired() }

        Log.d(TAG, "Expired pulses cleaned up")
    }

    fun getStatistics(): SymbolicPulseStatistics {
        val allPulses = _pulses.value + _receivedPulses.value
        return SymbolicPulseStatistics(
            totalPulsesEmitted = _pulses.value.size,
            totalPulsesReceived = _receivedPulses.value.size,
            activePulses = getActivePulses().size,
            currentResonance = _currentResonance.value,
            highEnergyPulses = getHighEnergyPulses().size,
            activeChannels = _activeChannels.value.size
        )
    }

    fun getStatus(): String {
        val stats = getStatistics()
        return """
        Symbolic Pulse System Status:
        - Emitted: ${stats.totalPulsesEmitted}
        - Received: ${stats.totalPulsesReceived}
        - Active: ${stats.activePulses}
        - Current resonance: ${String.format("%.2f", stats.currentResonance)}
        - High energy: ${stats.highEnergyPulses}
        - Channels: ${stats.activeChannels}
        """.trimIndent()
    }
}

data class SymbolicPulseStatistics(
    val totalPulsesEmitted: Int,
    val totalPulsesReceived: Int,
    val activePulses: Int,
    val currentResonance: Float,
    val highEnergyPulses: Int,
    val activeChannels: Int
)
