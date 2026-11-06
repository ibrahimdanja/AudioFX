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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bearinmind.equalizer314.ui.theme.*
import com.bearinmind.equalizer314.viewmodel.AudioFxUiState
import com.bearinmind.equalizer314.viewmodel.AudioFxViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun StereoBassScreen(
    state: AudioFxUiState,
    viewModel: AudioFxViewModel
) {
    val stereo = state.stereo
    val bass = state.bassExciter

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Section: Stereo Processing
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stereo_card"),
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
                    Text(
                        text = "Stereo Stage & Channel Matrix",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )

                    // Balance Slider (-1.0 to +1.0)
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Left / Right Balance", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            val balanceText = when {
                                abs(stereo.balance) < 0.05f -> "Center"
                                stereo.balance < 0 -> "L ${(-stereo.balance * 100).toInt()}%"
                                else -> "R ${(stereo.balance * 100).toInt()}%"
                            }
                            Text(balanceText, style = MaterialTheme.typography.bodyMedium, color = NeonCyan)
                        }
                        Slider(
                            value = stereo.balance,
                            onValueChange = {
                                val snapped = if (abs(it) < 0.08f) 0f else (it * 20).roundToInt() / 20f
                                viewModel.updateStereo(stereo.copy(balance = snapped))
                            },
                            valueRange = -1.0f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                        )
                    }

                    // Channel Mutes & Mono
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = stereo.monoDownmix,
                            onClick = { viewModel.updateStereo(stereo.copy(monoDownmix = !stereo.monoDownmix)) },
                            label = { Text("Mono Downmix", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricPurpleDim,
                                selectedLabelColor = Color.White
                            )
                        )

                        FilterChip(
                            selected = stereo.muteLeft,
                            onClick = { viewModel.updateStereo(stereo.copy(muteLeft = !stereo.muteLeft)) },
                            label = { Text(if (stereo.muteLeft) "Muted L" else "Mute L", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonRed.copy(alpha = 0.3f),
                                selectedLabelColor = NeonRed
                            )
                        )

                        FilterChip(
                            selected = stereo.muteRight,
                            onClick = { viewModel.updateStereo(stereo.copy(muteRight = !stereo.muteRight)) },
                            label = { Text(if (stereo.muteRight) "Muted R" else "Mute R", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonRed.copy(alpha = 0.3f),
                                selectedLabelColor = NeonRed
                            )
                        )
                    }

                    HorizontalDivider(color = StudioCardBorder)

                    // Bauer Headphone Crossfeed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Bauer Headphone Crossfeed", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Text("Reduces headphone listening fatigue by simulating speaker acoustic crosstalk", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Switch(
                            checked = stereo.crossfeedEnabled,
                            onCheckedChange = { viewModel.updateStereo(stereo.copy(crossfeedEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyanDim)
                        )
                    }

                    if (stereo.crossfeedEnabled) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Crossfeed Depth", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Text("${(stereo.crossfeedLevel * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = NeonCyan)
                            }
                            Slider(
                                value = stereo.crossfeedLevel,
                                onValueChange = { viewModel.updateStereo(stereo.copy(crossfeedLevel = it)) },
                                valueRange = 0.1f..1.0f,
                                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                            )
                        }
                    }

                    // Stereo Width (0 to 200%)
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Stereo Spatial Width", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("${(stereo.stereoWidth * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = ElectricPurple)
                        }
                        Slider(
                            value = stereo.stereoWidth,
                            onValueChange = {
                                val rounded = (it * 20).roundToInt() / 20f
                                viewModel.updateStereo(stereo.copy(stereoWidth = rounded))
                            },
                            valueRange = 0.0f..2.0f,
                            colors = SliderDefaults.colors(thumbColor = ElectricPurple, activeTrackColor = ElectricPurple)
                        )
                    }
                }
            }
        }

        // Section: Bass Enhancement & Tonal Exciter
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bass_card"),
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
                            Text("Bass Enhancement & Exciter", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text(
                                if (state.hardwareCapabilities.hasBassBoost) "Hardware Native BassBoost" else "DSP Parametric Bass Boost",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (state.hardwareCapabilities.hasBassBoost) NeonGreen else AmberGold
                            )
                        }
                    }

                    // Sub-Bass Boost Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sub-Bass Boost Level", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("${(bass.bassBoostStrength / 10).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = NeonCyan)
                        }
                        Slider(
                            value = bass.bassBoostStrength,
                            onValueChange = { viewModel.updateBassExciter(bass.copy(bassBoostStrength = it)) },
                            valueRange = 0f..1000f,
                            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                        )
                    }

                    // Tonal Tilt (-6dB to +6dB)
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tonal Tilt (Warm ↔ Bright)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            val tiltLabel = when {
                                abs(bass.tonalTiltDb) < 0.2f -> "Neutral"
                                bass.tonalTiltDb < 0 -> "${"%.1f".format(bass.tonalTiltDb)} dB (Warm)"
                                else -> "+${"%.1f".format(bass.tonalTiltDb)} dB (Bright)"
                            }
                            Text(tiltLabel, style = MaterialTheme.typography.bodyMedium, color = AmberGold)
                        }
                        Slider(
                            value = bass.tonalTiltDb,
                            onValueChange = {
                                val snapped = if (abs(it) < 0.2f) 0f else (it * 10).roundToInt() / 10f
                                viewModel.updateBassExciter(bass.copy(tonalTiltDb = snapped))
                            },
                            valueRange = -6.0f..6.0f,
                            colors = SliderDefaults.colors(thumbColor = AmberGold, activeTrackColor = AmberGold)
                        )
                    }

                    // Harmonic Exciter & Air Brilliance
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Harmonic Exciter (Air Brilliance)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("${bass.harmonicExciterStrength.toInt()}%", style = MaterialTheme.typography.bodyMedium, color = ElectricPurple)
                        }
                        Slider(
                            value = bass.harmonicExciterStrength,
                            onValueChange = { viewModel.updateBassExciter(bass.copy(harmonicExciterStrength = it)) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(thumbColor = ElectricPurple, activeTrackColor = ElectricPurple)
                        )
                    }
                }
            }
        }
    }
}
