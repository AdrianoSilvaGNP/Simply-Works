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
    @JvmField @SerialName("Program1") var program1: String,
    @JvmField @SerialName("Program2") var program2: String,
    @JvmField @SerialName("Program3") var program3: String,
    @JvmField @SerialName("Program4") var program4: String,
    @JvmField @SerialName("Program5") var program5: String,
    @JvmField @SerialName("Program6") var program6: String,
    @JvmField @SerialName("Program7") var program7: String,
    @JvmField @SerialName("Program8") var program8: String,
    @JvmField @SerialName("Program9") var program9: String,
    @JvmField @SerialName("Program10") var program10: String,
    @JvmField @SerialName("Program11") var program11: String,
    @JvmField @SerialName("Program12") var program12: String,
    @JvmField @SerialName("Program13") var program13: String,
    @JvmField @SerialName("Program14") var program14: String,
    @JvmField @SerialName("Program15") var program15: String,
    @JvmField @SerialName("Program16") var program16: String,
    @JvmField @SerialName("Program17") var program17: String,
    @JvmField @SerialName("Program18") var program18: String,
    @JvmField @SerialName("Program19") var program19: String,
    @JvmField @SerialName("Program20") var program20: String,
    @JvmField @SerialName("Program21") var program21: String,
    @JvmField @SerialName("Temp0to30") var temp0to30: String,
    @JvmField @SerialName("Temp40") var temp40: String,
    @JvmField @SerialName("Temp60to90") var temp60to90: String,
    @JvmField @SerialName("CounterMV") var counterMV: String,
    @JvmField @SerialName("DryCottonExtra") var dryCottonExtra: String,
    @JvmField @SerialName("DryCottonPA") var dryCottonPA: String,
    @JvmField @SerialName("DryCottonPS") var dryCottonPS: String,
    @JvmField @SerialName("DryCotton120") var dryCotton120: String,
    @JvmField @SerialName("DryCotton90") var dryCotton90: String,
    @JvmField @SerialName("DryCotton60") var dryCotton60: String,
    @JvmField @SerialName("DryCotton30") var dryCotton30: String,
    @JvmField @SerialName("DrySyntExtra") var drySyntExtra: String,
    @JvmField @SerialName("DrySyntPA") var drySyntPA: String,
    @JvmField @SerialName("DrySyntPS") var drySyntPS: String,
    @JvmField @SerialName("DrySynt120") var drySynt120: String,
    @JvmField @SerialName("DrySynt90") var drySynt90: String,
    @JvmField @SerialName("DrySynt60") var drySynt60: String,
    @JvmField @SerialName("DrySynt30") var drySynt30: String
)