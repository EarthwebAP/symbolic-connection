package com.glyphos.symbolic.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.glyphos.symbolic.ui.screens.auth.LoginScreen
import com.glyphos.symbolic.ui.screens.auth.RegisterScreen
import com.glyphos.symbolic.ui.screens.home.HomeScreen
import com.glyphos.symbolic.ui.screens.glyph.GlyphScreen
import com.glyphos.symbolic.ui.screens.glyph.GlyphVerificationScreen
import com.glyphos.symbolic.ui.screens.rooms.RoomsListScreen
import com.glyphos.symbolic.ui.screens.rooms.RoomDetailScreen
import com.glyphos.symbolic.ui.screens.presence.PresenceScreen
import com.glyphos.symbolic.ui.screens.presence.PresenceMapScreen
import com.glyphos.symbolic.ui.screens.settings.SettingsScreen
import com.glyphos.symbolic.ui.screens.splash.SplashScreen
import com.glyphos.symbolic.ui.screens.splash.SecurityBlurbScreen
import com.glyphos.symbolic.ui.screens.chat.ChatScreen
import com.glyphos.symbolic.ui.screens.contacts.ContactsScreen
import com.glyphos.symbolic.ui.screens.rituals.RitualDashboardScreen
import com.glyphos.symbolic.ui.screens.security.EmergencySealScreen
import com.glyphos.symbolic.ui.screens.security.GesturePatternScreen
import com.glyphos.symbolic.ui.screens.interaction.RadialMenuScreen
import com.glyphos.symbolic.ui.screens.batcave.BatcaveScreen
import com.glyphos.symbolic.ui.screens.rooms.CallKeypadScreen

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
                    // Skip authentication for now, go directly to Home
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SecurityBlurb.route) { inclusive = true }
                    }
                }
            )
        }

        // Auth screens
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Register.route) { inclusive = true } } },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        // Main app screens
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Glyph.route) {
            GlyphScreen(navController = navController)
        }
        composable(Screen.Rooms.route) {
            RoomsListScreen(navController = navController)
        }
        composable("room_detail/{roomId}") { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            RoomDetailScreen(navController = navController, roomId = roomId)
        }
        composable(Screen.Presence.route) {
            PresenceScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        // Symbolic Connection screens
        composable(Screen.Contacts.route) {
            ContactsScreen(navController = navController)
        }
        composable(Screen.Chat.route, arguments = listOf(navArgument("chatId") { type = NavType.StringType })) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(navController = navController, chatId = chatId)
        }
        composable(Screen.Batcave.route) {
            BatcaveScreen(navController = navController)
        }
        composable(Screen.RadialMenu.route) {
            RadialMenuScreen(navController = navController)
        }
        composable(Screen.PresenceMap.route) {
            PresenceMapScreen(navController = navController)
        }

        // Dialer/Call screen
        composable(Screen.Call.route, arguments = listOf(navArgument("callId") { type = NavType.StringType })) {
            CallKeypadScreen(
                onCall = { number ->
                    // Handle phone call
                    navController.popBackStack()
                },
                onClose = { navController.popBackStack() }
            )
        }
    }
}
