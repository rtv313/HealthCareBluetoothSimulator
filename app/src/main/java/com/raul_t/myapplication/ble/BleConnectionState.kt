package com.raul_t.myapplication.ble

sealed class BleConnectionState {
    object Idle : BleConnectionState()
    object Advertising : BleConnectionState()
    data class Connected(val deviceName: String?) : BleConnectionState()
    data class Error(val message: String) : BleConnectionState()
}
