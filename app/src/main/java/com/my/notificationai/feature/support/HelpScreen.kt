package com.my.notificationai.feature.support

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.my.notificationai.core.designsystem.components.AppTopBar
import com.my.notificationai.core.designsystem.theme.AppTheme

@Composable
fun HelpScreen(
    innerPadding: PaddingValues,
    onBackClick: () -> Unit
) {
    val faqs = listOf(
        "Why does My Notification require Notification Access?" to
            "Android requires the NotificationListenerService permission so the app can inspect incoming notifications, record them in local history, and dismiss notifications matching your active blocking rules.",
        "Will I miss important bank OTPs or calls?" to
            "No. Vital communications—such as phone calls, emergency alarms, bank OTP verification codes, and financial alerts—are protected at the highest priority in the rule hierarchy and are never blocked.",
        "Why are some notifications not fully suppressed?" to
            "Certain Android OEMs (such as Samsung, Xiaomi, and Google) protect specific persistent system services (like active ongoing phone calls or foreground system downloads) from being cancelled by third-party apps.",
        "How does deduplication work?" to
            "Continuous notifications like download progress or media controls update many times per minute. My Notification merges updates with identical keys into a single logical event, recording lifecycle duration without inflating your analytics.",
        "How can I ensure the blocker is not killed in the background?" to
            "Disable battery optimization for My Notification in Android Settings, and enable 'Autostart' or lock the app in recent apps if you use MIUI, HyperOS, or ColorOS."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(innerPadding)
    ) {
        AppTopBar(
            title = "Help & FAQ",
            canGoBack = true,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            faqs.forEach { (question, answer) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppTheme.colors.surface)
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = question,
                            style = AppTheme.typography.titleSmall,
                            color = AppTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = answer,
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
