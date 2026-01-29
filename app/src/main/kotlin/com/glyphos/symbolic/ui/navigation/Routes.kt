package com.glyphos.symbolic.ui.navigation

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object SecurityBlurb : Screen("security_blurb")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Contacts : Screen("contacts")
    object Chat : Screen("chat/{chatId}")
    object Glyph : Screen("glyph")
    object Rooms : Screen("rooms")
    object Presence : Screen("presence")
    object Batcave : Screen("batcave")
    object SecureRoom : Screen("secure_room")
    object Settings : Screen("settings")
    object Call : Screen("call/{callId}")
    object AddContact : Screen("add_contact")
    object RadialMenu : Screen("radial_menu")
    object PresenceMap : Screen("presence_map")
    object EmergencySeal : Screen("emergency_seal")
    object GesturePattern : Screen("gesture_pattern")
    object RitualDashboard : Screen("ritual_dashboard")
    object GlyphVerification : Screen("glyph_verification")
    object InfiniteZoom : Screen("infinite_zoom/{glyphId}")
}
