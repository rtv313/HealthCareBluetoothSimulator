package com.raul_t.myapplication.service.Bluetooth

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import com.raul_t.myapplication.data.datasource.FakeSensorDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import javax.inject.Inject

@AndroidEntryPoint
class BluetoothSensorForegroundService : Service() {

    @Inject
    lateinit var dataSource: FakeSensorDataSource

    @Inject
    lateinit var notificationHelper: BluetoothNotificationHelper

    @Inject
    lateinit var serviceManager: BluetoothServiceManager

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    override fun onCreate() {
        super.onCreate()
        Log.d("BluetoothSensorService", "onCreate")
        notificationHelper.createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BluetoothSensorService", "onStartCommand")
        
        startForeground(
            202,
            notificationHelper.createNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
        )

        serviceManager.setServiceRunning(true)

        // Future logic for BLE advertising will go here
        
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("BluetoothSensorService", "onDestroy")
        serviceManager.setServiceRunning(false)
        serviceScope.cancel()
    }
}
