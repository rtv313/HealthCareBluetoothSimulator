package com.raul_t.myapplication.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeartRateServiceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun startService() {
        val intent = Intent(context, HeartRateForegroundService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopService() {
        val intent = Intent(context, HeartRateForegroundService::class.java)
        context.stopService(intent)
    }
}
