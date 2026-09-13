package com.raul_t.myapplication.service.Bluetooth

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.raul_t.myapplication.service.HealthcareSimulationService
import com.raul_t.myapplication.data.datasource.FakeHeartRateDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothServiceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val heartRateDataSource: FakeHeartRateDataSource
) {
    fun startService() {
        val intent = Intent(context, HealthcareSimulationService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopService() {
        // Only truly stop the service if the Heart Rate simulation is also off
        if (!heartRateDataSource.config.value.isBpmStarted) {
            val intent = Intent(context, HealthcareSimulationService::class.java)
            context.stopService(intent)
        }
    }
}
