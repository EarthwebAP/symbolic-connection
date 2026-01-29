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
import com.glyphos.symbolic.ui.screens.auth.LoginScreen
import com.glyphos.symbolic.ui.screens.auth.RegisterScreen
import com.glyphos.symbolic.ui.screens.home.HomeScreen
import com.glyphos.symbolic.ui.screens.glyph.GlyphScreen
import com.glyphos.symbolic.ui.screens.rooms.RoomsListScreen
import com.glyphos.symbolic.ui.screens.rooms.RoomDetailScreen
import com.glyphos.symbolic.ui.screens.presence.PresenceScreen
import com.glyphos.symbolic.ui.screens.settings.SettingsScreen
import com.glyphos.symbolic.ui.screens.splash.SplashScreen
import com.glyphos.symbolic.ui.screens.splash.SecurityBlurbScreen
import com.glyphos.symbolic.ui.screens.chat.ChatScreen
// import com.glyphos.symbolic.ui.screens.contacts.ContactsScreen  // STUB: Disabled for compilation

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
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Main app screens
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Glyph.route) {
            GlyphScreen(navController)
        }
        composable(Screen.Rooms.route) {
            RoomsListScreen(navController)
        }
        composable("room_detail/{roomId}") { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: "room-1"
            RoomDetailScreen(roomId = roomId, navController = navController)
        }
        composable(Screen.Presence.route) {
            PresenceScreen(navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
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
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(chatId = chatId, contactName = "Contact", navController = navController)
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
