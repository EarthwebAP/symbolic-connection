package com.glyphos.symbolic.ui.screens.security

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glyphos.symbolic.security.lens.GesturePatternUnlock

/**
 * Gesture Pattern Unlock Screen
 * Allows drawing a gesture pattern on 3x3 grid to unlock
 */
@Composable
fun GesturePatternScreen(
    gesturePatternUnlock: GesturePatternUnlock,
    onUnlockSuccess: () -> Unit = {},
    mode: GesturePatternUnlock.PatternMode = GesturePatternUnlock.PatternMode.VERIFY
) {
    val patternLocked by gesturePatternUnlock.patternLocked.collectAsState()
    val currentPattern by gesturePatternUnlock.currentPattern.collectAsState()
    val patternAttempts by gesturePatternUnlock.patternAttempts.collectAsState()
    val security = gesturePatternUnlock.getPatternSecurity()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(
            text = when (mode) {
                GesturePatternUnlock.PatternMode.CREATE -> "Create Pattern"
                GesturePatternUnlock.PatternMode.VERIFY -> "Draw Pattern to Unlock"
                GesturePatternUnlock.PatternMode.UPDATE -> "Update Pattern"
            },
            color = Color(0xFF00FFFF),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3x3 Pattern Grid
        PatternGrid(
            currentPattern = currentPattern,
            onNodeTap = { nodeId ->
                gesturePatternUnlock.recordPoint(nodeId, 0f, 0f)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Pattern Status
        Text(
            text = "Pattern: ${currentPattern.joinToString("-")}",
            color = Color(0xFF0099FF),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        if (patternAttempts > 0) {
            Text(
                text = "Attempts: $patternAttempts",
                color = if (patternAttempts >= 5) Color(0xFFFF0000) else Color(0xFF008B8B),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { gesturePatternUnlock.clearPattern() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF008B8B))
            ) {
                Text("Clear", color = Color(0xFF008B8B))
            }

            Button(
                onClick = {
                    if (gesturePatternUnlock.verifyPattern()) {
                        onUnlockSuccess()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FFFF)
                ),
                enabled = currentPattern.size >= 4
            ) {
                Text("Unlock", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Security Info
        Text(
            text = "Security: ${security.strength}",
            color = Color(0xFF0099FF),
            fontSize = 12.sp
        )
        Text(
            text = "Estimated crack time: ${security.estimatedCrackTime}",
            color = Color(0xFF008B8B),
            fontSize = 10.sp
        )
    }
}

@Composable
fun PatternGrid(
    currentPattern: List<Int>,
    onNodeTap: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .size(300.dp)
            .background(Color(0xFF1A1A1A), shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .border(2.dp, Color(0xFF00FFFF), shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
    ) {
        repeat(3) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                repeat(3) { col ->
                    val nodeId = row * 3 + col
                    val isSelected = nodeId in currentPattern
                    val isInPattern = currentPattern.contains(nodeId)
                    val position = currentPattern.indexOf(nodeId)

                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .weight(1f)
                            .background(
                                color = if (isSelected) Color(0xFF00FFFF) else Color(0xFF333333),
                                shape = CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = if (isSelected) Color(0xFF00FFFF) else Color(0xFF0099FF),
                                shape = CircleShape
                            )
                            .clickable { onNodeTap(nodeId) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isInPattern) {
                            Text(
                                text = (position + 1).toString(),
                                color = if (isSelected) Color.Black else Color(0xFF00FFFF),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
