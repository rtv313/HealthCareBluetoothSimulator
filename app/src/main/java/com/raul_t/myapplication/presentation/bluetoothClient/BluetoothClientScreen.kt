package com.raul_t.myapplication.presentation.bluetoothClient

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raul_t.myapplication.R
import com.raul_t.myapplication.presentation.bluetoothClient.components.BluetoothDevices
import com.raul_t.myapplication.presentation.bluetoothClient.components.ConnectedSensorPanel
import com.raul_t.myapplication.presentation.bluetoothClient.components.ConnectionDialog
import com.raul_t.myapplication.presentation.common.SimulationUiEvent
import com.raul_t.myapplication.presentation.common.rememberSimulationPermissionState

@Composable
fun BluetoothClientScreen(
    viewModel: BluetoothClientViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val permissionState = rememberSimulationPermissionState()

    LaunchedEffect(viewModel.event) {
        viewModel.event.collect { event ->
            when (event) {
                is SimulationUiEvent.RequestPermissions -> {
                    permissionState.requestPermissions { granted ->
                        viewModel.onPermissionResult(granted)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.bluetooth_client_simulator_title),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.available_devices_label),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Component 1: Scrollable list of discovered bluetooth devices
        BluetoothDevices(
            devices = uiState.discoveredDevices,
            onDeviceClick = { device -> viewModel.selectDevice(device) },
            modifier = Modifier.weight(1f)
        )

        // Component 2: Connection Dialog Confirmation Modal
        uiState.selectedDeviceForDialog?.let { device ->
            ConnectionDialog(
                device = device,
                onConnect = { viewModel.connectDevice(device) },
                onDismiss = { viewModel.clearDialog() }
            )
        }

        // Component 3: Observation Data Summary Panel
        uiState.connectedDevice?.let { connectedDevice ->
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.connected_sensor_data_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            ConnectedSensorPanel(
                connectedDevice = connectedDevice,
                mockBpm = uiState.mockBpm,
                mockStatus = uiState.mockStatus,
                onDisconnect = { viewModel.disconnect() }
            )
        }
    }
}
