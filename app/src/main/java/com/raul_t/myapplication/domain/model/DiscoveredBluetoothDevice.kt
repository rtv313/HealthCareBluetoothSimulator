package com.raul_t.myapplication.domain.model

data class DiscoveredBluetoothDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val lastSeen: Long = System.currentTimeMillis()
)
