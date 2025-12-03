package com.adrianosilva.simply_works.data.dto

/**
 * Data class representing a request to set a wash program on the washing machine.
 */
data class WashProgramRequest(
    // Control Fields
    val write: Int = 1,                     // Write operation flag
    val delayValue: Int? = null,            // Delay Value for requests. 1 means 30 minutes delay, 2 means 1 hour, etc.
    val startStop: Int? = null,             // StSt: Start/Stop Status (0 = Stop, 1 = Start)

    // Program Identification
    val programNumber: Int? = null,         // PrNm: Program Number
    val programCode: Int? = null,           // PrCode: Program Code
    val programName: String? = null,        // PrStr: Program String Name

    // Program Targets
    val targetTemperature: Int? = null,     // TmpTgt: Target Temperature in Celsius
    val targetSoilLevel: Int? = null,       // SLevTgt: Target Soil Level
    val targetSpinSpeed: Int? = null,       // SpdTgt: Target Spin Speed (e.g., 8 = 800 RPM)

    // Program Options & Settings
    val optionMask1: Int? = null,           // OptMsk1: Bitmask for options group 1
    val optionMask2: Int? = null,           // OptMsk2: Bitmask for options group 2
    val language: Int? = null,              // Lang: Language ID
    val steam: Int? = null,                 // Stm: Steam option enabled (0 or 1)
    val extraDry: Int? = null,              // Dry: Extra Dry option enabled (0 or 1)
    val energyDeclaration: Int? = null,     // ED: Energy Declaration mode (0 or 1)
    val recipeId: Int? = null,              // RecipeId: Custom recipe identifier
    val startCheckUp: Int? = null,          // StartCheckUp: Flag for initial check-up
    val displayTestOn: Int? = null,         // DispTestOn: Display test mode

    // Statistics
    val getStats: Int? = null               // GetStat: Request usage statistics (1 to request)
) {

    /**
     * Converts the WashProgramRequest to a query string format for HTTP requests.
     */
    fun toQueryString(): String {
        val parts = mutableListOf<String>()

        parts.add("Write=$write")
        startStop?.let { parts.add("StSt=$it") }
        delayValue?.let { parts.add("DelVl=$it") }

        programNumber?.let { parts.add("PrNm=$it") }
        programCode?.let { parts.add("PrCode=$it") }
        programName?.let { parts.add("PrStr=$it") }

        targetTemperature?.let { parts.add("TmpTgt=$it") }
        targetSoilLevel?.let { parts.add("SLevTgt=$it") }
        targetSpinSpeed?.let { parts.add("SpdTgt=$it") }

        optionMask1?.let { parts.add("OptMsk1=$it") }
        optionMask2?.let { parts.add("OptMsk2=$it") }
        language?.let { parts.add("Lang=$it") }

        steam?.let { parts.add("Stm=$it") }
        extraDry?.let { parts.add("Dry=$it") }
        energyDeclaration?.let { parts.add("ED=$it") }
        recipeId?.let { parts.add("RecipeId=$it") }

        startCheckUp?.let { parts.add("StartCheckUp=$it") }
        displayTestOn?.let { parts.add("DispTestOn=$it") }

        getStats?.let { parts.add("GetStat=$it") }

        return parts.joinToString("&")
    }
}