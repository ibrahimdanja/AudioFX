package com.bearinmind.equalizer314.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bearinmind.equalizer314.model.EffectScopeType
import com.bearinmind.equalizer314.ui.AppNavDestination
import com.bearinmind.equalizer314.ui.theme.*
import com.bearinmind.equalizer314.viewmodel.AudioFxUiState
import com.bearinmind.equalizer314.viewmodel.AudioFxViewModel

data class DashboardModuleItem(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val scope: EffectScopeType,
    val status: String,
    val isEnabled: Boolean,
    val targetNav: AppNavDestination,
    val onToggle: ((Boolean) -> Unit)? = null
)

@Composable
fun DashboardScreen(
    state: AudioFxUiState,
    viewModel: AudioFxViewModel,
    onNavigateTo: (AppNavDestination) -> Unit
) {
    val modules = listOf(
        DashboardModuleItem(
            id = "preamp",
            name = "Input Gain & Preamp",
            description = "Master stage volume alignment before DSP processing",
            icon = Icons.Default.VolumeUp,
            scope = EffectScopeType.GLOBAL_SESSION_0,
            status = "${if (state.preampDb > 0) "+" else ""}${"%.1f".format(state.preampDb)} dB",
            isEnabled = true,
            targetNav = AppNavDestination.EQUALIZER
        ),
        DashboardModuleItem(
            id = "stereo",
            name = "Stereo Matrix & Crossfeed",
            description = "L/R Balance, Mono downmix, Bauer headphone crossfeed, Stereo width",
            icon = Icons.Default.Shuffle,
            scope = EffectScopeType.LOCAL_DSP,
            status = "Bal: ${"%.1f".format(state.stereo.balance)} • Width: ${(state.stereo.stereoWidth * 100).toInt()}%",
            isEnabled = true,
            targetNav = AppNavDestination.STEREO_BASS
        ),
        DashboardModuleItem(
            id = "equalizer",
            name = "Equalizer (${state.eqMode.displayName})",
            description = "10/20/31-band graphic filters & multi-band parametric biquad",
            icon = Icons.Default.Tune,
            scope = EffectScopeType.GLOBAL_SESSION_0,
            status = state.currentPresetName,
            isEnabled = true,
            targetNav = AppNavDestination.EQUALIZER
        ),
        DashboardModuleItem(
            id = "bass_exciter",
            name = "Bass Enhancement & Tonal Exciter",
            description = "Sub-bass harmonic boost, high-frequency air, and tonal tilt",
            icon = Icons.Default.Speaker,
            scope = if (state.hardwareCapabilities.hasBassBoost) EffectScopeType.HARDWARE_NATIVE else EffectScopeType.LOCAL_DSP,
            status = "Bass: ${(state.bassExciter.bassBoostStrength / 10).toInt()}% • Tilt: ${"%.1f".format(state.bassExciter.tonalTiltDb)} dB",
            isEnabled = state.bassExciter.bassBoostStrength > 0 || state.bassExciter.tonalTiltDb != 0f,
            targetNav = AppNavDestination.STEREO_BASS
        ),
        DashboardModuleItem(
            id = "mbc",
            name = "Multiband Dynamics Compressor",
            description = "4-band studio dynamic range compressor with live GR metering",
            icon = Icons.Default.Compress,
            scope = EffectScopeType.GLOBAL_SESSION_0,
            status = "4-Bands Active • Sub to Air",
            isEnabled = true,
            targetNav = AppNavDestination.DYNAMICS
        ),
        DashboardModuleItem(
            id = "reverb",
            name = "Environmental Reverb",
            description = "Acoustic space reflection and decay modeling",
            icon = Icons.Default.GraphicEq,
            scope = EffectScopeType.GLOBAL_SESSION_0,
            status = if (state.reverb.enabled) state.reverb.preset.displayName else "Bypassed",
            isEnabled = state.reverb.enabled,
            targetNav = AppNavDestination.REVERB,
            onToggle = { viewModel.updateReverb(state.reverb.copy(enabled = it)) }
        ),
        DashboardModuleItem(
            id = "limiter_loudness",
            name = "Loudness & Brickwall Limiter",
            description = "True-peak ceiling protection and target loudness gain",
            icon = Icons.Default.Shield,
            scope = EffectScopeType.GLOBAL_SESSION_0,
            status = "Ceiling: ${"%.1f".format(state.limiter.ceilingDb)} dB",
            isEnabled = state.limiter.enabled,
            targetNav = AppNavDestination.DYNAMICS,
            onToggle = { viewModel.updateLimiter(state.limiter.copy(enabled = it)) }
        ),
        DashboardModuleItem(
            id = "autoeq",
            name = "AutoEQ Target Calibration",
            description = "Calibrated parametric curves from 10,000+ headphone measurements",
            icon = Icons.Default.Headphones,
            scope = EffectScopeType.LOCAL_DSP,
            status = state.activeAutoEqProfile?.headphoneName ?: "No profile selected",
            isEnabled = state.activeAutoEqProfile != null,
            targetNav = AppNavDestination.AUTO_EQ
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // System Hardware & Processing Architecture Status Banner
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
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Engine Architecture & Routing",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )

                        // Online / Processing pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (state.isPoweredOn) NeonGreen.copy(alpha = 0.15f) else TextMuted.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (state.isPoweredOn) NeonGreen else TextMuted)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.isPoweredOn) "PROCESSING" else "STANDBY",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (state.isPoweredOn) NeonGreen else TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }

                    HorizontalDivider(color = StudioCardBorder)

                    // Diagnostic status chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Target Device Target:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("Samsung Galaxy A10s (Android 10)", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("DynamicsProcessing (Session 0):", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                if (state.hardwareCapabilities.hasDynamicsProcessing) "Active & Permissive" else "Fallback Mode",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (state.hardwareCapabilities.hasDynamicsProcessing) NeonGreen else AmberGold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Hardware Native BassBoost:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                if (state.hardwareCapabilities.hasBassBoost) "Supported (HAL)" else "Parametric Fallback",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (state.hardwareCapabilities.hasBassBoost) NeonGreen else TextSecondary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Active Audio Sessions:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                "${state.hardwareCapabilities.activeSessionCount} attached players",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricPurple
                            )
                        }
                    }
                }
            }
        }

        // Section Title: EasyEffects Modular Audio Pipeline
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Effects Pipeline (Signal Chain)",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Tap module to open controls",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        // Module Cards
        items(modules, key = { it.id }) { item ->
            PipelineModuleCard(
                item = item,
                onClick = { onNavigateTo(item.targetNav) }
            )
        }
    }
}

@Composable
private fun PipelineModuleCard(
    item: DashboardModuleItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("pipeline_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    if (item.isEnabled) StudioCardBorder else StudioCardBorder.copy(alpha = 0.5f),
                    if (item.isEnabled) StudioCardBorder else StudioCardBorder.copy(alpha = 0.5f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudioSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = if (item.isEnabled) NeonCyan else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Scope badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(item.scope.badgeColorHex).copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = item.scope.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(item.scope.badgeColorHex),
                                fontSize = 8.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = item.status,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                }
            }

            if (item.onToggle != null) {
                Switch(
                    checked = item.isEnabled,
                    onCheckedChange = item.onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonCyan,
                        checkedTrackColor = NeonCyanDim
                    ),
                    modifier = Modifier.height(24.dp)
                )
            } else {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Open",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
