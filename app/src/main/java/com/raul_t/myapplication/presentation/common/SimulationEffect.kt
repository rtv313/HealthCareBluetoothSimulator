package com.raul_t.myapplication.presentation.common

sealed interface SimulationUiEvent {
    data object RequestPermissions : SimulationUiEvent
}
