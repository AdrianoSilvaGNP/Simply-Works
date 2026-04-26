package com.adrianosilva.simply_works.ui.machinestatus

sealed class MachineStatusAction {
    data object Refresh : MachineStatusAction()
    data object ResetWashCycle : MachineStatusAction()
    data object FetchUsageStats : MachineStatusAction()
    data object GoToWashProgram : MachineStatusAction()
    data object GoToUsageStats : MachineStatusAction()
    data object GoToSettings : MachineStatusAction()
}