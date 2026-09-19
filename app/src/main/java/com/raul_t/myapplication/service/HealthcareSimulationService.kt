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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HealthcareSimulationService : Service() {

    companion object {
        private const val TAG = "HealthcareSimulationService"
    }

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
        Log.d(TAG, "onCreate - Initializing data relays")
        notificationHelper.createNotificationChannel()

        // 1. BLE Data Relay: Listen to BPM changes and push to BLE
        // We do this in onCreate to ensure it only happens once for the life of the service.
        serviceScope.launch {
            heartRateDataSource.currentBpm.collect { bpm ->
                if (bpm > 0) {
                    Log.v(TAG, "Relaying BPM to BLE: $bpm")
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
                            Log.e(TAG, "Bluetooth is disabled, cannot advertise")
                        } else {
                            Log.i(TAG, "Starting Advertising")
                            bleManager.startAdvertising(sensor)
                        }
                    } else {
                        Log.i(TAG, "Stopping Advertising")
                        bleManager.stopAdvertising()
                    }
                }

                // --- Handle Dynamic Data Update (Soft Update) ---
                // We push status AND name updates via the existing GATT connection.
                if (statusChanged || nameChanged || (sensor.isStarted && sensor.isAdvertising && lastSensorState == null)) {
                    Log.d(TAG, "Updating Sensor Data via BLE: Status=${sensor.status} Name=${sensor.name}")
                    bleManager.updateSensorStatus(sensor.status)
                    bleManager.updateSensorName(sensor.name)
                }
                
                lastSensorState = sensor
            }
        }

        // 3. Heart Rate Simulation Loop Management
        serviceScope.launch {
            heartRateDataSource.config
                .map { it.isBpmStarted to it.updateIntervalMs }
                .distinctUntilChanged()
                .collect { (isStarted, interval) ->
                    simulationJob?.cancel()
                    if (isStarted) {
                        simulationJob = launch {
                            Log.i(TAG, "Starting loop (Interval: ${interval}ms)")
                            while (true) {
                                heartRateDataSource.createNewHeartRate()
                                delay(interval)
                            }
                        }
                    }
                }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")

        startForeground(
            303,
            notificationHelper.createNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE or ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        bleManager.stopAdvertising()
        serviceScope.cancel()
    }
}
