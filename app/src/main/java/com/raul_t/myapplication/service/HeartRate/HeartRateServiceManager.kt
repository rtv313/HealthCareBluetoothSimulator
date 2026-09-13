package com.raul_t.myapplication.service.HeartRate

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.raul_t.myapplication.service.HealthcareSimulationService
import com.raul_t.myapplication.data.datasource.FakeSensorDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeartRateServiceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sensorDataSource: FakeSensorDataSource
) {
    fun startService() {
        val intent = Intent(context, HealthcareSimulationService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopService() {
        // Only truly stop the service if the Bluetooth simulation is also off
        if (!sensorDataSource.sensorState.value.isStarted) {
            val intent = Intent(context, HealthcareSimulationService::class.java)
            context.stopService(intent)
        }
    }
}
