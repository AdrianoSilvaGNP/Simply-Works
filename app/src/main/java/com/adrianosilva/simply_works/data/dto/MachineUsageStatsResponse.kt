package com.adrianosilva.simply_works.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MachineUsageStatsResponse(
    val statusCounters: StatusCounters
)

@Serializable
@SerialName("statusCounters")
data class StatusCounters(
    @SerialName("Program1") val program1: String,
    @SerialName("Program2") val program2: String,
    @SerialName("Program3") val program3: String,
    @SerialName("Program4") val program4: String,
    @SerialName("Program5") val program5: String,
    @SerialName("Program6") val program6: String,
    @SerialName("Program7") val program7: String,
    @SerialName("Program8") val program8: String,
    @SerialName("Program9") val program9: String,
    @SerialName("Program10") val program10: String,
    @SerialName("Program11") val program11: String,
    @SerialName("Program12") val program12: String,
    @SerialName("Program13") val program13: String,
    @SerialName("Program14") val program14: String,
    @SerialName("Program15") val program15: String,
    @SerialName("Program16") val program16: String,
    @SerialName("Program17") val program17: String,
    @SerialName("Program18") val program18: String,
    @SerialName("Program19") val program19: String,
    @SerialName("Program20") val program20: String,
    @SerialName("Program21") val program21: String,
    @SerialName("Temp0to30") val temp0to30: String,
    @SerialName("Temp40") val temp40: String,
    @SerialName("Temp60to90") val temp60to90: String,
    @SerialName("CounterMV") val counterMV: String,
    @SerialName("DryCottonExtra") val dryCottonExtra: String,
    @SerialName("DryCottonPA") val dryCottonPA: String,
    @SerialName("DryCottonPS") val dryCottonPS: String,
    @SerialName("DryCotton120") val dryCotton120: String,
    @SerialName("DryCotton90") val dryCotton90: String,
    @SerialName("DryCotton60") val dryCotton60: String,
    @SerialName("DryCotton30") val dryCotton30: String,
    @SerialName("DrySyntExtra") val drySyntExtra: String,
    @SerialName("DrySyntPA") val drySyntPA: String,
    @SerialName("DrySyntPS") val drySyntPS: String,
    @SerialName("DrySynt120") val drySynt120: String,
    @SerialName("DrySynt90") val drySynt90: String,
    @SerialName("DrySynt60") val drySynt60: String,
    @SerialName("DrySynt30") val drySynt30: String
)