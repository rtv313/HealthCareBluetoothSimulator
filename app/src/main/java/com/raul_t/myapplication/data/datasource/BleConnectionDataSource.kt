package com.raul_t.myapplication.data.datasource

import com.raul_t.myapplication.ble_connect.BleScannerManager
import com.raul_t.myapplication.domain.model.DiscoveredBluetoothDevice
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleConnectionDataSource @Inject constructor(
    private val bleScannerManager: BleScannerManager
) {
    val discoveredDevices: StateFlow<List<DiscoveredBluetoothDevice>> = bleScannerManager.discoveredDevices

    fun startScanning() {
        bleScannerManager.startScanning()
    }

    fun stopScanning() {
        bleScannerManager.stopScanning()
    }
}
