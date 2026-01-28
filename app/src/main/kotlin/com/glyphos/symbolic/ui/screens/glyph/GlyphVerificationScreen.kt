package com.glyphos.symbolic.ui.screens.glyph

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glyphos.symbolic.hardware.GlyphScannerEngine

/**
 * Glyph Verification Screen
 * Allows scanning and verifying glyphs for identity confirmation
 */
@Composable
fun GlyphVerificationScreen(
    glyphScannerEngine: GlyphScannerEngine,
    onVerificationSuccess: (glyphId: String) -> Unit = {}
) {
    var isScanning by remember { mutableStateOf(false) }
    var verificationResult by remember { mutableStateOf<String?>(null) }
    var confidenceScore by remember { mutableStateOf(0f) }

    val isCameraActive by glyphScannerEngine.isCameraActive.collectAsState()
    val currentFrame by glyphScannerEngine.currentFrame.collectAsState()
    val featureVector by glyphScannerEngine.featureVector.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Glyph Verification",
            color = Color(0xFF00FFFF),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Camera Preview Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFF1A1A1A))
                .border(2.dp, Color(0xFF00FFFF)),
            contentAlignment = Alignment.Center
        ) {
            if (isScanning && isCameraActive) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "📷",
                        fontSize = 64.sp,
                        modifier = Modifier.padding(16.dp)
                    )

                    Text(
                        text = "Hold glyph in frame",
                        color = Color(0xFF0099FF),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = confidenceScore / 100f,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(4.dp),
                        color = Color(0xFF00FFFF),
                        trackColor = Color(0xFF333333)
                    )
                }
            } else {
                Text(
                    text = "📸 Ready to scan",
                    color = Color(0xFF0099FF),
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Verification Status
        if (verificationResult != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1B1B))
                    .border(2.dp, Color(0xFF008B8B))
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (confidenceScore > 85) "✓ Verification Successful" else "✗ Verification Failed",
                        color = if (confidenceScore > 85) Color(0xFF00FF00) else Color(0xFFFF0000),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Confidence: ${confidenceScore.toInt()}%",
                        color = Color(0xFF00FFFF),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Glyph ID: $verificationResult",
                        color = Color(0xFF0099FF),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFF0D1B1B))
                    .border(2.dp, Color(0xFF333333)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isScanning) "Analyzing..." else "Ready for verification",
                    color = Color(0xFF008B8B),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Feature Vector Display (if available)
        if (featureVector.isNotEmpty()) {
            Text(
                text = "Features Detected: ${featureVector.size}",
                color = Color(0xFF0099FF),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color(0xFF1A1A1A))
                    .border(1.dp, Color(0xFF008B8B))
                    .padding(4.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
            ) {
                featureVector.take(8).forEachIndexed { index, value ->
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(
                                color = Color(0xFF00FFFF).copy(
                                    alpha = (value / 255f).coerceIn(0f, 1f)
                                )
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { isScanning = !isScanning },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) Color(0xFFFF0000) else Color(0xFF00FFFF)
                )
            ) {
                Text(
                    text = if (isScanning) "Stop" else "Start",
                    color = if (isScanning) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    verificationResult = null
                    confidenceScore = 0f
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF008B8B))
            ) {
                Text("Clear", color = Color(0xFF008B8B))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (confidenceScore > 85 && verificationResult != null) {
            Button(
                onClick = { onVerificationSuccess(verificationResult!!) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FF00)
                )
            ) {
                Text(
                    text = "Confirm Identity",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
