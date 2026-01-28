package com.glyphos.symbolic.ui.screens.presence

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.glyphos.symbolic.core.models.PresenceMode
import com.glyphos.symbolic.core.models.EmotionalTone
import com.glyphos.symbolic.core.models.FocusLevel
import com.glyphos.symbolic.core.models.SocialContext

@Composable
fun PresenceScreen(navController: NavController, viewModel: PresenceViewModel = hiltViewModel()) {
    val presence by viewModel.currentPresence.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Configure Presence",
            color = Color.Cyan,
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Mode Selection
        ConfigSection(
            title = "Presence Mode",
            items = PresenceMode.values().map { it.name },
            selectedItem = presence.mode.name,
            onSelected = { selected ->
                viewModel.updateMode(PresenceMode.valueOf(selected))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Emotional Tone Selection
        ConfigSection(
            title = "Emotional Tone",
            items = EmotionalTone.values().map { it.name },
            selectedItem = presence.emotionalTone.name,
            onSelected = { selected ->
                viewModel.updateTone(EmotionalTone.valueOf(selected))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Focus Level Selection
        ConfigSection(
            title = "Focus Level",
            items = FocusLevel.values().map { it.name },
            selectedItem = presence.focusLevel.name,
            onSelected = { selected ->
                viewModel.updateFocus(FocusLevel.valueOf(selected))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Social Context Selection
        ConfigSection(
            title = "Social Context",
            items = SocialContext.values().map { it.name },
            selectedItem = presence.socialContext.name,
            onSelected = { selected ->
                viewModel.updateSocial(SocialContext.valueOf(selected))
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Save Button
        Button(
            onClick = { viewModel.save() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Presence")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ConfigSection(
    title: String,
    items: List<String>,
    selectedItem: String,
    onSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = Color.Cyan,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelected(item) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedItem == item,
                        onClick = { onSelected(item) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.Cyan,
                            unselectedColor = Color(0xFF008B8B)
                        )
                    )
                    Text(
                        text = item,
                        color = Color.Cyan,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
