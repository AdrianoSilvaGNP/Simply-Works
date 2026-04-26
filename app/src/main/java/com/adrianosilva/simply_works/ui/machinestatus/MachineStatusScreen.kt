package com.adrianosilva.simply_works.ui.machinestatus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adrianosilva.simply_works.data.dto.StatusCounters
import com.adrianosilva.simply_works.domain.models.MachineState
import com.adrianosilva.simply_works.domain.models.WashProgram
import com.adrianosilva.simply_works.domain.models.WashProgramPhase
import com.adrianosilva.simply_works.domain.models.WashingMachineStatus
import com.adrianosilva.simply_works.ui.components.GlassButton
import com.adrianosilva.simply_works.ui.components.StatCard
import com.adrianosilva.simply_works.ui.components.StatusCircularIndicator
import com.adrianosilva.simply_works.ui.components.WaveBackground
import com.adrianosilva.simply_works.ui.theme.CyanAccent
import com.adrianosilva.simply_works.ui.theme.GlassBorder
import com.adrianosilva.simply_works.ui.theme.GlassSurface
import com.adrianosilva.simply_works.ui.theme.SimplyworksTheme
import com.adrianosilva.simply_works.ui.theme.SoftWhite

@Composable
fun MachineStatusScreenRoot(
    viewModel: MachineStatusViewModel,
    onGoToWashProgram: () -> Unit,
    onGoToUsageStats: () -> Unit,
    onGoToSettings: () -> Unit
) {
    MachineStatusScreen(
        state = viewModel.state,
        onAction = {
            when (it) {
                is MachineStatusAction.GoToUsageStats -> onGoToUsageStats()
                is MachineStatusAction.GoToWashProgram -> onGoToWashProgram()
                is MachineStatusAction.GoToSettings -> onGoToSettings()
                else -> viewModel.onAction(it)
            }
        }
    )
}

