package com.bearinmind.equalizer314.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.bearinmind.equalizer314.ui.theme.*

@Composable
fun SpectrumMeter(
    modifier: Modifier = Modifier,
    bars: List<Float>, // 16 values in [0f, 1f]
    isPowered: Boolean
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(StudioSurface)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalBars = bars.size.coerceAtLeast(1)
            val barGap = 4.dp.toPx()
            val availableWidth = size.width - (barGap * (totalBars - 1))
            val barWidth = (availableWidth / totalBars).coerceAtLeast(2f)
            val h = size.height

            val gradientBrush = Brush.verticalGradient(
                colors = listOf(
                    NeonCyan,
                    ElectricPurple,
                    NeonCyanDim
                ),
                startY = 0f,
                endY = h
            )

            bars.forEachIndexed { i, level ->
                val barH = (level.coerceIn(0.04f, 1f) * h).coerceAtLeast(3f)
                val x = i * (barWidth + barGap)
                val y = h - barH

                // Draw audio bar
                drawRoundRect(
                    brush = if (isPowered) gradientBrush else Brush.verticalGradient(listOf(TextMuted, StudioCardBorder)),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )

                // Draw peak cap line
                if (isPowered && level > 0.15f) {
                    drawRect(
                        color = AmberGold,
                        topLeft = Offset(x, (y - 3f).coerceAtLeast(0f)),
                        size = Size(barWidth, 2.dp.toPx())
                    )
                }
            }
        }
    }
}
