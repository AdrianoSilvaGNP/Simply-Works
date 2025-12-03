package com.adrianosilva.simply_works.domain.models

/**
 * Enumeration representing the state of a washing machine. "MachMd" field in the API.
 */
enum class MachineState(
    val code: Int,
    val label: String
) {

    IDLE(1, "Idle"),
    RUNNING(2, "Running"),
    PAUSED(3, "Paused"),
    DELAYED_START_SELECTION(4, "Delayed start selection"),
    DELAYED_START_PROGRAMMED(5, "Delayed start programmed"),
    ERROR(6, "Error"),
    FINISHED1(7, "Finished"),
    FINISHED2(8, "Finished");

    companion object {
        fun fromCode(code: Int): MachineState =
            entries.firstOrNull { it.code == code }
                ?: throw IllegalArgumentException("Unknown MachineState code: $code")
    }

    override fun toString(): String = label
}