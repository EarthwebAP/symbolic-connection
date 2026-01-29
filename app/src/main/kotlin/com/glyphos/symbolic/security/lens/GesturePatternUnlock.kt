package com.glyphos.symbolic.security.lens

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gesture Pattern Unlock
 * Uses custom gesture patterns (drawn on screen) to unlock secure features
 */
@Singleton
class GesturePatternUnlock @Inject constructor() {

    private val _patternLocked = MutableStateFlow(true)
    val patternLocked: StateFlow<Boolean> = _patternLocked

    private val _currentPattern = MutableStateFlow<List<Int>>(emptyList())
    val currentPattern: StateFlow<List<Int>> = _currentPattern

    private val _patternAttempts = MutableStateFlow(0)
    val patternAttempts: StateFlow<Int> = _patternAttempts

    private var storedPattern: List<Int> = emptyList()
    private val maxAttempts = 5
    private val patternMinLength = 4
    private val patternMaxLength = 9

    enum class PatternMode {
        CREATE,
        VERIFY,
        UPDATE
    }

    data class PatternPoint(
        val nodeId: Int, // 0-8 for 3x3 grid
        val x: Float,
        val y: Float,
        val timestamp: Long
    )

    fun setStoredPattern(pattern: List<Int>) {
        if (pattern.size in patternMinLength..patternMaxLength) {
            storedPattern = pattern
            _patternLocked.value = true
        }
    }

    fun recordPoint(nodeId: Int, x: Float, y: Float): Boolean {
        if (nodeId !in 0..8) return false

        val currentPattern = _currentPattern.value.toMutableList()

        // Prevent duplicate consecutive nodes
        if (currentPattern.isNotEmpty() && currentPattern.last() == nodeId) {
            return false
        }

        currentPattern.add(nodeId)
        _currentPattern.value = currentPattern

        // Auto-unlock if pattern is complete
        if (currentPattern.size >= patternMinLength) {
            return true
        }

        return false
    }

    fun verifyPattern(): Boolean {
        if (storedPattern.isEmpty()) return false

        val attempts = _patternAttempts.value + 1
        _patternAttempts.value = attempts

        val isMatch = _currentPattern.value == storedPattern

        if (isMatch) {
            _patternLocked.value = false
            _patternAttempts.value = 0
        } else if (attempts >= maxAttempts) {
            lockPattern()
        }

        _currentPattern.value = emptyList()
        return isMatch
    }

    fun clearPattern() {
        _currentPattern.value = emptyList()
    }

    fun lockPattern() {
        _patternLocked.value = true
        _patternAttempts.value = 0
        _currentPattern.value = emptyList()
    }

    fun unlockPattern() {
        _patternLocked.value = false
        _patternAttempts.value = 0
    }

    fun isPatternValid(pattern: List<Int>): Boolean {
        return pattern.size in patternMinLength..patternMaxLength &&
                pattern.distinct().size == pattern.size
    }

    fun getGridPosition(nodeId: Int): Pair<Float, Float> {
        // 3x3 grid positions (0-8)
        val row = nodeId / 3
        val col = nodeId % 3
        return Pair(col.toFloat() * 100f, row.toFloat() * 100f)
    }

    fun getPatternSecurity(): PatternSecurity {
        val complexity = calculateComplexity(_currentPattern.value)
        val entropy = calculateEntropy(storedPattern)

        return PatternSecurity(
            complexity = complexity,
            entropy = entropy,
            strength = when {
                entropy >= 90 -> "Very Strong"
                entropy >= 70 -> "Strong"
                entropy >= 50 -> "Medium"
                entropy >= 30 -> "Weak"
                else -> "Very Weak"
            },
            estimatedCrackTime = estimateCrackTime(entropy)
        )
    }

    private fun calculateComplexity(pattern: List<Int>): Int {
        var complexity = pattern.size * 10

        // Bonus for diagonal connections
        for (i in 0 until pattern.size - 1) {
            val from = pattern[i]
            val to = pattern[i + 1]
            val distance = calculateDistance(from, to)
            if (distance > 1.4f) complexity += 5
        }

        // Bonus for non-sequential nodes
        for (i in 0 until pattern.size - 1) {
            if ((pattern[i] + 1 != pattern[i + 1]) && (pattern[i] - 1 != pattern[i + 1])) {
                complexity += 3
            }
        }

        return minOf(complexity, 100)
    }

    private fun calculateEntropy(pattern: List<Int>): Int {
        if (pattern.isEmpty()) return 0

        var entropy = 0

        // Length component (20 points)
        entropy += (pattern.size.toFloat() / patternMaxLength * 20).toInt()

        // Uniqueness component (30 points)
        entropy += (pattern.distinct().size.toFloat() / patternMaxLength * 30).toInt()

        // Distance component (30 points)
        var totalDistance = 0f
        for (i in 0 until pattern.size - 1) {
            totalDistance += calculateDistance(pattern[i], pattern[i + 1])
        }
        entropy += minOf((totalDistance / patternMaxLength * 30).toInt(), 30)

        // Complexity component (20 points)
        entropy += minOf(calculateComplexity(pattern) / 5, 20)

        return minOf(entropy, 100)
    }

    private fun calculateDistance(nodeA: Int, nodeB: Int): Float {
        val posA = getGridPosition(nodeA)
        val posB = getGridPosition(nodeB)
        return kotlin.math.sqrt(
            (posB.first - posA.first).pow(2) + (posB.second - posA.second).pow(2)
        )
    }

    private fun estimateCrackTime(entropy: Int): String {
        val possibilities = 9 * 8 * 7 * 6 * 5 // Rough estimate
        val entropyFactor = entropy / 100.0
        val adjustedPossibilities = (possibilities * entropyFactor).toLong()

        return when {
            adjustedPossibilities > 1000000 -> "Millions of years"
            adjustedPossibilities > 100000 -> "Hundreds of thousands of years"
            adjustedPossibilities > 10000 -> "Thousands of years"
            adjustedPossibilities > 1000 -> "Hundreds of years"
            adjustedPossibilities > 100 -> "Years"
            else -> "Days"
        }
    }

    private fun Float.pow(exponent: Int): Float {
        return kotlin.math.pow(this, exponent.toFloat())
    }

    data class PatternSecurity(
        val complexity: Int, // 0-100
        val entropy: Int, // 0-100
        val strength: String,
        val estimatedCrackTime: String
    )
}
