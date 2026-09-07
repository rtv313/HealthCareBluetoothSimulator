package com.raul_t.myapplication.service.Bluetooth

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothServiceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning = _isServiceRunning.asStateFlow()

    fun startService() {
        val intent = Intent(context, BluetoothSensorForegroundService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopService() {
        val intent = Intent(context, BluetoothSensorForegroundService::class.java)
        context.stopService(intent)
    }

    fun setServiceRunning(running: Boolean) {
        _isServiceRunning.value = running
    }
}
