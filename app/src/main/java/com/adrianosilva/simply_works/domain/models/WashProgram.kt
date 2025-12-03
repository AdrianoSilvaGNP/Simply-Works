package com.adrianosilva.simply_works.domain.models

sealed class WashProgram(
    val code: Int,
    val number: Int,
    val name: String,
    val description: String
) {
    data object Eco4060: WashProgram(
        code = 2,
        number = 3,
        name = "Eco 40-60",
        description = "",
    )

    data object Jeans: WashProgram(
        code = 10,
        number = 4,
        name = "Jeans",
        description = "",
    )

    data object HandWashAndWool: WashProgram(
        code = 5,
        number = 5,
        name = "Hand Wash + Wool",
        description = "",
    )

    data object SyntheticAndColoured: WashProgram(
        code = 3,
        number = 6,
        name = "Synthetic and Coloured",
        description = "",
    )

    data object Rinse: WashProgram(
        code = 35,
        number = 7,
        name = "Rinse",
        description = "",
    )

    data object DrainAndSpin: WashProgram(
        code = 129,
        number = 8,
        name = "Drain + spin",
        description = ""
    )

    data object Delicate59: WashProgram(
        code = 4,
        number = 9,
        name = "Delicate 59'",
        description = "",
    )

    data object SportPlus39: WashProgram(
        code = 136,
        number = 10,
        name = "Sport Plus 39'",
        description = "",
    )

    data object HygienePlus59: WashProgram(
        code = 40,
        number = 11,
        name = "Hygiene Plus 59'",
        description = "",
    )

    data object Quick14: WashProgram(
        code = 39,
        number = 12,
        name = "Quick 14'",
        description = "",
    )

    data object MixedAndColoured59: WashProgram(
        code = 135,
        number = 13,
        name = "Mixed and Coloured 59'",
        description = "",
    )

    data object PerfectCotton59: WashProgram(
        code = 8,
        number = 14,
        name = "Perfect Cotton 59'",
        description = "",
    )

    data object Special49: WashProgram(
        code = 167,
        number = 15,
        name = "Special 49'",
        description = "",
    )

    companion object {
        val allPrograms: List<WashProgram> = listOf(
            Eco4060,
            Jeans,
            HandWashAndWool,
            SyntheticAndColoured,
            Rinse,
            DrainAndSpin,
            Delicate59,
            SportPlus39,
            HygienePlus59,
            Quick14,
            MixedAndColoured59,
            PerfectCotton59,
            Special49
        )
    }
}