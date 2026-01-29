package com.glyphos.symbolic.identity.glyph

import android.util.Log
import com.glyphos.symbolic.core.models.EmotionalTone
import com.glyphos.symbolic.core.models.GlyphIdentity
import com.glyphos.symbolic.core.models.PresenceState
import com.glyphos.symbolic.core.models.SemanticMetrics
import java.security.MessageDigest

/**
 * PHASE 2: Glyph Generator
 *
 * Generates unique personal glyphs from user presence patterns, emotional tone,
 * and interaction rhythm. Integrates with GlyphCP kernel to find nearest matching glyph.
 */
class GlyphGenerator {
    companion object {
        private const val TAG = "GlyphGenerator"
    }

    /**
     * Generate personal glyph from user behavioral patterns
     * @param userId User identifier
     * @param presencePatterns List of recent presence states
     * @param emotionalTone Dominant emotional tone
     * @param interactionRhythm User's interaction frequency (messages/hour)
     * @param lineageMarkers Identity ancestry markers
     * @return Generated GlyphIdentity
     */
    fun generatePersonalGlyph(
        userId: String,
        presencePatterns: List<PresenceState>,
        emotionalTone: EmotionalTone,
        interactionRhythm: Float,
        lineageMarkers: List<String> = emptyList()
    ): GlyphIdentity {
        // Compute semantic metrics from patterns
        val metrics = computeSemanticMetrics(
            presencePatterns = presencePatterns,
            emotionalTone = emotionalTone,
            interactionRhythm = interactionRhythm
        )

        // Generate stable glyph ID based on userId and metrics
        val glyphId = generateStableGlyphId(userId, metrics)

        // Create visual representation (64-dim latent vector)
        val latentVector = generateLatentVector(metrics, lineageMarkers)

        // Create glyph name
        val glyphName = generateGlyphName(emotionalTone, metrics)

        // Determine category from metrics
        val category = categorizeGlyph(metrics)

        return GlyphIdentity(
            glyphId = glyphId,
            name = glyphName,
            visualData = generateVisualData(glyphId, metrics),
            semanticMetrics = metrics,
            resonancePattern = latentVector,
            category = category
        )
    }

    /**
     * Compute 6D semantic metrics from presence patterns
     */
    private fun computeSemanticMetrics(
        presencePatterns: List<PresenceState>,
        emotionalTone: EmotionalTone,
        interactionRhythm: Float
    ): SemanticMetrics {
        if (presencePatterns.isEmpty()) {
            return SemanticMetrics(50, 50, 50, 50, 50, 50)
        }

        // Power: average focus level
        val power = presencePatterns.map { it.focusLevel.ordinal * 25 + 10 }.average().toInt()

        // Complexity: based on presence mode diversity
        val complexity = (presencePatterns.map { it.mode.ordinal }.distinct().size * 20).coerceIn(10, 100)

        // Resonance: interaction rhythm normalized
        val resonance = (interactionRhythm * 10).toInt().coerceIn(0, 100)

        // Stability: consistency of presence (low variance = high stability)
        val focusVariance = presencePatterns.map { it.focusLevel.ordinal }.variance()
        val stability = (100 - focusVariance * 10).toInt().coerceIn(10, 100)

        // Connectivity: social context frequency
        val connectivity = presencePatterns.count { it.socialContext.ordinal > 1 } * 20

        // Affinity: emotional tone affinity (how well tone matches patterns)
        val affinity = emotionalTone.ordinal * 20 + 10

        return SemanticMetrics(
            power = power.coerceIn(0, 100),
            complexity = complexity,
            resonance = resonance,
            stability = stability,
            connectivity = connectivity.coerceIn(0, 100),
            affinity = affinity.coerceIn(0, 100)
        )
    }

