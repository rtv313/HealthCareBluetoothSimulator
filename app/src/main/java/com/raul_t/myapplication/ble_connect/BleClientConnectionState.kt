package com.raul_t.myapplication.ble_connect

sealed class BleClientConnectionState {
    object Disconnected : BleClientConnectionState()
    object Connecting : BleClientConnectionState()
    object Connected : BleClientConnectionState()
    object WaitingForPin : BleClientConnectionState()
    object InvalidPin : BleClientConnectionState()
    data class Error(val message: String) : BleClientConnectionState()
}
