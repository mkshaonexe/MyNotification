package com.my.notificationai.ui

sealed class Screen(val route: String) {
    // Primary Tabs
    object Home : Screen("home")
    object History : Screen("history")
    object Rules : Screen("rules")
    object Analytics : Screen("analytics")
    object Settings : Screen("settings")

    // Sub-screens
    object NotificationDetail : Screen("history/detail/{eventId}") {
        fun createRoute(eventId: Long) = "history/detail/$eventId"
    }
    object RuleBuilder : Screen("rules/builder")
    object Schedules : Screen("rules/schedules")
    object SelectedApps : Screen("rules/selected-apps")
    object SocialMediaApps : Screen("rules/category/social")
    object WhitelistedApps : Screen("rules/whitelist/apps")
    object WhitelistedKeywords : Screen("rules/whitelist/keywords")
    object RulePriority : Screen("rules/priority")

    // Settings Sub-screens
    object ProtectionCenter : Screen("settings/protection")
    object DataPrivacy : Screen("settings/privacy")
    object BackupRestore : Screen("settings/backup")
    object About : Screen("settings/about")

    // Support Sub-screens
    object Help : Screen("support/help")
    object Tutorial : Screen("support/tutorial")
    object ReportProblem : Screen("support/report")

    // Onboarding
    object Onboarding : Screen("onboarding")

    // Legacy aliases for backward compatibility
    object Dashboard : Screen("home")
    object AppList : Screen("rules/selected-apps")
}

fun titleForRoute(route: String?): String = when {
    route == null -> "My Notification"
    route == "home" -> "My Notification"
    route == "history" -> "Notification History"
    route == "rules" -> "Blocking Rules"
    route == "analytics" -> "Analytics"
    route == "settings" -> "Settings"
    route.startsWith("history/detail") -> "Notification Details"
    route == "rules/builder" -> "Custom Rule Builder"
    route == "rules/schedules" -> "Schedules"
    route == "rules/selected-apps" -> "Block Selected Apps"
    route == "rules/category/social" -> "Social Media Apps"
    route == "rules/whitelist/apps" -> "Whitelisted Apps"
    route == "rules/whitelist/keywords" -> "Whitelisted Keywords"
    route == "rules/priority" -> "Rule Hierarchy"
    route == "settings/protection" -> "Protection Center"
    route == "settings/privacy" -> "Data & Privacy"
    route == "settings/backup" -> "Backup & Restore"
    route == "settings/about" -> "About"
    route == "support/help" -> "Help & FAQ"
    route == "support/tutorial" -> "Tutorial"
    route == "support/report" -> "Report a Problem"
    route == "onboarding" -> "Welcome"
    else -> "My Notification"
}
