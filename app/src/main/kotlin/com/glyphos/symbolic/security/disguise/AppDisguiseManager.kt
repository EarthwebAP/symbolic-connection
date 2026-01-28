package com.glyphos.symbolic.security.disguise

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.glyphos.symbolic.core.contracts.AppDisguise
import com.glyphos.symbolic.core.contracts.DisguiseType
import com.glyphos.symbolic.core.contracts.GestureType
import com.glyphos.symbolic.core.contracts.UserId
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "app_disguise_prefs")

/**
 * App Disguise Manager
 * Hides the app behind decoy interfaces (calculator, notes, etc.)
 */
@Singleton
class AppDisguiseManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _activeDisguise = MutableStateFlow<DisguiseType>(DisguiseType.NONE)
    val activeDisguise: StateFlow<DisguiseType> = _activeDisguise

    private val _isDisguised = MutableStateFlow(false)
    val isDisguised: StateFlow<Boolean> = _isDisguised

    private val _unlockGesture = MutableStateFlow<GestureType?>(null)
    val unlockGesture: StateFlow<GestureType?> = _unlockGesture

    private val _dischargeAttempts = MutableStateFlow(0)
    val dischargeAttempts: StateFlow<Int> = _dischargeAttempts

    suspend fun activateDisguise(
        userId: UserId,
        disguiseType: DisguiseType,
        unlockGesture: GestureType? = null
    ) {
        val disguise = AppDisguise(
            disguiseId = "disguise-${System.currentTimeMillis()}",
            userId = userId,
            activeDisguise = disguiseType,
            unlockGesture = unlockGesture,
            hiddenAccessible = true
        )

        saveDisguise(disguise)
        _activeDisguise.value = disguiseType
        _isDisguised.value = true
        _unlockGesture.value = unlockGesture
    }

    suspend fun deactivateDisguise() {
        _activeDisguise.value = DisguiseType.NONE
        _isDisguised.value = false
        _unlockGesture.value = null
    }

    suspend fun attemptUnlock(gesture: GestureType?): Boolean {
        val required = _unlockGesture.value
        return if (gesture == required) {
            deactivateDisguise()
            true
        } else {
            _dischargeAttempts.value++
            if (_dischargeAttempts.value >= 3) {
                // Trigger emergency seal after 3 failed attempts
                triggerEmergencySeal()
            }
            false
        }
    }

    fun getDisguiseInterface(): String {
        return when (_activeDisguise.value) {
            DisguiseType.CALCULATOR -> "CalculatorDisguiseScreen"
            DisguiseType.NOTES -> "NotesDisguiseScreen"
            DisguiseType.SETTINGS -> "SettingsDisguiseScreen"
            DisguiseType.MESSAGING_APP -> "MessagingAppDisguiseScreen"
            DisguiseType.NONE -> "MainApp"
        }
    }

    fun isAppDisguised(): Boolean {
        return _isDisguised.value
    }

    fun canAccessRealApp(): Boolean {
        return !_isDisguised.value
    }

    private suspend fun saveDisguise(disguise: AppDisguise) {
        try {
            val preferences = context.dataStore.edit { prefs ->
                prefs[stringPreferencesKey("active_disguise")] = disguise.activeDisguise.name
                prefs[booleanPreferencesKey("is_disguised")] = true
                disguise.unlockGesture?.let {
                    prefs[stringPreferencesKey("unlock_gesture")] = it.name
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadDisguise(userId: UserId): AppDisguise? {
        return try {
            val prefs = context.dataStore.data.first()
            val disguiseType = DisguiseType.valueOf(
                prefs[stringPreferencesKey("active_disguise")] ?: "NONE"
            )
            val unlockGesture = try {
                prefs[stringPreferencesKey("unlock_gesture")]?.let {
                    GestureType.valueOf(it)
                }
            } catch (e: Exception) {
                null
            }

            AppDisguise(
                disguiseId = "disguise-${System.currentTimeMillis()}",
                userId = userId,
                activeDisguise = disguiseType,
                unlockGesture = unlockGesture,
                hiddenAccessible = true
            )
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun triggerEmergencySeal() {
        // Lock the entire app
        _isDisguised.value = true
        _activeDisguise.value = DisguiseType.CALCULATOR
        _dischargeAttempts.value = 0
    }
}
