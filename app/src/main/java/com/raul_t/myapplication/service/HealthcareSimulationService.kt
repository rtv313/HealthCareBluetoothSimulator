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

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    private var simulationJob: kotlinx.coroutines.Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("HealthcareService", "onCreate - Initializing data relays")
        notificationHelper.createNotificationChannel()

        // 1. BLE Data Relay: Listen to BPM changes and push to BLE
        // We do this in onCreate to ensure it only happens once for the life of the service.
        serviceScope.launch {
            heartRateDataSource.currentBpm.collect { bpm ->
                if (bpm > 0) {
                    Log.v("HealthcareService", "Relaying BPM to BLE: $bpm")
                    bleManager.updateHeartRate(bpm)
                }
            }
        }

        // 2. BLE Status Relay: Listen to status changes and push to BLE
        serviceScope.launch {
            sensorDataSource.sensorState.collect { sensor ->
                Log.d("HealthcareService", "Relaying Status to BLE: ${sensor.status}")
                bleManager.updateSensorStatus(sensor.status)
            }
        }

        // 3. BLE Advertising Management
        serviceScope.launch {
            sensorDataSource.sensorState.collect { sensor ->
                if (sensor.isStarted && sensor.isAdvertising) {
                    if (!bleManager.isBluetoothEnabled()) {
                        Log.e("HealthcareService", "Bluetooth is disabled, cannot advertise")
                    } else {
                        bleManager.startAdvertising(sensor)
                    }
                } else {
                    bleManager.stopAdvertising()
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("HealthcareService", "onStartCommand")

        startForeground(
            303,
            notificationHelper.createNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE or ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )

        // Heart Rate Simulation Loop Management
        serviceScope.launch {
            heartRateDataSource.config.collect { config ->
                simulationJob?.cancel()
                
                if (config.isBpmStarted) {
                    simulationJob = launch {
                        Log.i("HealthcareService", "Starting loop (Interval: ${config.updateIntervalMs}ms)")
                        while (true) {
                            heartRateDataSource.createNewHeartRate()
                            delay(config.updateIntervalMs)
                        }
                    }
                }
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
