package com.adrianosilva.simply_works.ui.machinestatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adrianosilva.simply_works.domain.models.MachineState
import com.adrianosilva.simply_works.domain.models.WashProgram
import com.adrianosilva.simply_works.domain.models.WashProgramPhase
import com.adrianosilva.simply_works.domain.models.WashingMachineStatus
import com.adrianosilva.simply_works.ui.components.GlassButton
import com.adrianosilva.simply_works.ui.components.StatCard
import com.adrianosilva.simply_works.ui.components.StatusCircularIndicator
import com.adrianosilva.simply_works.ui.components.TextPill
import com.adrianosilva.simply_works.ui.components.WaveBackground
import com.adrianosilva.simply_works.ui.theme.CyanAccent
import com.adrianosilva.simply_works.ui.theme.SimplyworksTheme
import com.adrianosilva.simply_works.ui.theme.SoftWhite

@Composable
fun MachineStatusScreenRoot(
    viewModel: MachineStatusViewModel,
    onGoToWashProgram: () -> Unit,
    onGoToUsageStats: () -> Unit
) {
    MachineStatusScreen(
        state = viewModel.state,
        onAction = {
            when (it) {
                is MachineStatusAction.GoToUsageStats -> onGoToUsageStats()
                is MachineStatusAction.GoToWashProgram -> onGoToWashProgram()
                else -> viewModel.onAction(it)
            }
        }
    )
}

@Composable
fun MachineStatusScreen(state: MachineStatusUiState, onAction: (MachineStatusAction) -> Unit) {
    // Refresh data when screen is created
    LaunchedEffect(Unit) { onAction(MachineStatusAction.Refresh) }

    var showResetDialog by remember { mutableStateOf(false) }
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(text = "Reset Cycle", color = SoftWhite) },
            text = {
                Text(
                    text =
                        "Are you sure you want to stop and reset the current wash cycle?",
                    color = SoftWhite.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAction(MachineStatusAction.ResetWashCycle)
                        showResetDialog = false
                    }
                ) { Text("Reset", color = CyanAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = SoftWhite.copy(alpha = 0.6f))
                }
            }
        )
    }

    WaveBackground {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CyanAccent)
            }
            return@WaveBackground
        }

        if (state.errorMessage != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Connection Error",
                    style = MaterialTheme.typography.headlineMedium.copy(color = Color.Red)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(color = SoftWhite)
                )
                Spacer(Modifier.height(24.dp))
                GlassButton(
                    text = "Retry",
                    onClick = { onAction(MachineStatusAction.Refresh) }
                )
            }
            return@WaveBackground
        }

        val status = state.status ?: return@WaveBackground

        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Simply-Works",
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        color = SoftWhite,
                        shadow = Shadow(
                            color = CyanAccent,
                            blurRadius = 20f
                        )
                    ),
                modifier = Modifier.padding(top = 16.dp)
            )

            val progress = calculateProgress(status)
            val isStandby = status.machineState == MachineState.IDLE
            val isDelayed = status.machineState == MachineState.DELAYED_START_PROGRAMMED ||
                    status.machineState == MachineState.DELAYED_START_SELECTION

            val mainText = when {
                isStandby -> "--"
                isDelayed -> getTimeUntilStart(status)
                else -> status.remainingTime.replace(" min", "m")
            }

            val subText = when {
                isStandby -> "Ready"
                isDelayed -> "Starts in"
                else -> status.program.name
            }

            StatusCircularIndicator(
                progress = progress,
                mainText = mainText,
                subText = subText
            )

            if (!isStandby) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextPill(text = status.machineState.toString())
                    Spacer(Modifier.width(8.dp))
                    TextPill(text = status.programState.toString())
                }
                Spacer(Modifier.height(16.dp))
            } else {
                Spacer(Modifier.height(32.dp))
            }

            // Stats Row
            if (!isStandby) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard(
                        label = "Temp",
                        value = "${status.temp}°"
                    )
                    StatCard(
                        label = "Spin",
                        value = "${status.spinSpeed}"
                    )
                    status.delayMinutes?.let {
                        if (it > 0) StatCard(
                            label = "Delay",
                            value = "${it}m"
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(90.dp))
            }

            // Actions
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassButton(
                    text = "Request Wash",
                    onClick = { onAction(MachineStatusAction.GoToWashProgram) },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassButton(
                        text = "Stats",
                        onClick = { onAction(MachineStatusAction.GoToUsageStats) },
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "Refresh",
                        onClick = { onAction(MachineStatusAction.Refresh) },
                        modifier = Modifier.weight(1f)
                    )
                }

                GlassButton(
                    text = "Reset Cycle",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showResetDialog = true },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun getTimeUntilStart(status: WashingMachineStatus): String {
    val minutes = status.delayMinutes ?: return "--"
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0)
        "${h}h ${m}m"
    else
        "${m}m"
}

private fun calculateProgress(status: WashingMachineStatus): Float {
    if (status.machineState == MachineState.IDLE ||
        status.machineState == MachineState.DELAYED_START_PROGRAMMED ||
        status.machineState == MachineState.DELAYED_START_SELECTION
    ) return 0f

    val remainingString = status.remainingTime.replace(" min", "").trim()
    val remaining = remainingString.toIntOrNull() ?: 0
    val duration = 60f // TODO: Get actual program duration

    val p = 1f - (remaining / duration)
    return p.coerceIn(0.05f, 1f) // Always show a little bit if running
}

@Preview(showBackground = true)
@Composable
private fun MachineStatusScreenPreview() {
    SimplyworksTheme {
        MachineStatusScreen(
            state =
                MachineStatusUiState(
                    isLoading = false,
                    errorMessage = null,
                    status =
                        WashingMachineStatus(
                            machineState = MachineState.RUNNING,
                            programState = WashProgramPhase.WASH,
                            program = WashProgram.Eco4060,
                            temp = 40,
                            spinSpeed = 800,
                            remainingTime = "55 min"
                        )
                ),
            onAction = {}
        )
    }
}