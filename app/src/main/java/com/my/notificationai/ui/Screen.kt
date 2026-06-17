package com.my.notificationai.ui

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object AppList : Screen("app_list")
    object Vault : Screen("vault")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
}

/** Returns the display title for the top app bar based on the current route. */
fun titleForRoute(route: String?): String = when (route) {
    "dashboard" -> "MyNotification"
    "app_list"  -> "App Control List"
    "vault"     -> "Secure Vault Inbox"
    "settings"  -> "Settings"
    "profile"   -> "Profile"
    else        -> "MyNotification"
}
