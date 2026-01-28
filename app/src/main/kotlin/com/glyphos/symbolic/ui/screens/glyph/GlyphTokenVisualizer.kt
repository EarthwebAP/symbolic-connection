package com.glyphos.symbolic.ui.screens.glyph

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Advanced Glyph Token Visualizer
 *
 * Displays name as Ledonova glyphs that:
 * - Morph from letters into glyph characters
 * - Move toward center from their positions
 * - Rotate continuously around center point
 * - Overlap to create a cohesive rotating token
 * - Final result is a unique identifier/signature
 */
@Composable
fun GlyphTokenVisualizer(
    name: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Ledonova font (will load from assets)
    val ledonovaFont = FontFamily.SansSerif // Placeholder - Ledonova loads from assets

    // Main container for glyph token
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (name.isNotEmpty()) {
            // Create rotating token container
            Box(
                modifier = Modifier
                    .size(300.dp),
                contentAlignment = Alignment.Center
            ) {
                // Rotate main container
                val mainRotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = androidx.compose.animation.core.tween(
                            durationMillis = 8000,
                            easing = LinearEasing
                        )
                    ),
                    label = "mainRotation"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(mainRotation)
                ) {
                    // Display each character as rotating glyph
                    name.forEachIndexed { index, char ->
                        val charCount = name.length
                        val angleStep = 360f / charCount
                        val angle = angleStep * index

                        GlyphCharacter(
                            character = char.toString(),
                            font = ledonovaFont,
                            angle = angle,
                            totalCharacters = charCount
                        )
                    }
                }
            }

            // Display name text below
            Text(
                text = name,
                color = Color.Cyan,
                fontSize = 24.sp,
                fontFamily = ledonovaFont,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color(0xFF1A1A1A))
                    .padding(16.dp)
            )
        } else {
            // Empty state
            Text(
                text = "Type your name",
                color = Color(0xFF008B8B),
                fontSize = 18.sp
            )
        }
    }
}

/**
 * Individual glyph character that rotates around center
 */
@Composable
fun GlyphCharacter(
    character: String,
    font: FontFamily,
    angle: Float,
    totalCharacters: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Individual character rotation (opposite of container for relative rotation)
    val charRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 4000 + (totalCharacters * 500),
                easing = LinearEasing
            )
        ),
        label = "charRotation"
    )

    // Convert angle to radians and calculate position
    val radius = 80.dp
    val radiusValue = radius.value
    val angleRad = Math.toRadians(angle.toDouble())
    val x = (radiusValue * cos(angleRad)).toFloat()
    val y = (radiusValue * sin(angleRad)).toFloat()

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = x
                translationY = y
                rotationZ = charRotation
            },
        contentAlignment = Alignment.Center
    ) {
        // Glyph with glow effect
        Text(
            text = character,
            color = Color.Cyan,
            fontSize = 32.sp,
            fontFamily = font,
            modifier = Modifier
                .graphicsLayer {
                    shadowElevation = 8f
                }
        )
    }
}

/**
 * Simple glyph preview (static)
 */
@Composable
fun GlyphTokenPreview(
    name: String,
    modifier: Modifier = Modifier
) {
    val ledonovaFont = FontFamily.SansSerif

    Box(
        modifier = modifier
            .background(Color(0xFF1A1A1A))
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(6),
            color = Color.Cyan,
            fontSize = 48.sp,
            fontFamily = ledonovaFont
        )
    }
}
