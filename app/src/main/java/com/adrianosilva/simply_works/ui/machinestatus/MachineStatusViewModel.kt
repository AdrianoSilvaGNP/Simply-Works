package com.adrianosilva.simply_works.ui.machinestatus

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adrianosilva.simply_works.data.remote.CandyApiService
import kotlinx.coroutines.launch
import timber.log.Timber

class MachineStatusViewModel(
    private val api: CandyApiService
): ViewModel() {

    var state by mutableStateOf(MachineStatusUiState())
        private set

    fun onAction(action: MachineStatusAction) {
        when(action) {
            is MachineStatusAction.Refresh -> loadStatus()
            is MachineStatusAction.CallTestCycleIn30Min -> callTestCycleIn30Min()
            is MachineStatusAction.ResetWashCycle -> resetWashCycle()
            is MachineStatusAction.GetUsageStats -> getUsageStats()
            else -> {}
        }
    }

    private fun loadStatus() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)
            try {
                Timber.d("Fetching machine status...")
                val status = api.getStatus()
                state = state.copy(isLoading = false, status = status)
            } catch (e: Exception) {
                Timber.e("Error fetching machine status", e)
                state = state.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    private fun callTestCycleIn30Min() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)
            try {
                api.callTestCycleIn30Min()
                Timber.d("Test cycle scheduled successfully.")
                loadStatus()
            } catch (e: Exception) {
                Timber.e("Error scheduling test cycle", e)
                state = state.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    private fun resetWashCycle() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)
            try {
                api.callResetWashCycle()
                Timber.d("Wash cycle reset successfully.")
                loadStatus()
            } catch (e: Exception) {
                Timber.e("Error resetting wash cycle", e)
                state = state.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    private fun getUsageStats() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)
            try {
                val stats = api.getUsageStats()
                Timber.d("Stats: $stats")
            } catch (e: Exception) {
                Timber.e("Error fetching stats", e)
                state = state.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    companion object {
        class MachineStatusViewModelFactory(
            private val candyApiService: CandyApiService
        ): ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T: ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MachineStatusViewModel::class.java)) {
                    return MachineStatusViewModel(candyApiService) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}