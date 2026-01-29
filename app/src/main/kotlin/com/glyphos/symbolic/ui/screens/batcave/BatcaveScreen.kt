package com.glyphos.symbolic.ui.screens.batcave

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.glyphos.symbolic.ui.screens.batcave.BatcaveViewModel

@Composable
fun BatcaveScreen(
    navController: NavController,
    viewModel: BatcaveViewModel = hiltViewModel()
) {
    val activeBatcave by viewModel.activeBatcave.collectAsState()
    val sealedMode by viewModel.sealedMode.collectAsState()
    val notificationsBlocked by viewModel.notificationsBlocked.collectAsState()

    val thoughtText = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Header with exit option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Exit", tint = Color.Cyan)
                }
                Column {
                    Text(
                        text = "Batcave",
                        color = Color.Cyan,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (sealedMode) "🔒 SEALED" else "OPEN",
                        color = if (sealedMode) Color.Cyan else Color(0xFF008B8B),
                        fontSize = 10.sp
                    )
                }
            }

            IconButton(onClick = { /* Settings */ }) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.Cyan)
            }
        }

        // Status indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(Color(0xFF1A1A1A))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusIndicator(
                label = "Sealed",
                active = sealedMode,
                color = Color.Cyan
            )
            StatusIndicator(
                label = "Silent",
                active = notificationsBlocked,
                color = Color(0xFF00CCCC)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Infinite canvas area for thoughts/notes
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(Color(0xFF0D0D0D))
                .padding(12.dp)
        ) {
            Text(
                text = "Infinite Canvas",
                color = Color(0xFF008B8B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas placeholder - would be interactive infinite zoom
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Primordial Zoom Canvas\nDrag & drop thoughts, files, glyphs\nInfinite depth, zero pixelation",
                    color = Color(0xFF008B8B),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Record Thought",
                color = Color.Cyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = thoughtText.value,
                    onValueChange = { thoughtText.value = it },
                    placeholder = { Text("Encrypted thought...", color = Color(0xFF008B8B)) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Cyan,
                        unfocusedTextColor = Color.Cyan,
                        focusedBorderColor = Color.Cyan,
                        unfocusedBorderColor = Color(0xFF008B8B),
                        cursorColor = Color.Cyan
                    ),
                    singleLine = true
                )

                // Voice input button
                IconButton(
                    onClick = { /* Start voice recording */ },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF1A1A1A), CircleShape)
                ) {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = "Voice",
                        tint = Color.Cyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Save button
                IconButton(
                    onClick = { /* Save thought */ },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Cyan, CircleShape)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Save",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusIndicator(
    label: String,
    active: Boolean,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (active) color else Color(0xFF4D4D4D),
                    shape = CircleShape
                )
        )
        Text(
            text = label,
            color = if (active) color else Color(0xFF8B8B8B),
            fontSize = 12.sp
        )
    }
}
