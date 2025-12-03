package com.adrianosilva.simply_works.ui.machinestatus

sealed class MachineStatusAction {
    object Refresh: MachineStatusAction()
    object ResetWashCycle: MachineStatusAction()
    object CallTestCycleIn30Min: MachineStatusAction()
    object GoToWashProgram: MachineStatusAction()
    object GetUsageStats: MachineStatusAction()
}