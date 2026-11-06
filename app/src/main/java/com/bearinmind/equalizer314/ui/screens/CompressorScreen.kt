package com.bearinmind.equalizer314.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bearinmind.equalizer314.ui.theme.*
import com.bearinmind.equalizer314.viewmodel.AudioFxUiState
import com.bearinmind.equalizer314.viewmodel.AudioFxViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressorScreen(
    state: AudioFxUiState,
    viewModel: AudioFxViewModel
) {
    val selectedIndex = state.selectedCompressorBandIndex.coerceIn(0, state.compressorBands.lastIndex)
    val currentBand = state.compressorBands[selectedIndex]

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Section: Multiband Compressor (MBC)
        item {
            Text(
                text = "Multiband Dynamics Compressor",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = "Studio 4-band dynamic range control with independent threshold, ratio, attack, and knee.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        // 4-Band Tab Selector
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                state.compressorBands.forEachIndexed { index, band ->
                    SegmentedButton(
                        selected = index == selectedIndex,
                        onClick = { viewModel.selectCompressorBand(index) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = state.compressorBands.size),
                        modifier = Modifier.testTag("mbc_band_tab_$index")
                    ) {
                        Text(
                            text = when(index) {
                                0 -> "Low"
                                1 -> "Low-Mid"
                                2 -> "Mid-High"
                                else -> "High"
                            },
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Live Gain Reduction Meter Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StudioCardBorder, StudioCardBorder)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentBand.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonCyan
                        )
                        Text(
                            text = "${currentBand.lowCutoffHz.toInt()} Hz - ${currentBand.highCutoffHz.toInt()} Hz",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Gain Reduction: -${"%.1f".format(currentBand.gainReductionDb)} dB",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (currentBand.gainReductionDb > 0.5f) AmberGold else TextMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioSurfaceVariant)
                    ) {
                        val reductionNorm = (currentBand.gainReductionDb / 15f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(reductionNorm)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(AmberGold, NeonRed)
                                    )
                                )
                        )
                    }
                }
            }
        }

        // MBC Controls Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StudioCardBorder, StudioCardBorder)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CompressorParamSlider(
                        label = "Threshold",
                        value = currentBand.thresholdDb,
                        unit = "dB",
                        range = -60f..0f,
                        step = 1f,
                        color = NeonCyan,
                        onValueChange = { viewModel.updateCompressorBand(currentBand.copy(thresholdDb = it)) }
                    )

                    CompressorParamSlider(
                        label = "Ratio",
                        value = currentBand.ratio,
                        unit = ":1",
                        range = 1f..20f,
                        step = 0.5f,
                        color = ElectricPurple,
                        onValueChange = { viewModel.updateCompressorBand(currentBand.copy(ratio = it)) }
                    )

                    CompressorParamSlider(
                        label = "Knee Width",
                        value = currentBand.kneeDb,
                        unit = "dB",
                        range = 0f..20f,
                        step = 1f,
                        color = TextSecondary,
                        onValueChange = { viewModel.updateCompressorBand(currentBand.copy(kneeDb = it)) }
                    )

                    CompressorParamSlider(
                        label = "Attack Time",
                        value = currentBand.attackMs,
                        unit = "ms",
                        range = 1f..300f,
                        step = 1f,
                        color = AmberGold,
                        onValueChange = { viewModel.updateCompressorBand(currentBand.copy(attackMs = it)) }
                    )

                    CompressorParamSlider(
                        label = "Release Time",
                        value = currentBand.releaseMs,
                        unit = "ms",
                        range = 10f..1500f,
                        step = 10f,
                        color = AmberGold,
                        onValueChange = { viewModel.updateCompressorBand(currentBand.copy(releaseMs = it)) }
                    )

                    CompressorParamSlider(
                        label = "Post Make-up Gain",
                        value = currentBand.postGainDb,
                        unit = "dB",
                        range = -15f..15f,
                        step = 0.5f,
                        color = NeonGreen,
                        onValueChange = { viewModel.updateCompressorBand(currentBand.copy(postGainDb = it)) }
                    )

                    CompressorParamSlider(
                        label = "Noise Gate Threshold",
                        value = currentBand.noiseGateThresholdDb,
                        unit = "dB",
                        range = -90f..-30f,
                        step = 1f,
                        color = TextMuted,
                        onValueChange = { viewModel.updateCompressorBand(currentBand.copy(noiseGateThresholdDb = it)) }
                    )
                }
            }
        }

        // Section: Loudness Normalizer & Limiter
        item {
            Text(
                text = "Loudness & Peak Protection",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }

        // Loudness Booster Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StudioCardBorder, StudioCardBorder)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Loudness Enhancer", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text("Android native loudness normalization", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Switch(
                            checked = state.loudness.enabled,
                            onCheckedChange = { viewModel.updateLoudness(state.loudness.copy(enabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyanDim)
                        )
                    }

                    if (state.loudness.enabled) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Target Boost", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Text("+${state.loudness.targetGainMilliBels / 100} dB", style = MaterialTheme.typography.bodyMedium, color = NeonCyan)
                            }
                            Slider(
                                value = state.loudness.targetGainMilliBels.toFloat(),
                                onValueChange = { viewModel.updateLoudness(state.loudness.copy(targetGainMilliBels = it.toInt())) },
                                valueRange = 0f..2000f,
                                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                            )
                        }
                    }
                }
            }
        }

        // Brickwall Limiter Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StudioCardBorder, StudioCardBorder)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Brickwall Peak Limiter", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text("Prevents digital clipping distortion", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (state.limiter.enabled) NeonGreen else TextMuted)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = state.limiter.enabled,
                                onCheckedChange = { viewModel.updateLimiter(state.limiter.copy(enabled = it)) },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyanDim)
                            )
                        }
                    }

                    CompressorParamSlider(
                        label = "Ceiling Limit",
                        value = state.limiter.ceilingDb,
                        unit = "dB",
                        range = -6.0f..0.0f,
                        step = 0.1f,
                        color = NeonCyan,
                        onValueChange = { viewModel.updateLimiter(state.limiter.copy(ceilingDb = it)) }
                    )

                    CompressorParamSlider(
                        label = "Threshold",
                        value = state.limiter.thresholdDb,
                        unit = "dB",
                        range = -12.0f..0.0f,
                        step = 0.5f,
                        color = AmberGold,
                        onValueChange = { viewModel.updateLimiter(state.limiter.copy(thresholdDb = it)) }
                    )

                    CompressorParamSlider(
                        label = "Release Time",
                        value = state.limiter.releaseMs,
                        unit = "ms",
                        range = 5f..300f,
                        step = 5f,
                        color = ElectricPurple,
                        onValueChange = { viewModel.updateLimiter(state.limiter.copy(releaseMs = it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompressorParamSlider(
    label: String,
    value: Float,
    unit: String,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Text("${"%.1f".format(value)} $unit", style = MaterialTheme.typography.bodyMedium, color = color)
        }
        Slider(
            value = value,
            onValueChange = {
                val stepped = ((it / step).roundToInt() * step).coerceIn(range.start, range.endInclusive)
                onValueChange(stepped)
            },
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
        )
    }
}
