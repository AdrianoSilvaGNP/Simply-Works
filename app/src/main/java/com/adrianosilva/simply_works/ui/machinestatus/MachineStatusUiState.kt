package com.adrianosilva.simply_works.ui.machinestatus

import com.adrianosilva.simply_works.data.dto.StatusCounters
import com.adrianosilva.simply_works.domain.models.WashingMachineStatus

data class MachineStatusUiState(
    val isLoading: Boolean = true,
    val isProcessingAction: Boolean = false,
    val status: WashingMachineStatus? = null,
    val usageStats: StatusCounters? = null,
    val errorMessage: String? = null
)