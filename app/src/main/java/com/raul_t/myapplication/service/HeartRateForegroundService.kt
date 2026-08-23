package com.raul_t.myapplication.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.raul_t.myapplication.data.datasource.FakeHeartRateDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HeartRateForegroundService : Service() {

    @Inject
    lateinit var dataSource: FakeHeartRateDataSource

    @Inject
    lateinit var notificationHelper: HeartRateNotificationHelper

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    override fun onCreate() {
        super.onCreate()
        Log.d("HeartRateService", "onCreate")
        notificationHelper.createNotificationChannel()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("HeartRateService", "onStartCommand")
        Log.d("HeartRateService", "Starting foreground with type")
        startForeground(
            101,
            notificationHelper.createNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )

        serviceScope.launch {
            while (true) {
                dataSource.createNewHeartRate()
                delay(dataSource.config.value.updateIntervalMs)
            }
        }
        return START_STICKY
    }
}
