package com.adrianosilva.simply_works.data.dto

/**
 * Data class representing a request to set a wash program on the washing machine.
 */
data class WashProgramRequest(
    // Control Fields
    val write: Int = 1,             // Write operation flag
    val stateStatus: Int,           // State Status
    val delVl: Int,                 // Delay Value. 1 means 15 minutes delay, 2 means 30 minutes, etc.

    // Program Identification
    val programNumber: Int,         // PrNm: Program Number
    val programCode: Int,           // PrCode: Program Code
    val programName: String,        // PrStr: Program String Name

    // Program Targets
    val targetTemperature: Int,     // TmpTgt: Target Temperature in Celsius
    val targetSoilLevel: Int,       // SLevTgt: Target Soil Level
    val targetSpinSpeed: Int,       // SpdTgt: Target Spin Speed (e.g., 8 = 800 RPM)

    // Program Options & Settings
    val optionMask1: Int,           // OptMsk1: Bitmask for options group 1
    val optionMask2: Int,           // OptMsk2: Bitmask for options group 2
    val language: Int,              // Lang: Language ID
    val steam: Int,                 // Stm: Steam option enabled (0 or 1)
    val extraDry: Int,              // Dry: Extra Dry option enabled (0 or 1)
    val energyDeclaration: Int,     // ED: Energy Declaration mode (0 or 1)
    val recipeId: Int,              // RecipeId: Custom recipe identifier
    val startCheckUp: Int,          // StartCheckUp: Flag for initial check-up
    val displayTestOn: Int          // DispTestOn: Display test mode
)


/**
 * Converts the WashProgramRequest to a query string format for HTTP requests.
 */
fun WashProgramRequest.toQueryString(): String {
    return buildString {
        append("Write=${write}")
        append("&StSt=${stateStatus}")
        append("&DelVl=${delVl}")
        append("&PrNm=${programNumber}")
        append("&PrCode=${programCode}")
        append("&PrStr=${programName}")
        append("&TmpTgt=${targetTemperature}")
        append("&SLevTgt=${targetSoilLevel}")
        append("&SpdTgt=${targetSpinSpeed}")
        append("&OptMsk1=${optionMask1}")
        append("&OptMsk2=${optionMask2}")
        append("&Lang=${language}")
        append("&Stm=${steam}")
        append("&Dry=${extraDry}")
        append("&ED=${energyDeclaration}")
        append("&RecipeId=${recipeId}")
        append("&StartCheckUp=${startCheckUp}")
        append("&DispTestOn=${displayTestOn}")
    }
}

