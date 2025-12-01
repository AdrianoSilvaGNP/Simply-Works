package com.adrianosilva.simply_works

// ------------------------------
// Base sealed interface for code enums
// ------------------------------
interface StatusCode {
    val code: Int
    val label: String
}

// ------------------------------
// Machine state (MachMd)
// ------------------------------
enum class MachineState(
    override val code: Int,
    override val label: String
) : StatusCode {

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

// ------------------------------
// Wash program state (PrPh)
// ------------------------------
enum class WashProgramState(
    override val code: Int,
    override val label: String
) : StatusCode {

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
        fun fromCode(code: Int): WashProgramState =
            entries.firstOrNull { it.code == code }
                ?: throw IllegalArgumentException("Unknown WashProgramState code: $code")
    }

    override fun toString(): String = label
}

// ------------------------------
// Data class for mapped status
// ------------------------------
data class WashingMachineStatus(
    val machineState: MachineState,
    val programState: WashProgramState,
    val program: Int,
    val temp: Int,
    val spinSpeed: Int,
    val remainingMinutes: Int
) {
    companion object {
        fun fromJson(json: Map<String, String>): WashingMachineStatus {
            return WashingMachineStatus(
                machineState = MachineState.fromCode(json["MachMd"]!!.toInt()),
                programState = WashProgramState.fromCode(json["PrPh"]!!.toInt()),
                program = json["PrNm"]!!.toInt(), // Your JSON uses PrNm, python used Pr
                temp = json["Temp"]!!.toInt(),
                spinSpeed = json["SpinSp"]!!.toInt() * 100,
                remainingMinutes = json["RemTime"]!!.toInt()
            )
        }
    }
}


