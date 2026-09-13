package com.raul_t.myapplication.presentation.bluetoothClient

import com.raul_t.myapplication.domain.model.SensorStatus

data class DiscoveredBluetoothDevice(
    val name: String,
    val address: String,
    val rssi: Int
)

data class BluetoothClientUiState(
    val discoveredDevices: List<DiscoveredBluetoothDevice> = listOf(
        DiscoveredBluetoothDevice("Health Sensor Alpha", "AA:BB:CC:11:22:33", -65),
        DiscoveredBluetoothDevice("HeartRate Emulator", "44:55:66:77:88:99", -42),
        DiscoveredBluetoothDevice("Pulse Oximeter Beta", "11:22:33:AA:BB:CC", -82),
        DiscoveredBluetoothDevice("Smart Band Mock", "77:88:99:00:11:22", -58)
    ),
    val selectedDeviceForDialog: DiscoveredBluetoothDevice? = null,
    val connectedDevice: DiscoveredBluetoothDevice? = null,
    val mockBpm: Int = 72,
    val mockStatus: SensorStatus = SensorStatus.Healthy
)
