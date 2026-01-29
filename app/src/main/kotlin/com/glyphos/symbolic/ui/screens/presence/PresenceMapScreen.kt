package com.glyphos.symbolic.ui.screens.presence

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.glyphos.symbolic.ui.screens.presence.PresenceMapViewModel

@Composable
fun PresenceMapScreen(
    navController: NavController,
    viewModel: PresenceMapViewModel = hiltViewModel()
) {
    val userPresence by viewModel.userPresence.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.Cyan)
                }
                Text(
                    text = "Presence Map",
                    color = Color.Cyan,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = "Cognitive-Emotional Presence Fields",
            color = Color(0xFF008B8B),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Presence constellation visualization
        if (userPresence.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No presence signals detected\nUsers will appear here",
                    color = Color(0xFF008B8B),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(userPresence.size) { index ->
                    val entry = userPresence.entries.toList()[index]
                    val userId = entry.key
                    val presence = entry.value

                    PresenceFieldView(
                        userId = userId,
                        cognitiveName = presence.cognitive.name,
                        emotionalName = presence.emotional.name,
                        bandwidth = presence.bandwidth.name,
                        socialContext = presence.socialContext.name
                    )
                }
            }
        }
    }
}

@Composable
private fun PresenceFieldView(
    userId: String,
    cognitiveName: String,
    emotionalName: String,
    bandwidth: String,
    socialContext: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A))
            .clickable { /* Show details */ }
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Presence orb (glow intensity indicates activity)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color.Cyan.copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userId.take(1).uppercase(),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userId,
                        color = Color.Cyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Cognitive: $cognitiveName",
                        color = Color(0xFF008B8B),
                        fontSize = 10.sp
                    )

                    Text(
                        text = "Emotional: $emotionalName",
                        color = Color(0xFF008B8B),
                        fontSize = 10.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RessonanceTag(label = "Bandwidth: $bandwidth", color = Color(0xFF00CCCC))
                RessonanceTag(label = "Context: $socialContext", color = Color(0xFF004D4D))
            }
        }
    }
}

@Composable
private fun RessonanceTag(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color = color, shape = CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.Black,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
