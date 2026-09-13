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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raul_t.myapplication.R
import com.raul_t.myapplication.presentation.common.rememberSimulationPermissionState
import com.raul_t.myapplication.presentation.bluetoothSensorEmulator.components.BluetoothSection
import com.raul_t.myapplication.presentation.bluetoothSensorEmulator.components.DeviceSection
import com.raul_t.myapplication.presentation.bluetoothSensorEmulator.components.SecuritySection

@Composable
fun SensorEmitterScreen(
    viewModel: BluetoothSensorEmulatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sensor = uiState.sensor
    val scrollState = rememberScrollState()
    val permissionState = rememberSimulationPermissionState()

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

        DeviceSection(
            name = sensor.name,
            status = sensor.status,
            isStarted = uiState.isServiceRunning,
            onNameChange = viewModel::updateName,
            onStatusChange = viewModel::updateStatus,
            onToggleStart = {
                if (uiState.isServiceRunning) {
                    viewModel.toggleStart()
                } else {
                    // Explicit responsibility to start service remains in the UI
                    if (permissionState.allGranted) {
                        viewModel.toggleStart()
                    } else {
                        permissionState.requestPermissions { granted ->
                            if (granted) viewModel.toggleStart()
                        }
                    }
                }
            }
        )

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        BluetoothSection(
            isAdvertising = sensor.isAdvertising,
            allowConnection = sensor.allowConnection,
            onAdvertisingChange = viewModel::toggleAdvertising,
            onAllowConnectionChange = viewModel::toggleAllowConnection
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        SecuritySection(
            isPinEnabled = sensor.isPinEnabled,
            savedPin = sensor.pin.toString().padStart(4, '0'),
            onPinEnabledChange = viewModel::togglePinEnabled,
            onPinChange = viewModel::updatePin
        )
    }
}
