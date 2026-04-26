package com.adrianosilva.simply_works.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class WashPreset(
    val name: String,
    val programCode: Int,
    val temp: Int,
    val spinSpeed: Int
)
