package com.glyphos.symbolic.ui.screens.rooms

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ResonanceContact(
    val id: Int,
    val name: String,
    val glyphMetric: Int,
    val lastSeen: String
)

@Composable
fun ResonanceDirectoryScreen(
    onCall: (String) -> Unit,
    onMessage: (String) -> Unit,
    onClose: () -> Unit
) {
    val contacts = listOf(
        ResonanceContact(1, "Aurora", 87, "2 min ago"),
        ResonanceContact(2, "Cipher", 92, "1 hour ago"),
        ResonanceContact(3, "Nexus", 76, "4 hours ago"),
        ResonanceContact(4, "Phantom", 65, "1 day ago"),
        ResonanceContact(5, "Prism", 88, "30 min ago")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Resonance Directory",
                color = Color.Cyan,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A1A))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = Color.Cyan,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Text(
            text = "${contacts.size} contacts in your primordial network",
            color = Color(0xFF008B8B),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(contacts) { contact ->
                ResonanceContactCard(
                    contact = contact,
                    onCall = { onCall(contact.name) },
                    onMessage = { onMessage(contact.name) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ResonanceContactCard(
    contact: ResonanceContact,
    onCall: () -> Unit,
    onMessage: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Cyan.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.first().toString(),
                    color = Color.Cyan,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    color = Color.Cyan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Resonance: ",
                        color = Color(0xFF008B8B),
                        fontSize = 12.sp
                    )
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .width((contact.glyphMetric / 100f * 60).dp)
                            .background(Color.Cyan)
                    )
                    Text(
                        text = "${contact.glyphMetric}%",
                        color = Color.Cyan,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Text(
                    text = "Last seen: ${contact.lastSeen}",
                    color = Color(0xFF008B8B),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Cyan.copy(alpha = 0.2f))
                        .clickable { onCall() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Call,
                        contentDescription = "Call",
                        tint = Color.Cyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Cyan.copy(alpha = 0.2f))
                        .clickable { onMessage() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Message,
                        contentDescription = "Message",
                        tint = Color.Cyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
