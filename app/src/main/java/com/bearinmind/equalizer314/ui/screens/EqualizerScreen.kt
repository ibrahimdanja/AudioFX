package com.bearinmind.equalizer314.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
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
import com.bearinmind.equalizer314.dsp.AudioDspEngine
import com.bearinmind.equalizer314.dsp.BiquadMath
import com.bearinmind.equalizer314.model.EqMode
import com.bearinmind.equalizer314.model.FilterType
import com.bearinmind.equalizer314.model.ParametricFilter
import com.bearinmind.equalizer314.ui.components.FaderSlider
import com.bearinmind.equalizer314.ui.components.FrequencyResponseCurve
import com.bearinmind.equalizer314.ui.theme.*
import com.bearinmind.equalizer314.viewmodel.AudioFxUiState
import com.bearinmind.equalizer314.viewmodel.AudioFxViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    state: AudioFxUiState,
    viewModel: AudioFxViewModel
) {
    val curvePoints = remember(state.eqMode, state.preampDb, state.graphic10Gains, state.graphic20Gains, state.graphic31Gains, state.parametricFilters, state.bassExciter.tonalTiltDb) {
        when (state.eqMode) {
            EqMode.GRAPHIC_10 -> {
                BiquadMath.evaluateGraphicCurve(
                    frequencies = AudioDspEngine.ISO_10_FREQUENCIES,
                    gainsDb = state.graphic10Gains,
                    preampDb = state.preampDb,
                    tonalTiltDb = state.bassExciter.tonalTiltDb,
                    qFactor = 1.414
                )
            }
            EqMode.GRAPHIC_20 -> {
                BiquadMath.evaluateGraphicCurve(
                    frequencies = AudioDspEngine.ISO_20_FREQUENCIES,
                    gainsDb = state.graphic20Gains,
                    preampDb = state.preampDb,
                    tonalTiltDb = state.bassExciter.tonalTiltDb,
                    qFactor = 2.87
                )
            }
            EqMode.GRAPHIC_31 -> {
                BiquadMath.evaluateGraphicCurve(
                    frequencies = AudioDspEngine.ISO_31_FREQUENCIES,
                    gainsDb = state.graphic31Gains,
                    preampDb = state.preampDb,
                    tonalTiltDb = state.bassExciter.tonalTiltDb,
                    qFactor = 4.3
                )
            }
            EqMode.PARAMETRIC -> {
                BiquadMath.evaluateParametricCurve(
                    filters = state.parametricFilters,
                    preampDb = state.preampDb,
                    tonalTiltDb = state.bassExciter.tonalTiltDb
                )
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Mode Selector: 10-Band, 20-Band, 31-Band, Parametric
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.weight(1f)
                ) {
                    SegmentedButton(
                        selected = state.eqMode == EqMode.GRAPHIC_10,
                        onClick = { viewModel.setEqMode(EqMode.GRAPHIC_10) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4),
                        modifier = Modifier.testTag("tab_10_band")
                    ) {
                        Text("10-B", fontSize = 10.sp, maxLines = 1)
                    }
                    SegmentedButton(
                        selected = state.eqMode == EqMode.GRAPHIC_20,
                        onClick = { viewModel.setEqMode(EqMode.GRAPHIC_20) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4),
                        modifier = Modifier.testTag("tab_20_band")
                    ) {
                        Text("20-B", fontSize = 10.sp, maxLines = 1)
                    }
                    SegmentedButton(
                        selected = state.eqMode == EqMode.GRAPHIC_31,
                        onClick = { viewModel.setEqMode(EqMode.GRAPHIC_31) },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 4),
                        modifier = Modifier.testTag("tab_31_band")
                    ) {
                        Text("31-ISO", fontSize = 10.sp, maxLines = 1)
                    }
                    SegmentedButton(
                        selected = state.eqMode == EqMode.PARAMETRIC,
                        onClick = { viewModel.setEqMode(EqMode.PARAMETRIC) },
                        shape = SegmentedButtonDefaults.itemShape(index = 3, count = 4),
                        modifier = Modifier.testTag("tab_parametric")
                    ) {
                        Text("Param", fontSize = 10.sp, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { viewModel.resetEq() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioSurface)
                        .border(1.dp, StudioCardBorder, RoundedCornerShape(8.dp))
                        .testTag("reset_eq_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset EQ curve",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Real-time Frequency Response Canvas
        item {
            FrequencyResponseCurve(
                points = curvePoints,
                parametricNodes = if (state.eqMode == EqMode.PARAMETRIC) state.parametricFilters else emptyList()
            )
        }

        // Mode specific sliders
        when (state.eqMode) {
            EqMode.GRAPHIC_10 -> {
                item {
                    Text(
                        text = "10-Band Graphic Equalizer (1-Octave Standard)",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StudioSurface)
                            .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AudioDspEngine.ISO_10_FREQUENCIES.forEachIndexed { index, freq ->
                                FaderSlider(
                                    label = AudioDspEngine.formatFrequency(freq),
                                    valueDb = state.graphic10Gains.getOrElse(index) { 0f },
                                    testTagId = "slider_10_band_$index",
                                    onValueChange = { newDb ->
                                        viewModel.updateGraphic10Band(index, newDb)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            EqMode.GRAPHIC_20 -> {
                item {
                    Text(
                        text = "20-Band Graphic Equalizer (1/2-Octave ISO)",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StudioSurface)
                            .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AudioDspEngine.ISO_20_FREQUENCIES.forEachIndexed { index, freq ->
                                FaderSlider(
                                    label = AudioDspEngine.formatFrequency(freq),
                                    valueDb = state.graphic20Gains.getOrElse(index) { 0f },
                                    testTagId = "slider_20_band_$index",
                                    onValueChange = { newDb ->
                                        viewModel.updateGraphic20Band(index, newDb)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            EqMode.GRAPHIC_31 -> {
                item {
                    Text(
                        text = "31-Band ISO 1/3-Octave Standard Equalizer",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StudioSurface)
                            .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            AudioDspEngine.ISO_31_FREQUENCIES.forEachIndexed { index, freq ->
                                FaderSlider(
                                    label = AudioDspEngine.formatFrequency(freq),
                                    valueDb = state.graphic31Gains.getOrElse(index) { 0f },
                                    testTagId = "slider_31_band_$index",
                                    onValueChange = { newDb ->
                                        viewModel.updateGraphic31Band(index, newDb)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            EqMode.PARAMETRIC -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Parametric Filters (${state.parametricFilters.size}/16)",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Customizable Frequency, Gain dB and Q Resonance factor",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Button(
                            onClick = { viewModel.addParametricFilter() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = StudioBackground),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("add_filter_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Filter", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Filter", fontSize = 12.sp)
                        }
                    }
                }

                items(state.parametricFilters, key = { it.id }) { filter ->
                    ParametricFilterCard(
                        filter = filter,
                        onUpdate = { viewModel.updateParametricFilter(it) },
                        onDelete = { viewModel.removeParametricFilter(filter.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParametricFilterCard(
    filter: ParametricFilter,
    onUpdate: (ParametricFilter) -> Unit,
    onDelete: () -> Unit
) {
    var typeDropdownOpen by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("parametric_card_${filter.id}"),
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StudioCardBorder, StudioCardBorder)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${filter.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricPurple
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box {
                        FilterChip(
                            selected = true,
                            onClick = { typeDropdownOpen = true },
                            label = { Text(filter.type.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioSurfaceVariant,
                                selectedLabelColor = NeonCyan
                            )
                        )
                        DropdownMenu(
                            expanded = typeDropdownOpen,
                            onDismissRequest = { typeDropdownOpen = false }
                        ) {
                            FilterType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.displayName) },
                                    onClick = {
                                        onUpdate(filter.copy(type = type))
                                        typeDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = filter.enabled,
                        onCheckedChange = { onUpdate(filter.copy(enabled = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = NeonCyanDim
                        ),
                        modifier = Modifier.height(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete filter",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Frequency", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text("${filter.frequency.toInt()} Hz", style = MaterialTheme.typography.bodyMedium, color = NeonCyan)
                }
                Slider(
                    value = filter.frequency,
                    onValueChange = { onUpdate(filter.copy(frequency = (it / 10).roundToInt() * 10f)) },
                    valueRange = 20f..20000f,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                )
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Gain", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text("${"%.1f".format(filter.gainDb)} dB", style = MaterialTheme.typography.bodyMedium, color = AmberGold)
                }
                Slider(
                    value = filter.gainDb,
                    onValueChange = { onUpdate(filter.copy(gainDb = (it * 10).roundToInt() / 10f)) },
                    valueRange = -15f..15f,
                    colors = SliderDefaults.colors(thumbColor = AmberGold, activeTrackColor = AmberGold)
                )
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Q Factor (Bandwidth)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text("${"%.2f".format(filter.qFactor)}", style = MaterialTheme.typography.bodyMedium, color = ElectricPurple)
                }
                Slider(
                    value = filter.qFactor,
                    onValueChange = { onUpdate(filter.copy(qFactor = (it * 100).roundToInt() / 100f)) },
                    valueRange = 0.2f..10.0f,
                    colors = SliderDefaults.colors(thumbColor = ElectricPurple, activeTrackColor = ElectricPurple)
                )
            }
        }
    }
}
