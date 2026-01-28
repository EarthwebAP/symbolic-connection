package com.glyphos.symbolic.visual

import android.util.Log
import com.glyphos.symbolic.core.models.GlyphIdentity
import com.glyphos.symbolic.core.models.SemanticMetrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * PHASE 6: Contributor Glyph Manager
 *
 * Special glyphs for project contributors and team members.
 * - Contributor identity glyphs
 * - Recognition and attribution
 * - Team member profiles
 * - Contribution tracking
 */
class ContributorGlyphManager {
    companion object {
        private const val TAG = "ContributorGlyphManager"
    }

    private val _contributors = MutableStateFlow<List<ContributorProfile>>(emptyList())
    val contributors: StateFlow<List<ContributorProfile>> = _contributors.asStateFlow()

    private val _contributorGlyphs = MutableStateFlow<Map<String, GlyphIdentity>>(emptyMap())
    val contributorGlyphs: StateFlow<Map<String, GlyphIdentity>> = _contributorGlyphs.asStateFlow()

    data class ContributorProfile(
        val id: String = UUID.randomUUID().toString(),
        val name: String,
        val role: ContributorRole,
        val email: String? = null,
        val profile: String? = null,
        val glyphId: String? = null,
        val joinDate: Long = System.currentTimeMillis(),
        val contributions: Int = 0,
        val recognitionLevel: RecognitionLevel = RecognitionLevel.CONTRIBUTOR
    )

    enum class ContributorRole {
        ARCHITECT,
        ENGINEER,
        DESIGNER,
        RESEARCHER,
        COMMUNITY_LEAD,
        CONTRIBUTOR,
        TRANSLATOR,
        ADVOCATE
    }

    enum class RecognitionLevel {
        CONTRIBUTOR,
        CORE_CONTRIBUTOR,
        MAINTAINER,
        LEAD,
        FOUNDER
    }

    fun registerContributor(
        name: String,
        role: ContributorRole,
        email: String? = null,
        profile: String? = null
    ): ContributorProfile {
        val contributor = ContributorProfile(
            name = name,
            role = role,
            email = email,
            profile = profile
        )

        _contributors.value = _contributors.value + contributor

        // Generate contributor glyph
        val glyph = generateContributorGlyph(contributor)
        _contributorGlyphs.value = _contributorGlyphs.value + (contributor.id to glyph)

        Log.d(TAG, "Contributor registered: $name ($role)")
        return contributor
    }

    fun updateContribution(contributorId: String) {
        _contributors.value = _contributors.value.map { contributor ->
            if (contributor.id == contributorId) {
                contributor.copy(contributions = contributor.contributions + 1)
            } else {
                contributor
            }
        }
    }

    fun promoteContributor(contributorId: String, level: RecognitionLevel) {
        _contributors.value = _contributors.value.map { contributor ->
            if (contributor.id == contributorId) {
                contributor.copy(recognitionLevel = level)
            } else {
                contributor
            }
        }

        Log.d(TAG, "Contributor promoted: $contributorId to ${level.name}")
    }

    fun getContributor(id: String): ContributorProfile? {
        return _contributors.value.firstOrNull { it.id == id }
    }

    fun getContributorsByRole(role: ContributorRole): List<ContributorProfile> {
        return _contributors.value.filter { it.role == role }
    }

    fun getContributorGlyph(contributorId: String): GlyphIdentity? {
        return _contributorGlyphs.value[contributorId]
    }

    fun getContributorsWithGlyphs(): Map<ContributorProfile, GlyphIdentity> {
        return _contributors.value.associate { contributor ->
            contributor to (_contributorGlyphs.value[contributor.id]
                ?: generateContributorGlyph(contributor))
        }
    }

    private fun generateContributorGlyph(contributor: ContributorProfile): GlyphIdentity {
        // Generate deterministic glyph based on contributor name and role
        val seed = "${contributor.name}${contributor.role}".hashCode().toLong()
        val randomness = (seed % 100) / 100f

        val metrics = SemanticMetrics(
            power = (30 + (contributor.contributions * 2)).coerceAtMost(100),
            complexity = when (contributor.role) {
                ContributorRole.ARCHITECT -> 90
                ContributorRole.ENGINEER -> 85
                ContributorRole.DESIGNER -> 75
                ContributorRole.RESEARCHER -> 80
                else -> 60
            },
            resonance = (contributor.contributions * 5).coerceAtMost(100),
            stability = 85,
            connectivity = 80,
            affinity = 75
        )

        return GlyphIdentity(
            glyphId = "contributor-${contributor.id.take(8)}",
            visualData = generateVisualData(contributor),
            semanticMetrics = metrics,
            resonancePattern = ByteArray(64) { (it * 4).toByte() }
        )
    }

    private fun generateVisualData(contributor: ContributorProfile): ByteArray {
        // Generate visual representation data
        // In real implementation, would create deterministic visual pattern
        return contributor.name.toByteArray()
    }

    fun getStatistics(): ContributorStatistics {
        val contributors = _contributors.value
        return ContributorStatistics(
            totalContributors = contributors.size,
            byRole = contributors.groupingBy { it.role }.eachCount(),
            byRecognitionLevel = contributors.groupingBy { it.recognitionLevel }.eachCount(),
            totalContributions = contributors.sumOf { it.contributions },
            averageContributions = if (contributors.isNotEmpty()) {
                contributors.map { it.contributions }.average().toInt()
            } else 0
        )
    }

    fun getStatus(): String {
        val stats = getStatistics()
        return """
        Contributor Glyph Manager Status:
        - Total contributors: ${stats.totalContributors}
        - Total contributions: ${stats.totalContributions}
        - Average per contributor: ${stats.averageContributions}
        - By role: ${stats.byRole}
        - Recognition levels: ${stats.byRecognitionLevel}
        """.trimIndent()
    }

    fun exportContributorList(): String {
        return _contributors.value.joinToString("\n") { contributor ->
            "${contributor.name} (${contributor.role.name}) - ${contributor.contributions} contributions"
        }
    }
}

data class ContributorStatistics(
    val totalContributors: Int,
    val byRole: Map<ContributorGlyphManager.ContributorRole, Int>,
    val byRecognitionLevel: Map<ContributorGlyphManager.RecognitionLevel, Int>,
    val totalContributions: Int,
    val averageContributions: Int
)
