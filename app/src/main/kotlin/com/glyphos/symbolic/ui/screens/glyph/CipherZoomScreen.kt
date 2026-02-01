package com.glyphos.symbolic.ui.screens.glyph

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.glyphos.symbolic.identity.glyph.InfiniteZoomEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CipherZoomViewModel @Inject constructor(
    private val zoomEngine: InfiniteZoomEngine
) : androidx.lifecycle.ViewModel() {
    private val _zoomLevel = MutableStateFlow(1f)
    val zoomLevel: StateFlow<Float> = _zoomLevel

    private val _embeddedMessage = MutableStateFlow<String?>(null)
    val embeddedMessage: StateFlow<String?> = _embeddedMessage

    fun updateZoom(glyphId: String, factor: Double) {
        val newZoom = zoomEngine.zoom(glyphId, factor)
        _zoomLevel.value = newZoom.toFloat()
    }

    fun embedMessage(glyphId: String, message: String) {
        _embeddedMessage.value = message
    }

    fun getZoomLevel(): Float = _zoomLevel.value
}

@Composable
fun CipherZoomScreen(
    glyphId: String,
    contactName: String,
    messageToEmbed: String,
    navController: NavController,
    viewModel: CipherZoomViewModel = hiltViewModel(),
    onEmbeddingComplete: (Float) -> Unit
) {
    val zoomLevel by viewModel.zoomLevel.collectAsState()
    val embeddedMessage by viewModel.embeddedMessage.collectAsState()
    var showEmbedDialog by remember { mutableStateOf(true) }

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
                        text = "Cipher Zoom",
                        color = Color.Cyan,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Zoom: ${(zoomLevel * 100).toInt()}%",
                        color = Color(0xFF008B8B),
                        fontSize = 12.sp
                    )
                }
            }

            // Embed button
            if (embeddedMessage == null) {
                IconButton(
                    onClick = {
                        viewModel.embedMessage(glyphId, messageToEmbed)
                        showEmbedDialog = true
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Embed Message", tint = Color.Cyan)
                }
            }
        }

        // Main zoom visualization
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Zoom indicator
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            Color.Cyan.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(60.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(zoomLevel * 100).toInt()}%",
                        color = Color.Cyan,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Embedded message display at zoom level
                if (embeddedMessage != null && zoomLevel >= 0.5f) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Cyan.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "📌 Message appears at this depth",
                                color = Color.Cyan,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = messageToEmbed,
                                color = Color.Cyan,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Card(
                        modifier = Modifier
                            .clickable {
                                viewModel.updateZoom(glyphId, 0.9)
                            }
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Cyan.copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Zoom Out ÷",
                            color = Color.Black,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Card(
                        modifier = Modifier
                            .clickable {
                                viewModel.updateZoom(glyphId, 1.5)
                            }
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Cyan.copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Zoom In ×",
                            color = Color.Black,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Card(
                        modifier = Modifier
                            .clickable {
                                if (embeddedMessage != null) {
                                    onEmbeddingComplete(zoomLevel)
                                    navController.popBackStack()
                                }
                            }
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF008B8B)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "✓ Send",
                            color = Color.Black,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
