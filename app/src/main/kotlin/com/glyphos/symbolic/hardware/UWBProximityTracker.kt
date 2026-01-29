package com.glyphos.symbolic.hardware

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UWB (Ultra-Wideband) Proximity Tracker
 * Detects nearby users with spatial precision
 */
@Singleton
class UWBProximityTracker @Inject constructor(
    private val context: Context
) {

    private val _nearbyUsers = MutableStateFlow<List<NearbyUser>>(emptyList())
    val nearbyUsers: StateFlow<List<NearbyUser>> = _nearbyUsers

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled

    private val _currentDistance = MutableStateFlow(0.0)
    val currentDistance: StateFlow<Double> = _currentDistance

    fun startTracking() {
        _isEnabled.value = true
    }

    fun stopTracking() {
        _isEnabled.value = false
    }

    fun detectNearbyUser(userId: String, distance: Double, angle: Double = 0.0) {
        if (!_isEnabled.value) return

        val current = _nearbyUsers.value.toMutableList()
        val existing = current.indexOfFirst { it.userId == userId }

        val user = NearbyUser(
            userId = userId,
            distance = distance,
            angle = angle,
            lastSeen = System.currentTimeMillis()
        )

        if (existing >= 0) {
            current[existing] = user
        } else {
            current.add(user)
        }

        _nearbyUsers.value = current.sortedBy { it.distance }
        _currentDistance.value = distance
    }

    fun removeUser(userId: String) {
        val current = _nearbyUsers.value.toMutableList()
        current.removeAll { it.userId == userId }
        _nearbyUsers.value = current
    }

    fun getNearestUser(): NearbyUser? {
        return _nearbyUsers.value.firstOrNull()
    }

    fun getUsersInRange(maxDistance: Double): List<NearbyUser> {
        return _nearbyUsers.value.filter { it.distance <= maxDistance }
    }

    fun getPresenceOrbit(): List<NearbyUser> {
        return _nearbyUsers.value
    }

    data class NearbyUser(
        val userId: String,
        val distance: Double,  // meters
        val angle: Double,     // degrees (0-360)
        val lastSeen: Long
    )
}
