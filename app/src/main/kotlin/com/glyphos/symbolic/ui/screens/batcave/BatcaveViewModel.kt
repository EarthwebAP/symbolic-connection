package com.glyphos.symbolic.ui.screens.batcave

import androidx.lifecycle.ViewModel
import com.glyphos.symbolic.spaces.BatcaveRoomManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Batcave Screen ViewModel
 * Delegates to BatcaveRoomManager
 */
@HiltViewModel
class BatcaveViewModel @Inject constructor(
    val batcaveManager: BatcaveRoomManager
) : ViewModel() {
    // Expose properties from batcaveManager
    val activeBatcave = batcaveManager.activeBatcave
    val sealedMode = batcaveManager.sealedMode
    val notificationsBlocked = batcaveManager.notificationsBlocked
}
