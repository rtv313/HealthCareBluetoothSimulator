package com.raul_t.myapplication.navigation

import androidx.annotation.DrawableRes
import com.raul_t.myapplication.R

sealed class Screen(val route: String, val label: String, @DrawableRes val icon: Int) {
    object HeartRate : Screen("heart_rate", "Heart Rate", R.drawable.ic_heartbeat)
    object BluetoothEmitter : Screen("bluetooth_emitter", "Bluetooth", R.drawable.ic_bluetooth)
}
