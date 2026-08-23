package com.raul_t.myapplication.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionUtils {
    
    /**
     * Checks if the app has permission to post notifications.
     * Required for Android 13 (API 33) and above.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Placeholder for future Bluetooth permissions check.
     */
    fun hasBluetoothPermissions(context: Context): Boolean {
        // To be implemented when Bluetooth functionality is added
        return true 
    }
}
