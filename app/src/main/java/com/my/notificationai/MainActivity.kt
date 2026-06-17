package com.my.notificationai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.my.notificationai.ui.MainViewModel
import com.my.notificationai.ui.Screen
import com.my.notificationai.ui.components.AppDrawerContent
import com.my.notificationai.ui.components.AppHeader
import com.my.notificationai.ui.screens.AppListScreen
import com.my.notificationai.ui.screens.DashboardScreen
import com.my.notificationai.ui.screens.ProfileScreen
import com.my.notificationai.ui.screens.SettingsScreen
import com.my.notificationai.ui.screens.VaultScreen
import com.my.notificationai.ui.theme.MyNotificationTheme
import com.my.notificationai.ui.titleForRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePref by viewModel.themePreference.collectAsState()
            val darkTheme = when (themePref) {
                "DARK"  -> true
                "LIGHT" -> false
                else    -> isSystemInDarkTheme()
            }

            MyNotificationTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val drawerState   = rememberDrawerState(DrawerValue.Closed)
                val scope         = rememberCoroutineScope()

                // Observe current route for header title & icon mode
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute   = backStackEntry?.destination?.route

                // Sub-screens show a back arrow instead of hamburger
                val isSubScreen = currentRoute in listOf(
                    Screen.AppList.route,
                    Screen.Vault.route,
                    Screen.Settings.route,
                    Screen.Profile.route
                )

                val notifications by viewModel.savedNotifications.collectAsState()
                val totalBlocked  = notifications.size
                val vaultUnread   = notifications.count { !it.isRead }

                ModalNavigationDrawer(
                    drawerState     = drawerState,
                    gesturesEnabled = !isSubScreen,
                    drawerContent   = {
                        AppDrawerContent(
                            currentRoute  = currentRoute,
                            blockedCount  = totalBlocked,
                            vaultCount    = vaultUnread,
                            onNavigate    = { route ->
                                navController.navigate(route) {
                                    popUpTo(Screen.Dashboard.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            onCloseDrawer = { scope.launch { drawerState.close() } }
                        )
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar   = {
                            AppHeader(
                                title          = titleForRoute(currentRoute),
                                canGoBack      = isSubScreen,
                                onMenuClick    = { scope.launch { drawerState.open() } },
                                onBackClick    = { navController.popBackStack() },
                                onProfileClick = {
                                    navController.navigate(Screen.Profile.route) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController    = navController,
                            startDestination = Screen.Dashboard.route,
                            modifier         = Modifier.fillMaxSize()
                        ) {
                            composable(Screen.Dashboard.route) {
                                DashboardScreen(
                                    viewModel         = viewModel,
                                    innerPadding      = innerPadding,
                                    onNavigateToApps  = { navController.navigate(Screen.AppList.route) },
                                    onNavigateToVault = { navController.navigate(Screen.Vault.route) }
                                )
                            }

                            composable(Screen.AppList.route) {
                                AppListScreen(
                                    viewModel    = viewModel,
                                    innerPadding = innerPadding
                                )
                            }

                            composable(Screen.Vault.route) {
                                VaultScreen(
                                    viewModel    = viewModel,
                                    innerPadding = innerPadding
                                )
                            }

                            composable(Screen.Settings.route) {
                                SettingsScreen(
                                    viewModel    = viewModel,
                                    innerPadding = innerPadding
                                )
                            }

                            composable(Screen.Profile.route) {
                                ProfileScreen(
                                    viewModel    = viewModel,
                                    innerPadding = innerPadding
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}