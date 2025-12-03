package com.adrianosilva.simply_works.domain.models

/**
 * Data class representing the status of a washing machine.
 */
data class WashingMachineStatus(
    val machineState: MachineState,
    val programState: WashProgramPhase,
    val program: Int,
    val temp: Int,
    val spinSpeed: Int,
    val remainingMinutes: Int,
    val delayMinutes: Int? = null
)
