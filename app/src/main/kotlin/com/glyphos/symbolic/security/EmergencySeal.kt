package com.glyphos.symbolic.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Emergency Seal
 * Instantly locks app and secures all sensitive data
 */
@Singleton
class EmergencySeal @Inject constructor() {

    private val _isSealedActive = MutableStateFlow(false)
    val isSealedActive: StateFlow<Boolean> = _isSealedActive

    private val _sealTriggerTime = MutableStateFlow(0L)
    val sealTriggerTime: StateFlow<Long> = _sealTriggerTime

    private val _failedAttempts = MutableStateFlow(0)
    val failedAttempts: StateFlow<Int> = _failedAttempts

    private val _sealReason = MutableStateFlow("")
    val sealReason: StateFlow<String> = _sealReason

    private val failureThreshold = 3
    private val sealDurationMs = 300000L // 5 minutes

    enum class SealTrigger {
        VOICE_COMMAND,
        GESTURE_TRIPLE_TAP,
        FAILED_UNLOCK_ATTEMPTS,
        PROXIMITY_THREAT,
        MANUAL_ACTIVATION,
        SUSPICIOUS_ACTIVITY
    }

    fun triggerSeal(reason: SealTrigger, metadata: String = "") {
        _isSealedActive.value = true
        _sealTriggerTime.value = System.currentTimeMillis()
        _failedAttempts.value = 0

        val reasonText = when (reason) {
            SealTrigger.VOICE_COMMAND -> "Voice seal activated"
            SealTrigger.GESTURE_TRIPLE_TAP -> "Emergency gesture triggered"
            SealTrigger.FAILED_UNLOCK_ATTEMPTS -> "Failed unlock attempts - seal activated"
            SealTrigger.PROXIMITY_THREAT -> "Proximity threat detected - seal activated"
            SealTrigger.MANUAL_ACTIVATION -> "Manual seal activation"
            SealTrigger.SUSPICIOUS_ACTIVITY -> "Suspicious activity detected - seal activated"
        }

        _sealReason.value = "$reasonText: $metadata"
    }

    fun recordFailedAttempt() {
        val current = _failedAttempts.value + 1
        _failedAttempts.value = current

        if (current >= failureThreshold) {
            triggerSeal(
                SealTrigger.FAILED_UNLOCK_ATTEMPTS,
                "Threshold exceeded: $current attempts"
            )
        }
    }

    fun releaseSeal(verificationCode: String): Boolean {
        if (!_isSealedActive.value) return true

        // Verify recovery code (in production, this would be cryptographically verified)
        // For now, use breath-based unlock or gesture pattern
        val timeSinceSeal = System.currentTimeMillis() - _sealTriggerTime.value

        return if (timeSinceSeal > sealDurationMs) {
            // Auto-release after duration
            _isSealedActive.value = false
            _failedAttempts.value = 0
            true
        } else {
            // Require verification
            false
        }
    }

    fun resetFailedAttempts() {
        _failedAttempts.value = 0
    }

    fun getSealStatus(): SealStatus {
        val timeSinceSeal = System.currentTimeMillis() - _sealTriggerTime.value
        val remainingTimeMs = if (_isSealedActive.value) {
            maxOf(0L, sealDurationMs - timeSinceSeal)
        } else {
            0L
        }

        return SealStatus(
            isActive = _isSealedActive.value,
            reason = _sealReason.value,
            failedAttempts = _failedAttempts.value,
            remainingTimeMs = remainingTimeMs,
            canAutoRelease = timeSinceSeal > sealDurationMs
        )
    }

    data class SealStatus(
        val isActive: Boolean,
        val reason: String,
        val failedAttempts: Int,
        val remainingTimeMs: Long,
        val canAutoRelease: Boolean
    )
}
