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

        // 2. Smarter BLE Advertising & Status Management
        serviceScope.launch {
            var lastSensorState: com.raul_t.myapplication.domain.model.BluetoothSensor? = null
            
            sensorDataSource.sensorState.collect { sensor ->
                val startStateChanged = lastSensorState?.isStarted != sensor.isStarted
                val advertisingStateChanged = lastSensorState?.isAdvertising != sensor.isAdvertising
                val statusChanged = lastSensorState?.status != sensor.status
                val nameChanged = lastSensorState?.name != sensor.name
                
                // --- Handle Advertising Restart (Hard Update) ---
                // We ONLY restart advertising if the toggles change.
                // Discovery name is now hardcoded to "Health Sensor", so name changes are ignored here.
                if (startStateChanged || advertisingStateChanged) {
                    if (sensor.isStarted && sensor.isAdvertising) {
                        if (!bleManager.isBluetoothEnabled()) {
                            Log.e("HealthcareService", "Bluetooth is disabled, cannot advertise")
                        } else {
                            Log.i("HealthcareService", "Starting Advertising")
                            bleManager.startAdvertising(sensor)
                        }
                    } else {
                        Log.i("HealthcareService", "Stopping Advertising")
                        bleManager.stopAdvertising()
                    }
                }

                // --- Handle Dynamic Data Update (Soft Update) ---
                // We push status AND name updates via the existing GATT connection.
                if (statusChanged || nameChanged || (sensor.isStarted && sensor.isAdvertising && lastSensorState == null)) {
                    Log.d("HealthcareService", "Updating Sensor Data via BLE: Status=${sensor.status} Name=${sensor.name}")
                    bleManager.updateSensorStatus(sensor.status)
                    bleManager.updateSensorName(sensor.name)
                }
                
                lastSensorState = sensor
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
