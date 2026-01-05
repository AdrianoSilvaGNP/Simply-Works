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

class MachineStatusViewModel(
    private val api: CandyApiService
): ViewModel() {

    var state by mutableStateOf(MachineStatusUiState())
        private set

    fun onAction(action: MachineStatusAction) {
        when (action) {
            is MachineStatusAction.Refresh -> loadStatus()
            is MachineStatusAction.ResetWashCycle -> resetWashCycle()
            else -> {}
        }
    }

    private fun loadStatus() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)

            when (val result = api.getStatus()) {
                is Result.Success -> {
                    Timber.d("Fetching machine status success")
                    state = state.copy(isLoading = false, status = result.data)
                }

                is Result.Error -> {
                    Timber.e("Error fetching machine status: ${result.reason}")
                    state = state.copy(
                        isLoading = false,
                        errorMessage = result.reason.toUiMessage()
                    )
                }
            }
        }
    }

    private fun resetWashCycle() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)

            when (val result = api.callResetWashCycle()) {
                is Result.Success -> {
                    Timber.d("Wash cycle reset successfully.")
                    loadStatus()
                }

                is Result.Error -> {
                    Timber.e("Error resetting wash cycle: ${result.reason}")
                    state = state.copy(
                        isLoading = false,
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
            is ErrorReason.NetworkError -> "Network error: Are you connected to the machine's home Wi-Fi? Or is the machine turned on?\n\nDetails: $message"
            is ErrorReason.Unknown -> "Unknown error: ${exception.localizedMessage}"
        }

    companion object {
        class MachineStatusViewModelFactory(private val candyApiService: CandyApiService):
            ViewModelProvider.Factory {

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
