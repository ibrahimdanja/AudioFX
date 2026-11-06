package com.bearinmind.equalizer314.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Search
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
import com.bearinmind.equalizer314.dsp.BiquadMath
import com.bearinmind.equalizer314.model.AutoEqEntry
import com.bearinmind.equalizer314.ui.components.FrequencyResponseCurve
import com.bearinmind.equalizer314.ui.theme.*
import com.bearinmind.equalizer314.viewmodel.AudioFxUiState
import com.bearinmind.equalizer314.viewmodel.AudioFxViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoEqScreen(
    state: AudioFxUiState,
    viewModel: AudioFxViewModel
) {
    var selectedEntry by remember { mutableStateOf<AutoEqEntry?>(null) }

    val activeProfile = state.activeAutoEqProfile
    val curvePoints = remember(activeProfile) {
        if (activeProfile != null) {
            BiquadMath.evaluateParametricCurve(
                filters = activeProfile.filters,
                preampDb = activeProfile.preampDb
            )
        } else emptyList()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = "AutoEQ Headphone Target Correction",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = "Harmonize headphone frequency response towards the Harman target using thousands of calibrated lab measurements.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        // Active Profile Card & Target Curve Preview
        if (activeProfile != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_autoeq_card"),
                    colors = CardDefaults.cardColors(containerColor = StudioSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(NeonCyanDim, ElectricPurpleDim)))
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeProfile.headphoneName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = NeonCyan,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Lab measurement: ${activeProfile.source} • Preamp: ${"%.1f".format(activeProfile.preampDb)} dB",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            Button(
                                onClick = { viewModel.applyAutoEqToParametric() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = StudioBackground),
                                modifier = Modifier.testTag("apply_autoeq_button")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Apply to EQ", fontSize = 12.sp)
                            }
                        }

                        // Compensation Curve Graph
                        FrequencyResponseCurve(
                            points = curvePoints,
                            parametricNodes = activeProfile.filters
                        )

                        // Filter summary chips
                        Text(
                            text = "${activeProfile.filters.size} Parametric Correction Bands loaded",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberGold
                        )
                    }
                }
            }
        }

        // Search Bar & Filter Chips
        item {
            OutlinedTextField(
                value = state.autoEqSearchQuery,
                onValueChange = { viewModel.onAutoEqQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("autoeq_search_input"),
                placeholder = { Text("Search 10,000+ headphones (e.g. Sony, Sennheiser, AirPods, Moondrop)") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = NeonCyan)
                },
                trailingIcon = {
                    if (state.autoEqSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onAutoEqQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = StudioCardBorder,
                    focusedContainerColor = StudioSurface,
                    unfocusedContainerColor = StudioSurface
                ),
                singleLine = true
            )
        }

        // Type filter chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val types = listOf(
                    Pair(null, "All Types"),
                    Pair("in-ear", "In-Ear / IEM"),
                    Pair("over-ear", "Over-Ear"),
                    Pair("earbud", "Earbud")
                )

                types.forEach { (typeKey, label) ->
                    val isSelected = state.autoEqTypeFilter == typeKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setAutoEqTypeFilter(typeKey) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricPurpleDim,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Results List
        if (state.isAutoEqLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NeonCyan)
                }
            }
        } else {
            items(state.autoEqSearchResults, key = { "${it.name}_${it.source}_${it.path}" }) { entry ->
                AutoEqResultItem(
                    entry = entry,
                    isSelected = entry.name == activeProfile?.headphoneName,
                    onClick = {
                        selectedEntry = entry
                        viewModel.selectAndLoadAutoEqProfile(entry)
                    }
                )
            }
        }
    }
}

@Composable
private fun AutoEqResultItem(
    entry: AutoEqEntry,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("autoeq_item_${entry.name.replace(" ", "_")}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) StudioSurfaceVariant else StudioSurface
        ),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    if (isSelected) NeonCyan else StudioCardBorder,
                    if (isSelected) ElectricPurple else StudioCardBorder
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Headphones,
                    contentDescription = null,
                    tint = if (isSelected) NeonCyan else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "${entry.source} • ${entry.type}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Text(
                text = "Load",
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) NeonCyan else TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}
