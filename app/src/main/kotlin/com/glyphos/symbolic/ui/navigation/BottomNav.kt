package com.glyphos.symbolic.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Bottom navigation bar for main screens
 */
@Composable
fun BottomNavigationBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Chat, contentDescription = "Contacts") },
            label = { Text("Contacts") },
            selected = currentRoute == Screen.Contacts.route,
            onClick = {
                navController.navigate(Screen.Contacts.route) {
                    popUpTo(Screen.Home.route)
                    launchSingleTop = true
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Star, contentDescription = "Glyph") },
            label = { Text("Glyph") },
            selected = currentRoute == Screen.Glyph.route,
            onClick = {
                navController.navigate(Screen.Glyph.route) {
                    popUpTo(Screen.Home.route)
                    launchSingleTop = true
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == Screen.Home.route,
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Status") },
            label = { Text("Status", maxLines = 1) },
            selected = currentRoute == Screen.Presence.route,
            onClick = {
                navController.navigate(Screen.Presence.route) {
                    popUpTo(Screen.Home.route)
                    launchSingleTop = true
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = currentRoute == Screen.Settings.route,
            onClick = {
                navController.navigate(Screen.Settings.route) {
                    popUpTo(Screen.Home.route)
                    launchSingleTop = true
                }
            }
        )
    }
}
