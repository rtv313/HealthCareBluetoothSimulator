package com.raul_t.myapplication.ble_connect

import com.raul_t.myapplication.domain.model.SensorStatus
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BleClientManager {
    val connectionState: StateFlow<BleClientConnectionState>
    val heartRate: SharedFlow<Int>
    val sensorStatus: SharedFlow<SensorStatus>

    fun connect(address: String)
    fun disconnect()
}
