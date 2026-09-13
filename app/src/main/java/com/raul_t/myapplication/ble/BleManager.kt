package com.raul_t.myapplication.ble

import com.raul_t.myapplication.domain.model.BluetoothSensor
import com.raul_t.myapplication.domain.model.SensorStatus
import kotlinx.coroutines.flow.StateFlow

interface BleManager {
    val connectionState: StateFlow<BleConnectionState>
    fun startAdvertising(sensor: BluetoothSensor)
    fun stopAdvertising()
    fun isBluetoothEnabled(): Boolean
    fun updateHeartRate(bpm: Int)
    fun updateSensorStatus(status: SensorStatus)
}
