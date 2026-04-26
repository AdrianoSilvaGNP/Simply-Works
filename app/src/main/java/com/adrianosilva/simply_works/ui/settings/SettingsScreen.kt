package com.adrianosilva.simply_works.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.adrianosilva.simply_works.domain.models.WashPreset
import com.adrianosilva.simply_works.ui.components.GlassButton
import com.adrianosilva.simply_works.ui.components.WaveBackground
import com.adrianosilva.simply_works.ui.theme.CyanAccent
import com.adrianosilva.simply_works.ui.theme.GlassSurface
import com.adrianosilva.simply_works.ui.theme.SoftWhite

@Composable
fun SettingsScreenRoot(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onIpUpdated: () -> Unit
) {
    SettingsScreen(
        ipAddress = viewModel.ipAddress,
        isScanning = viewModel.isScanning,
        scanMessage = viewModel.scanMessage,
        savedPresets = viewModel.savedPresets,
        onIpChanged = viewModel::onIpChanged,
        onSaveIp = {
            viewModel.saveIp()
            onIpUpdated()
        },
        onScanNetwork = {
            viewModel.scanNetwork()
            onIpUpdated()
        },
        onDeletePreset = viewModel::deletePreset,
        onBack = onBack
    )
}

@Composable
fun SettingsScreen(
    ipAddress: String,
    isScanning: Boolean,
    scanMessage: String?,
    savedPresets: List<WashPreset>,
    onIpChanged: (String) -> Unit,
    onSaveIp: () -> Unit,
    onScanNetwork: () -> Unit,
    onDeletePreset: (WashPreset) -> Unit,
    onBack: () -> Unit
) {
    WaveBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = SoftWhite)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = SoftWhite,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // IP Settings
            Box(
                modifier = Modifier
                    .background(GlassSurface, RoundedCornerShape(24.dp))
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Washing Machine Connection",
                        style = MaterialTheme.typography.titleMedium,
                        color = CyanAccent,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = onIpChanged,
                        label = { Text("IP Address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SoftWhite,
                            unfocusedTextColor = SoftWhite,
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = SoftWhite.copy(alpha = 0.5f),
                            focusedLabelColor = CyanAccent,
                            unfocusedLabelColor = SoftWhite.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassButton(
                            text = "Save",
                            onClick = onSaveIp,
                            modifier = Modifier.weight(1f)
                        )
                        GlassButton(
                            text = "Re-Scan",
                            onClick = onScanNetwork,
                            modifier = Modifier.weight(1f),
                            icon = {
                                if (isScanning) {
                                    CircularProgressIndicator(
                                        color = CyanAccent,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        )
                    }

                    if (scanMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = scanMessage,
                            color = if (scanMessage.contains("Error") || scanMessage.contains("Could not")) Color(0xFFFF6B6B) else CyanAccent,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Presets
            Text(
                text = "Saved Presets",
                style = MaterialTheme.typography.titleMedium,
                color = CyanAccent,
                modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
            )

            if (savedPresets.isEmpty()) {
                Text(
                    text = "No saved presets yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftWhite.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedPresets) { preset ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GlassSurface.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = preset.name, color = SoftWhite, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Temp: ${preset.temp}° | Spin: ${preset.spinSpeed}",
                                        color = SoftWhite.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = { onDeletePreset(preset) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Preset", tint = Color(0xFFFF6B6B))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
