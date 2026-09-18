package com.raul_t.myapplication.presentation.bluetoothClient

import com.raul_t.myapplication.ble_connect.BleClientConnectionState
import com.raul_t.myapplication.domain.model.DiscoveredBluetoothDevice
import com.raul_t.myapplication.domain.model.SensorStatus

data class BluetoothClientUiState(
    val discoveredDevices: List<DiscoveredBluetoothDevice> = emptyList(),
    val selectedDeviceForDialog: DiscoveredBluetoothDevice? = null,
    val connectedDevice: DiscoveredBluetoothDevice? = null,
    val connectionState: BleClientConnectionState = BleClientConnectionState.Disconnected,
    val isWaitingForPin: Boolean = false,
    val pinErrorMessage: String? = null,
    val isLoading: Boolean = false,
    val mockBpm: Int = 72,
    val mockStatus: SensorStatus = SensorStatus.Healthy,
    val mockName: String = ""
)
