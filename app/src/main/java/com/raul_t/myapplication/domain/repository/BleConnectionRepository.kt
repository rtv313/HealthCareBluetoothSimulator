package com.raul_t.myapplication.domain.repository

import com.raul_t.myapplication.ble_connect.BleClientConnectionState
import com.raul_t.myapplication.domain.model.DiscoveredBluetoothDevice
import com.raul_t.myapplication.domain.model.SensorStatus
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BleConnectionRepository {
    val discoveredDevices: StateFlow<List<DiscoveredBluetoothDevice>>
    val connectionState: StateFlow<BleClientConnectionState>
    val heartRate: SharedFlow<Int>
    val sensorStatus: SharedFlow<SensorStatus>
    val sensorName: SharedFlow<String>

    fun startScanning()
    fun stopScanning()
    fun clearDiscoveredDevices()

    fun connect(address: String)
    fun disconnect()
}
