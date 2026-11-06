package com.bearinmind.equalizer314.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bearinmind.equalizer314.ui.theme.*
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun FaderSlider(
    modifier: Modifier = Modifier,
    label: String,
    valueDb: Float,
    minDb: Float = -15f,
    maxDb: Float = 15f,
    testTagId: String,
    onValueChange: (Float) -> Unit
) {
    val totalHeight = 160.dp

    Column(
        modifier = modifier
            .width(42.dp)
            .testTag(testTagId),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gain display text
        val formattedGain = if (valueDb > 0f) "+${"%.1f".format(valueDb)}" else "%.1f".format(valueDb)
        Text(
            text = formattedGain,
            style = MaterialTheme.typography.labelSmall,
            color = if (abs(valueDb) < 0.1f) TextMuted else NeonCyan,
            fontSize = 9.sp,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Vertical Fader Track
        Box(
            modifier = Modifier
                .height(totalHeight)
                .width(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(StudioSurfaceVariant)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            // Track center slot
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SliderTrack)
            )

            // Center 0 dB Detent line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(1.5.dp)
                    .background(DetentLine)
            )

            // Draggable thumb implementation
            val normValue = ((valueDb - minDb) / (maxDb - minDb)).coerceIn(0f, 1f)
            // Normalized 0 to 1 where 1 is top (maxDb) and 0 is bottom (minDb)

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Colored active stem from center
                val centerNorm = (0f - minDb) / (maxDb - minDb) // usually 0.5
                if (normValue > centerNorm) {
                    // Positive gain (above center)
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight(normValue - centerNorm)
                            .offset(y = -((centerNorm * 140).dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(NeonCyan, NeonCyan.copy(alpha = 0.5f))
                                )
                            )
                    )
                } else if (normValue < centerNorm) {
                    // Negative gain (below center)
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight(centerNorm - normValue)
                            .offset(y = -((normValue * 140).dp))
                            .background(ElectricPurple.copy(alpha = 0.7f))
                    )
                }

                // Interactive thumb pill
                Box(
                    modifier = Modifier
                        .offset(y = -((normValue * 128).dp))
                        .size(width = 24.dp, height = 14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF333C4E), Color(0xFF1E2430))
                            )
                        )
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta ->
                                // Dragging up delta is negative
                                val changeDb = (-delta / 4.0f)
                                val newDb = (valueDb + changeDb).coerceIn(minDb, maxDb)
                                // Snap near 0dB
                                val snapped = if (abs(newDb) < 0.35f) 0f else newDb
                                onValueChange((snapped * 10).roundToInt() / 10f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Center accent notch on thumb
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .height(2.dp)
                            .background(if (abs(valueDb) < 0.1f) TextMuted else NeonCyan)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Frequency Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 9.sp,
            maxLines = 1
        )
    }
}
