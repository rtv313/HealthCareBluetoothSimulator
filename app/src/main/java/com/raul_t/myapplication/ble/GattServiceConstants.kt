package com.raul_t.myapplication.ble

import java.util.UUID

object GattServiceConstants {
    // Heart Rate Service
    val HEART_RATE_SERVICE_UUID: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    
    // Heart Rate Measurement Characteristic
    val HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
    
    // Client Characteristic Configuration Descriptor (CCCD) - for notifications
    val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    // Simulator Meta Service (Custom)
    val SIMULATOR_SERVICE_UUID: UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb")
    
    // Sensor Status Characteristic (Custom)
    val SENSOR_STATUS_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb")

    // PIN Validation Characteristic (Custom)
    val PIN_VALIDATION_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000ff03-0000-1000-8000-00805f9b34fb")
}
