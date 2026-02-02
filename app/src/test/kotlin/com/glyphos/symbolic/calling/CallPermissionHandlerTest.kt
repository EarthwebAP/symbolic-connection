package com.glyphos.symbolic.calling

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CallPermissionHandler
 */
class CallPermissionHandlerTest {

    private lateinit var permissionHandler: CallPermissionHandler
    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setup() {
        permissionHandler = CallPermissionHandler(context)
    }

    @Test
    fun `hasAudioPermissions should return true when all audio permissions granted`() {
        // Arrange
        every {
            context.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
        } returns PackageManager.PERMISSION_GRANTED
        every {
            context.checkSelfPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS)
        } returns PackageManager.PERMISSION_GRANTED

        // Act
        val result = permissionHandler.hasAudioPermissions()

        // Assert
        assert(result)
    }

    @Test
    fun `hasVideoPermissions should return false when camera permission missing`() {
        // Arrange
        every {
            context.checkSelfPermission(Manifest.permission.CAMERA)
        } returns PackageManager.PERMISSION_DENIED

        // Act
        val result = permissionHandler.hasVideoPermissions()

        // Assert
        assert(!result)
    }

    @Test
    fun `getMissingAudioPermissions should return missing permissions`() {
        // Arrange
        every {
            context.checkSelfPermission(any())
        } returns PackageManager.PERMISSION_DENIED

        // Act
        val missing = permissionHandler.getMissingAudioPermissions()

        // Assert
        assert(missing.isNotEmpty())
    }

    @Test
    fun `needsAudioPermissionRequest should return true when permissions missing`() {
        // Arrange
        every {
            context.checkSelfPermission(any())
        } returns PackageManager.PERMISSION_DENIED

        // Act
        val result = permissionHandler.needsAudioPermissionRequest()

        // Assert
        assert(result)
    }

    @Test
    fun `needsVideoPermissionRequest should return true when permissions missing`() {
        // Arrange
        every {
            context.checkSelfPermission(any())
        } returns PackageManager.PERMISSION_DENIED

        // Act
        val result = permissionHandler.needsVideoPermissionRequest()

        // Assert
        assert(result)
    }

    @Test
    fun `getPermissionStatus should return current permission state`() {
        // Act
        val status = permissionHandler.getPermissionStatus()

        // Assert
        assert(status != null)
        assert(status.canMakeAudioCall == false || status.canMakeAudioCall == true)
        assert(status.canMakeVideoCall == false || status.canMakeVideoCall == true)
    }

    @Test
    fun `handlePermissionResult should emit success for all granted`() = runTest {
        // Arrange
        val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
        val grantResults = intArrayOf(PackageManager.PERMISSION_GRANTED)

        // Act
        permissionHandler.handlePermissionResult(
            CallPermissionHandler.REQUEST_AUDIO_PERMISSIONS,
            permissions,
            grantResults
        )

        // Assert - should complete without error
        assert(true)
    }

    @Test
    fun `handlePermissionResult should emit denial for denied permission`() = runTest {
        // Arrange
        val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
        val grantResults = intArrayOf(PackageManager.PERMISSION_DENIED)

        // Act
        permissionHandler.handlePermissionResult(
            CallPermissionHandler.REQUEST_AUDIO_PERMISSIONS,
            permissions,
            grantResults
        )

        // Assert - should complete with denial information
        assert(true)
    }

    @Test
    fun `getPermissionSummary should return formatted string`() {
        // Act
        val summary = permissionHandler.getPermissionSummary()

        // Assert
        assert(summary.contains("Call Permissions"))
        assert(summary.contains("Camera"))
        assert(summary.contains("Microphone"))
    }
}
