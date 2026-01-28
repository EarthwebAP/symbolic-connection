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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CallKeypadScreen(onCall: (String) -> Unit, onClose: () -> Unit) {
    val dialedNumber = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Close button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Resonance Dial",
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

        Spacer(modifier = Modifier.height(24.dp))

        // Display screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xFF1A1A1A))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (dialedNumber.value.isEmpty()) "Enter number..." else dialedNumber.value,
                color = Color.Cyan,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Keypad buttons
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(12) { index ->
                val (label, letters) = when (index) {
                    0 -> Pair("1", "")
                    1 -> Pair("2", "ABC")
                    2 -> Pair("3", "DEF")
                    3 -> Pair("4", "GHI")
                    4 -> Pair("5", "JKL")
                    5 -> Pair("6", "MNO")
                    6 -> Pair("7", "PQRS")
                    7 -> Pair("8", "TUV")
                    8 -> Pair("9", "WXYZ")
                    9 -> Pair("*", "")
                    10 -> Pair("0", "+")
                    11 -> Pair("#", "")
                    else -> Pair("", "")
                }

                KeypadButton(
                    label = label,
                    letters = letters,
                    onClick = {
                        dialedNumber.value += label
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Backspace and Call buttons
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Backspace
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A1A))
                    .clickable {
                        if (dialedNumber.value.isNotEmpty()) {
                            dialedNumber.value = dialedNumber.value.dropLast(1)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "←",
                    color = Color.Cyan,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Call button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .clip(CircleShape)
                    .background(Color.Cyan)
                    .clickable {
                        if (dialedNumber.value.isNotEmpty()) {
                            onCall(dialedNumber.value)
                            dialedNumber.value = ""
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Call,
                    contentDescription = "Call",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun KeypadButton(label: String, letters: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Color(0xFF1A1A1A))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = Color.Cyan,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    color = Color(0xFF008B8B),
                    fontSize = 10.sp
                )
            }
        }
    }
}
