package com.glyphos.symbolic.ui.screens.rooms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun RoomDetailScreen(
    roomId: String = "room-1",
    navController: NavController,
    viewModel: RoomDetailViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val newMessage by viewModel.newMessage.collectAsState()

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
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.Cyan)
            }
            Text(
                text = "Room Chat",
                color = Color.Cyan,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f)
            )
        }

        // Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            items(messages) { message ->
                MessageCard(
                    message = message,
                    isOwn = message.senderId == "user-1"
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Input area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newMessage,
                    onValueChange = { viewModel.updateNewMessage(it) },
                    placeholder = { Text("Type message...", color = Color(0xFF008B8B)) },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Cyan,
                        unfocusedTextColor = Color.Cyan,
                        focusedBorderColor = Color.Cyan,
                        unfocusedBorderColor = Color(0xFF008B8B)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = { viewModel.sendMessage() }) {
                    Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.Cyan)
                }
            }
        }
    }
}

@Composable
private fun MessageCard(message: MessageItem, isOwn: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwn) androidx.compose.foundation.layout.Arrangement.End else androidx.compose.foundation.layout.Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f),
            colors = CardDefaults.cardColors(
                containerColor = if (isOwn) Color(0xFF004D4D) else Color(0xFF1A1A1A)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isOwn) "You" else message.senderId,
                    color = Color.Cyan,
                    fontSize = 12.sp
                )
                Text(
                    text = message.content,
                    color = Color.Cyan,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
