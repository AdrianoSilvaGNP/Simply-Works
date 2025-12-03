package com.adrianosilva.simply_works.ui.washprogram

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adrianosilva.simply_works.ui.theme.SimplyworksTheme

@Composable
fun WashProgramScreenRoot(
    viewModel: WashProgramViewModel
) {
    WashProgramScreen(
        state = viewModel.state,
        onAction = viewModel::onAction
    )
}

@Composable
private fun WashProgramScreen(
    state: WashProgramUiState,
    onAction: (WashProgramAction) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Program Selector
        Selector(
            label = "Program",
            options = state.washPrograms,
            selectedOption = state.selectedProgram,
            onOptionSelected = { onAction(WashProgramAction.SelectProgram(it)) },
            optionToString = { it.name }
        )

        // Temperature Selector
        Selector(
            label = "Temperature (°C)",
            options = state.temperatures,
            selectedOption = state.selectedTemperature,
            onOptionSelected = { onAction(WashProgramAction.SelectTemperature(it)) },
            optionToString = { if (it == 0) "Cold" else "$it°" }
        )

        // Spin Speed Selector
        Selector(
            label = "Spin Speed (RPM)",
            options = state.spinSpeeds,
            selectedOption = state.selectedSpinSpeed,
            onOptionSelected = { onAction(WashProgramAction.SelectSpinSpeed(if (it == 0) 0 else it / 100)) },
            optionToString = { if (it == 0) "No Spin" else it.toString() }
        )

        // Delay Selector
        Selector(
            label = "Delay Start (Minutes)",
            options = (0..12).map { it * 30 }, // 0 to 360 minutes in 30 min increments
            selectedOption = state.delayValue,
            onOptionSelected = { onAction(WashProgramAction.SetDelayValue(if (it == 0) 0 else it / 30)) },
            optionToString = { it.toString() }
        )

        //Spacer(modifier = Modifier.weight(1f))

        // Submit Button
        Button(
            onClick = { onAction(WashProgramAction.SendWashProgram) },
            enabled = !state.isSendingRequest,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSendingRequest) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Send Program to Machine")
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> Selector(
    label: String,
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    optionToString: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = optionToString(selectedOption),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionToString(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
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