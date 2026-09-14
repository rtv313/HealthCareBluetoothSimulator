package com.raul_t.myapplication.ble_connect

import com.raul_t.myapplication.domain.model.DiscoveredBluetoothDevice
import kotlinx.coroutines.flow.StateFlow

interface BleScannerManager {
    val discoveredDevices: StateFlow<List<DiscoveredBluetoothDevice>>
    fun startScanning()
    fun stopScanning()
}
