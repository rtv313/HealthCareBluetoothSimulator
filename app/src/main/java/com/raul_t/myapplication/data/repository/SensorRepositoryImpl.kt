package com.raul_t.myapplication.data.repository

import com.raul_t.myapplication.data.datasource.FakeSensorDataSource
import com.raul_t.myapplication.domain.model.BluetoothSensor
import com.raul_t.myapplication.domain.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorRepositoryImpl @Inject constructor(
    private val dataSource: FakeSensorDataSource
) : SensorRepository {
    
    override fun observeSensorConfig(): Flow<BluetoothSensor> {
        return dataSource.sensorState
    }

    override suspend fun updateSensorConfig(newSensor: BluetoothSensor) {
        dataSource.updateSensorState(newSensor)
    }

    override suspend fun updateSensorConfig(update: (BluetoothSensor) -> BluetoothSensor) {
        dataSource.updateSensorState(update)
    }
}
