package com.raul_t.myapplication.presentation.bluetoothSensorEmulator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raul_t.myapplication.R
import com.raul_t.myapplication.presentation.bluetoothSensorEmulator.components.BluetoothSection
import com.raul_t.myapplication.presentation.bluetoothSensorEmulator.components.DeviceSection
import com.raul_t.myapplication.presentation.bluetoothSensorEmulator.components.SecuritySection

@Composable
fun SensorEmitterScreen() {

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {

        Text(
            text = stringResource(R.string.bluetooth_sensor_emulator_title),
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
