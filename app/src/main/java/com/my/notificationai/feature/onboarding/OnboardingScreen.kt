package com.my.notificationai.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.my.notificationai.core.designsystem.components.StatusBadge
import com.my.notificationai.core.designsystem.theme.AppTheme

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: () -> Unit
) {
    val step by viewModel.currentStep.collectAsState()
    val strategy by viewModel.selectedStrategy.collectAsState()
    val isAccessGranted = viewModel.isNotificationAccessGranted()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Progress Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                (1..7).forEach { s ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (s == step) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (s == step) AppTheme.colors.primary else AppTheme.colors.border)
                    )
                }
            }

            // Step Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (step) {
                    1 -> Step1Intro()
                    2 -> Step2Access(isAccessGranted = isAccessGranted, onGrant = { viewModel.openNotificationListenerSettings() })
                    3 -> Step3Strategy(selectedStrategy = strategy, onSelect = { viewModel.setStrategy(it) })
                    4 -> Step4Apps()
                    5 -> Step5Protection()
                    6 -> Step6Schedule()
                    7 -> Step7Finish()
                }
            }

            // Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    TextButton(onClick = { viewModel.prevStep() }) {
                        Text("Back", color = AppTheme.colors.textSecondary)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Button(
                    onClick = {
                        if (step < 7) {
                            viewModel.nextStep()
                        } else {
                            viewModel.completeOnboarding()
                            onFinished()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (step == 7) "Get Started" else "Continue",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun Step1Intro() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = AppTheme.colors.primary,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "My Notification", style = AppTheme.typography.displayMedium, color = AppTheme.colors.textPrimary)
        Text(
            text = "Less noise. More you.",
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.primary,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "A quiet control center for your phone. Block distractions, protect essential alerts, and keep complete offline notification history.",
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun Step2Access(isAccessGranted: Boolean, onGrant: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.security.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = AppTheme.colors.security,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Notification Access Required", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "To capture incoming alerts and dismiss distracting notifications, Android requires the Notification Listener permission. All data stays 100% on your device.",
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (isAccessGranted) {
            StatusBadge(text = "✓ Permission Granted", textColor = AppTheme.colors.success, bgColor = AppTheme.colors.successContainer)
        } else {
            Button(
                onClick = onGrant,
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Enable Notification Access", color = Color.White)
            }
        }
    }
}

@Composable
private fun Step3Strategy(selectedStrategy: String, onSelect: (String) -> Unit) {
    val options = listOf(
        "SOCIAL_MEDIA" to "Block Social Media (Instagram, Facebook, TikTok...)",
        "SELECTED_APPS" to "Block Selected Apps (Custom App Picker)",
        "BLOCK_ALL" to "Block Everything (Except whitelisted apps)",
        "ALLOW_ALL" to "Monitoring Only (Allow everything, capture history)"
    )

    Column {
        Text(text = "Choose Default Strategy", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
        Text(
            text = "Select your initial notification filter. You can adjust this anytime.",
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        options.forEach { (mode, label) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppTheme.colors.surface)
                    .border(1.dp, if (selectedStrategy == mode) AppTheme.colors.primary else AppTheme.colors.border, RoundedCornerShape(12.dp))
                    .clickable { onSelect(mode) }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedStrategy == mode,
                        onClick = { onSelect(mode) },
                        colors = RadioButtonDefaults.colors(selectedColor = AppTheme.colors.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textPrimary)
                }
            }
        }
    }
}

@Composable
private fun Step4Apps() {
    Column {
        Text(text = "Vital App Protection", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
        Text(
            text = "Essential communication apps are automatically pre-configured to be safely protected.",
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )
        listOf("Phone & Dialer", "Google Messages & SMS", "bKash & Banking Apps", "Google Calendar & Alarms").forEach { app ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = app, style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textPrimary)
                    StatusBadge(text = "Protected", textColor = AppTheme.colors.success, bgColor = AppTheme.colors.successContainer)
                }
            }
        }
    }
}

@Composable
private fun Step5Protection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.security.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = AppTheme.colors.security, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(text = "Protection Verification", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "OTP Protection: Enabled (Always bypasses blockers)\nFinancial Alerts: Enabled (Never miss transactions)\nEmergency Calls: Enabled (Always rings through)",
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun Step6Schedule() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Optional Night Focus", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "A default Night Focus schedule (10:00 PM to 7:00 AM) has been prepared for you. You can activate it anytime in the Blocking Rules tab.",
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun Step7Finish() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.success.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = AppTheme.colors.success, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "You're All Set!", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Welcome to a quieter phone and a calmer mind.",
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.textSecondary
        )
    }
}
