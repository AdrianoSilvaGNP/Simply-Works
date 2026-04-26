package com.adrianosilva.simply_works.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.adrianosilva.simply_works.ui.components.GlassButton
import com.adrianosilva.simply_works.ui.components.WaveBackground
import com.adrianosilva.simply_works.ui.theme.CyanAccent
import com.adrianosilva.simply_works.ui.theme.SoftWhite

@Composable
fun SetupScreenRoot(
    viewModel: SetupViewModel,
    onSetupComplete: (String) -> Unit
) {
    SetupScreen(
        ipAddress = viewModel.ipAddress,
        isScanning = viewModel.isScanning,
        scanError = viewModel.scanError,
        onIpChanged = viewModel::onIpChanged,
        onScanClicked = { viewModel.scanNetwork(onSetupComplete) },
        onSaveClicked = { viewModel.saveIpAndContinue(viewModel.ipAddress, onSetupComplete) }
    )
}

@Composable
fun SetupScreen(
    ipAddress: String,
    isScanning: Boolean,
    scanError: String?,
    onIpChanged: (String) -> Unit,
    onScanClicked: () -> Unit,
    onSaveClicked: () -> Unit
) {
    WaveBackground {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isScanning) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = CyanAccent,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Scanning local network...",
                        color = SoftWhite,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Welcome to Simply-Works",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SoftWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Let's connect to your washing machine.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftWhite.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = onIpChanged,
                        label = { Text("Washing Machine IP Address") },
                        placeholder = { Text("e.g. 192.168.1.185") },
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

                    if (scanError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = scanError,
                            color = Color(0xFFFF6B6B),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    GlassButton(
                        text = "Save & Continue",
                        onClick = onSaveClicked,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "OR",
                        style = MaterialTheme.typography.labelLarge,
                        color = SoftWhite.copy(alpha = 0.5f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    GlassButton(
                        text = "Auto-Scan Network",
                        onClick = onScanClicked,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
