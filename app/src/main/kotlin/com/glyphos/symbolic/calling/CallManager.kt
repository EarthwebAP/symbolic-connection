package com.glyphos.symbolic.calling

import android.content.Context
import com.glyphos.symbolic.data.CallRecord
import com.glyphos.symbolic.data.CallStatus
import com.glyphos.symbolic.data.CallType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages voice and video calls for Symbolic Connection
 */
@Singleton
class CallManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _currentCall = MutableStateFlow<CallRecord?>(null)
    val currentCall: StateFlow<CallRecord?> = _currentCall

    private val _callState = MutableStateFlow<CallState>(CallState.IDLE)
    val callState: StateFlow<CallState> = _callState

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted

    private val _isVideoEnabled = MutableStateFlow(true)
    val isVideoEnabled: StateFlow<Boolean> = _isVideoEnabled

    private val _isSpeakerEnabled = MutableStateFlow(true)
    val isSpeakerEnabled: StateFlow<Boolean> = _isSpeakerEnabled

    private var webRTCEngine: WebRTCEngine? = null

    fun initiateCall(
        callId: String,
        initiatorId: String,
        recipientId: String,
        callType: CallType
    ) {
        val call = CallRecord(
            callId = callId,
            initiatorId = initiatorId,
            recipientId = recipientId,
            callType = callType,
            startTime = System.currentTimeMillis(),
            status = CallStatus.OUTGOING
        )
        _currentCall.value = call
        _callState.value = CallState.INITIATING

        // TODO: Send call signal to recipient
    }

    fun acceptCall(callRecord: CallRecord) {
        _currentCall.value = callRecord.copy(status = CallStatus.COMPLETED)
        _callState.value = CallState.ACTIVE

        // Initialize WebRTC
        webRTCEngine = WebRTCEngine(context)
        webRTCEngine?.setupPeerConnection()

        // TODO: Send acceptance signal
    }

    fun declineCall() {
        _callState.value = CallState.ENDED
        _currentCall.value = _currentCall.value?.copy(status = CallStatus.DECLINED)
        cleanup()
    }

    fun endCall() {
        val endTime = System.currentTimeMillis()
        val call = _currentCall.value
        if (call != null) {
            val duration = endTime - call.startTime
            _currentCall.value = call.copy(
                endTime = endTime,
                duration = duration,
                status = CallStatus.COMPLETED
            )
        }
        _callState.value = CallState.ENDED
        cleanup()
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        webRTCEngine?.setAudioEnabled(!_isMuted.value)
    }

    fun toggleVideo() {
        _isVideoEnabled.value = !_isVideoEnabled.value
        webRTCEngine?.setVideoEnabled(_isVideoEnabled.value)
    }

    fun toggleSpeaker() {
        _isSpeakerEnabled.value = !_isSpeakerEnabled.value
        // TODO: Adjust audio routing
    }

    private fun cleanup() {
        webRTCEngine?.cleanup()
        webRTCEngine = null
    }
}

enum class CallState {
    IDLE,
    INITIATING,
    RINGING,
    ACTIVE,
    HELD,
    ENDED
}
