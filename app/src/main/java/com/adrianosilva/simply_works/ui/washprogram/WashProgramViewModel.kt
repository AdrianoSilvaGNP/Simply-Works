package com.adrianosilva.simply_works.ui.washprogram

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adrianosilva.simply_works.data.dto.WashProgramRequest
import com.adrianosilva.simply_works.data.remote.CandyApiService
import com.adrianosilva.simply_works.domain.ErrorReason
import com.adrianosilva.simply_works.domain.Result
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class WashProgramViewModel(private val api: CandyApiService): ViewModel() {

    var state by mutableStateOf(WashProgramUiState())
        private set

    private val _event = Channel<WashProgramEvent>()
    val event = _event.receiveAsFlow()

    fun onAction(action: WashProgramAction) {
        when (action) {
            is WashProgramAction.SelectProgram -> {
                state = state.copy(selectedProgram = action.program)
            }

            is WashProgramAction.SelectTemperature -> {
                state = state.copy(selectedTemperature = action.temperature)
            }

            is WashProgramAction.SelectSpinSpeed -> {
                state = state.copy(selectedSpinSpeed = action.spinSpeed)
            }

            is WashProgramAction.SetDelayValue -> {
                state = state.copy(delayValue = action.delayValue)
            }

            is WashProgramAction.SendWashProgram -> {
                sendWashProgram()
            }
        }
    }

    private fun sendWashProgram() {
        viewModelScope.launch {
            state = state.copy(isSendingRequest = true, errorMessage = null)
            val request =
                WashProgramRequest(
                    startStop = 1,
                    programNumber = state.selectedProgram.number,
                    programCode = state.selectedProgram.code,
                    targetTemperature = state.selectedTemperature,
                    targetSpinSpeed = state.selectedSpinSpeed,
                    delayValue = state.delayValue
                )

            when (val result = api.sendWashRequest(request)) {
                is Result.Success -> {
                    state = state.copy(isSendingRequest = false)
                    _event.send(WashProgramEvent.NavigateBack)
                }

                is Result.Error -> {
                    val message = when (val reason = result.reason) {
                        is ErrorReason.NoConnection -> "No connection"
                        is ErrorReason.NoData -> "No data"
                        is ErrorReason.NetworkError -> "Network error: ${reason.message}"
                        is ErrorReason.Unknown ->
                            "Unknown error: ${reason.exception.localizedMessage}"
                    }
                    state = state.copy(isSendingRequest = false, errorMessage = message)
                }
            }
        }
    }

    companion object {
        class WashProgramViewModelFactory(private val apiService: CandyApiService):
            ViewModelProvider.Factory {
            override fun <T: ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(WashProgramViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST") return WashProgramViewModel(apiService) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
