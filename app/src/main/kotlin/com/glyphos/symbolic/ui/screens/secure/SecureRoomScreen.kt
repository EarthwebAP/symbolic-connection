package com.glyphos.symbolic.ui.screens.secure

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SecureRoomScreen(navController: NavController) {
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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.Cyan)
            }
            Icon(Icons.Filled.Lock, contentDescription = "Secure", tint = Color.Cyan, modifier = Modifier)
            Column {
                Text(
                    text = "Secure Digital Room",
                    color = Color.Cyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View-Only • Ephemeral • Encrypted",
                    color = Color(0xFF008B8B),
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0xFF1A1A1A))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SecurityFeature("No Copy", "Content cannot be copied")
            SecurityFeature("No Screenshot", "Capture protection active")
            SecurityFeature("No Export", "Media stays in room")
            SecurityFeature("Auto-Delete", "Ephemeral after 24h")
            SecurityFeature("Zero Notifications", "Silent access")
            SecurityFeature("AI Blocked", "No AI processing")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0xFF0D0D0D)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = "Secure",
                    tint = Color.Cyan,
                    modifier = Modifier.height(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Room is Empty",
                    color = Color.Cyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Shared media will appear here\nAll content is encrypted and isolated",
                    color = Color(0xFF008B8B),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Room info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0xFF1A1A1A))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Created", color = Color(0xFF008B8B), fontSize = 10.sp)
                    Text("Just now", color = Color.Cyan, fontSize = 10.sp)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Expires", color = Color(0xFF008B8B), fontSize = 10.sp)
                    Text("24 hours", color = Color.Cyan, fontSize = 10.sp)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Participants", color = Color(0xFF008B8B), fontSize = 10.sp)
                    Text("2", color = Color.Cyan, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun SecurityFeature(title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .height(2.dp)
                .weight(0.3f)
                .background(Color.Cyan)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(description, color = Color(0xFF008B8B), fontSize = 10.sp)
        }
    }
}
