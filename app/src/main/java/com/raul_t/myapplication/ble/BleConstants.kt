package com.raul_t.myapplication.ble

object BleConstants {
    // Delays & Timeouts (Milliseconds)
    const val GATT_SETTLE_DELAY_MS = 300L
    const val GATT_RETRY_DELAY_MS = 500L
    const val WATCHDOG_TIMEOUT_MS = 5000L
    
    // Scanner Settings
    const val DISCOVERY_CLEANUP_INTERVAL_MS = 1000L
    const val DISCOVERY_STALE_THRESHOLD_MS = 3000L

    // GATT Protocol Values
    const val GATT_ERROR_STALE = 133
    const val HEART_RATE_VALUE_INDEX = 1
    const val MIN_HEART_RATE_PACKET_SIZE = 2
}
