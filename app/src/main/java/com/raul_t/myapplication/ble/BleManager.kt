package com.raul_t.myapplication.ble

import kotlinx.coroutines.flow.StateFlow

interface BleManager {
    val connectionState: StateFlow<BleConnectionState>
    fun startAdvertising(deviceName: String)
    fun stopAdvertising()
    fun isBluetoothEnabled(): Boolean
}
