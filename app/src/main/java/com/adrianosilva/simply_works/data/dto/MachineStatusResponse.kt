package com.adrianosilva.simply_works.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MachineStatusResponse(
    val statusLavatrice: StatusLavatrice
)

@Serializable
@SerialName("statusLavatrice")
data class StatusLavatrice(
    @SerialName("WiFiStatus") val wifiStatus: String,
    @SerialName("Err") val error: String,
    @SerialName("MachMd") val machineMode: String,
    @SerialName("PrNm") val programNumber: String,
    @SerialName("PrCode") val programCode: String,
    @SerialName("PrPh") val programPhase: String,
    @SerialName("SLevel") val soilLevel: String,
    @SerialName("Temp") val temp: String,
    @SerialName("SpinSp") val spinSpeed: String,
    @SerialName("Opt1") val option1: String,
    @SerialName("Opt2") val option2: String,
    @SerialName("Opt3") val option3: String,
    @SerialName("Opt4") val option4: String,
    @SerialName("Opt5") val option5: String,
    @SerialName("Opt6") val option6: String,
    @SerialName("Opt7") val option7: String,
    @SerialName("Opt8") val option8: String,
    @SerialName("Opt9") val option9: String,
    @SerialName("DelVal") val delayValue: String, // here delay value is in minutes. In the request, it's in units of 30 minutes.
    @SerialName("Steam") val steam: String,
    @SerialName("DryT") val dryT: String, // dry time/temp/type?
    @SerialName("RemTime") val remainingTime: String,
    @SerialName("RecipeId") val recipeId: String,
    @SerialName("CheckUpState") val checkUpState: String,
    @SerialName("Lang") val language: String,
    @SerialName("FillR") val fillR: String, // fill rate?
    @SerialName("Det") val detergent: String,
    @SerialName("Soft") val softener: String,
    @SerialName("DetWarn") val detergentWarning: String,
    @SerialName("SoftWarn") val softenerWarning: String,
    @SerialName("DetPreW") val detergentPreW: String, // detergent pre-wash?
    @SerialName("SoftPreW") val softenerPreW: String, // softener pre-wash?
    @SerialName("DPrgCnt") val dPrgCnt: String, // detergent program count?
    @SerialName("SPrgCnt") val sPrgCnt: String, // softener program count?
    @SerialName("WaterHard") val waterHardness: String, // water hardness level?
    @SerialName("rED") val energyDeclaration: String // energy declaration/doctor?
)
