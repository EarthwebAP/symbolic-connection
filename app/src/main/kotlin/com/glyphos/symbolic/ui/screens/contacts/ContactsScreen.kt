package com.glyphos.symbolic.ui.screens.contacts

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.glyphos.symbolic.data.ChatSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ContactsScreen(
    navController: NavController,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val chatSessions by viewModel.chatSessions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Symbolic Connection",
                    color = Color.Cyan,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { /* TODO: Show menu */ }) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = Color.Cyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    viewModel.updateSearchQuery(it)
                    if (it.isNotEmpty()) viewModel.searchContacts(it)
                },
                placeholder = { Text("Search conversations...", color = Color(0xFF008B8B)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Cyan,
                    unfocusedTextColor = Color.Cyan,
                    focusedBorderColor = Color.Cyan,
                    unfocusedBorderColor = Color(0xFF008B8B),
                    cursorColor = Color.Cyan
                ),
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Chat sessions list
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.Cyan)
                }
            } else if (chatSessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No conversations yet\nStart by adding a contact",
                        color = Color(0xFF008B8B),
                        fontSize = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    items(chatSessions) { session ->
                        ChatSessionCard(
                            session = session,
                            onSessionClick = {
                                navController.navigate("chat/${session.chatId}")
                            }
                        )
                    }
                }
            }
        }

        // FAB for new chat
        FloatingActionButton(
            onClick = { navController.navigate("add_contact") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color.Cyan
        ) {
            Icon(Icons.Filled.Add, contentDescription = "New Chat", tint = Color.Black)
        }
    }
}

@Composable
private fun ChatSessionCard(
    session: ChatSession,
    onSessionClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onSessionClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF004D4D), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = session.lastMessage?.senderId?.value?.firstOrNull()?.uppercase() ?: "?",
                    color = Color.Cyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Message content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = session.participantIds.joinToString(", "),
                        color = Color.Cyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatTime(session.lastMessage?.timestamp ?: 0),
                        color = Color(0xFF008B8B),
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "[Message]",
                    color = Color(0xFF008B8B),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Unread badge
            if (session.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.Cyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = session.unreadCount.toString(),
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m"
        diff < 24 * 60 * 60 * 1000 -> {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
        else -> {
            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
