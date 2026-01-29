package com.glyphos.symbolic.identity.glyph

import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI

/**
 * Generates unique personal glyphs from user names
 * Each name creates a deterministic, unique wave-based glyph
 */
object PersonalGlyphGenerator {

    /**
     * Generate glyph from name - creates deterministic visual from text
     */
    fun generateFromName(name: String): GlyphData {
        val hash = hashName(name)
        val metrics = deriveMetricsFromHash(hash, name)
        val svg = generateSvgGlyph(name, hash, metrics)

        return GlyphData(
            name = name,
            hash = hash,
            metrics = metrics,
            svg = svg,
            glyphData = generateGlyphBytes(name, hash)
        )
    }

    data class GlyphData(
        val name: String,
        val hash: Long,
        val metrics: GlyphMetrics,
        val svg: String,
        val glyphData: String
    )

    data class GlyphMetrics(
        val power: Int,      // 0-100
        val complexity: Int, // 0-100
        val resonance: Int,  // 0-100
        val stability: Int,  // 0-100
        val connectivity: Int, // 0-100
        val affinity: Int    // 0-100
    )

    private fun hashName(name: String): Long {
        var hash = 0L
        for ((i, char) in name.withIndex()) {
            hash = ((hash shl 5) - hash) + char.code
            hash = hash and hash // Convert to 32bit integer
        }
        return abs(hash)
    }

    private fun deriveMetricsFromHash(hash: Long, name: String): GlyphMetrics {
        val seed1 = hash
        val seed2 = name.length.toLong()
        val seed3 = name.sumOf { it.code.toLong() }

        return GlyphMetrics(
            power = ((seed1 * 73856093L) % 101).toInt(),
            complexity = ((seed2 * 19349663L) % 101).toInt(),
            resonance = ((seed3 * 83492791L) % 101).toInt(),
            stability = (((seed1 + seed2) * 42) % 101).toInt(),
            connectivity = (((seed2 + seed3) * 37) % 101).toInt(),
            affinity = (((seed1 + seed3) * 61) % 101).toInt()
        )
    }

    private fun generateSvgGlyph(name: String, hash: Long, metrics: GlyphMetrics): String {
        val size = 200
        val centerX = size / 2
        val centerY = size / 2

        // Generate wave paths based on name
        val waveCount = name.length.coerceIn(3, 8)
        val baseFundamental = 0.1 + (hash % 90).toDouble() / 1000.0

        var paths = ""

        // Create resonant waves for each character
        for ((index, char) in name.withIndex()) {
            val charValue = char.code.toDouble()
            val frequency = baseFundamental * (index + 1) * (charValue / 100.0)
            val amplitude = (metrics.resonance / 100.0) * 40

            val pathData = generateWavePath(
                centerX,
                centerY,
                amplitude.toInt(),
                frequency,
                index,
                waveCount,
                hash
            )

            val colorBrightness = 50 + (charValue.toInt() % 206)
            paths += """
            <path d="$pathData"
                  stroke="rgb(0, $colorBrightness, $colorBrightness)"
                  stroke-width="2"
                  fill="none"
                  opacity="0.8"/>
            """.trimIndent()
        }

        // Add center glyph marker
        val centerColor = 50 + (metrics.power / 100.0) * 200
        paths += """
        <circle cx="$centerX" cy="$centerY" r="3"
                fill="rgb(0, ${centerColor.toInt()}, ${centerColor.toInt()})" opacity="0.9"/>
        """.trimIndent()

        // Add resonance aura
        val auraRadius = 70 + (metrics.resonance / 100.0) * 30
        paths += """
        <circle cx="$centerX" cy="$centerY" r="${auraRadius.toInt()}"
                stroke="rgb(0, 200, 200)"
                stroke-width="1"
                fill="none"
                opacity="0.3"/>
        """.trimIndent()

        return """
        <svg viewBox="0 0 $size $size" xmlns="http://www.w3.org/2000/svg" width="200" height="200">
            <rect width="$size" height="$size" fill="black"/>
            <g>
                $paths
            </g>
        </svg>
        """.trimIndent()
    }

    private fun generateWavePath(
        centerX: Int,
        centerY: Int,
        amplitude: Int,
        frequency: Double,
        waveIndex: Int,
        totalWaves: Int,
        hash: Long
    ): String {
        val startAngle = (waveIndex.toDouble() / totalWaves) * 2 * PI + (hash % 360).toDouble() * PI / 180.0
        val points = mutableListOf<String>()

        for (t in 0..100) {
            val angle = startAngle + (t.toDouble() / 100.0) * 2 * PI
            val waveOffset = sin(frequency * t.toDouble()) * amplitude

            val x = centerX + cos(angle) * (50 + waveOffset)
            val y = centerY + sin(angle) * (50 + waveOffset)

            if (t == 0) {
                points.add("M $x $y")
            } else {
                points.add("L $x $y")
            }
        }

        return points.joinToString(" ")
    }

    private fun generateGlyphBytes(name: String, hash: Long): String {
        val bytes = ByteArray(256)
        val nameBytes = name.toByteArray()

        for (i in nameBytes.indices) {
            bytes[i] = nameBytes[i]
        }

        for (i in nameBytes.size until 256) {
            bytes[i] = ((hash shr (i % 64)) and 0xFF).toByte()
        }

        return bytes.joinToString(",") { (it.toInt() and 0xFF).toString() }
    }
}
