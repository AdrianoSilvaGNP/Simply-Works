package com.adrianosilva.simply_works.ui.usagestats

data class UsageStatsUiState(
    val stats: List<Pair<String, Int>> = emptyList()
)
