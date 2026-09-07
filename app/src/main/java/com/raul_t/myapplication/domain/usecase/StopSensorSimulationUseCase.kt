package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.service.Bluetooth.BluetoothServiceManager
import javax.inject.Inject

class StopSensorSimulationUseCase @Inject constructor(
    private val serviceManager: BluetoothServiceManager
) {
    fun invoke() {
        serviceManager.stopService()
    }
}
