package com.glyphos.symbolic.hardware

import android.content.Context
import android.media.projection.MediaProjectionManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.UUID

/**
 * PHASE 5: Screen Recording Manager
 *
 * MediaProjection API for screen recording detection and management.
 * - Recording state detection
 * - Internal screen recording prevention
 * - Notification of recording attempts
 * - Recording session management
 */
class ScreenRecordingManager(context: Context) {
    companion object {
        private const val TAG = "ScreenRecordingManager"
    }

    private val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingSessions = MutableStateFlow<List<RecordingSession>>(emptyList())
    val recordingSessions: StateFlow<List<RecordingSession>> = _recordingSessions.asStateFlow()

    private val _recordingAttempts = MutableStateFlow<List<RecordingAttempt>>(emptyList())
    val recordingAttempts: StateFlow<List<RecordingAttempt>> = _recordingAttempts.asStateFlow()

    data class RecordingSession(
        val id: String = UUID.randomUUID().toString(),
        val startTime: Long = System.currentTimeMillis(),
        var endTime: Long? = null,
        val outputPath: String? = null,
        val isInternal: Boolean = false // Whether initiated internally
    ) {
        fun duration(): Long = (endTime ?: System.currentTimeMillis()) - startTime
    }

    data class RecordingAttempt(
        val id: String = UUID.randomUUID().toString(),
        val timestamp: Long = System.currentTimeMillis(),
        val source: String, // "SYSTEM", "EXTERNAL", "UNKNOWN"
        val isBlocked: Boolean = false,
        val reason: String? = null
    )

    fun startInternalRecording(outputPath: String): RecordingSession? {
        val session = RecordingSession(
            outputPath = outputPath,
            isInternal = true
        )

        _recordingSessions.value = _recordingSessions.value + session
        _isRecording.value = true

        Log.d(TAG, "Internal recording started: ${session.id}")
        return session
    }

    fun stopRecording(sessionId: String) {
        val session = _recordingSessions.value.find { it.id == sessionId } ?: return

        session.endTime = System.currentTimeMillis()

        _recordingSessions.value = _recordingSessions.value.map {
            if (it.id == sessionId) it.copy(endTime = System.currentTimeMillis()) else it
        }

        _isRecording.value = false

        Log.d(TAG, "Recording stopped: $sessionId (duration: ${session.duration()}ms)")
    }

    fun detectExternalRecording(): Boolean {
        // In real implementation, monitor for MediaProjection without permission
        // For now, return false
        return false
    }

    fun notifyRecordingAttempt(source: String = "UNKNOWN", blocked: Boolean = false) {
        val attempt = RecordingAttempt(
            source = source,
            isBlocked = blocked,
            reason = if (blocked) "Recording blocked by secure content" else null
        )

        _recordingAttempts.value = _recordingAttempts.value + attempt

        Log.w(TAG, "Recording attempt detected: $source (blocked: $blocked)")
    }

    fun getSessionDuration(sessionId: String): Long? {
        return _recordingSessions.value.find { it.id == sessionId }?.duration()
    }

    fun getRecordingStatistics(): ScreenRecordingStatistics {
        val sessions = _recordingSessions.value
        return ScreenRecordingStatistics(
            totalSessions = sessions.size,
            internalCount = sessions.count { it.isInternal },
            totalDuration = sessions.sumOf { it.duration() },
            externalAttempts = _recordingAttempts.value.size,
            blockedAttempts = _recordingAttempts.value.count { it.isBlocked }
        )
    }

    fun getStatus(): String {
        val stats = getRecordingStatistics()
        return """
        Screen Recording Status:
        - Currently recording: ${_isRecording.value}
        - Total sessions: ${stats.totalSessions}
        - Internal: ${stats.internalCount}
        - External attempts: ${stats.externalAttempts}
        - Blocked: ${stats.blockedAttempts}
        - Total duration: ${stats.totalDuration}ms
        """.trimIndent()
    }
}

data class ScreenRecordingStatistics(
    val totalSessions: Int,
    val internalCount: Int,
    val totalDuration: Long,
    val externalAttempts: Int,
    val blockedAttempts: Int
)
