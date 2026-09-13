package com.raul_t.myapplication.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import com.raul_t.myapplication.ble.BleManager
import com.raul_t.myapplication.data.datasource.FakeHeartRateDataSource
import com.raul_t.myapplication.data.datasource.FakeSensorDataSource
import com.raul_t.myapplication.service.Bluetooth.BluetoothNotificationHelper
import com.raul_t.myapplication.service.HeartRate.HeartRateServiceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HealthcareSimulationService : Service() {

    @Inject
    lateinit var heartRateDataSource: FakeHeartRateDataSource

    @Inject
    lateinit var sensorDataSource: FakeSensorDataSource

    @Inject
    lateinit var bleManager: BleManager

    @Inject
    lateinit var notificationHelper: BluetoothNotificationHelper

    @Inject
    lateinit var heartRateServiceManager: HeartRateServiceManager

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    override fun onCreate() {
        super.onCreate()
        Log.d("HealthcareService", "onCreate")
        notificationHelper.createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("HealthcareService", "onStartCommand")

        // Start as foreground with combined types
        startForeground(
            303,
            notificationHelper.createNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE or ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )

        // 1. Heart Rate Simulation Loop
        serviceScope.launch {
            heartRateDataSource.config.collect { config ->
                if (config.isBpmStarted) {
                    launch {
                        while (heartRateDataSource.config.value.isBpmStarted) {
                            heartRateDataSource.createNewHeartRate()
                            delay(heartRateDataSource.config.value.updateIntervalMs)
                        }
                    }
                }
            }
        }

        // 2. BLE Data Relay: Listen to BPM changes and push to BLE
        serviceScope.launch {
            heartRateDataSource.currentBpm.collect { bpm ->
                if (bpm > 0) {
                    bleManager.updateHeartRate(bpm)
                }
            }
        }

        // 3. BLE Advertising Management
        serviceScope.launch {
            sensorDataSource.sensorState.collect { sensor ->
                if (sensor.isStarted && sensor.isAdvertising) {
                    if (!bleManager.isBluetoothEnabled()) {
                        Log.e("HealthcareService", "Bluetooth is disabled, cannot advertise")
                    } else {
                        // startAdvertising logic handles internal restarts if settings change
                        bleManager.startAdvertising(sensor)
                    }
                } else {
                    bleManager.stopAdvertising()
                }
            }
        }

        // 4. BLE Status Relay: Listen to status changes and push to BLE
        serviceScope.launch {
            sensorDataSource.sensorState.collect { sensor ->
                bleManager.updateSensorStatus(sensor.status)
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("HealthcareService", "onDestroy")
        bleManager.stopAdvertising()
        serviceScope.cancel()
    }
}
