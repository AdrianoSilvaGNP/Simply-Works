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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adrianosilva.simply_works.domain.models.WashProgram
import com.adrianosilva.simply_works.ui.components.GlassButton
import com.adrianosilva.simply_works.ui.components.WaveBackground
import com.adrianosilva.simply_works.ui.theme.CyanAccent
import com.adrianosilva.simply_works.ui.theme.GlassBorder
import com.adrianosilva.simply_works.ui.theme.GlassSurface
import com.adrianosilva.simply_works.ui.theme.SimplyworksTheme
import com.adrianosilva.simply_works.ui.theme.SoftWhite

@Composable
fun WashProgramScreenRoot(
    viewModel: WashProgramViewModel,
    onWashStarted: () -> Unit
) {
    LaunchedEffect(viewModel.event) {
        viewModel.event.collect { effect ->
            when (effect) {
                is WashProgramEvent.NavigateBack -> onWashStarted()
            }
        }
    }
    WashProgramScreen(state = viewModel.state, onAction = viewModel::onAction)
}

@Composable
private fun WashProgramScreen(state: WashProgramUiState, onAction: (WashProgramAction) -> Unit) {
    WaveBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Configure Wash",
                style = MaterialTheme.typography.headlineMedium.copy(
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(
                            color = Color.Red.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.Red.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
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

            Box(
                modifier = Modifier
                    .background(GlassSurface, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    ProgramSelector(
                        programs = state.washPrograms,
                        selectedProgram = state.selectedProgram,
                        onProgramSelected = { onAction(WashProgramAction.SelectProgram(it)) }
                    )

                    SelectionRow(
                        label = "Temperature",
                        options = state.temperatures,
                        selectedOption = state.selectedTemperature,
                        onOptionSelected = {
                            onAction(WashProgramAction.SelectTemperature(it))
                        },
                        optionToString = { if (it == 0) "Cold" else "$it°" }
                    )

                    SelectionRow(
                        label = "Spin Speed",
                        options = state.spinSpeeds,
                        // Convert selected code (e.g. 8) to UI value (800)
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

                    SelectionRow(
                        label = "Delay Start (min)",
                        options = (0..12).map { it * 30 },
                        selectedOption = state.delayValue * 30,
                        onOptionSelected = {
                            onAction(
                                WashProgramAction.SetDelayValue(if (it == 0) 0 else it / 30)
                            )
                        },
                        optionToString = { if (it == 0) "Now" else it.toString() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Submit Button
            GlassButton(
                text = if (state.isSendingRequest) "SENDING..." else "START WASH",
                onClick = { onAction(WashProgramAction.SendWashProgram) },
                modifier = Modifier.fillMaxWidth(),
                icon = {
                    if (state.isSendingRequest) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = CyanAccent,
                            strokeWidth = 2.dp
                        )
                    }
                }
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
    Surface(
        onClick = onClick,
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
                style = MaterialTheme.typography.bodyMedium.copy(
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
private fun ProgramSelector(
    programs: List<WashProgram>,
    selectedProgram: WashProgram,
    onProgramSelected: (WashProgram) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Program",
            style = MaterialTheme.typography.labelMedium,
            color = SoftWhite.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedProgram.name,
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
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
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
                        text = {
                            Text(
                                text = option.name,
                                color = SoftWhite
                            )
                        },
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
    SimplyworksTheme {
        WashProgramScreen(
            state = WashProgramUiState(),
            onAction = {}
        )
    }
}
