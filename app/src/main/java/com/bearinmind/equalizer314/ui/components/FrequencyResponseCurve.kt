package com.bearinmind.equalizer314.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bearinmind.equalizer314.dsp.BiquadMath
import com.bearinmind.equalizer314.model.ParametricFilter
import com.bearinmind.equalizer314.ui.theme.*
import kotlin.math.log10

@Composable
fun FrequencyResponseCurve(
    modifier: Modifier = Modifier,
    points: List<Pair<Float, Float>>, // (Frequency Hz, Gain dB)
    targetPoints: List<Pair<Float, Float>>? = null,
    parametricNodes: List<ParametricFilter> = emptyList(),
    minDb: Float = -15f,
    maxDb: Float = 15f
) {
    val gridFrequencies = listOf(20f, 50f, 100f, 250f, 500f, 1000f, 2500f, 5000f, 10000f, 20000f)
    val gridDbs = listOf(12f, 6f, 0f, -6f, -12f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(StudioSurface)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(14.dp))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val minLog = log10(20.0)
            val maxLog = log10(20000.0)

            fun xForFreq(freq: Float): Float {
                val fLog = log10(freq.toDouble().coerceIn(20.0, 20000.0))
                return (((fLog - minLog) / (maxLog - minLog)) * w).toFloat()
            }

            fun yForDb(db: Float): Float {
                val clamped = db.coerceIn(minDb, maxDb)
                val norm = (clamped - minDb) / (maxDb - minDb)
                return ((1f - norm) * h)
            }

            // Draw horizontal dB grid lines
            for (db in gridDbs) {
                val y = yForDb(db)
                val isZero = db == 0f
                drawLine(
                    color = if (isZero) DetentLine.copy(alpha = 0.8f) else StudioCardBorder.copy(alpha = 0.4f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = if (isZero) 1.5f else 1f
                )
            }

            // Draw vertical log frequency grid lines
            for (freq in gridFrequencies) {
                val x = xForFreq(freq)
                drawLine(
                    color = StudioCardBorder.copy(alpha = 0.35f),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1f
                )
            }

            // Draw target compensation curve if provided (AutoEQ overlay)
            targetPoints?.let { tPoints ->
                if (tPoints.isNotEmpty()) {
                    val targetPath = Path()
                    tPoints.forEachIndexed { index, (freq, db) ->
                        val x = xForFreq(freq)
                        val y = yForDb(db)
                        if (index == 0) targetPath.moveTo(x, y)
                        else targetPath.lineTo(x, y)
                    }
                    drawPath(
                        path = targetPath,
                        color = AmberGold.copy(alpha = 0.85f),
                        style = Stroke(
                            width = 2.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                        )
                    )
                }
            }

            // Draw main response curve
            if (points.isNotEmpty()) {
                val curvePath = Path()
                val fillPath = Path()

                points.forEachIndexed { index, (freq, db) ->
                    val x = xForFreq(freq)
                    val y = yForDb(db)
                    if (index == 0) {
                        curvePath.moveTo(x, y)
                        fillPath.moveTo(x, h)
                        fillPath.lineTo(x, y)
                    } else {
                        curvePath.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }

                val lastX = xForFreq(points.last().first)
                fillPath.lineTo(lastX, h)
                fillPath.close()

                // Draw neon gradient area fill
                val fillBrush = Brush.verticalGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.30f),
                        ElectricPurple.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h
                )
                drawPath(path = fillPath, brush = fillBrush)

                // Draw glowing response stroke
                drawPath(
                    path = curvePath,
                    color = NeonCyan,
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // Draw parametric control nodes
            for (node in parametricNodes) {
                if (!node.enabled) continue
                val nx = xForFreq(node.frequency)
                val ny = yForDb(node.gainDb)

                // Outer glowing circle
                drawCircle(
                    color = ElectricPurple.copy(alpha = 0.35f),
                    radius = 14f,
                    center = Offset(nx, ny)
                )
                // Center point
                drawCircle(
                    color = ElectricPurple,
                    radius = 6f,
                    center = Offset(nx, ny)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5f,
                    center = Offset(nx, ny)
                )
            }
        }

        // dB axis labels on the left
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("+12dB", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
            Text("0dB", style = MaterialTheme.typography.labelSmall, color = NeonCyan.copy(alpha = 0.7f), fontSize = 9.sp)
            Text("-12dB", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
        }

        // Frequency labels along the bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("50", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 8.sp)
            Text("250", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 8.sp)
            Text("1k", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 8.sp)
            Text("5k", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 8.sp)
            Text("16k", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 8.sp)
        }
    }
}
