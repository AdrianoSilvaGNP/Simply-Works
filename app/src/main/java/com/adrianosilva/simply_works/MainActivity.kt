package com.adrianosilva.simply_works

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adrianosilva.simply_works.data.remote.CandyApiService
import com.adrianosilva.simply_works.ui.theme.SimplyworksTheme

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val service = CandyApiService(
            baseUrl = "http://192.168.1.185",
            xorKey = "".toByteArray()
        )
        setContent {
            SimplyworksTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MachineStatusScreen(
                            viewModel(factory = MachineStatusViewModelFactory(service))
                        )
                    }
                }
            }
        }


    }
}

@Composable
fun MachineStatusScreen(viewModel: MachineStatusViewModel) {
    val state = viewModel.uiState

    LaunchedEffect(Unit) {
        viewModel.loadStatus()
    }

    when (state) {
        is MachineStatusUiState.Loading -> {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is MachineStatusUiState.Error -> {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Error: ${state.message}")
                Spacer(Modifier.height(12.dp))
                Button(onClick = { viewModel.loadStatus() }) {
                    Text("Retry")
                }
                Button(modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = { viewModel.resetWashCycle() }) {
                    Text("Reset Wash Cycle")
                }
            }
        }

        is MachineStatusUiState.Success -> {
            val status = state.status
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

                Spacer(Modifier.height(24.dp))

                Button(modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = { viewModel.loadStatus() }) {
                    Text("Refresh")
                }

                Button(modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = { viewModel.callTestCycleIn30Min() }) {
                    Text("Eco 30ºC 800 RPM in 30 min")
                }

                Button(modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = { viewModel.resetWashCycle() }) {
                    Text("Reset Wash Cycle")
                }
            }
        }
    }
}

class MachineStatusViewModelFactory(
    private val api: CandyApiService
): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MachineStatusViewModel::class.java)) {
            return MachineStatusViewModel(api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
