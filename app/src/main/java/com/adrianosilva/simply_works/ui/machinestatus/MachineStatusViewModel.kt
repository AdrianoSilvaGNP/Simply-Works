package com.adrianosilva.simply_works.ui.machinestatus

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adrianosilva.simply_works.data.remote.CandyApiService
import com.adrianosilva.simply_works.domain.ErrorReason
import com.adrianosilva.simply_works.domain.Result
import kotlinx.coroutines.launch
import timber.log.Timber

import androidx.work.WorkManager
class MachineStatusViewModel(private val api: CandyApiService, private val workManager: WorkManager): ViewModel() {

    var state by mutableStateOf(MachineStatusUiState())
        private set

    fun onAction(action: MachineStatusAction) {
        when (action) {
            is MachineStatusAction.Refresh -> loadStatus()
            is MachineStatusAction.ResetWashCycle -> resetWashCycle()
            is MachineStatusAction.FetchUsageStats -> fetchUsageStats()
            else -> {}
        }
    }

    private fun loadStatus() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)

            when (val result = api.getStatus()) {
                is Result.Success -> {
                    Timber.d("Fetching machine status success")
                    val status = result.data
                    state = state.copy(isLoading = false, status = status)
                    
                    if (status.machineState == com.adrianosilva.simply_works.domain.models.MachineState.RUNNING || 
                        status.machineState == com.adrianosilva.simply_works.domain.models.MachineState.DELAYED_START_SELECTION ||
                        status.machineState == com.adrianosilva.simply_works.domain.models.MachineState.DELAYED_START_PROGRAMMED) {
                        com.adrianosilva.simply_works.data.worker.WashProgramWorker.startTracking(workManager, status.remainingTimeMinutes)
                    }
                }

                is Result.Error -> {
                    Timber.e("Error fetching machine status: ${result.reason}")
                    state =
                        state.copy(
                            isLoading = false,
                            errorMessage = result.reason.toUiMessage()
                        )
                }
            }
        }
    }

    private fun fetchUsageStats() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)
            when (val result = api.getUsageStats()) {
                is Result.Success -> {
                    state = state.copy(isLoading = false, usageStats = result.data)
                }
                is Result.Error -> {
                    state = state.copy(isLoading = false, errorMessage = result.reason.toUiMessage())
                }
            }
        }
    }

    private fun resetWashCycle() {
        viewModelScope.launch {
            state = state.copy(isProcessingAction = true, errorMessage = null)

            when (val result = api.callResetWashCycle()) {
                is Result.Success -> {
                    Timber.d("Wash cycle reset successfully. Waiting for machine to update...")
                    // Wait for the machine to process the reset and update its internal state
                    kotlinx.coroutines.delay(2000)
                    state = state.copy(isProcessingAction = false)
                    loadStatus()
                }

                is Result.Error -> {
                    Timber.e("Error resetting wash cycle: ${result.reason}")
                    state =
                        state.copy(
                            isProcessingAction = false,
                            errorMessage = result.reason.toUiMessage()
                        )
                }
            }
        }
    }

    private fun ErrorReason.toUiMessage(): String =
        when (this) {
            is ErrorReason.NoConnection -> "No connection to machine"
            is ErrorReason.NoData -> "No data received"
            is ErrorReason.NetworkError -> "Network error: $message"
            is ErrorReason.Unknown -> "Unknown error: ${exception.localizedMessage}"
        }

    companion object {
        class MachineStatusViewModelFactory(private val candyApiService: CandyApiService, private val workManager: WorkManager):
            ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T: ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MachineStatusViewModel::class.java)) {
                    return MachineStatusViewModel(candyApiService, workManager) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
