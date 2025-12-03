package com.adrianosilva.simply_works.ui.machinestatus

import com.adrianosilva.simply_works.domain.models.WashingMachineStatus

data class MachineStatusUiState(
    val isLoading: Boolean = true,
    val status: WashingMachineStatus? = null,
    val errorMessage: String? = null
)