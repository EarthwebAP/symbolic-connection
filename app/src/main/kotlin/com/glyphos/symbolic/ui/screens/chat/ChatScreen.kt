package com.glyphos.symbolic.ui.screens.chat

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.glyphos.symbolic.data.MessageItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    chatId: String = "chat-1",
    contactName: String = "Contact",
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val newMessage by viewModel.newMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val typingUsers by viewModel.typingUsers.collectAsState()

    LaunchedEffect(chatId) {
        viewModel.loadChatSession(chatId)
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
                        text = contactName,
                        color = Color.Cyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (typingUsers.isNotEmpty()) {
                        Text(
                            text = "typing...",
                            color = Color(0xFF008B8B),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Call buttons
            IconButton(onClick = { /* TODO: Initiate voice call */ }) {
                Icon(Icons.Filled.Call, contentDescription = "Voice Call", tint = Color.Cyan)
            }
            IconButton(onClick = { /* TODO: Initiate video call */ }) {
                Icon(Icons.Filled.VideoCall, contentDescription = "Video Call", tint = Color.Cyan)
            }
        }

        // Messages area
        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Cyan)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp),
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        isOwn = message.senderId == "current-user-id",  // TODO: Get from auth
                        onDelete = { viewModel.deleteMessage(it) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newMessage,
                    onValueChange = {
                        viewModel.updateNewMessage(it)
                        viewModel.setTypingIndicator(it.isNotEmpty())
                    },
                    placeholder = { Text("Type message...", color = Color(0xFF008B8B)) },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Cyan,
                        unfocusedTextColor = Color.Cyan,
                        focusedBorderColor = Color.Cyan,
                        unfocusedBorderColor = Color(0xFF008B8B),
                        cursorColor = Color.Cyan
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = { viewModel.sendMessage() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.Cyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: MessageItem,
    isOwn: Boolean,
    onDelete: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .clickable { /* Show options */ },
            colors = CardDefaults.cardColors(
                containerColor = if (isOwn) Color(0xFF004D4D) else Color(0xFF1A1A1A)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    color = Color.Cyan,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(message.timestamp),
                        color = Color(0xFF008B8B),
                        fontSize = 10.sp
                    )

                    Row {
                        Text(
                            text = when (message.deliveryStatus) {
                                com.glyphos.symbolic.data.DeliveryStatus.SENDING -> "⏳"
                                com.glyphos.symbolic.data.DeliveryStatus.SENT -> "✓"
                                com.glyphos.symbolic.data.DeliveryStatus.DELIVERED -> "✓✓"
                                com.glyphos.symbolic.data.DeliveryStatus.READ -> "✓✓"
                                com.glyphos.symbolic.data.DeliveryStatus.FAILED -> "✗"
                            },
                            color = Color.Cyan,
                            fontSize = 10.sp
                        )

                        if (isOwn) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFF008B8B),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onDelete(message.messageId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
