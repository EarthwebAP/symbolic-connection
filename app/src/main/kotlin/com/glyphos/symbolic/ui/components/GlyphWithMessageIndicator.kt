package com.glyphos.symbolic.ui.components

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Glyph display with red pulsing dot indicator
 * Shows that an invoked message is hidden inside the glyph
 */
@Composable
fun GlyphWithMessageIndicator(
    glyphId: String,
    label: String = "Glyph",
    size: androidx.compose.ui.unit.Dp = 96.dp,
    hasInvokedMessage: Boolean = false,
    onTap: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    // Pulsing red dot animation
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 1500,
                easing = FastOutLinearInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotPulse"
    )

    Box(
        modifier = Modifier
            .size(size)
            .clickable { onTap?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        // Base glyph display
        GlyphDisplay(
            glyphId = glyphId,
            label = label,
            size = size,
            glowIntensity = if (hasInvokedMessage) 0.9f else 0.7f
        )

        // Red pulsing dot indicator (top-right)
        if (hasInvokedMessage) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        color = Color.Red.copy(alpha = dotAlpha),
                        shape = CircleShape
                    )
            )

            // Inner white dot for contrast
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        color = Color.White.copy(alpha = dotAlpha * 0.7f),
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * Glyph with indicator and message preview tooltip
 */
@Composable
fun GlyphWithMessagePreview(
    glyphId: String,
    label: String = "Glyph",
    size: androidx.compose.ui.unit.Dp = 96.dp,
    hasInvokedMessage: Boolean = false,
    messagePreview: String = "Secret Message",
    senderName: String = "Unknown",
    onTap: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 1500,
                easing = FastOutLinearInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotPulse"
    )

    Box(
        modifier = Modifier
            .clickable { onTap?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glyph with indicator
            Box(
                modifier = Modifier.size(size),
                contentAlignment = Alignment.Center
            ) {
                GlyphDisplay(
                    glyphId = glyphId,
                    label = label,
                    size = size,
                    glowIntensity = if (hasInvokedMessage) 0.9f else 0.7f
                )

                // Red pulsing dot
                if (hasInvokedMessage) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.TopEnd)
                            .background(
                                color = Color.Red.copy(alpha = dotAlpha),
                                shape = CircleShape
                            )
                    )

                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .align(Alignment.TopEnd)
                            .background(
                                color = Color.White.copy(alpha = dotAlpha * 0.7f),
                                shape = CircleShape
                            )
                    )
                }
            }

            // Message indicator text
            if (hasInvokedMessage) {
                androidx.compose.foundation.layout.Spacer(
                    modifier = Modifier.size(8.dp)
                )
                Text(
                    text = "🔐 Invoked message from $senderName",
                    color = Color.Cyan,
                    fontSize = 10.sp
                )
            }
        }
    }
}
