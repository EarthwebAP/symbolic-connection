package com.glyphos.symbolic.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.glyphos.symbolic.data.CipherCodec
import com.glyphos.symbolic.data.CipherMessage
import com.glyphos.symbolic.ui.components.GlyphDisplay

/**
 * Cipher Composer Screen
 * Allows embedding messages inside infinite zoom glyphs
 * 1. User enters plaintext message
 * 2. Opens infinite zoom on personal glyph
 * 3. Zooms to embedding depth (1% - 100%)
 * 4. Message appears at that zoom level
 * 5. Zooms back out and sends as cipher
 */
@Composable
fun CipherComposerScreen(
    contactId: String,
    contactName: String,
    navController: NavController,
    onSendCipher: (CipherMessage) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var zoomDepth by remember { mutableFloatStateOf(50f) }
    var isZoomMode by remember { mutableStateOf(false) }
    var glyphId by remember { mutableStateOf("glyph-cipher-${System.currentTimeMillis()}") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.Cyan)
                }
                Column {
                    Text(
                        text = "Cipher to $contactName",
                        color = Color.Cyan,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Embed message in glyph",
                        color = Color(0xFF008B8B),
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(
                onClick = {
                    if (messageText.isNotEmpty()) {
                        val seed = CipherCodec.generateCipherSeed()
                        val encrypted = CipherCodec.encode(messageText, seed)
                        val cipher = CipherMessage(
                            messageId = "cipher-${System.currentTimeMillis()}",
                            glyphId = glyphId,
                            senderId = "current-user-id",
                            recipientId = contactId,
                            plaintext = messageText,
                            zoomDepth = zoomDepth,
                            xCoordinate = (Math.random() * 1000),
                            yCoordinate = (Math.random() * 1000),
                            encryptedData = encrypted,
                            timestamp = System.currentTimeMillis()
                        )
                        onSendCipher(cipher)
                        navController.popBackStack()
                    }
                }
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Send", tint = Color.Cyan)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isZoomMode) {
            // Message composition view
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Your Secret Message",
                        color = Color.Cyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Enter secret message...", color = Color(0xFF008B8B)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Cyan,
                            unfocusedTextColor = Color.Cyan,
                            focusedBorderColor = Color.Cyan,
                            unfocusedBorderColor = Color(0xFF008B8B),
                            cursorColor = Color.Cyan
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Glyph selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clickable { isZoomMode = true },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Embedding Glyph",
                        color = Color.Cyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    GlyphDisplay(
                        glyphId = glyphId.substring(0, 8),
                        label = "Your Glyph",
                        size = 96.dp,
                        glowIntensity = 0.8f
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Tap to enter zoom mode",
                        color = Color(0xFF008B8B),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Zoom depth slider
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Embedding Depth",
                            color = Color.Cyan,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${zoomDepth.toInt()}%",
                            color = Color.Cyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Simple slider mock (would use androidx.compose.material3.Slider in real app)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color(0xFF008B8B), shape = RoundedCornerShape(2.dp))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Deeper zoom = more hidden",
                        color = Color(0xFF008B8B),
                        fontSize = 10.sp
                    )
                }
            }
        } else {
            // Infinite zoom view placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0A0A)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Infinite Zoom Mode",
                        color = Color.Cyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Zoom to depth: ${zoomDepth.toInt()}%",
                        color = Color(0xFF008B8B),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Message will appear at this zoom level",
                        color = Color.Cyan,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Exit zoom mode button
                    Card(
                        modifier = Modifier
                            .clickable { isZoomMode = false }
                            .padding(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Cyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Exit Zoom Mode",
                            color = Color.Black,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
