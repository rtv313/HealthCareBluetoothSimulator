package com.raul_t.myapplication.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.raul_t.myapplication.R
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

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startForeground(1, createNotification())

        serviceScope.launch {
            while (true) {
                dataSource.createNewHeartRate()
                delay(1000)
            }
        }
        return START_STICKY
    }

    private fun createNotification(): Notification {

        val channelId = "heart_rate_service"

        createNotificationChannel()

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Heart Rate Simulator")
            .setContentText("Generating heart rate data...")
            .setSmallIcon(R.drawable.ic_heart)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {

        val channelId = "heart_rate_service"
        val channel = NotificationChannel(
            channelId,
            "Heart Rate Simulator",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Background heart rate simulation service"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}