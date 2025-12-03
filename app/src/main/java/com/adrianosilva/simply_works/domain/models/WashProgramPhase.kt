package com.adrianosilva.simply_works.domain.models

/**
 * Enumeration representing the various phases of a wash program. "PrPh" field in the API.
 */
enum class WashProgramPhase(
    val code: Int,
    val label: String
) {

    STOPPED(0, "Stopped"),
    PRE_WASH(1, "Pre-wash"),
    WASH(2, "Wash"),
    RINSE(3, "Rinse"),
    LAST_RINSE(4, "Last rinse"),
    END(5, "End"),
    DRYING(6, "Drying"),
    ERROR(7, "Error"),
    STEAM(8, "Steam"),
    GOOD_NIGHT(9, "Spin - Good Night"),
    SPIN(10, "Spin");

    companion object {
        fun fromCode(code: Int): WashProgramPhase =
            entries.firstOrNull { it.code == code }
                ?: throw IllegalArgumentException("Unknown WashProgramState code: $code")
    }

    override fun toString(): String = label
}