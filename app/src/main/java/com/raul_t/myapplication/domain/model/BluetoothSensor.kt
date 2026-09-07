package com.raul_t.myapplication.domain.model

enum class SensorStatus {
    Healthy, Damaged, Offline
}

data class BluetoothSensor(
    val name: String = "",
    val status: SensorStatus = SensorStatus.Healthy,
    val isAdvertising: Boolean = true,
    val allowConnection: Boolean = true,
    val isPinEnabled: Boolean = false,
    val pin: Int = 0
)
