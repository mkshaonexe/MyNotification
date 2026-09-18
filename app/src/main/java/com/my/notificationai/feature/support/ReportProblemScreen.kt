package com.my.notificationai.feature.support

import android.os.Build
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.my.notificationai.core.designsystem.components.AppTopBar
import com.my.notificationai.core.designsystem.theme.AppTheme

@Composable
fun ReportProblemScreen(
    innerPadding: PaddingValues,
    onBackClick: () -> Unit
) {
    var problemText by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(innerPadding)
    ) {
        AppTopBar(
            title = "Report a Problem",
            canGoBack = true,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Device Diagnostic Info",
                        style = AppTheme.typography.titleSmall,
                        color = AppTheme.colors.primary
                    )
                    Text(
                        text = "• Model: ${Build.MANUFACTURER} ${Build.MODEL}",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textSecondary
                    )
                    Text(
                        text = "• Android Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textSecondary
                    )
                }
            }

            Column {
                Text(
                    text = "Describe the issue or unexpected notification behavior:",
                    style = AppTheme.typography.labelMedium,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppTheme.colors.surface)
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    BasicTextField(
                        value = problemText,
                        onValueChange = { problemText = it },
                        textStyle = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.textPrimary),
                        cursorBrush = SolidColor(AppTheme.colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (submitted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppTheme.colors.successContainer)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Thank you! Your issue report has been logged locally.",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.success
                    )
                }
            } else {
                Button(
                    onClick = {
                        if (problemText.isNotBlank()) {
                            submitted = true
                        }
                    },
                    enabled = problemText.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primary)
                ) {
                    Text("Submit Report", color = Color.White)
                }
            }
        }
    }
}
