package com.glyphos.symbolic.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by remember { mutableStateOf(AuthState()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        // Logo/Title
        Text(
            text = "GLYPH007",
            color = Color.Cyan,
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Username
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username", color = Color.Cyan) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Cyan,
                unfocusedTextColor = Color.Cyan,
                focusedBorderColor = Color.Cyan,
                unfocusedBorderColor = Color(0xFF008B8B),
                focusedLabelColor = Color.Cyan,
                unfocusedLabelColor = Color(0xFF008B8B)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = Color.Cyan) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Cyan,
                unfocusedTextColor = Color.Cyan,
                focusedBorderColor = Color.Cyan,
                unfocusedBorderColor = Color(0xFF008B8B),
                focusedLabelColor = Color.Cyan,
                unfocusedLabelColor = Color(0xFF008B8B)
            ),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = {
                if (username.isNotBlank() && password.isNotBlank()) {
                    viewModel.login(username, password)
                    onLoginSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (authState.isLoading) {
                CircularProgressIndicator(
                    color = Color.Black,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text("Login")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Register Link
        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register", color = Color.Cyan)
        }

        // Error message
        if (authState.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = authState.error ?: "",
                color = Color(0xFFFF5555),
                fontSize = 12.sp
            )
        }
    }
}
