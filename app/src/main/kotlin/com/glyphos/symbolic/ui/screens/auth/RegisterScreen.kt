package com.glyphos.symbolic.ui.screens.auth

import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }

    val generatedGlyph by viewModel.generatedGlyph.collectAsState()
    val authState by viewModel.authState.collectAsState()

    // Update glyph as user types name
    LaunchedEffect(displayName) {
        if (displayName.isNotBlank()) {
            viewModel.updateName(displayName)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateToLogin) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.Cyan)
            }
            Text("Create Account", color = Color.Cyan, fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Display Name with Live Glyph Preview
        Text("Your Name", color = Color.Cyan, fontSize = 14.sp)
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Full Name", color = Color.Cyan) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Cyan,
                unfocusedTextColor = Color.Cyan,
                focusedBorderColor = Color.Cyan,
                unfocusedBorderColor = Color(0xFF008B8B)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live Glyph Preview
        if (generatedGlyph != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Your Exclusive Glyph",
                        color = Color.Cyan,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Display SVG as text (simplified preview)
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✦ ${generatedGlyph!!.name} ✦\nPower: ${generatedGlyph!!.metrics.power}%\nResonance: ${generatedGlyph!!.metrics.resonance}%",
                            color = Color.Cyan,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(12.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Unique to you • Primordial waves • Infinite zoom capable",
                        color = Color(0xFF008B8B),
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Username
        Text("Username", color = Color.Cyan, fontSize = 14.sp)
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Choose username", color = Color.Cyan) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Cyan,
                unfocusedTextColor = Color.Cyan,
                focusedBorderColor = Color.Cyan,
                unfocusedBorderColor = Color(0xFF008B8B)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password
        Text("Password", color = Color.Cyan, fontSize = 14.sp)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Create password", color = Color.Cyan) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Cyan,
                unfocusedTextColor = Color.Cyan,
                focusedBorderColor = Color.Cyan,
                unfocusedBorderColor = Color(0xFF008B8B)
            ),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password
        Text("Confirm Password", color = Color.Cyan, fontSize = 14.sp)
        OutlinedTextField(
            value = passwordConfirm,
            onValueChange = { passwordConfirm = it },
            label = { Text("Confirm password", color = Color.Cyan) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Cyan,
                unfocusedTextColor = Color.Cyan,
                focusedBorderColor = Color.Cyan,
                unfocusedBorderColor = Color(0xFF008B8B)
            ),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Register Button
        Button(
            onClick = {
                if (username.isNotBlank() && password.isNotBlank() && displayName.isNotBlank()) {
                    if (password == passwordConfirm) {
                        viewModel.register(username, password, displayName)
                        onRegisterSuccess()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (authState.isLoading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.padding(end = 8.dp))
            }
            Text("Create My Glyph")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
