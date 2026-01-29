package com.glyphos.symbolic.identity.glyph

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.glyphos.symbolic.core.models.GlyphIdentity
import kotlin.math.sin

/**
 * PHASE 2: Glyph Animator
 *
 * Animates the transition from user's name to their personal glyph.
 * Creates smooth morphing effect over 2 seconds.
 */
class GlyphAnimator {

    /**
     * Animate name morphing into glyph
     * Over 2 seconds: text fades out, glyph fades in
     */
    @Composable
    fun NameToGlyphAnimation(
        name: String,
        targetGlyph: GlyphIdentity,
        onComplete: () -> Unit = {}
    ) {
        val animationProgress = remember { Animatable(0f) }
        val isComplete = remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing)
            )
            isComplete.value = true
            onComplete()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            val progress = animationProgress.value

            if (progress < 0.5f) {
                // First half: text fades out
                Text(
                    text = name,
                    style = TextStyle(
                        fontSize = 40.sp,
                        color = Color.Cyan.copy(alpha = 1f - progress * 2)
                    )
                )
            } else {
                // Second half: glyph fades in
                GlyphVisual(
                    glyph = targetGlyph,
                    alpha = (progress - 0.5f) * 2,
                    scale = 1f + (progress - 0.5f) * 0.2f
                )
            }
        }
    }

    /**
     * Animate glyph entrance (zoom + fade)
     */
    @Composable
    fun GlyphEntrance(
        glyph: GlyphIdentity,
        durationMs: Int = 800,
        onComplete: () -> Unit = {}
    ) {
        val progress = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            progress.animateTo(
                1f,
                animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing)
            )
            onComplete()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            GlyphVisual(
                glyph = glyph,
                alpha = progress.value,
                scale = 0.3f + progress.value * 0.7f
            )
        }
    }

    /**
     * Animate glyph exit (shrink + fade)
     */
    @Composable
    fun GlyphExit(
        glyph: GlyphIdentity,
        durationMs: Int = 800,
        onComplete: () -> Unit = {}
    ) {
        val progress = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            progress.animateTo(
                1f,
                animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing)
            )
            onComplete()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            GlyphVisual(
                glyph = glyph,
                alpha = 1f - progress.value,
                scale = 1f - progress.value * 0.7f
            )
        }
    }

    /**
     * Continuous glyph pulse animation
     */
    @Composable
    fun GlyphPulse(
        glyph: GlyphIdentity,
        modifier: Modifier = Modifier
    ) {
        val time = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            while (true) {
                time.animateTo(
                    1f,
                    animationSpec = tween(durationMillis = 2000)
                )
                time.snapTo(0f)
            }
        }

        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            val pulse = (sin(time.value * 3.14f) + 1) / 2
            GlyphVisual(
                glyph = glyph,
                alpha = 0.7f + pulse * 0.3f,
                scale = 0.9f + pulse * 0.1f
            )
        }
    }

    /**
     * Glyph rotation animation
     */
    @Composable
    fun GlyphRotation(
        glyph: GlyphIdentity,
        modifier: Modifier = Modifier,
        rotationsPerSecond: Float = 0.5f
    ) {
        val rotation = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            while (true) {
                rotation.animateTo(
                    360f,
                    animationSpec = tween(durationMillis = (1000 / rotationsPerSecond).toInt())
                )
                rotation.snapTo(0f)
            }
        }

        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            GlyphVisual(
                glyph = glyph,
                rotation = rotation.value
            )
        }
    }
}

/**
 * Renders a glyph with animation properties
 */
@Composable
fun GlyphVisual(
    glyph: GlyphIdentity,
    alpha: Float = 1f,
    scale: Float = 1f,
    rotation: Float = 0f
) {
    Box(
        modifier = Modifier
            .fillMaxSize(scale)
            .background(
                color = Color.Cyan.copy(alpha = alpha * 0.1f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = glyph.name,
            style = TextStyle(
                fontSize = (40 * scale).sp,
                color = Color.Cyan.copy(alpha = alpha)
            )
        )

        // Glyph ID subtitle
        Text(
            text = "ID: ${glyph.glyphId}",
            style = TextStyle(
                fontSize = (12 * scale).sp,
                color = Color.Gray.copy(alpha = alpha * 0.7f)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Combined sequence: name morphing → glyph entrance → pulse
 */
@Composable
fun FullGlyphSequence(
    name: String,
    glyph: GlyphIdentity,
    onComplete: () -> Unit = {}
) {
    val stage = remember { mutableStateOf(0) }

    when (stage.value) {
        0 -> {
            GlyphAnimator().NameToGlyphAnimation(name, glyph) {
                stage.value = 1
            }
        }
        1 -> {
            GlyphAnimator().GlyphPulse(glyph)
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                onComplete()
            }
        }
    }
}
