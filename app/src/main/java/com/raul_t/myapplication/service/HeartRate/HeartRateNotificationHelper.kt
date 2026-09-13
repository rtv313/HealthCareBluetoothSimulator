package com.raul_t.myapplication.service.HeartRate

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.raul_t.myapplication.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeartRateNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val channelId = "heart_rate_service"

    fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            context.getString(R.string.heart_rate_notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.heart_rate_notification_channel_description)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun createNotification(): Notification {
        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(context.getString(R.string.heart_rate_notification_title))
            .setContentText(context.getString(R.string.heart_rate_notification_text))
            .setSmallIcon(R.drawable.ic_heart)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()
    }
}
