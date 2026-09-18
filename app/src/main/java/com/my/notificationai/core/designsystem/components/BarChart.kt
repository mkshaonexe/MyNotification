package com.my.notificationai.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.my.notificationai.core.designsystem.theme.AppTheme

data class BarChartItem(
    val label: String,
    val value: Int
)

@Composable
fun BarChart(
    items: List<BarChartItem>,
    modifier: Modifier = Modifier,
    barColor: Color = AppTheme.colors.primary,
    activeBarColor: Color = AppTheme.colors.primary,
    inactiveBarColor: Color = AppTheme.colors.primary.copy(alpha = 0.35f),
    gridLineColor: Color = AppTheme.colors.borderSubtle
) {
    if (items.isEmpty()) return

    val maxValue = (items.maxOfOrNull { it.value } ?: 1).coerceAtLeast(1)

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barCount = items.size
            val barWidth = (canvasWidth / barCount) * 0.45f
            val slotWidth = canvasWidth / barCount

            // Draw subtle horizontal grid lines (0%, 50%, 100%)
            val lineY50 = canvasHeight * 0.5f
            val lineY100 = 0f
            drawLine(
                color = gridLineColor,
                start = Offset(0f, lineY50),
                end = Offset(canvasWidth, lineY50),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = gridLineColor,
                start = Offset(0f, lineY100),
                end = Offset(canvasWidth, lineY100),
                strokeWidth = 1.dp.toPx()
            )

            // Draw bars
            items.forEachIndexed { index, item ->
                val barHeight = (item.value.toFloat() / maxValue.toFloat()) * (canvasHeight * 0.88f)
                val x = index * slotWidth + (slotWidth - barWidth) / 2f
                val y = canvasHeight - barHeight

                val color = if (item.value == maxValue && maxValue > 0) activeBarColor else inactiveBarColor

                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight.coerceAtLeast(4.dp.toPx())),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }

        // Labels under bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                Text(
                    text = item.label,
                    style = AppTheme.typography.labelSmall,
                    color = AppTheme.colors.textTertiary
                )
            }
        }
    }
}
