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
import com.adrianosilva.simply_works.data.local.KeyManager
import com.adrianosilva.simply_works.domain.models.WashPreset

sealed interface WashProgramEffect {
    data object NavigateBack : WashProgramEffect
}

class WashProgramViewModel(private val api: CandyApiService, private val keyManager: KeyManager) : ViewModel() {

    var state by mutableStateOf(WashProgramUiState())
        private set

    private val _effect = Channel<WashProgramEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        state = state.copy(savedPresets = keyManager.getPresets())
    }

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
            is WashProgramAction.SelectPreset -> {
                val program = state.washPrograms.find { it.code == action.preset.programCode }
                if (program != null) {
                    state = state.copy(
                        selectedProgram = program,
                        selectedTemperature = action.preset.temp,
                        selectedSpinSpeed = action.preset.spinSpeed / 100 // spinSpeed is stored as raw value (e.g., 800), state needs code (8)
                    )
                }
            }
            is WashProgramAction.SavePreset -> {
                val newPreset = WashPreset(
                    name = action.name,
                    programCode = state.selectedProgram.code,
                    temp = state.selectedTemperature,
                    spinSpeed = if (state.selectedSpinSpeed == 0) 0 else state.selectedSpinSpeed * 100
                )
                val updatedPresets = state.savedPresets + newPreset
                keyManager.savePresets(updatedPresets)
                state = state.copy(savedPresets = updatedPresets)
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
                    state = state.copy(isSendingRequest = false, isProcessing = true)
                    // Give machine time to process before navigating back
                    kotlinx.coroutines.delay(1500)
                    _effect.send(WashProgramEffect.NavigateBack)
                }
                is Result.Error -> {
                    val message =
                            when (val reason = result.reason) {
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
        class WashProgramViewModelFactory(private val apiService: CandyApiService, private val keyManager: KeyManager) :
                ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(WashProgramViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST") return WashProgramViewModel(apiService, keyManager) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
