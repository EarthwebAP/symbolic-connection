package com.glyphos.symbolic.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glyphos.symbolic.data.api.GlyphApiClient
import com.glyphos.symbolic.identity.glyph.PersonalGlyphGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoggedIn: Boolean = false,
    val userId: Int? = null,
    val username: String? = null,
    val displayName: String? = null,
    val token: String? = null,
    val glyphSvg: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val apiClient = GlyphApiClient()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentName = MutableStateFlow("")
    val currentName: StateFlow<String> = _currentName.asStateFlow()

    private val _generatedGlyph = MutableStateFlow<PersonalGlyphGenerator.GlyphData?>(null)
    val generatedGlyph: StateFlow<PersonalGlyphGenerator.GlyphData?> = _generatedGlyph.asStateFlow()

    fun updateName(name: String) {
        _currentName.value = name
        if (name.isNotBlank()) {
            val glyph = PersonalGlyphGenerator.generateFromName(name)
            _generatedGlyph.value = glyph
        }
    }

    fun register(username: String, password: String, displayName: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            val glyphData = PersonalGlyphGenerator.generateFromName(displayName)

            val response = apiClient.register(
                username = username,
                password = password,
                displayName = displayName,
                glyphData = glyphData.glyphData,
                glyphSvg = glyphData.svg
            )

            if (response.success && response.data != null) {
                val data = response.data
                _authState.value = AuthState(
                    isLoggedIn = true,
                    userId = (data["userId"] as? Number)?.toInt(),
                    username = data["username"] as? String,
                    displayName = data["displayName"] as? String,
                    token = data["token"] as? String,
                    glyphSvg = glyphData.svg,
                    isLoading = false,
                    error = null
                )
                _currentName.value = ""
                _generatedGlyph.value = null
            } else {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = response.error ?: "Registration failed"
                )
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            val response = apiClient.login(username, password)

            if (response.success && response.data != null) {
                val data = response.data
                _authState.value = AuthState(
                    isLoggedIn = true,
                    userId = (data["userId"] as? Number)?.toInt(),
                    username = data["username"] as? String,
                    displayName = data["displayName"] as? String,
                    token = data["token"] as? String,
                    glyphSvg = data["glyphSvg"] as? String,
                    isLoading = false,
                    error = null
                )
                _currentName.value = ""
                _generatedGlyph.value = null
            } else {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = response.error ?: "Login failed"
                )
            }
        }
    }

    fun logout() {
        _authState.value = AuthState()
    }
}
