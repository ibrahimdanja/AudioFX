package com.bearinmind.equalizer314.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bearinmind.equalizer314.ui.components.SpectrumMeter
import com.bearinmind.equalizer314.ui.screens.*
import com.bearinmind.equalizer314.ui.theme.*
import com.bearinmind.equalizer314.viewmodel.AudioFxViewModel
import kotlin.math.roundToInt

enum class AppNavDestination(val title: String) {
    DASHBOARD("Pipeline"),
    EQUALIZER("Equalizer"),
    DYNAMICS("Dynamics"),
    STEREO_BASS("Stereo & Bass"),
    REVERB("Reverb"),
    AUTO_EQ("AutoEQ"),
    PRESETS("Presets & FX")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioFxAppScaffold(
    viewModel: AudioFxViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var currentNav by remember { mutableStateOf(AppNavDestination.DASHBOARD) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = StudioBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioSurface)
                    .border(1.dp, StudioCardBorder)
            ) {
                // Top App Bar
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AudioFX",
                                style = MaterialTheme.typography.titleLarge,
                                color = NeonCyan
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "STUDIO",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberGold,
                                fontSize = 10.sp
                            )
                        }
                    },
                    actions = {
                        // Preamp Quick Slider Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(StudioSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PREAMP",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${if (state.preampDb > 0) "+" else ""}${"%.1f".format(state.preampDb)}dB",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (state.preampDb == 0f) TextSecondary else NeonCyan,
                                fontSize = 10.sp
                            )
                            Slider(
                                value = state.preampDb,
                                onValueChange = { viewModel.setPreamp((it * 10).roundToInt() / 10f) },
                                valueRange = -15f..15f,
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(28.dp),
                                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Master Power Switch
                        IconButton(
                            onClick = { viewModel.toggleMasterPower() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (state.isPoweredOn) NeonGreen.copy(alpha = 0.2f) else StudioSurfaceVariant)
                                .border(1.dp, if (state.isPoweredOn) NeonGreen else StudioCardBorder, CircleShape)
                                .testTag("master_power_button")
                        ) {
                            Icon(
                                Icons.Default.PowerSettingsNew,
                                contentDescription = "Master Audio Power Toggle",
                                tint = if (state.isPoweredOn) NeonGreen else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioSurface)
                )

                // Real-time audio spectrum analyzer banner
                SpectrumMeter(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    bars = state.spectrumBars,
                    isPowered = state.isPoweredOn
                )

                // Fast scrollable tab bar for instant module switching
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppNavDestination.values().forEach { destination ->
                        val isSelected = currentNav == destination
                        FilterChip(
                            selected = isSelected,
                            onClick = { currentNav = destination },
                            label = { Text(destination.title, fontSize = 11.sp) },
                            leadingIcon = {
                                val icon = when (destination) {
                                    AppNavDestination.DASHBOARD -> Icons.Default.Dashboard
                                    AppNavDestination.EQUALIZER -> Icons.Default.Tune
                                    AppNavDestination.DYNAMICS -> Icons.Default.Compress
                                    AppNavDestination.STEREO_BASS -> Icons.Default.Shuffle
                                    AppNavDestination.REVERB -> Icons.Default.GraphicEq
                                    AppNavDestination.AUTO_EQ -> Icons.Default.Headphones
                                    AppNavDestination.PRESETS -> Icons.Default.SettingsVoice
                                }
                                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.18f),
                                selectedLabelColor = NeonCyan,
                                selectedLeadingIconColor = NeonCyan,
                                containerColor = StudioSurfaceVariant,
                                labelColor = TextSecondary,
                                iconColor = TextSecondary
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) NeonCyan else StudioCardBorder
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentNav) {
                AppNavDestination.DASHBOARD -> DashboardScreen(
                    state = state,
                    viewModel = viewModel,
                    onNavigateTo = { currentNav = it }
                )
                AppNavDestination.EQUALIZER -> EqualizerScreen(state = state, viewModel = viewModel)
                AppNavDestination.DYNAMICS -> CompressorScreen(state = state, viewModel = viewModel)
                AppNavDestination.STEREO_BASS -> StereoBassScreen(state = state, viewModel = viewModel)
                AppNavDestination.REVERB -> ReverbScreen(state = state, viewModel = viewModel)
                AppNavDestination.AUTO_EQ -> AutoEqScreen(state = state, viewModel = viewModel)
                AppNavDestination.PRESETS -> PresetsScreen(state = state, viewModel = viewModel)
            }
        }
    }
}
