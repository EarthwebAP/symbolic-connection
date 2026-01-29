package com.glyphos.symbolic.ui.screens.rituals

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glyphos.symbolic.rituals.RitualOrchestrator

/**
 * Ritual Dashboard Screen
 * Shows ritual history, ceremonial connections, and presence contracts
 */
data class RitualEvent(
    val type: RitualType = RitualType.UNKNOWN,
    val userId: String = "",
    val timestamp: Long = 0L
)

enum class RitualType {
    BREATH_UNLOCK, GESTURE_UNLOCK, PRESENCE_SYNC, CEREMONIAL_CONNECTION, UNKNOWN
}

data class RitualStats(
    val totalRituals: Int = 0,
    val breathUnlockCount: Int = 0,
    val gestureUnlockCount: Int = 0,
    val presenceSyncCount: Int = 0,
    val ceremonialConnectionCount: Int = 0,
    val activePresenceContractCount: Int = 0,
    val presencePulseCount: Int = 0,
    val recentRituals: List<RitualEvent> = emptyList()
)

@Composable
fun RitualDashboardScreen(
    ritualOrchestrator: RitualOrchestrator
) {
    val stats = RitualStats()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A))
                .padding(16.dp)
        ) {
            Text(
                text = "Ritual Dashboard",
                color = Color(0xFF00FFFF),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Stats Cards
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                RitualStatCard(
                    title = "Total Rituals",
                    value = stats.totalRituals.toString(),
                    icon = "✨"
                )
            }

            item {
                RitualStatCard(
                    title = "Breath Unlock Rituals",
                    value = stats.breathUnlockCount.toString(),
                    icon = "🌬️"
                )
            }

            item {
                RitualStatCard(
                    title = "Gesture Unlock Rituals",
                    value = stats.gestureUnlockCount.toString(),
                    icon = "👆"
                )
            }

            item {
                RitualStatCard(
                    title = "Presence Synchronizations",
                    value = stats.presenceSyncCount.toString(),
                    icon = "🔮"
                )
            }

            item {
                RitualStatCard(
                    title = "Ceremonial Connections",
                    value = stats.ceremonialConnectionCount.toString(),
                    icon = "🎭"
                )
            }

            item {
                RitualStatCard(
                    title = "Active Contracts",
                    value = stats.activePresenceContractCount.toString(),
                    icon = "📜"
                )
            }

            item {
                RitualStatCard(
                    title = "Presence Pulses Emitted",
                    value = stats.presencePulseCount.toString(),
                    icon = "📡"
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Recent Rituals",
                    color = Color(0xFF00FFFF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
            }

            items(stats.recentRituals.take(5)) { ritual ->
                RitualHistoryCard(ritual)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun RitualStatCard(
    title: String,
    value: String,
    icon: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF008B8B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    color = Color(0xFF0099FF),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    color = Color(0xFF00FFFF),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = icon,
                fontSize = 48.sp
            )
        }
    }
}

@Composable
fun RitualHistoryCard(ritual: RitualEvent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0D1B1B)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00FFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = ritual.type.name,
                color = Color(0xFF00FFFF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = ritual.userId,
                    color = Color(0xFF008B8B),
                    fontSize = 11.sp
                )

                Text(
                    text = formatTimestamp(ritual.timestamp),
                    color = Color(0xFF0099FF),
                    fontSize = 11.sp
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> "${diff / 86400000}d ago"
    }
}
