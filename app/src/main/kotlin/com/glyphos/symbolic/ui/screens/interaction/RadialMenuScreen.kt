package com.glyphos.symbolic.ui.screens.interaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.glyphos.symbolic.interaction.RadialMenuViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadialMenuScreen(
    navController: NavController,
    viewModel: RadialMenuViewModel = hiltViewModel()
) {
    val isMenuOpen by viewModel.isMenuOpen.collectAsState()
    val currentActions by viewModel.currentActions.collectAsState()
    val selectedAction by viewModel.selectedAction.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .clickable { if (isMenuOpen) viewModel.closeMenu() }
    ) {
        if (isMenuOpen) {
            // Center point
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center)
                    .background(Color.Cyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.Black, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }

            // Radial menu slots (6)
            val radius = 180.dp
            val slotCount = minOf(currentActions.size, 6)

            for (i in 0 until slotCount) {
                val angle = (360 / slotCount) * i - 90  // Start from top
                val radians = Math.toRadians(angle.toDouble())
                val offsetX = (radius.value * cos(radians)).dp
                val offsetY = (radius.value * sin(radians)).dp

                val action = currentActions[i]
                val isSelected = selectedAction?.actionId == action.actionId

                RadialMenuSlot(
                    label = action.label,
                    isSelected = isSelected,
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.Center)
                        .offset(x = offsetX, y = offsetY)
                        .clickable {
                            viewModel.selectAction(action.actionId)
                            viewModel.executeAction(action.actionId)
                        }
                )
            }
        }
    }
}

@Composable
private fun RadialMenuSlot(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (isSelected) Color.Cyan else Color(0xFF1A1A1A),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .clickable { }
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.Black else Color.Cyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
