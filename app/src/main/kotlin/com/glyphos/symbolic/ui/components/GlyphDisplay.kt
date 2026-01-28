package com.glyphos.symbolic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Glyph Display Component
 * Non-interactive placeholder for letter-to-glyph transformations (upcoming)
 */
@Composable
fun GlyphDisplay(
    glyphId: String = "000",
    label: String = "Personal Glyph",
    size: Float = 64f,
    glowIntensity: Float = 0.0f,
    isAnimated: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .background(
                    color = Color.Cyan.copy(alpha = 0.2f + (glowIntensity * 0.8f)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = glyphId,
                color = Color.Cyan,
                fontSize = (size / 2).sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color(0xFF008B8B),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GlyphRow(glyphs: List<String>) {
    androidx.compose.foundation.layout.Row(
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
        modifier = Modifier.androidx.compose.foundation.layout.fillMaxWidth()
    ) {
        glyphs.forEach { glyphId ->
            GlyphDisplay(glyphId = glyphId, size = 48f)
        }
    }
}

@Composable
fun GlyphMiniView(glyphId: String) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(Color.Cyan.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = glyphId.take(1),
            color = Color.Cyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
