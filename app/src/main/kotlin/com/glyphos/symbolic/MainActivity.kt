package com.glyphos.symbolic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.glyphos.symbolic.service.ConnectionStatusManager
import com.glyphos.symbolic.ui.navigation.BottomNavigationBar
import com.glyphos.symbolic.ui.navigation.NavGraph
import com.glyphos.symbolic.ui.navigation.Screen
import com.glyphos.symbolic.ui.theme.Glyph007Theme
import com.glyphos.symbolic.telemetry.SymbolicConnectionClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main entry point for Glyph007
 *
 * PHASE 0: Grounding
 * Initial activity that sets up the Compose environment with navigation
 * Enhanced with telemetry tracking for app usage analytics
 * + Connection status indicator in system status bar
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var telemetryClient: SymbolicConnectionClient
    private val scope = CoroutineScope(Dispatchers.Main)

    @Inject
    lateinit var connectionStatusManager: ConnectionStatusManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize telemetry client
        telemetryClient = SymbolicConnectionClient(
            context = this,
            apiBaseUrl = "https://glyph.seodr.ovh:9998"
        )

        // Start connection status indicator in status bar
        connectionStatusManager.setConnected(false)

        // Register APK and log launch
        scope.launch {
            telemetryClient.registerAPK()
            telemetryClient.logAppLaunch()
        }

        setContent {
            Glyph007App()
        }
    }

    override fun onResume() {
        super.onResume()
        scope.launch {
            telemetryClient.sendHeartbeat()
        }
    }

    override fun onPause() {
        super.onPause()
        scope.launch {
            telemetryClient.logEvent("app_paused", mapOf(
                "timestamp" to System.currentTimeMillis()
            ))
        }
    }
}

@Composable
fun Glyph007App() {
    Glyph007Theme {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        Scaffold(
            bottomBar = {
                // Only show bottom nav for main app screens
                if (currentRoute != Screen.Login.route &&
                    currentRoute != Screen.Register.route &&
                    currentRoute != Screen.Splash.route &&
                    currentRoute != Screen.SecurityBlurb.route) {
                    BottomNavigationBar(navController)
                }
            }
        ) { paddingValues ->
            NavGraph(
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
