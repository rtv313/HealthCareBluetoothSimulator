package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.service.Bluetooth.BluetoothServiceManager
import javax.inject.Inject

class StartSensorSimulationUseCase @Inject constructor(
    private val serviceManager: BluetoothServiceManager
) {
    fun invoke() {
        serviceManager.startService()
    }
}
