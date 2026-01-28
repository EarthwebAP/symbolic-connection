package com.glyphos.symbolic.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.glyphos.symbolic.R
import com.glyphos.symbolic.ui.navigation.Screen

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val userGlyph by viewModel.userGlyph.collectAsState()
    val presence by viewModel.presence.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.Cyan,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Cover Image Display
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clickable { navController.navigate(Screen.Glyph.route) },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1A1A1A)
                ) {
                    AsyncImage(
                        model = R.drawable.glyph007_cover,
                        contentDescription = "Glyph007 Cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Presence State Card
                presence?.let { presenceState ->
                    Card(
                        modifier = Modifier.fillMaxSize(0.8f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A1A1A)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Current Presence",
                                color = Color.Cyan,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Text(
                                text = "Mode: ${presenceState.mode.name}",
                                color = Color.Cyan,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Tone: ${presenceState.emotionalTone.name}",
                                color = Color.Cyan,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Focus: ${presenceState.focusLevel.name}",
                                color = Color.Cyan,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Context: ${presenceState.socialContext.name}",
                                color = Color.Cyan,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Quick Action Buttons
                Button(
                    onClick = { navController.navigate(Screen.Rooms.route) },
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                ) {
                    Text("View Rooms")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { navController.navigate(Screen.Presence.route) },
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                ) {
                    Text("Update Presence")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { navController.navigate(Screen.Glyph.route) },
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                ) {
                    Text("View Glyph")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
