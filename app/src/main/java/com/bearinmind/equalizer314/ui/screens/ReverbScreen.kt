package com.bearinmind.equalizer314.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bearinmind.equalizer314.model.ReverbPreset
import com.bearinmind.equalizer314.ui.theme.*
import com.bearinmind.equalizer314.viewmodel.AudioFxUiState
import com.bearinmind.equalizer314.viewmodel.AudioFxViewModel
import kotlin.math.roundToInt

@Composable
fun ReverbScreen(
    state: AudioFxUiState,
    viewModel: AudioFxViewModel
) {
    val reverb = state.reverb

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = "Environmental Reverb & Ambience",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = "Acoustic spatial simulation with early reflection and tail decay modeling.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reverb_main_card"),
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
                            Text("Acoustic Modeler Engine", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text(
                                if (state.hardwareCapabilities.hasEnvironmentalReverb) "Hardware Reverb Active" else "Bypassed",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (state.hardwareCapabilities.hasEnvironmentalReverb) NeonGreen else TextMuted
                            )
                        }

                        Switch(
                            checked = reverb.enabled,
                            onCheckedChange = { viewModel.updateReverb(reverb.copy(enabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ElectricPurple,
                                checkedTrackColor = ElectricPurpleDim
                            )
                        )
                    }

                    HorizontalDivider(color = StudioCardBorder)

                    // Presets Grid
                    Text(
                        text = "Acoustic Spaces & Environments",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val presets = ReverbPreset.values().filter { it != ReverbPreset.NONE }
                        presets.chunked(2).forEach { rowPresets ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowPresets.forEach { preset ->
                                    val isSelected = reverb.preset == preset && reverb.enabled
                                    OutlinedButton(
                                        onClick = { viewModel.setReverbPreset(preset) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (isSelected) ElectricPurple.copy(alpha = 0.2f) else StudioSurfaceVariant,
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
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
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
                            Text("Decay Time (RT60)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("${reverb.decayTimeMs} ms", style = MaterialTheme.typography.bodyMedium, color = NeonCyan)
                        }
                        Slider(
                            value = reverb.decayTimeMs.toFloat(),
                            onValueChange = { viewModel.updateReverb(reverb.copy(decayTimeMs = it.toInt())) },
                            valueRange = 100f..5000f,
                            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                        )
                    }

                    // Reverb Wet Level Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Wet Mix Level", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("${"%.1f".format(reverb.reverbLevelDb)} dB", style = MaterialTheme.typography.bodyMedium, color = AmberGold)
                        }
                        Slider(
                            value = reverb.reverbLevelDb,
                            onValueChange = { viewModel.updateReverb(reverb.copy(reverbLevelDb = (it * 10).roundToInt() / 10f)) },
                            valueRange = -30f..0f,
                            colors = SliderDefaults.colors(thumbColor = AmberGold, activeTrackColor = AmberGold)
                        )
                    }

                    // Room Size & Damping Sliders
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Room Size Simulation", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("${(reverb.roomSize * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = ElectricPurple)
                        }
                        Slider(
                            value = reverb.roomSize,
                            onValueChange = { viewModel.updateReverb(reverb.copy(roomSize = it)) },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = ElectricPurple, activeTrackColor = ElectricPurple)
                        )
                    }
                }
            }
        }
    }
}