@Composable
fun MachineStatusScreen(state: MachineStatusUiState, onAction: (MachineStatusAction) -> Unit) {

    // Refresh data when screen is created OR when we return from WashProgram screen
    LaunchedEffect(Unit) { 
        // Small delay to ensure any remote request from previous screen had time to be processed by the machine
        kotlinx.coroutines.delay(1000)
        onAction(MachineStatusAction.Refresh) 
    }

    var showResetDialog by remember { mutableStateOf(false) }
    var showUsageStatsDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(text = "Reset Cycle", color = SoftWhite) },
            text = {
                Text(
                    text = "Are you sure you want to stop and reset the current wash cycle?",
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

    if (showUsageStatsDialog) {
        UsageStatsDialog(
            stats = state.usageStats,
            onDismiss = { showUsageStatsDialog = false },
            onViewFullStats = { onAction(MachineStatusAction.GoToUsageStats) }
        )
    }

    WaveBackground {
        if (state.isLoading && state.status == null && state.usageStats == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CyanAccent)
            }
            return@WaveBackground
        }

        if (state.errorMessage != null && state.status == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
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
                GlassButton(text = "Retry", onClick = { onAction(MachineStatusAction.Refresh) })
            }
            return@WaveBackground
        }

        val status = state.status ?: return@WaveBackground

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(600)) + slideInVertically(initialOffsetY = { 50 }),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(48.dp)) // Balance the icon
                
                // Enhanced App Name
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SIMPLY",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = CyanAccent.copy(alpha = 0.6f),
                            letterSpacing = 8.sp,
                            fontWeight = FontWeight.Light
                        )
                    )
                    Text(
                        text = "WORKS",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = SoftWhite,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = CyanAccent.copy(alpha = 0.5f),
                                blurRadius = 15f
                            )
                        )
                    )
                }

                IconButton(onClick = { onAction(MachineStatusAction.GoToSettings) }) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = SoftWhite.copy(alpha = 0.5f))
                }
            }

            // Main Indicator
            val progress = calculateProgress(status)
            val isStandby = status.machineState == MachineState.IDLE
            val isDelayed =
                status.machineState == MachineState.DELAYED_START_PROGRAMMED ||
                    status.machineState == MachineState.DELAYED_START_SELECTION

            val mainText =
                when {
                    isStandby -> "--"
                    isDelayed -> getTimeUntilStart(status)
                    else -> status.remainingTime.replace(" min", "m")
                }

            val subText =
                when {
                    isStandby -> "Ready"
                    isDelayed -> "Starts in"
                    else -> status.program.name
                }

            StatusCircularIndicator(
                progress = progress, 
                mainText = mainText, 
                subText = subText,
                isLoading = state.isProcessingAction
            )

            // Phase Tracking Bar
            if (!isStandby && !isDelayed && !state.isProcessingAction) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = CyanAccent,
                        trackColor = GlassSurface,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Phase: ${status.programState}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SoftWhite.copy(alpha = 0.8f)
                    )
                }
            }

            // Phase & State Indicators
            if (!isStandby) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Simple Pills for State
                    Surface(
                        color = GlassSurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Text(
                            text = status.machineState.toString(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAccent
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = GlassSurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Text(
                            text = status.programState.toString(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftWhite
                        )
                    }
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
                    StatCard(label = "Temp", value = "${status.temp}°")
                    StatCard(label = "Spin", value = "${status.spinSpeed}")
                    status.delayMinutes?.let {
                        if (it > 0) StatCard(label = "Delay", value = "${it}m")
                    }
                }
            } else {
                Spacer(
                    modifier = Modifier.height(90.dp)
                ) // Placeholder height to keep buttons at bottom
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
                        onClick = { 
                            onAction(MachineStatusAction.FetchUsageStats)
                            showUsageStatsDialog = true 
                        },
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
}

@Composable
private fun UsageStatsDialog(
    stats: StatusCounters?,
    onDismiss: () -> Unit,
    onViewFullStats: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Insights", color = SoftWhite) },
        text = {
            if (stats == null) {
                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyanAccent)
                }
            } else {
                val totalCycles = (stats.temp0to30.toIntOrNull() ?: 0) +
                        (stats.temp40.toIntOrNull() ?: 0) +
                        (stats.temp60to90.toIntOrNull() ?: 0)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    UsageRow(label = "Total Cycles", value = totalCycles.toString())
                    UsageRow(label = "Temp 0-30°C", value = stats.temp0to30)
                    UsageRow(label = "Temp 40°C", value = stats.temp40)
                    UsageRow(label = "Temp 60-90°C", value = stats.temp60to90)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onViewFullStats()
            }) { Text("View Detailed Stats", color = CyanAccent) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = SoftWhite.copy(alpha = 0.6f)) }
        }
    )
}

@Composable
private fun UsageRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = SoftWhite.copy(alpha = 0.7f))
        Text(text = value, color = CyanAccent, fontWeight = FontWeight.Bold)
    }
}

private fun getTimeUntilStart(status: WashingMachineStatus): String {
    // delayMinutes is in minutes.
    val minutes = status.delayMinutes ?: return "--"
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

private fun calculateProgress(status: WashingMachineStatus): Float {
    if (status.machineState == MachineState.IDLE) return 0f

    // If delayed, we could show full circle or maybe a different indication?
    // Let's keep it 0 or 1 for delayed to distinct it from running
    if (status.machineState == MachineState.DELAYED_START_PROGRAMMED ||
                    status.machineState == MachineState.DELAYED_START_SELECTION
    ) {
        return 1f // Full circle to indicate "active/waiting"
    }

    // Attempt to parse remaining time
    val remainingString = status.remainingTime.replace(" min", "").trim()
    val remaining = remainingString.toIntOrNull() ?: 0

    // Heuristic: If we don't have total duration, we can't show a perfect "progress bar".
    // However, the user asked to "associate progress to remaining time".
    val maxDuration = 90f // Standard generous wash time

    val p = 1f - (remaining / maxDuration)
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
                                                remainingTime = "55 min",
                                                remainingTimeMinutes = 55
                                        )
                        ),
                onAction = {}
        )
    }
}
