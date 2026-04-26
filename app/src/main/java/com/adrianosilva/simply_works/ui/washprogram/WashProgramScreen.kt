package com.adrianosilva.simply_works.ui.washprogram

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adrianosilva.simply_works.ui.components.GlassButton
import com.adrianosilva.simply_works.ui.components.SwipeToConfirm
import com.adrianosilva.simply_works.ui.components.WaveBackground
import com.adrianosilva.simply_works.ui.theme.CyanAccent
import com.adrianosilva.simply_works.ui.theme.GlassBorder
import com.adrianosilva.simply_works.ui.theme.GlassSurface
import com.adrianosilva.simply_works.ui.theme.SimplyworksTheme
import com.adrianosilva.simply_works.ui.theme.SoftWhite
import kotlin.math.roundToInt

@Composable
fun WashProgramScreenRoot(viewModel: WashProgramViewModel, onWashStarted: () -> Unit) {
    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WashProgramEffect.NavigateBack -> onWashStarted()
            }
        }
    }
    WashProgramScreen(state = viewModel.state, onAction = viewModel::onAction)
}

@Composable
private fun WashProgramScreen(state: WashProgramUiState, onAction: (WashProgramAction) -> Unit) {
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var presetName by remember { mutableStateOf("") }
    var showDelayDialog by remember { mutableStateOf(false) }
    var delayInHours by remember { mutableFloatStateOf(1f) }

    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text("Save Preset", color = SoftWhite) },
            text = {
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    label = { Text("Preset Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SoftWhite,
                        unfocusedTextColor = SoftWhite,
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = SoftWhite.copy(alpha = 0.5f)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (presetName.isNotBlank()) {
                        onAction(WashProgramAction.SavePreset(presetName))
                        showSavePresetDialog = false
                        presetName = ""
                    }
                }) { Text("Save", color = CyanAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) { Text("Cancel", color = SoftWhite) }
            }
        )
    }

    if (showDelayDialog) {
        AlertDialog(
            onDismissRequest = { showDelayDialog = false },
            title = { Text("Schedule Start", color = SoftWhite) },
            text = {
                val hours = delayInHours.toInt()
                val minutes = if (delayInHours % 1f > 0) 30 else 0
                val displayTime = if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h"

                Column {
                    Text("Start In: $displayTime", color = SoftWhite)
                    androidx.compose.material3.Slider(
                        value = delayInHours,
                        onValueChange = { delayInHours = it },
                        valueRange = 0.5f..24f,
                        steps = 46 // 0.5 hour steps from 0.5 to 24 (47 points total)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "The wash will begin in $displayTime.",
                        color = SoftWhite.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Convert hours to delayValue (each value represents 30 mins)
                    onAction(WashProgramAction.SetDelayValue((delayInHours * 2).roundToInt()))
                    showDelayDialog = false
                }) { Text("Apply Delay", color = CyanAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showDelayDialog = false }) { Text("Cancel", color = SoftWhite) }
            }
        )
    }

    WaveBackground {
        Column(
                modifier =
                        Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                    text = "Configure Wash",
                    style =
                            MaterialTheme.typography.headlineMedium.copy(
                                    color = SoftWhite,
                                    fontWeight = FontWeight.Bold
                            ),
                    modifier = Modifier.padding(bottom = 24.dp)
            )

            AnimatedVisibility(
                    visible = state.errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
            ) {
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .background(
                                                Color.Red.copy(alpha = 0.2f),
                                                RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                                1.dp,
                                                Color.Red.copy(alpha = 0.5f),
                                                RoundedCornerShape(12.dp)
                                        )
                                        .padding(16.dp)
                ) {
                    Text(
                            text = state.errorMessage ?: "",
                            color = SoftWhite,
                            style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (state.savedPresets.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    items(state.savedPresets) { preset ->
                        GlassChip(
                            text = preset.name,
                            isSelected = false,
                            onClick = { onAction(WashProgramAction.SelectPreset(preset)) }
                        )
                    }
                }
            }

            Box(
                    modifier =
                            Modifier.background(GlassSurface, RoundedCornerShape(24.dp))
                                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {

                    // Program Selector
                    ProgramSelector(
                            programs = state.washPrograms,
                            selectedProgram = state.selectedProgram,
                            onProgramSelected = { onAction(WashProgramAction.SelectProgram(it)) }
                    )

                    // Temperature Selector
                    SelectionRow(
                            label = "Temperature",
                            options = state.temperatures,
                            selectedOption = state.selectedTemperature,
                            onOptionSelected = {
                                onAction(WashProgramAction.SelectTemperature(it))
                            },
                            optionToString = { if (it == 0) "Cold" else "$it°" }
                    )

                    // Spin Speed Selector
                    SelectionRow(
                            label = "Spin Speed",
                            options = state.spinSpeeds,
                            // Convert selected code (e.g. 8) to raw value (800)
                            // for UI comparison
                            selectedOption =
                                    if (state.selectedSpinSpeed == 0) 0
                                    else state.selectedSpinSpeed * 100,
                            onOptionSelected = {
                                onAction(
                                        WashProgramAction.SelectSpinSpeed(
                                                if (it == 0) 0 else it / 100
                                        )
                                )
                            },
                            optionToString = { if (it == 0) "No Spin" else it.toString() }
                    )

                    // Delay Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Delay Start",
                                style = MaterialTheme.typography.labelMedium,
                                color = SoftWhite.copy(alpha = 0.7f)
                            )
                            Text(
                                text = if (state.delayValue == 0) "Now" else "${state.delayValue * 30} mins",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        GlassButton(
                            text = "Schedule...",
                            onClick = { showDelayDialog = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlassButton(
                text = "Save as Preset",
                onClick = { showSavePresetDialog = true },
                modifier = Modifier.fillMaxWidth(0.5f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Submit Button
            SwipeToConfirm(
                text = "SWIPE TO START",
                onConfirm = { onAction(WashProgramAction.SendWashProgram) },
                isLoading = state.isSendingRequest || state.isProcessing,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun <T> SelectionRow(
        label: String,
        options: List<T>,
        selectedOption: T,
        onOptionSelected: (T) -> Unit,
        optionToString: (T) -> String
) {
    Column {
        Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = SoftWhite.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )
        LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(options) { option ->
                val isSelected = option == selectedOption
                GlassChip(
                        text = optionToString(option),
                        isSelected = isSelected,
                        onClick = { onOptionSelected(option) }
                )
            }
        }
    }
}

@Composable
private fun GlassChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Surface(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
            shape = RoundedCornerShape(16.dp),
            color =
                    if (isSelected) CyanAccent.copy(alpha = 0.2f)
                    else GlassSurface.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, if (isSelected) CyanAccent else GlassBorder),
            modifier = Modifier.height(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                    text = text,
                    style =
                            MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight =
                                            if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                    color = if (isSelected) CyanAccent else SoftWhite
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ProgramSelector(
        programs: List<T>,
        selectedProgram: T,
        onProgramSelected: (T) -> Unit
) {
    val optionToString: (T) -> String = {
        (it as com.adrianosilva.simply_works.domain.models.WashProgram).name
    }

    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
                text = "Program",
                style = MaterialTheme.typography.labelMedium,
                color = SoftWhite.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                    value = optionToString(selectedProgram),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors =
                            OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanAccent,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedTextColor = SoftWhite,
                                    unfocusedTextColor = SoftWhite,
                                    focusedContainerColor = GlassSurface.copy(alpha = 0.1f),
                                    unfocusedContainerColor = GlassSurface.copy(alpha = 0.1f)
                            ),
                    shape = RoundedCornerShape(16.dp),
                    modifier =
                            Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = Color(0xFF1E2A38).copy(alpha = 0.95f),
                    modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(4.dp))
            ) {
                programs.forEach { option ->
                    DropdownMenuItem(
                            text = { Text(optionToString(option), color = SoftWhite) },
                            onClick = {
                                onProgramSelected(option)
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WashProgramScreenPreview() {
    SimplyworksTheme { WashProgramScreen(state = WashProgramUiState(), onAction = {}) }
}
