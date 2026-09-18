package com.my.notificationai.feature.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.my.notificationai.core.designsystem.components.AppTopBar
import com.my.notificationai.core.designsystem.theme.AppTheme

@Composable
fun RuleBuilderScreen(
    viewModel: RulesViewModel,
    innerPadding: PaddingValues,
    onBackClick: () -> Unit
) {
    var ruleName by remember { mutableStateOf("") }
    var conditionType by remember { mutableStateOf("TEXT") } // APP, TITLE, TEXT, CHANNEL
    var operator by remember { mutableStateOf("CONTAINS") } // CONTAINS, EQUALS, STARTS_WITH
    var targetValue by remember { mutableStateOf("") }
    var action by remember { mutableStateOf("BLOCK") } // BLOCK, ALLOW

    var showTypeDropdown by remember { mutableStateOf(false) }
    var showOpDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(innerPadding)
    ) {
        AppTopBar(
            title = "Custom Rule Builder",
            canGoBack = true,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Rule Name
            BuilderField(label = "Rule Name") {
                BasicTextField(
                    value = ruleName,
                    onValueChange = { ruleName = it },
                    textStyle = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.textPrimary),
                    cursorBrush = SolidColor(AppTheme.colors.primary),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Visual Sentence Builder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Condition Predicate",
                        style = AppTheme.typography.titleSmall,
                        color = AppTheme.colors.primary
                    )

                    // Target Field Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "When Field", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                        Box {
                            PillButton(text = conditionType, onClick = { showTypeDropdown = true })
                            DropdownMenu(
                                expanded = showTypeDropdown,
                                onDismissRequest = { showTypeDropdown = false },
                                modifier = Modifier.background(AppTheme.colors.surface)
                            ) {
                                listOf("TEXT", "TITLE", "APP", "CHANNEL").forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type, color = AppTheme.colors.textPrimary) },
                                        onClick = {
                                            conditionType = type
                                            showTypeDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Operator Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Condition", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                        Box {
                            PillButton(text = operator, onClick = { showOpDropdown = true })
                            DropdownMenu(
                                expanded = showOpDropdown,
                                onDismissRequest = { showOpDropdown = false },
                                modifier = Modifier.background(AppTheme.colors.surface)
                            ) {
                                listOf("CONTAINS", "EQUALS", "STARTS_WITH", "NOT_CONTAINS").forEach { op ->
                                    DropdownMenuItem(
                                        text = { Text(op, color = AppTheme.colors.textPrimary) },
                                        onClick = {
                                            operator = op
                                            showOpDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Value Input
                    BuilderField(label = "Match Value") {
                        BasicTextField(
                            value = targetValue,
                            onValueChange = { targetValue = it },
                            textStyle = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.textPrimary),
                            cursorBrush = SolidColor(AppTheme.colors.primary),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Action Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Action", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PillButton(
                                text = "BLOCK",
                                isSelected = action == "BLOCK",
                                activeBg = AppTheme.colors.error,
                                onClick = { action = "BLOCK" }
                            )
                            PillButton(
                                text = "ALLOW",
                                isSelected = action == "ALLOW",
                                activeBg = AppTheme.colors.success,
                                onClick = { action = "ALLOW" }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Rule Button
            Button(
                onClick = {
                    if (ruleName.isNotBlank() && targetValue.isNotBlank()) {
                        viewModel.addCustomRule(
                            name = ruleName,
                            description = "When $conditionType $operator '$targetValue' then $action",
                            action = action,
                            conditionType = conditionType,
                            operator = operator,
                            value = targetValue
                        )
                        onBackClick()
                    }
                },
                enabled = ruleName.isNotBlank() && targetValue.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.primary,
                    disabledContainerColor = AppTheme.colors.surfaceElevated
                )
            ) {
                Text(
                    text = "Save Custom Rule",
                    style = AppTheme.typography.labelLarge,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
}

@Composable
private fun BuilderField(
    label: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = label,
            style = AppTheme.typography.labelSmall,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppTheme.colors.surface)
                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            content()
        }
    }
}

@Composable
private fun PillButton(
    text: String,
    isSelected: Boolean = false,
    activeBg: androidx.compose.ui.graphics.Color = AppTheme.colors.primary,
    onClick: () -> Unit
) {
    val bg = if (isSelected) activeBg else AppTheme.colors.surfaceElevated
    val textColor = if (isSelected) androidx.compose.ui.graphics.Color.White else AppTheme.colors.textPrimary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(999.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(text = text, style = AppTheme.typography.labelMedium, color = textColor)
    }
}
