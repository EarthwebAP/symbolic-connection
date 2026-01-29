package com.glyphos.symbolic.ui.screens.glyph

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glyphos.symbolic.identity.glyph.PrimordialZoomEngine

/**
 * Infinite Zoom Screen
 * Displays the primordial zoom field with wave-based visualization
 * Allows navigation through harmonic frequencies
 */
@Composable
fun InfiniteZoomScreen(
    primordialZoomEngine: PrimordialZoomEngine,
    glyphId: String = "000"
) {
    var zoomLevel by remember { mutableStateOf(1f) }
    var posX by remember { mutableStateOf(0.5f) }
    var posY by remember { mutableStateOf(0.5f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Primordial Zoom",
                        color = Color(0xFF00FFFF),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Glyph: $glyphId | Zoom: ${(zoomLevel * 100).toInt()}%",
                        color = Color(0xFF0099FF),
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFF0D1B1B))
                ) {
                    Text(
                        text = "✨",
                        fontSize = 32.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        // Zoom Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color(0xFF0D1B1B))
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        zoomLevel = (zoomLevel * zoom).coerceIn(1f, 30000f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Wave Pattern Visualization
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color(0xFF1A1A1A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌊",
                        fontSize = (64 * zoomLevel.coerceAtMost(2f)).sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Harmonic Field Active",
                    color = Color(0xFF00FFFF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Position: (${(posX * 100).toInt()}%, ${(posY * 100).toInt()}%)",
                    color = Color(0xFF0099FF),
                    fontSize = 12.sp
                )

                Text(
                    text = "Zoom to explore micro-content",
                    color = Color(0xFF008B8B),
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color(0xFF1A1A1A))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Field Content",
                    color = Color(0xFF00FFFF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (zoomLevel > 100) {
                    Column {
                        Text(
                            text = "📝 Attached Notes",
                            color = Color(0xFF0099FF),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "At deep zoom, micro-content becomes visible",
                            color = Color(0xFF008B8B),
                            fontSize = 10.sp
                        )
                    }
                } else {
                    Column {
                        Text(
                            text = "Zoom deeper to reveal hidden layers",
                            color = Color(0xFF0099FF),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Current resolution: ${(zoomLevel * 1000).toInt()} pixels",
                            color = Color(0xFF008B8B),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { zoomLevel = 1f },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF008B8B))
            ) {
                Text("Reset", color = Color(0xFF008B8B))
            }

            Button(
                onClick = { zoomLevel = (zoomLevel * 10).coerceAtMost(30000f) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FFFF)
                )
            ) {
                Text("Zoom In", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { zoomLevel = (zoomLevel / 10).coerceAtLeast(1f) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0099FF)
                )
            ) {
                Text("Zoom Out", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
