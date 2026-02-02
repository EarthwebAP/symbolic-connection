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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.glyphos.symbolic.identity.glyph.PrimordialZoomEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PrimordialCipherZoomViewModel @Inject constructor(
    private val primordialEngine: PrimordialZoomEngine
) : ViewModel() {
    private val _currentFrequency = MutableStateFlow(440.0)
    val currentFrequency: StateFlow<Double> = _currentFrequency

    private val _zoomLevel = MutableStateFlow(1.0)
    val zoomLevel: StateFlow<Double> = _zoomLevel

    private val _embeddedMessage = MutableStateFlow<String?>(null)
    val embeddedMessage: StateFlow<String?> = _embeddedMessage

    private val _invokingMessage = MutableStateFlow<String>("")
    val invokingMessage: StateFlow<String> = _invokingMessage

    private val _isInvokeMode = MutableStateFlow(false)
    val isInvokeMode: StateFlow<Boolean> = _isInvokeMode

    private val _selectedMediaUri = MutableStateFlow<String?>(null)
    val selectedMediaUri: StateFlow<String?> = _selectedMediaUri

    private val _selectedMediaType = MutableStateFlow<com.glyphos.symbolic.data.InvokedContentType>(
        com.glyphos.symbolic.data.InvokedContentType.TEXT
    )
    val selectedMediaType: StateFlow<com.glyphos.symbolic.data.InvokedContentType> = _selectedMediaType

    private val _fieldStats = MutableStateFlow<PrimordialZoomEngine.FieldStats?>(null)
    val fieldStats: StateFlow<PrimordialZoomEngine.FieldStats?> = _fieldStats

    fun initializeGlyph(glyphId: String, userId: String) {
        primordialEngine.initializeGlyphField(glyphId, com.glyphos.symbolic.core.contracts.UserId(userId), 440.0)
        updateStats(glyphId)
    }

    fun zoomToHarmonic(glyphId: String, harmonicLevel: Int) {
        val baseFrequency = 440.0
        val targetFrequency = baseFrequency * harmonicLevel.coerceIn(1, 8)
        val zoomRatio = primordialEngine.zoomToFrequency(glyphId, targetFrequency)
        _currentFrequency.value = targetFrequency
        _zoomLevel.value = zoomRatio
        updateStats(glyphId)
    }

    fun embedMessage(glyphId: String, message: String) {
        _embeddedMessage.value = message
    }

    fun startInvokeMode() {
        _isInvokeMode.value = true
        _invokingMessage.value = ""
    }

    fun updateInvokingMessage(text: String) {
        _invokingMessage.value = text
    }

    fun invokeMessage(glyphId: String) {
        val message = _invokingMessage.value
        if (message.isNotEmpty() || _selectedMediaUri.value != null) {
            _embeddedMessage.value = message
            _isInvokeMode.value = false
            _invokingMessage.value = ""
        }
    }

    fun selectMedia(uri: String, mediaType: com.glyphos.symbolic.data.InvokedContentType) {
        _selectedMediaUri.value = uri
        _selectedMediaType.value = mediaType
    }

    fun clearMedia() {
        _selectedMediaUri.value = null
        _selectedMediaType.value = com.glyphos.symbolic.data.InvokedContentType.TEXT
    }

    fun getEmbeddingFrequency(): Double = _currentFrequency.value
    fun getInvokedMessage(): String = _embeddedMessage.value ?: ""
    fun getSelectedMediaUri(): String? = _selectedMediaUri.value
    fun getSelectedMediaType(): com.glyphos.symbolic.data.InvokedContentType = _selectedMediaType.value

    private fun updateStats(glyphId: String) {
        _fieldStats.value = primordialEngine.getGlyphFieldStats(glyphId)
    }
}