    /**
     * Generate stable, deterministic glyph ID
     */
    private fun generateStableGlyphId(userId: String, metrics: SemanticMetrics): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(userId.toByteArray())
        md.update(metrics.power.toByte())
        md.update(metrics.complexity.toByte())
        md.update(metrics.resonance.toByte())
        md.update(metrics.stability.toByte())
        md.update(metrics.connectivity.toByte())
        md.update(metrics.affinity.toByte())

        val hash = md.digest()
        val hashInt = hash.take(4).foldIndexed(0) { i, acc, byte ->
            acc or ((byte.toInt() and 0xFF) shl (i * 8))
        }

        val glyphNumber = (Math.abs(hashInt) % 600) + 1
        return glyphNumber.toString().padStart(3, '0')
    }

    /**
     * Generate 64-dimensional latent vector representation
     */
    private fun generateLatentVector(metrics: SemanticMetrics, lineageMarkers: List<String>): ByteArray {
        val latent = ByteArray(64)

        // Distribute metrics across latent dimensions
        val ratios = floatArrayOf(
            metrics.power / 100f,
            metrics.complexity / 100f,
            metrics.resonance / 100f,
            metrics.stability / 100f,
            metrics.connectivity / 100f,
            metrics.affinity / 100f
        )

        // Fill latent vector with metric-based values
        for (i in 0 until 64) {
            val metricIdx = i % 6
            val baseValue = (ratios[metricIdx] * 127).toInt().toByte()
            latent[i] = baseValue
        }

        // Incorporate lineage markers
        if (lineageMarkers.isNotEmpty()) {
            val markerHash = MessageDigest.getInstance("SHA-256")
            markerHash.update(lineageMarkers.joinToString(",").toByteArray())
            val markerBytes = markerHash.digest()

            // XOR lineage into latent vector
            for (i in latent.indices) {
                latent[i] = (latent[i].toInt() xor markerBytes[i % 32].toInt()).toByte()
            }
        }

        return latent
    }

    /**
     * Generate glyph name from emotional tone and metrics
     */
    private fun generateGlyphName(tone: EmotionalTone, metrics: SemanticMetrics): String {
        return when {
            tone == EmotionalTone.JOYFUL && metrics.power > 80 -> "RESONANT"
            tone == EmotionalTone.CALM && metrics.stability > 80 -> "SERENE"
            tone == EmotionalTone.ANXIOUS && metrics.complexity > 70 -> "TURBULENT"
            metrics.connectivity > 80 -> "NEXUS"
            metrics.complexity > 80 -> "KALEID"
            else -> "GLYPH${metrics.power}"
        }
    }

    /**
     * Categorize glyph based on metrics
     */
    private fun categorizeGlyph(metrics: SemanticMetrics): String {
        return when {
            metrics.power > 80 -> "neural"
            metrics.resonance > 80 -> "propulsion"
            metrics.connectivity > 80 -> "communication"
            metrics.stability > 80 -> "defense"
            metrics.complexity > 80 -> "research"
            else -> "life-support"
        }
    }

    /**
     * Generate visual representation (placeholder)
     */
    private fun generateVisualData(glyphId: String, metrics: SemanticMetrics): ByteArray {
        // In production, render actual glyph artwork
        return (glyphId + metrics.toString()).toByteArray()
    }

    /**
     * Update existing glyph based on new patterns
     */
    fun updateGlyph(
        existing: GlyphIdentity,
        newPatterns: List<PresenceState>,
        newTone: EmotionalTone
    ): GlyphIdentity {
        val updatedMetrics = computeSemanticMetrics(
            newPatterns,
            newTone,
            existing.semanticMetrics.power / 10f
        )

        return existing.copy(
            semanticMetrics = updatedMetrics,
            resonancePattern = generateLatentVector(updatedMetrics, emptyList())
        )
    }
}

/**
 * Calculate variance of a list
 */
private fun List<Int>.variance(): Float {
    if (size < 2) return 0f
    val mean = average()
    val sq_sum = map { (it - mean) * (it - mean) }.sum()
    return (sq_sum / size).toFloat()
}
