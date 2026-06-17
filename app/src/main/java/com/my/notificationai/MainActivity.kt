package com.my.notificationai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.my.notificationai.ui.MainViewModel
import com.my.notificationai.ui.Screen
import com.my.notificationai.ui.screens.AppListScreen
import com.my.notificationai.ui.screens.DashboardScreen
import com.my.notificationai.ui.screens.SettingsScreen
import com.my.notificationai.ui.screens.VaultScreen
import com.my.notificationai.ui.theme.MyNotificationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePref by viewModel.themePreference.collectAsState()
            val darkTheme = when (themePref) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            MyNotificationTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Dashboard.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToApps = { navController.navigate(Screen.AppList.route) },
                                onNavigateToVault = { navController.navigate(Screen.Vault.route) }
                            )
                        }

                        composable(Screen.AppList.route) {
                            AppListScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Vault.route) {
                            VaultScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}