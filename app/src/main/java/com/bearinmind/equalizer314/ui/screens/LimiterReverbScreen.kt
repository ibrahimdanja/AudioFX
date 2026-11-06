package com.bearinmind.equalizer314.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bearinmind.equalizer314.model.LimiterConfig
import com.bearinmind.equalizer314.model.ReverbConfig
import com.bearinmind.equalizer314.model.ReverbPreset
import com.bearinmind.equalizer314.ui.theme.*
import com.bearinmind.equalizer314.viewmodel.AudioFxUiState
import com.bearinmind.equalizer314.viewmodel.AudioFxViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimiterReverbScreen(
    state: AudioFxUiState,
    viewModel: AudioFxViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Peak Limiter Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("limiter_card"),
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
                            Text(
                                text = "Studio Brickwall Limiter",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Zero-clipping peak protection with lookahead",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Clipping indicator LED
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (state.limiter.enabled) NeonGreen else TextMuted)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Switch(
                                checked = state.limiter.enabled,
                                onCheckedChange = {
                                    viewModel.updateLimiter(state.limiter.copy(enabled = it))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonCyan,
                                    checkedTrackColor = NeonCyanDim
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = StudioCardBorder)

                    // Ceiling Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ceiling Limit", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("${"%.1f".format(state.limiter.ceilingDb)} dB", style = MaterialTheme.typography.bodyMedium, color = NeonCyan)
                        }
                        Slider(
                            value = state.limiter.ceilingDb,
                            onValueChange = { viewModel.updateLimiter(state.limiter.copy(ceilingDb = (it * 10).roundToInt() / 10f)) },
                            valueRange = -6.0f..0.0f,
                            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                        )
                    }

                    // Threshold Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Threshold", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("${"%.1f".format(state.limiter.thresholdDb)} dB", style = MaterialTheme.typography.bodyMedium, color = AmberGold)
                        }
                        Slider(
                            value = state.limiter.thresholdDb,
                            onValueChange = { viewModel.updateLimiter(state.limiter.copy(thresholdDb = (it * 10).roundToInt() / 10f)) },
                            valueRange = -12.0f..0.0f,
                            colors = SliderDefaults.colors(thumbColor = AmberGold, activeTrackColor = AmberGold)
                        )
                    }

                    // Release Time
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Release Time", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("${state.limiter.releaseMs.toInt()} ms", style = MaterialTheme.typography.bodyMedium, color = ElectricPurple)
                        }
                        Slider(
                            value = state.limiter.releaseMs,
                            onValueChange = { viewModel.updateLimiter(state.limiter.copy(releaseMs = (it / 5).roundToInt() * 5f)) },
                            valueRange = 5f..300f,
                            colors = SliderDefaults.colors(thumbColor = ElectricPurple, activeTrackColor = ElectricPurple)
                        )
                    }
                }
            }
        }

        // Environmental Reverb Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reverb_card"),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Environmental Reverb Modeler",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Acoustic spatial reflection and decay simulation",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        Switch(
                            checked = state.reverb.enabled,
                            onCheckedChange = {
                                viewModel.updateReverb(state.reverb.copy(enabled = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ElectricPurple,
                                checkedTrackColor = ElectricPurpleDim
                            )
                        )
                    }

                    HorizontalDivider(color = StudioCardBorder)

                    // Presets Grid
                    Text(
                        text = "Acoustic Spaces",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val presets = ReverbPreset.values().filter { it != ReverbPreset.NONE }
                        presets.chunked(2).forEach { rowPresets ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowPresets.forEach { preset ->
                                    val isSelected = state.reverb.preset == preset && state.reverb.enabled
                                    OutlinedButton(
                                        onClick = { viewModel.setReverbPreset(preset) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (isSelected) ElectricPurple.copy(alpha = 0.2f) else Color.Transparent,
                                            contentColor = if (isSelected) NeonCyan else TextSecondary
                                        ),
                                        border = ButtonDefaults.outlinedButtonBorder.copy(
                                            brush = Brush.linearGradient(
                                                listOf(
                                                    if (isSelected) NeonCyan else StudioCardBorder,
                                                    if (isSelected) ElectricPurple else StudioCardBorder
                                                )
                                            )
                                        ),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(preset.displayName, fontSize = 11.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }

                    // Decay Time Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Decay Time", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("${state.reverb.decayTimeMs} ms", style = MaterialTheme.typography.bodyMedium, color = NeonCyan)
                        }
                        Slider(
                            value = state.reverb.decayTimeMs.toFloat(),
                            onValueChange = { viewModel.updateReverb(state.reverb.copy(decayTimeMs = it.toInt())) },
                            valueRange = 200f..5000f,
                            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                        )
                    }

                    // Reverb Level Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Reverb Wet Mix", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("${"%.1f".format(state.reverb.reverbLevelDb)} dB", style = MaterialTheme.typography.bodyMedium, color = AmberGold)
                        }
                        Slider(
                            value = state.reverb.reverbLevelDb,
                            onValueChange = { viewModel.updateReverb(state.reverb.copy(reverbLevelDb = (it * 10).roundToInt() / 10f)) },
                            valueRange = -30f..0f,
                            colors = SliderDefaults.colors(thumbColor = AmberGold, activeTrackColor = AmberGold)
                        )
                    }
                }
            }
        }
    }
}
