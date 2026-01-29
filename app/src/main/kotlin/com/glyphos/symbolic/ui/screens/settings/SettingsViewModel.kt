package com.glyphos.symbolic.ui.screens.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _blurModeEnabled = MutableStateFlow(false)
    val blurModeEnabled: StateFlow<Boolean> = _blurModeEnabled.asStateFlow()

    private val _breathUnlockEnabled = MutableStateFlow(true)
    val breathUnlockEnabled: StateFlow<Boolean> = _breathUnlockEnabled.asStateFlow()

    private val _gestureUnlockEnabled = MutableStateFlow(true)
    val gestureUnlockEnabled: StateFlow<Boolean> = _gestureUnlockEnabled.asStateFlow()

    private val _appVersion = MutableStateFlow("1.0.0")
    val appVersion: StateFlow<String> = _appVersion.asStateFlow()

    fun toggleBlurMode() {
        _blurModeEnabled.value = !_blurModeEnabled.value
    }

    fun toggleBreathUnlock() {
        _breathUnlockEnabled.value = !_breathUnlockEnabled.value
    }

    fun toggleGestureUnlock() {
        _gestureUnlockEnabled.value = !_gestureUnlockEnabled.value
    }
}
