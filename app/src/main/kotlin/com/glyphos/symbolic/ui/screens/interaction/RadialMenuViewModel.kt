package com.glyphos.symbolic.ui.screens.interaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glyphos.symbolic.core.contracts.UserId
import com.glyphos.symbolic.interaction.RadialMenuSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RadialMenuViewModel @Inject constructor(
    private val radialMenuSystem: RadialMenuSystem
) : ViewModel() {

    val isMenuOpen: StateFlow<Boolean> = radialMenuSystem.isMenuOpen
    val currentActions = radialMenuSystem.currentActions
    val selectedAction = radialMenuSystem.selectedAction

    // TODO: Get current user ID from auth
    private val currentUserId = UserId("user-1")

    fun openMenu() {
        radialMenuSystem.openMenu(currentUserId)
    }

    fun closeMenu() {
        radialMenuSystem.closeMenu()
    }

    fun selectAction(actionId: String) {
        radialMenuSystem.selectAction(actionId)
    }

    fun executeAction(actionId: String): Boolean {
        return viewModelScope.run {
            radialMenuSystem.executeAction(actionId)
        }
    }
}
