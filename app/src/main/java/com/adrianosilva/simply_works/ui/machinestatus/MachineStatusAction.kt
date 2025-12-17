package com.adrianosilva.simply_works.ui.machinestatus

sealed class MachineStatusAction {
    object Refresh: MachineStatusAction()
    object ResetWashCycle: MachineStatusAction()
    object GoToWashProgram: MachineStatusAction()
    object GoToUsageStats: MachineStatusAction()
}