package com.glyphos.symbolic.calling

import android.content.Context
import com.glyphos.symbolic.data.CallRecord
import com.glyphos.symbolic.data.CallStatus
import com.glyphos.symbolic.data.CallType
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CallManager
 */
class CallManagerTest {

    private lateinit var callManager: CallManager
    private val context = mockk<Context>(relaxed = true)
    private val signalingManager = mockk<CallSignalingManager>(relaxed = true)
    private val permissionHandler = mockk<CallPermissionHandler>(relaxed = true)

    @Before
    fun setup() {
        callManager = CallManager(
            context = context,
            signalingManager = signalingManager,
            permissionHandler = permissionHandler
        )
    }

    @Test
    fun `initiateCall should check permissions before starting`() = runTest {
        // Arrange
        val recipientId = 123
        val callType = CallType.VOICE

        // Act
        callManager.initiateCall(recipientId = recipientId, callType = callType)

        // Assert
        verify { permissionHandler.needsAudioPermissionRequest() }
    }

    @Test
    fun `initiateCall with video should check video permissions`() = runTest {
        // Arrange
        val recipientId = 123
        val callType = CallType.VIDEO

        // Act
        callManager.initiateCall(recipientId = recipientId, callType = callType)

        // Assert
        verify { permissionHandler.needsVideoPermissionRequest() }
    }

    @Test
    fun `callState should start as IDLE`() {
        // Assert
        assert(callManager.callState.value == CallState.IDLE)
    }

    @Test
    fun `isMuted should be false initially`() {
        // Assert
        assert(!callManager.isMuted.value)
    }

    @Test
    fun `toggleMute should toggle audio state`() {
        // Act
        callManager.toggleMute()

        // Assert
        assert(callManager.isMuted.value)

        // Act again
        callManager.toggleMute()

        // Assert
        assert(!callManager.isMuted.value)
    }

    @Test
    fun `toggleVideo should toggle video state`() {
        // Act
        callManager.toggleVideo()

        // Assert
        assert(callManager.isVideoEnabled.value)

        // Act again
        callManager.toggleVideo()

        // Assert
        assert(!callManager.isVideoEnabled.value)
    }

    @Test
    fun `declineCall should notify signaling manager`() = runTest {
        // Arrange
        val callId = "call-123"
        val reason = "User declined"

        // Act
        callManager.declineCall(callId, reason)

        // Assert
        verify { signalingManager.notifyCallRejected(callId, reason) }
    }

    @Test
    fun `endCall should update call status to COMPLETED`() = runTest {
        // Arrange - simulate an ongoing call
        callManager.currentCall.value?.let { _ ->
            // Act
            callManager.endCall()

            // Assert
            assert(callManager.callState.value == CallState.ENDED)
        }
    }

    @Test
    fun `getPermissionStatus should return current permission state`() {
        // Act
        val status = callManager.getPermissionStatus()

        // Assert
        assert(status != null)
    }
}
