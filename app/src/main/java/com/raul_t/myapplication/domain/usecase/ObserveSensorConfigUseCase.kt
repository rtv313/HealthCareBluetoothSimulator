package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.domain.model.BluetoothSensor
import com.raul_t.myapplication.domain.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSensorConfigUseCase @Inject constructor(
    private val repository: SensorRepository
) {
    operator fun invoke(): Flow<BluetoothSensor> {
        return repository.observeSensorConfig()
    }
}
