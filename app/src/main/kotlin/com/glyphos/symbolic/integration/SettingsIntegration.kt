package com.glyphos.symbolic.integration

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Settings Integration
 * Hijacks Settings long-press to reveal secret menu
 * App lives in Settings, accessible via hidden gesture
 */
@Singleton
class SettingsIntegration @Inject constructor(
    private val context: Context
) {

    private val _isSettingsMenuOpen = MutableStateFlow(false)
    val isSettingsMenuOpen: StateFlow<Boolean> = _isSettingsMenuOpen

    private val _longPressDetected = MutableStateFlow(false)
    val longPressDetected: StateFlow<Boolean> = _longPressDetected

    private val _isSecretMenuActive = MutableStateFlow(false)
    val isSecretMenuActive: StateFlow<Boolean> = _isSecretMenuActive

    private var longPressStartTime: Long = 0
    private val LONG_PRESS_DURATION_MS = 1500  // 1.5 seconds

    fun onSettingsGearPressed() {
        longPressStartTime = System.currentTimeMillis()
    }

    fun onSettingsGearHeld() {
        val pressDuration = System.currentTimeMillis() - longPressStartTime
        if (pressDuration >= LONG_PRESS_DURATION_MS) {
            _longPressDetected.value = true
            revealSecretMenu()
        }
    }

    fun onSettingsGearReleased() {
        _longPressDetected.value = false
        if (System.currentTimeMillis() - longPressStartTime < LONG_PRESS_DURATION_MS) {
            // Normal tap - show regular settings
            openNormalSettings()
        }
    }

    private fun revealSecretMenu() {
        _isSecretMenuActive.value = true
        _isSettingsMenuOpen.value = true
        // Navigate to secret menu screen
    }

    fun closeSecretMenu() {
        _isSecretMenuActive.value = false
        _isSettingsMenuOpen.value = false
    }

    fun isAppAccessible(): Boolean {
        return _isSecretMenuActive.value
    }

    private fun openNormalSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun registerSettingsListener(activity: android.app.Activity) {
        // This would be called from the settings activity to intercept long-press
        // Implementation depends on how we hook into system settings
    }
}
