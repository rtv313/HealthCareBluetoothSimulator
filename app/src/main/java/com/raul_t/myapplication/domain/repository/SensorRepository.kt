package com.raul_t.myapplication.domain.repository

import com.raul_t.myapplication.domain.model.BluetoothSensor
import kotlinx.coroutines.flow.Flow

interface SensorRepository {
    fun observeSensorConfig(): Flow<BluetoothSensor>
    suspend fun updateSensorConfig(newSensor: BluetoothSensor)
    suspend fun updateSensorConfig(update: (BluetoothSensor) -> BluetoothSensor)
}
