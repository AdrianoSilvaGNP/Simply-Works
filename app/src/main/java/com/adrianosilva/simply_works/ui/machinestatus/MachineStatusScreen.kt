package com.adrianosilva.simply_works.ui.machinestatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adrianosilva.simply_works.domain.models.MachineState
import com.adrianosilva.simply_works.domain.models.WashProgramPhase
import com.adrianosilva.simply_works.domain.models.WashingMachineStatus
import com.adrianosilva.simply_works.ui.theme.SimplyworksTheme

@Composable
fun MachineStatusScreenRoot(
    viewModel: MachineStatusViewModel,
    onGoToWashProgram: () -> Unit
) {
    MachineStatusScreen(
        state = viewModel.state,
        onAction = {
            if (it is MachineStatusAction.GoToWashProgram) {
                onGoToWashProgram()
            } else {
                viewModel.onAction(it)
            }
        }
    )
}

@Composable
fun MachineStatusScreen(state: MachineStatusUiState, onAction: (MachineStatusAction) -> Unit) {

    LaunchedEffect(Unit) {
        onAction(MachineStatusAction.Refresh)
    }

    if (state.isLoading) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.errorMessage != null) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Error: ${state.errorMessage}")
            Spacer(Modifier.height(12.dp))
            Button(onClick = { onAction(MachineStatusAction.Refresh) }) {
                Text("Retry")
            }
            Button(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { onAction(MachineStatusAction.ResetWashCycle) }) {
                Text("Reset Wash Cycle")
            }
        }
        return
    }

    val status = state.status ?: return
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Washing Machine Status", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(20.dp))

        Text("State: ${status.machineState}")
        Text("Program: ${status.program}")
        Text("Program State: ${status.programState}")
        Text("Temperature: ${status.temp} ºC")
        Text("Spin Speed: ${status.spinSpeed} rpm")
        Text("Remaining Time: ${status.remainingMinutes} min")
        status.delayMinutes?.let {
            Text("Delay Start: $it min")
        }

        Spacer(Modifier.height(24.dp))

        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = { onAction(MachineStatusAction.Refresh) }) {
            Text("Refresh")
        }

        /*Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = { onAction(MachineStatusAction.CallTestCycleIn30Min) }) {
            Text("Eco 30ºC 800 RPM in 30 min")
        }*/

        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = { onAction(MachineStatusAction.GoToWashProgram) }) {
            Text("Request Wash")
        }

        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = { onAction(MachineStatusAction.ResetWashCycle) }) {
            Text("Reset Wash Cycle")
        }

        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = { onAction(MachineStatusAction.GetUsageStats) }) {
            Text("Get Usage Stats")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MachineStatusScreenPreview() {
    SimplyworksTheme {
        MachineStatusScreen(
            state = MachineStatusUiState(
                isLoading = false,
                errorMessage = null,
                status = WashingMachineStatus(
                    machineState = MachineState.RUNNING,
                    programState = WashProgramPhase.WASH,
                    program = 3,
                    temp = 40,
                    spinSpeed = 800,
                    remainingMinutes = 55
                )
            ),
            onAction = {}
        )
    }
}