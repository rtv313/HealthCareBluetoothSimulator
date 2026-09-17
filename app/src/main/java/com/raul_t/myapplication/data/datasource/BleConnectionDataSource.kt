package com.raul_t.myapplication.data.datasource

import com.raul_t.myapplication.ble_connect.BleClientConnectionState
import com.raul_t.myapplication.ble_connect.BleClientManager
import com.raul_t.myapplication.ble_connect.BleScannerManager
import com.raul_t.myapplication.domain.model.DiscoveredBluetoothDevice
import com.raul_t.myapplication.domain.model.SensorStatus
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleConnectionDataSource @Inject constructor(
    private val bleScannerManager: BleScannerManager,
    private val bleClientManager: BleClientManager
) {
    val discoveredDevices: StateFlow<List<DiscoveredBluetoothDevice>> = bleScannerManager.discoveredDevices
    val connectionState: StateFlow<BleClientConnectionState> = bleClientManager.connectionState
    val heartRate: SharedFlow<Int> = bleClientManager.heartRate
    val sensorStatus: SharedFlow<SensorStatus> = bleClientManager.sensorStatus
    val sensorName: SharedFlow<String> = bleClientManager.sensorName

    fun startScanning() {
        bleScannerManager.startScanning()
    }

    fun stopScanning() {
        bleScannerManager.stopScanning()
    }

    fun connect(address: String) {
        bleClientManager.connect(address)
    }

    fun disconnect() {
        bleClientManager.disconnect()
    }
}
