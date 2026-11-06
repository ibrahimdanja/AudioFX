package com.bearinmind.equalizer314.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bearinmind.equalizer314.model.AudioOutputDevice
import com.bearinmind.equalizer314.model.EqPreset
import com.bearinmind.equalizer314.repository.PresetRepository
import com.bearinmind.equalizer314.ui.theme.*
import com.bearinmind.equalizer314.viewmodel.AudioFxUiState
import com.bearinmind.equalizer314.viewmodel.AudioFxViewModel

@Composable
fun PresetsScreen(
    state: AudioFxUiState,
    viewModel: AudioFxViewModel
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val presetRepo = remember { PresetRepository(context) }

    var showSaveDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }

    var showExportDialog by remember { mutableStateOf(false) }
    var exportText by remember { mutableStateOf("") }

    var showImportDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Audio Output Routing Card
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
                    Text(
                        text = "Audio Output Route & Device Binding",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AudioOutputDevice.values().forEach { device ->
                            val isSelected = state.outputDevice == device
                            OutlinedButton(
                                onClick = { viewModel.setOutputDevice(device) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) NeonCyan.copy(alpha = 0.15f) else StudioSurfaceVariant,
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
                                contentPadding = PaddingValues(2.dp)
                            ) {
                                Text(
                                    text = when (device) {
                                        AudioOutputDevice.PHONE_SPEAKER -> "Speaker"
                                        AudioOutputDevice.WIRED_HEADPHONES -> "Headphones"
                                        AudioOutputDevice.BLUETOOTH -> "Bluetooth"
                                        AudioOutputDevice.USB_DAC -> "USB DAC"
                                    },
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Audition Live Tone Player Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StudioCardBorder, StudioCardBorder)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Audition Test Audio",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = if (state.isTestTonePlaying) "Streaming stereo chord to audition DSP..." else "Test equalizer, reverb and crossfeed live in app",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (state.isTestTonePlaying) NeonGreen else TextSecondary
                        )
                    }

                    Button(
                        onClick = { viewModel.toggleTestAudio() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isTestTonePlaying) NeonRed else NeonGreen,
                            contentColor = StudioBackground
                        ),
                        modifier = Modifier.testTag("toggle_test_tone_button")
                    ) {
                        Icon(
                            imageVector = if (state.isTestTonePlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (state.isTestTonePlaying) "Stop" else "Audition", fontSize = 12.sp)
                    }
                }
            }
        }

        // Preset Actions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Preset Library",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            exportText = viewModel.exportCurrentToApo()
                            showExportDialog = true
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export APO", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            importText = ""
                            showImportDialog = true
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import APO", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            newPresetName = ""
                            showSaveDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = StudioBackground),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("save_preset_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save", fontSize = 11.sp)
                    }
                }
            }
        }

        // Factory Presets List
        item {
            Text(
                text = "Studio Profiles (Factory)",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary
            )
        }

        items(presetRepo.factoryPresets, key = { it.id }) { preset ->
            PresetItemCard(
                preset = preset,
                isSelected = state.currentPresetName == preset.name,
                onApply = { viewModel.applyPreset(preset) },
                onBindToCurrentDevice = {
                    viewModel.bindPresetToDevice(state.outputDevice, preset.id)
                    Toast.makeText(context, "Bound to ${state.outputDevice.displayName}", Toast.LENGTH_SHORT).show()
                },
                onDelete = null
            )
        }

        // Custom User Presets List
        if (state.userPresets.isNotEmpty()) {
            item {
                Text(
                    text = "My Custom Presets",
                    style = MaterialTheme.typography.titleSmall,
                    color = ElectricPurple
                )
            }

            items(state.userPresets, key = { it.id }) { preset ->
                PresetItemCard(
                    preset = preset,
                    isSelected = state.currentPresetName == preset.name,
                    onApply = { viewModel.applyPreset(preset) },
                    onBindToCurrentDevice = {
                        viewModel.bindPresetToDevice(state.outputDevice, preset.id)
                        Toast.makeText(context, "Bound to ${state.outputDevice.displayName}", Toast.LENGTH_SHORT).show()
                    },
                    onDelete = { viewModel.deleteUserPreset(preset.id) }
                )
            }
        }
    }

    // Save Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Current Preset") },
            text = {
                OutlinedTextField(
                    value = newPresetName,
                    onValueChange = { newPresetName = it },
                    placeholder = { Text("e.g. My Hi-Fi Acoustic") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            viewModel.saveCurrentAsUserPreset(newPresetName.trim())
                            showSaveDialog = false
                            Toast.makeText(context, "Preset saved!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export EqualizerAPO") },
            text = {
                Column {
                    Text(
                        text = "Standard EqualizerAPO configuration text:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(exportText))
                        showExportDialog = false
                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import EqualizerAPO") },
            text = {
                Column {
                    Text(
                        text = "Paste your EqualizerAPO or Peace filter text here:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        placeholder = { Text("Preamp: -3.0 dB\nFilter 1: ON PK Fc 1000 Hz Gain 3.0 dB Q 1.41") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importText.isNotBlank()) {
                            viewModel.importFromApo(importText)
                            showImportDialog = false
                            Toast.makeText(context, "Imported filters!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PresetItemCard(
    preset: EqPreset,
    isSelected: Boolean,
    onApply: () -> Unit,
    onBindToCurrentDevice: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onApply)
            .testTag("preset_card_${preset.name.replace(" ", "_")}"),
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
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) NeonCyan else TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StudioCardBorder)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(preset.category, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 8.sp)
                    }
                }
                Text(
                    text = "Preamp: ${"%.1f".format(preset.preampDb)} dB • Bass: ${(preset.bassBoost / 10).toInt()}% • Spatial: ${(preset.virtualizer / 10).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBindToCurrentDevice,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.BookmarkBorder,
                        contentDescription = "Bind preset to device",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Active preset",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (onDelete != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete preset",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
