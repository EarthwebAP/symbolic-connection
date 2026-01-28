package com.glyphos.symbolic.ui.screens.glyph

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun GlyphScreen(navController: NavController, viewModel: GlyphViewModel = hiltViewModel()) {
    val userGlyph by viewModel.userGlyph.collectAsState()
    val embeddedContent by viewModel.embeddedContent.collectAsState()
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
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with back button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.Cyan)
                    }
                    Text(
                        text = "Personal Glyph",
                        color = Color.Cyan,
                        fontSize = 24.sp
                    )
                }

                // Large Glyph Display (Simplified)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(16.dp)
                        .background(Color(0xFF004D4D), shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                        .clickable { /* TODO: Enter infinite zoom */ },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "GLYPH VISUALIZATION",
                        color = Color.Cyan,
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Semantic Metrics
                userGlyph?.let { glyph ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A1A1A)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Semantic Metrics",
                                color = Color.Cyan,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            MetricRow(
                                label = "Power",
                                value = glyph.semanticMetrics.power.toFloat() / 100f
                            )
                            MetricRow(
                                label = "Complexity",
                                value = glyph.semanticMetrics.complexity.toFloat() / 100f
                            )
                            MetricRow(
                                label = "Resonance",
                                value = glyph.semanticMetrics.resonance.toFloat() / 100f
                            )
                            MetricRow(
                                label = "Stability",
                                value = glyph.semanticMetrics.stability.toFloat() / 100f
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Embedded Content
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A1A)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Embedded Content",
                            color = Color.Cyan,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            text = "${embeddedContent.size} items inside",
                            color = Color.Cyan,
                            fontSize = 14.sp
                        )

                        embeddedContent.forEachIndexed { _, content ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* TODO: View content */ },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF004D4D)
                                )
                            ) {
                                Text(
                                    text = content.title,
                                    color = Color.Cyan,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.Cyan,
            fontSize = 12.sp,
            modifier = Modifier.width(100.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .background(Color(0xFF008B8B))
        )
        Text(
            text = String.format("%.2f", value),
            color = Color.Cyan,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
