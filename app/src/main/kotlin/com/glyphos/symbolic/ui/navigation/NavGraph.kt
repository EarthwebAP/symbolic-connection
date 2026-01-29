package com.glyphos.symbolic.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import com.glyphos.symbolic.ui.screens.splash.SplashScreen
import com.glyphos.symbolic.ui.screens.splash.SecurityBlurbScreen
// All other screens disabled due to Hilt dependency injection issues with KAPT disabled

/**
 * Main navigation graph for the app
 */
@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier, isAuthenticated: Boolean = false) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        // Splash screens
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Screen.SecurityBlurb.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SecurityBlurb.route) {
            SecurityBlurbScreen(
                onBlurbComplete = {
                    navController.navigate(if (isAuthenticated) Screen.Home.route else Screen.Login.route) {
                        popUpTo(Screen.SecurityBlurb.route) { inclusive = true }
                    }
                }
            )
        }

        // Auth screens
        composable(Screen.Login.route) {
            // STUB: LoginScreen disabled for compilation
            Text("Login Screen (Coming Soon)", color = Color.Cyan)
        }

        composable(Screen.Register.route) {
            Text("Register (Coming Soon)", color = Color.Cyan)
        }

        // Main app screens
        composable(Screen.Home.route) {
            Text("Home (Coming Soon)", color = Color.Cyan)
        }
        composable(Screen.Glyph.route) {
            Text("Glyph (Coming Soon)", color = Color.Cyan)
        }
        composable(Screen.Rooms.route) {
            Text("Rooms (Coming Soon)", color = Color.Cyan)
        }
        composable("room_detail/{roomId}") { backStackEntry ->
            Text("Room Detail (Coming Soon)", color = Color.Cyan)
        }
        composable(Screen.Presence.route) {
            Text("Presence (Coming Soon)", color = Color.Cyan)
        }
        composable(Screen.Settings.route) {
            Text("Settings (Coming Soon)", color = Color.Cyan)
        }

        // Symbolic Connection screens
        composable(Screen.Contacts.route) {
            // STUB: ContactsScreen disabled for compilation
            Text("Contacts (Coming Soon)", color = Color.Cyan)
        }

        composable(
            Screen.Chat.route,
            arguments = listOf(
                navArgument("chatId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            Text("Chat (Coming Soon)", color = Color.Cyan)
        }

        composable(Screen.Batcave.route) {
            // BatcaveScreen(navController)
        }

        composable(Screen.SecureRoom.route) {
            // SecureRoomScreen(navController)
        }

        composable(
            Screen.Call.route,
            arguments = listOf(
                navArgument("callId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val callId = backStackEntry.arguments?.getString("callId") ?: ""
            // CallScreen(callId = callId, navController = navController)
        }

        composable(Screen.AddContact.route) {
            // AddContactScreen(navController)
        }

        composable(Screen.RadialMenu.route) {
            // RadialMenuScreen(navController)
        }

        composable(Screen.PresenceMap.route) {
            // PresenceMapScreen(navController)
        }
    }
}
