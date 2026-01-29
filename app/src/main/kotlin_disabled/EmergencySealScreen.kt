package com.glyphos.symbolic.ui.screens.security

import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glyphos.symbolic.security.EmergencySeal

/**
 * Emergency Seal Screen
 * Displays full-screen lockout when emergency seal is activated
 */
@Composable
fun EmergencySealScreen(
    emergencySeal: EmergencySeal
) {
    val isSealedActive by emergencySeal.isSealedActive.collectAsState()
    val sealReason by emergencySeal.sealReason.collectAsState()
    val sealTriggerTime by emergencySeal.sealTriggerTime.collectAsState()

    if (!isSealedActive) {
        return
    }

    val sealDurationMs = 300000L // 5 minutes
    val timeSinceSeal = System.currentTimeMillis() - sealTriggerTime
    val remainingMs = maxOf(0L, sealDurationMs - timeSinceSeal)
    val remainingSeconds = (remainingMs / 1000).toInt()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = isSealedActive,
                enter = scaleIn(initialScale = 0.5f),
                exit = scaleOut(targetScale = 0.5f)
            ) {
                Text(
                    text = "🔒",
                    fontSize = 96.sp,
                    color = Color(0xFF00FFFF)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "EMERGENCY SEAL ACTIVATED",
                color = Color(0xFF00FFFF),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = sealReason,
                color = Color(0xFF008B8B),
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Auto-release in: $remainingSeconds seconds",
                color = Color(0xFF0099FF),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "App is locked. Awaiting verification...",
                color = Color(0xFF666666),
                fontSize = 12.sp
            )
        }
    }
}
