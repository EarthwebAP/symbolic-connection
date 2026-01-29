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
import com.glyphos.symbolic.ui.navigation.BottomNavigationBar
import com.glyphos.symbolic.ui.navigation.NavGraph
import com.glyphos.symbolic.ui.navigation.Screen
import com.glyphos.symbolic.ui.theme.Glyph007Theme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry point for Glyph007
 *
 * PHASE 0: Grounding
 * Initial activity that sets up the Compose environment with navigation
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Glyph007App()
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