@Composable
fun PrimordialCipherZoomScreen(
    glyphId: String,
    userId: String,
    contactName: String,
    messageToEmbed: String,
    navController: NavController,
    viewModel: PrimordialCipherZoomViewModel = hiltViewModel(),
    onEmbeddingComplete: (Double) -> Unit
) {
    val currentFrequency by viewModel.currentFrequency.collectAsState()
    val zoomLevel by viewModel.zoomLevel.collectAsState()
    val embeddedMessage by viewModel.embeddedMessage.collectAsState()
    val fieldStats by viewModel.fieldStats.collectAsState()
    val invokingMessage by viewModel.invokingMessage.collectAsState()
    val isInvokeMode by viewModel.isInvokeMode.collectAsState()
    val selectedMediaUri by viewModel.selectedMediaUri.collectAsState()
    val selectedMediaType by viewModel.selectedMediaType.collectAsState()

    var selectedHarmonic by remember { mutableStateOf(1) }
    var invokeContentTab by remember { mutableStateOf(0) }  // 0=Text, 1=Image, 2=Video, 3=Audio

    LaunchedEffect(glyphId) {
        viewModel.initializeGlyph(glyphId, userId)
    }

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
                        text = "Primordial Cipher Zoom",
                        color = Color.Cyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Harmonic ${selectedHarmonic} · ${currentFrequency.toInt()}Hz",
                        color = Color(0xFF008B8B),
                        fontSize = 11.sp
                    )
                }
            }

            // Embed button
            if (embeddedMessage == null) {
                IconButton(
                    onClick = {
                        viewModel.embedMessage(glyphId, messageToEmbed)
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Embed Message", tint = Color.Cyan)
                }
            }
        }

        // Main harmonic field visualization
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
                // Harmonic field visualization
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(
                            Color.Cyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(80.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⬡",
                            fontSize = 48.sp,
                            color = Color.Cyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "H${selectedHarmonic}",
                            color = Color.Cyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentFrequency.toInt()}Hz",
                            color = Color(0xFF008B8B),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Embedded message display
                if (embeddedMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Cyan.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.material3.CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "📌 Message embedded at harmonic H${selectedHarmonic}",
                                color = Color.Cyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = messageToEmbed,
                                color = Color.Cyan,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Invoke message mode
                if (isInvokeMode) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "⚡ Invoke Content at H${selectedHarmonic}",
                                color = Color.Cyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Content type tabs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val contentTypes = listOf("💬 Text", "📷 Image", "🎬 Video", "🔊 Audio")
                                contentTypes.forEachIndexed { index, label ->
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { invokeContentTab = index },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (invokeContentTab == index) Color.Cyan else Color(0xFF008B8B)
                                        ),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(6.dp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Content input based on tab
                            when (invokeContentTab) {
                                0 -> {
                                    // Text mode
                                    OutlinedTextField(
                                        value = invokingMessage,
                                        onValueChange = { viewModel.updateInvokingMessage(it) },
                                        placeholder = { Text("Enter message...", color = Color(0xFF008B8B)) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.Cyan,
                                            unfocusedTextColor = Color.Cyan,
                                            focusedBorderColor = Color.Cyan,
                                            unfocusedBorderColor = Color(0xFF008B8B),
                                            cursorColor = Color.Cyan
                                        )
                                    )
                                }
                                1, 2, 3 -> {
                                    // Image, Video, Audio mode
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .clickable { /* TODO: Open media picker */ },
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF008B8B)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (selectedMediaUri == null) {
                                                Text(
                                                    text = when (invokeContentTab) {
                                                        1 -> "📷 Tap to select image"
                                                        2 -> "🎬 Tap to select video"
                                                        3 -> "🔊 Tap to select audio"
                                                        else -> "Select media"
                                                    },
                                                    color = Color.Black,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            } else {
                                                Text(
                                                    text = "✓ Media selected",
                                                    color = Color.Black,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            viewModel.updateInvokingMessage("")
                                            viewModel.clearMedia()
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF008B8B)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "✕ Cancel",
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(8.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            val hasContent = invokingMessage.isNotEmpty() || selectedMediaUri != null
                                            if (hasContent) {
                                                viewModel.invokeMessage(glyphId)
                                            }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (invokingMessage.isNotEmpty() || selectedMediaUri != null) Color.Cyan else Color(0xFF008B8B)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "⚡ Invoke",
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(8.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    // Button to enter invoke mode
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clickable { viewModel.startInvokeMode() }
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF008B8B)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⚡ Enter Invoke Mode",
                            color = Color.Black,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Harmonic selector (1-8)
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A1A)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Select Embedding Harmonic",
                            color = Color.Cyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Harmonic buttons
                        repeat(2) { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(4) { col ->
                                    val harmonic = row * 4 + col + 1
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedHarmonic = harmonic
                                                viewModel.zoomToHarmonic(glyphId, harmonic)
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (selectedHarmonic == harmonic)
                                                Color.Cyan else Color(0xFF008B8B)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "H$harmonic",
                                            color = Color.Black,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(8.dp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Send button
                Card(
                    modifier = Modifier
                        .clickable {
                            if (embeddedMessage != null) {
                                onEmbeddingComplete(currentFrequency)
                                navController.popBackStack()
                            }
                        }
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (embeddedMessage != null) Color.Cyan else Color(0xFF008B8B)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "✓ Send Cipher",
                        color = Color.Black,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
