package com.glyphos.symbolic.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    val context = LocalContext.current

    var showLogo = remember { mutableStateOf(true) }
    var showFrontDoor = remember { mutableStateOf(false) }
    var showDoorOpening = remember { mutableStateOf(false) }
    var showSphere = remember { mutableStateOf(false) }
    var sphereTapped = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 0-3s: Show logo
        delay(3000)
        showLogo.value = false

        // 3-4.5s: Fade logo out, front door fades in
        delay(1500)
        showFrontDoor.value = true

        // 6-7.5s: Fade front door out, door opening fades in
        delay(3000)
        showFrontDoor.value = false
        delay(1500)
        showDoorOpening.value = true

        // 9-10.5s: Fade door opening out, sphere fades in
        delay(3000)
        showDoorOpening.value = false
        delay(1500)
        showSphere.value = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // GlyphCP Logo (0-3s, fade out during 3-4.5s)
        AnimatedVisibility(
            visible = showLogo.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AsyncImage(
                model = context.resources.getIdentifier("glyph_cp_logo", "drawable", context.packageName),
                contentDescription = "GlyphCP Logo",
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Front Door (3-6s)
        AnimatedVisibility(
            visible = showFrontDoor.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AsyncImage(
                model = context.resources.getIdentifier("splash_front_door", "drawable", context.packageName),
                contentDescription = "Front Door",
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Door Opening (6-9s)
        AnimatedVisibility(
            visible = showDoorOpening.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AsyncImage(
                model = context.resources.getIdentifier("splash_door_opening", "drawable", context.packageName),
                contentDescription = "Door Opening",
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Sphere with Space (9s+, waits for tap in center)
        AnimatedVisibility(
            visible = showSphere.value && !sphereTapped.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        sphereTapped.value = true
                        onSplashComplete()
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = context.resources.getIdentifier("splash_sphere_space", "drawable", context.packageName),
                    contentDescription = "Sphere with Space",
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Show indicator while waiting for sphere tap
        if (showSphere.value && !sphereTapped.value) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.BottomCenter),
                color = Color.Cyan
            )
        }
    }
}
