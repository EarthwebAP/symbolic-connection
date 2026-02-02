package com.glyphos.symbolic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glyphos.symbolic.R
import com.glyphos.symbolic.data.CipherCodec
import com.glyphos.symbolic.data.CipherMessageItem
import com.glyphos.symbolic.data.InvokedContentType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun CipherMessageBubble(
    cipherMessage: CipherMessageItem,
    isOwn: Boolean,
    onDelete: (String) -> Unit,
    decryptMessage: (String) -> String?
) {
    var isRevealed by remember { mutableStateOf(false) }
    var revealedText by remember { mutableStateOf<String?>(null) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .clickable {
                    if (!isRevealed) {
                        isRevealed = true
                    }
                },
            colors = CardDefaults.cardColors(
                containerColor = if (isOwn) Color(0xFF004D4D) else Color(0xFF1A1A1A)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isRevealed) {
                    // Glyph with red pulsing indicator
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Generate glyph ID from message ID
                        val glyphId = cipherMessage.messageId.substring(0, minOf(8, cipherMessage.messageId.length))

                        GlyphWithMessageIndicator(
                            glyphId = glyphId,
                            label = cipherMessage.senderName.take(3),
                            size = 80.dp,
                            hasInvokedMessage = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Invoked message from ${cipherMessage.senderName}",
                            color = Color.Cyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Harmonic H${(cipherMessage.embeddingFrequency.toInt() / 440).coerceIn(1, 8)}",
                            color = Color(0xFF008B8B),
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "🔓 Tap to reveal",
                            color = Color.Cyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Revealed message
                    Text(
                        text = revealedText ?: "Unable to decrypt",
                        color = Color.Cyan,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "from ${cipherMessage.senderName}",
                        color = Color(0xFF008B8B),
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "✓ Message invoked at H${(cipherMessage.embeddingFrequency.toInt() / 440).coerceIn(1, 8)}",
                        color = Color.Cyan,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(cipherMessage.timestamp),
                        color = Color(0xFF008B8B),
                        fontSize = 10.sp
                    )

                    Row {
                        Text(
                            text = when (cipherMessage.deliveryStatus) {
                                com.glyphos.symbolic.data.DeliveryStatus.SENDING -> "⏳"
                                com.glyphos.symbolic.data.DeliveryStatus.SENT -> "✓"
                                com.glyphos.symbolic.data.DeliveryStatus.DELIVERED -> "✓✓"
                                com.glyphos.symbolic.data.DeliveryStatus.READ -> "✓✓"
                                com.glyphos.symbolic.data.DeliveryStatus.FAILED -> "✗"
                            },
                            color = Color.Cyan,
                            fontSize = 10.sp
                        )

                        if (isOwn) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFF008B8B),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onDelete(cipherMessage.messageId) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Decrypt when revealed
    if (isRevealed && revealedText == null) {
        revealedText = decryptMessage(cipherMessage.messageId)
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
