package com.raul_t.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.raul_t.myapplication.presentation.heart.HeartRateScreen
import com.raul_t.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkAndRequestNotificationPermission()

        setContent {
            MyApplicationTheme {
                HeartRateScreen()
            }
        }
    }

    private fun checkAndRequestNotificationPermission() {
        Log.d("MainActivity", "Checking notification permission. SDK: ${Build.VERSION.SDK_INT}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val status = ContextCompat.checkSelfPermission(this, permission)
            Log.d("MainActivity", "Permission status: $status")
            if (status != PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Requesting permission")
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}