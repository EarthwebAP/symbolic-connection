package com.glyphos.symbolic.ui.screens.rooms

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.glyphos.symbolic.core.models.Room
import com.glyphos.symbolic.core.models.RoomType

@Composable
fun RoomsListScreen(navController: NavController, viewModel: RoomsViewModel = hiltViewModel()) {
    val rooms by viewModel.rooms.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Your Rooms",
                color = Color.Cyan,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(rooms) { room ->
                    RoomCard(room = room, onRoomClick = { navController.navigate("room_detail/${room.roomId}") })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        FloatingActionButton(
            onClick = { /* TODO: Show create room dialog */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color.Cyan
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Create Room", tint = Color.Black)
        }
    }
}

@Composable
private fun RoomCard(room: Room, onRoomClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRoomClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .background(getRoomColor(room.type)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = room.type.name.first().toString(),
                    color = Color.Black,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = room.name,
                    color = Color.Cyan,
                    fontSize = 14.sp
                )
                Text(
                    text = room.type.name,
                    color = Color(0xFF008B8B),
                    fontSize = 12.sp
                )
                Text(
                    text = "${room.participants.size} members",
                    color = Color.Cyan,
                    fontSize = 10.sp
                )
            }
        }
    }
}

private fun getRoomColor(type: RoomType): Color {
    return when (type) {
        RoomType.STANDARD -> Color(0xFF00FFFF)
        RoomType.BATCAVE -> Color(0xFF004D4D)
        RoomType.SECURE_DIGITAL -> Color(0xFF008B8B)
        RoomType.CEREMONIAL -> Color(0xFF00CCCC)
    }
}
