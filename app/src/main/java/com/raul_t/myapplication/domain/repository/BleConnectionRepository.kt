package com.raul_t.myapplication.domain.repository

import com.raul_t.myapplication.domain.model.DiscoveredBluetoothDevice
import kotlinx.coroutines.flow.StateFlow

interface BleConnectionRepository {
    val discoveredDevices: StateFlow<List<DiscoveredBluetoothDevice>>
    fun startScanning()
    fun stopScanning()
}
