package com.adrianosilva.simply_works.ui.washprogram

import com.adrianosilva.simply_works.domain.models.WashProgram

sealed class WashProgramAction {
    data class SelectProgram(val program: WashProgram): WashProgramAction()
    data class SelectTemperature(val temperature: Int): WashProgramAction()
    data class SelectSpinSpeed(val spinSpeed: Int): WashProgramAction()
    data class SetDelayValue(val delayValue: Int): WashProgramAction()
    data object SendWashProgram: WashProgramAction()
}

sealed class WashProgramEvent {
    data object NavigateBack: WashProgramEvent()
}