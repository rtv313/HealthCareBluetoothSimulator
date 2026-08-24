package com.raul_t.myapplication.presentation.bluetoothSensorEmulator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raul_t.myapplication.presentation.bluetoothSensorEmulator.components.BluetoothSection
import com.raul_t.myapplication.presentation.bluetoothSensorEmulator.components.DeviceSection
import com.raul_t.myapplication.presentation.bluetoothSensorEmulator.components.SecuritySection

@Composable
fun SensorEmitterScreen() {

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {

            Text(
                text = "Bluetooth Sensor Emulator",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            DeviceSection()

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            BluetoothSection()

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            SecuritySection()
        }
    }
}
