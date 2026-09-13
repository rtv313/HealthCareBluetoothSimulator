package com.raul_t.myapplication.data.datasource

import com.raul_t.myapplication.domain.model.BluetoothSensor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSensorDataSource @Inject constructor() {

    private val _sensorState = MutableStateFlow(BluetoothSensor())
    val sensorState: StateFlow<BluetoothSensor> = _sensorState.asStateFlow()

    fun updateSensorState(newSensor: BluetoothSensor) {
        _sensorState.value = newSensor
    }

    fun updateSensorState(update: (BluetoothSensor) -> BluetoothSensor) {
        _sensorState.update(update)
    }
}
