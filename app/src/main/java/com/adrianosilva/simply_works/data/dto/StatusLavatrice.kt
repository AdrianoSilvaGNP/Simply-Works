package com.adrianosilva.simply_works.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    @SerialName("DelVal") val delayValue: String,
    @SerialName("RemTime") val remainingTime: String,
)
