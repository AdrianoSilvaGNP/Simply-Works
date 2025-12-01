package com.adrianosilva.simply_works

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrianosilva.simply_works.data.remote.CandyApiService
import kotlinx.coroutines.launch

class MachineStatusViewModel(
    private val api: CandyApiService
) : ViewModel() {

    var uiState by mutableStateOf<MachineStatusUiState>(MachineStatusUiState.Loading)
        private set

    fun loadStatus() {
        viewModelScope.launch {
            uiState = try {
                Log.d("MachineStatusViewModel", "Fetching machine status...")
                val status = api.getStatus()
                MachineStatusUiState.Success(status)
            } catch (e: Exception) {
                Log.e("MachineStatusViewModel", "Error fetching machine status", e)
                MachineStatusUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun callTestCycleIn30Min() {
        viewModelScope.launch {
            try {
                api.callTestCycleIn30Min()
                Log.d("MachineStatusViewModel", "Test cycle scheduled successfully.")
            } catch (e: Exception) {
                Log.e("MachineStatusViewModel", "Error scheduling test cycle", e)
            }
        }
    }

    fun resetWashCycle() {
        viewModelScope.launch {
            try {
                api.callResetWashCycle()
                Log.d("MachineStatusViewModel", "Wash cycle reset successfully.")
            } catch (e: Exception) {
                Log.e("MachineStatusViewModel", "Error resetting wash cycle", e)
            }
        }
    }
}

sealed interface MachineStatusUiState {
    object Loading : MachineStatusUiState
    data class Success(val status: WashingMachineStatus) : MachineStatusUiState
    data class Error(val message: String) : MachineStatusUiState
}
