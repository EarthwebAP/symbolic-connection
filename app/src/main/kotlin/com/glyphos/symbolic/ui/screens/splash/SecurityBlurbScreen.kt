package com.glyphos.symbolic.ui.screens.splash

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SecurityBlurbScreen(onBlurbComplete: () -> Unit) {
    var showContent = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500) // Brief fade-in delay
        showContent.value = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
            .clickable { onBlurbComplete() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "GLYPH007",
                color = Color.Cyan,
                fontSize = 36.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Security
            BlurbSection(
                title = "🛡️ SECURITY",
                content = "Your glyph is encrypted and unique to you. All communications are secured with end-to-end encryption. Your identity is primordial—never compromised.",
                show = showContent.value
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Safety
            BlurbSection(
                title = "🔒 SAFETY",
                content = "Control who sees you, when, and how much. Resonance-based notifications respect your presence. No intrusive alerts. No surveillance.",
                show = showContent.value
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Collaboration
            BlurbSection(
                title = "🤝 COLLABORATION",
                content = "Rooms support symbolic AI assistants to enhance your work. Collaborate seamlessly with primordial waves. Your data, your rules.",
                show = showContent.value
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Freedom
            BlurbSection(
                title = "✨ FREEDOM",
                content = "No ads. No tracking. No corporate overlords. You own your identity. You own your data. You own your presence. Complete digital sovereignty.",
                show = showContent.value
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Footer
            Text(
                text = "Tap anywhere to continue",
                color = Color(0xFF008B8B),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BlurbSection(title: String, content: String, show: Boolean) {
    if (show) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .padding(16.dp)
                .background(Color(0xFF1A1A1A))
                .padding(16.dp)
        ) {
            Text(
                text = title,
                color = Color.Cyan,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = content,
                color = Color(0xFF00FFFF).copy(alpha = 0.8f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}
