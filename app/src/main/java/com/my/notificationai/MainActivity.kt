package com.my.notificationai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.my.notificationai.core.designsystem.components.AppBottomNavBar
import com.my.notificationai.core.designsystem.theme.MyNotificationTheme
import com.my.notificationai.data.AppRepository
import com.my.notificationai.feature.analytics.AnalyticsScreen
import com.my.notificationai.feature.analytics.AnalyticsViewModel
import com.my.notificationai.feature.history.HistoryScreen
import com.my.notificationai.feature.history.HistoryViewModel
import com.my.notificationai.feature.history.NotificationDetailScreen
import com.my.notificationai.feature.home.HomeScreen
import com.my.notificationai.feature.home.HomeViewModel
import com.my.notificationai.feature.onboarding.OnboardingScreen
import com.my.notificationai.feature.onboarding.OnboardingViewModel
import com.my.notificationai.feature.rules.AppPickerScreen
import com.my.notificationai.feature.rules.BlockingRulesScreen
import com.my.notificationai.feature.rules.CategoriesScreen
import com.my.notificationai.feature.rules.RuleBuilderScreen
import com.my.notificationai.feature.rules.RulePriorityScreen
import com.my.notificationai.feature.rules.RulesViewModel
import com.my.notificationai.feature.rules.SchedulesScreen
import com.my.notificationai.feature.rules.WhitelistedAppsScreen
import com.my.notificationai.feature.rules.WhitelistedKeywordsScreen
import com.my.notificationai.feature.settings.AboutScreen
import com.my.notificationai.feature.settings.BackupRestoreScreen
import com.my.notificationai.feature.settings.DataPrivacyScreen
import com.my.notificationai.feature.settings.ProtectionCenterScreen
import com.my.notificationai.feature.settings.SettingsScreen
import com.my.notificationai.feature.settings.SettingsViewModel
import com.my.notificationai.feature.support.HelpScreen
import com.my.notificationai.feature.support.ReportProblemScreen
import com.my.notificationai.feature.support.TutorialScreen
import com.my.notificationai.ui.Screen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()
    private val rulesViewModel: RulesViewModel by viewModels()
    private val analyticsViewModel: AnalyticsViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val onboardingViewModel: OnboardingViewModel by viewModels()

    @Inject
    lateinit var repository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themePref by settingsViewModel.themePreference.collectAsState()
            val darkTheme = when (themePref) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            MyNotificationTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                val isOnboardingDone by onboardingViewModel.isOnboardingCompleted.collectAsState()

                val isPrimaryTab = currentRoute in listOf(
                    Screen.Home.route,
                    Screen.History.route,
                    Screen.Rules.route,
                    Screen.Analytics.route
                )

                val startDestination = if (isOnboardingDone) Screen.Home.route else Screen.Onboarding.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (isPrimaryTab) {
                            AppBottomNavBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // --- Primary Tabs ---
                        composable(Screen.Home.route) {
                            HomeScreen(
                                viewModel = homeViewModel,
                                innerPadding = innerPadding,
                                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                                onNavigateToRules = { navController.navigate(Screen.Rules.route) },
                                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                            )
                        }

                        composable(Screen.History.route) {
                            HistoryScreen(
                                viewModel = historyViewModel,
                                innerPadding = innerPadding,
                                onNotificationClick = { eventId ->
                                    navController.navigate(Screen.NotificationDetail.createRoute(eventId))
                                }
                            )
                        }

                        composable(Screen.Rules.route) {
                            BlockingRulesScreen(
                                viewModel = rulesViewModel,
                                innerPadding = innerPadding,
                                onNavigateToSocialApps = { navController.navigate(Screen.SocialMediaApps.route) },
                                onNavigateToSelectedApps = { navController.navigate(Screen.SelectedApps.route) },
                                onNavigateToKeywords = { navController.navigate(Screen.WhitelistedKeywords.route) },
                                onNavigateToSchedules = { navController.navigate(Screen.Schedules.route) },
                                onNavigateToWhitelistedApps = { navController.navigate(Screen.WhitelistedApps.route) },
                                onNavigateToRuleBuilder = { navController.navigate(Screen.RuleBuilder.route) },
                                onNavigateToRulePriority = { navController.navigate(Screen.RulePriority.route) }
                            )
                        }

                        composable(Screen.Analytics.route) {
                            AnalyticsScreen(
                                viewModel = analyticsViewModel,
                                innerPadding = innerPadding
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                innerPadding = innerPadding,
                                onNavigateToProtection = { navController.navigate(Screen.ProtectionCenter.route) },
                                onNavigateToPrivacy = { navController.navigate(Screen.DataPrivacy.route) },
                                onNavigateToBackup = { navController.navigate(Screen.BackupRestore.route) },
                                onNavigateToHelp = { navController.navigate(Screen.Help.route) },
                                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                                canGoBack = true,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        // --- Sub-Screens ---
                        composable(
                            route = Screen.NotificationDetail.route,
                            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
                        ) { backStack ->
                            val eventId = backStack.arguments?.getLong("eventId") ?: 0L
                            NotificationDetailScreen(
                                eventId = eventId,
                                repository = repository,
                                innerPadding = innerPadding,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.RuleBuilder.route) {
                            RuleBuilderScreen(
                                viewModel = rulesViewModel,
                                innerPadding = innerPadding,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Schedules.route) {
                            SchedulesScreen(
                                viewModel = rulesViewModel,
                                innerPadding = innerPadding,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.SelectedApps.route) {
                            AppPickerScreen(
                                viewModel = rulesViewModel,
                                innerPadding = innerPadding,
                                title = "Block Selected Apps",
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.SocialMediaApps.route) {
                            CategoriesScreen(
                                viewModel = rulesViewModel,
                                innerPadding = innerPadding,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.WhitelistedApps.route) {
                            WhitelistedAppsScreen(
                                viewModel = rulesViewModel,
                                innerPadding = innerPadding,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.WhitelistedKeywords.route) {
                            WhitelistedKeywordsScreen(
                                viewModel = rulesViewModel,
                                innerPadding = innerPadding,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.RulePriority.route) {
                            RulePriorityScreen(
                                innerPadding = innerPadding,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.ProtectionCenter.route) {
                            ProtectionCenterScreen(
                                viewModel = settingsViewModel,
                                innerPadding = innerPadding,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.DataPrivacy.route) {
                            DataPrivacyScreen(
                                viewModel = settingsViewModel,
                                innerPadding = innerPadding,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.BackupRestore.route) {
                            BackupRestoreScreen(
                                viewModel = settingsViewModel,
                                innerPadding = innerPadding,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.About.route) {
                            AboutScreen(
                                innerPadding = innerPadding,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Help.route) {
                            HelpScreen(
                                innerPadding = innerPadding,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Tutorial.route) {
                            TutorialScreen(
                                innerPadding = innerPadding,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.ReportProblem.route) {
                            ReportProblemScreen(
                                innerPadding = innerPadding,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Onboarding.route) {
                            OnboardingScreen(
                                viewModel = onboardingViewModel,
                                onFinished = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}