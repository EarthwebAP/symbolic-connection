package com.glyphos.symbolic.visual

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import android.util.Log

/**
 * PHASE 6: Glyph 006 Logo
 *
 * Primary visual identity - half-eye + blue shell.
 * - Iconic logo design
 * - Dynamic rendering
 * - Brand consistency
 * - Multiple size variants
 */
class Glyph006Logo {
    companion object {
        private const val TAG = "Glyph006Logo"

        // Design parameters
        const val EYE_RATIO = 0.4f        // Half-eye size ratio
        const val IRIS_RATIO = 0.25f      // Iris size ratio
        const val SHELL_OUTER_RATIO = 0.5f // Blue shell outer radius
        const val SHELL_INNER_RATIO = 0.35f // Blue shell inner radius

        // Colors
        val EYE_WHITE = Color(0xFFF5F5F5)
        val IRIS_COLOR = Color(0xFF1A1A1A)
        val PUPIL_COLOR = Color(0xFF000000)
        val SHELL_PRIMARY = Color(0xFF0066FF)    // Bright blue
        val SHELL_SECONDARY = Color(0xFF0044CC)  // Darker blue
        val SHELL_GLOW = Color(0xFF00AAFF)       // Cyan glow
    }

    /**
     * Render Glyph 006 logo
     * Composition: Half-eye (left) + Blue shell (right)
     */
    @Composable
    fun LogoFull(
        modifier: Modifier = Modifier,
        size: Float = 200f,
        animated: Boolean = true
    ) {
        Box(
            modifier = modifier
                .size(size.dp)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawLogo(this.size.width, animated)
            }
        }
    }

    /**
     * Render iconic version (eye only)
     */
    @Composable
    fun LogoIcon(
        modifier: Modifier = Modifier,
        size: Float = 48f
    ) {
        Box(
            modifier = modifier
                .size(size.dp)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawEyeOnly(this.size.width / 2, this.size.height / 2)
            }
        }
    }

    /**
     * Render shell only
     */
    @Composable
    fun LogoShell(
        modifier: Modifier = Modifier,
        size: Float = 48f
    ) {
        Box(
            modifier = modifier
                .size(size.dp)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawShellOnly(this.size.width / 2, this.size.height / 2)
            }
        }
    }

    private fun drawLogo(size: Float, animated: Boolean) {
        // Placeholder for canvas drawing - in real implementation would use Canvas API
        Log.d(TAG, "Drawing full Glyph 006 logo (size: $size, animated: $animated)")
    }

    private fun DrawScope.drawEyeOnly(centerX: Float, centerY: Float) {
        val eyeRadius = size.width * EYE_RATIO / 2

        // Draw eye white
        drawCircle(
            color = EYE_WHITE,
            radius = eyeRadius,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )

        // Draw iris
        drawCircle(
            color = IRIS_COLOR,
            radius = eyeRadius * IRIS_RATIO,
            center = androidx.compose.ui.geometry.Offset(centerX + eyeRadius * 0.3f, centerY)
        )

        // Draw pupil (animated look-at point)
        drawCircle(
            color = PUPIL_COLOR,
            radius = eyeRadius * IRIS_RATIO * 0.4f,
            center = androidx.compose.ui.geometry.Offset(centerX + eyeRadius * 0.5f, centerY - eyeRadius * 0.2f)
        )

        // Draw highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.6f),
            radius = eyeRadius * 0.15f,
            center = androidx.compose.ui.geometry.Offset(centerX + eyeRadius * 0.6f, centerY - eyeRadius * 0.3f)
        )
    }

    private fun DrawScope.drawShellOnly(centerX: Float, centerY: Float) {
        val outerRadius = size.width * SHELL_OUTER_RATIO
        val innerRadius = size.width * SHELL_INNER_RATIO

        // Outer shell with gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(SHELL_PRIMARY, SHELL_SECONDARY),
                center = androidx.compose.ui.geometry.Offset(centerX, centerY)
            ),
            radius = outerRadius,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )

        // Inner shell (lighter)
        drawCircle(
            color = SHELL_PRIMARY.copy(alpha = 0.8f),
            radius = innerRadius,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )
    }

    fun getStatus(): String {
        return """
        Glyph 006 Logo Status:
        - Design: Half-eye + Blue shell
        - Primary color: RGB(0, 102, 255) - Bright blue
        - Secondary color: RGB(0, 68, 204) - Dark blue
        - Glow color: RGB(0, 170, 255) - Cyan
        - Variants: Full, Icon, Shell
        """.trimIndent()
    }
}
