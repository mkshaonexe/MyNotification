package com.my.notificationai.ui

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object AppList : Screen("app_list")
    object Vault : Screen("vault")
    object Settings : Screen("settings")
}
