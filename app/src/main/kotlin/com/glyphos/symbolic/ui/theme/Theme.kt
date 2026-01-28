package com.glyphos.symbolic.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Glyph007 Dark Theme with Cyan and Black color scheme
 */
@Composable
fun Glyph007Theme(
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = Cyan,
        onPrimary = Black,
        primaryContainer = DarkCyan,
        onPrimaryContainer = Cyan,
        secondary = DarkCyan,
        onSecondary = Cyan,
        secondaryContainer = Black,
        onSecondaryContainer = Cyan,
        tertiary = LightGray,
        onTertiary = Cyan,
        tertiaryContainer = DarkGray,
        onTertiaryContainer = Cyan,
        error = Color(0xFFFF5555),
        onError = Black,
        background = Black,
        onBackground = Cyan,
        surface = DarkGray,
        onSurface = Cyan,
        surfaceVariant = LightGray,
        onSurfaceVariant = Cyan,
        outline = DarkCyan,
        outlineVariant = LightGray,
        scrim = Black
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
