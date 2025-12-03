package com.adrianosilva.simply_works.ui

sealed class Screen(val route: String) {
    data object MachineStatus : Screen("machine_status")
    data object WashProgram : Screen("wash_program")
    data object UsageStats : Screen("usage_stats")
}