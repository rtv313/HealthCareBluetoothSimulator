package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.domain.model.BluetoothSensor
import com.raul_t.myapplication.domain.repository.SensorRepository
import javax.inject.Inject

class UpdateSensorConfigUseCase @Inject constructor(
    private val repository: SensorRepository
) {
    suspend operator fun invoke(newSensor: BluetoothSensor) {
        repository.updateSensorConfig(newSensor)
    }

    suspend operator fun invoke(update: (BluetoothSensor) -> BluetoothSensor) {
        repository.updateSensorConfig(update)
    }
}
