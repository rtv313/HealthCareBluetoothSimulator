package com.raul_t.myapplication.presentation.bluetoothSensorEmulator

import com.raul_t.myapplication.domain.model.BluetoothSensor

data class BluetoothSensorEmulatorUiState(
    val sensor: BluetoothSensor = BluetoothSensor(),
    val isServiceRunning: Boolean = false
)
