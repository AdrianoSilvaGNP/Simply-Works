package com.adrianosilva.simply_works.ui.washprogram

import com.adrianosilva.simply_works.domain.models.WashProgram

data class WashProgramUiState(
    val washPrograms: List<WashProgram> = WashProgram.allPrograms,
    val selectedProgram: WashProgram = WashProgram.Eco4060,
    val temperatures: List<Int> = listOf(0, 20, 30, 40, 60, 90), // 0 for cold wash
    val selectedTemperature: Int = 30,
    val spinSpeeds: List<Int> = listOf(0, 400, 800, 1000, 1200), // 0 for no spin
    val selectedSpinSpeed: Int = 800,
    val delayValue: Int = 0, // each value represents 30 minutes
    val isSendingRequest: Boolean = false,
    val errorMessage: String? = null
)
