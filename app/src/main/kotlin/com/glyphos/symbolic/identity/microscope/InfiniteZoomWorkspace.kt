package com.glyphos.symbolic.identity.microscope

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.glyphos.symbolic.core.models.EmbeddedContent
import com.glyphos.symbolic.core.models.GlyphIdentity
import kotlin.math.log10

/**
 * PHASE 2: Glyph Microscope - Infinite Zoom
 *
 * Zoomable glyph that reveals nested workspace when zoomed in >5x.
 * Zoom levels: 1x (exterior) → 5x (threshold) → 30,000x (deep workspace)
 */
class InfiniteZoomWorkspace {

    data class ZoomLevel(
        val factor: Float,
        val description: String,
        val showsWorkspace: Boolean
    )

    @Composable
    fun ZoomableGlyph(
        glyph: GlyphIdentity,
        content: List<EmbeddedContent> = emptyList(),
        onZoomChanged: (Float) -> Unit = {}
    ) {
        val zoomLevel = remember { mutableStateOf(1f) }
        val offsetX = remember { mutableStateOf(0f) }
        val offsetY = remember { mutableStateOf(0f) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomLevel.value = (zoomLevel.value * zoom).coerceIn(1f, 30000f)
                        offsetX.value += pan.x
                        offsetY.value += pan.y
                        onZoomChanged(zoomLevel.value)
                    }
                }
                .graphicsLayer {
                    scaleX = zoomLevel.value
                    scaleY = zoomLevel.value
                    translationX = offsetX.value
                    translationY = offsetY.value
                },
            contentAlignment = Alignment.Center
        ) {
            if (zoomLevel.value < 5f) {
                // Exterior view
                GlyphExterior(glyph, zoomLevel.value)
            } else if (zoomLevel.value < 100f) {
                // Transitional view
                GlyphTransition(glyph, zoomLevel.value)
            } else {
                // Workspace interior
                GlyphWorkspace(glyph, content, zoomLevel.value)
            }
        }

        // Zoom level indicator
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.TopStart
        ) {
            Text(
                text = "Zoom: ${String.format("%.1f", zoomLevel.value)}x",
                style = TextStyle(color = Color.Gray, fontSize = 12.sp)
            )
        }
    }

    @Composable
    private fun GlyphExterior(glyph: GlyphIdentity, zoom: Float) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Glyph exterior visual
            Text(
                text = "◉ ${glyph.name}",
                style = TextStyle(
                    fontSize = (40 / zoom).sp,
                    color = Color.Cyan
                )
            )

            // Hint to zoom
            if (zoom < 2f) {
                Text(
                    text = "Pinch to zoom →",
                    style = TextStyle(
                        fontSize = (14 / zoom).sp,
                        color = Color.Gray
                    ),
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    @Composable
    private fun GlyphTransition(glyph: GlyphIdentity, zoom: Float) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = glyph.name,
                    style = TextStyle(
                        fontSize = (30 / (zoom / 5f)).sp,
                        color = Color.Cyan
                    )
                )

                Text(
                    text = "Approaching workspace...",
                    style = TextStyle(
                        fontSize = (12 / (zoom / 5f)).sp,
                        color = Color.Yellow
                    )
                )
            }
        }
    }

    @Composable
    private fun GlyphWorkspace(
        glyph: GlyphIdentity,
        content: List<EmbeddedContent>,
        zoom: Float
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color(red = 0.05f, green = 0.1f, blue = 0.15f)
                ),
            contentAlignment = Alignment.TopStart
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Workspace header
                Text(
                    text = "Glyph Workspace: ${glyph.name}",
                    style = TextStyle(
                        fontSize = ((18 / log10(zoom.toDouble())).toInt().coerceAtLeast(12)).sp,
                        color = Color.Cyan
                    )
                )

                // Content items
                content.forEach { item ->
                    when (item) {
                        is EmbeddedContent.Note -> NoteItem(item)
                        is EmbeddedContent.File -> FileItem(item)
                        is EmbeddedContent.MessageRef -> MessageItem(item)
                        is EmbeddedContent.MicroThread -> ThreadItem(item)
                    }
                }

                // Zoom-out instruction
                Text(
                    text = "Pinch to zoom out",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = Color.Gray
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun NoteItem(note: EmbeddedContent.Note) {
    Box(
        modifier = Modifier
            .fillMaxSize(0.9f)
            .background(Color(red = 0.1f, green = 0.1f, blue = 0.2f))
    ) {
        Text(
            text = note.text,
            style = TextStyle(color = Color.White, fontSize = 12.sp)
        )
    }
}

@Composable
private fun FileItem(file: EmbeddedContent.File) {
    Text(
        text = "📄 ${file.path}",
        style = TextStyle(color = Color.Green, fontSize = 11.sp)
    )
}

@Composable
private fun MessageItem(message: EmbeddedContent.MessageRef) {
    Text(
        text = "💬 Message: ${message.messageId}",
        style = TextStyle(color = Color.Magenta, fontSize = 11.sp)
    )
}

@Composable
private fun ThreadItem(thread: EmbeddedContent.MicroThread) {
    Text(
        text = "🧵 Thread: ${thread.title} (${thread.messages.size} msgs)",
        style = TextStyle(color = Color.Yellow, fontSize = 11.sp)
    )
}
