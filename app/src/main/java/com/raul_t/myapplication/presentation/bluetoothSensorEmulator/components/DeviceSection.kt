package com.raul_t.myapplication.presentation.bluetoothSensorEmulator.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raul_t.myapplication.R
import com.raul_t.myapplication.domain.model.SensorStatus
import com.raul_t.myapplication.ui.theme.MyApplicationTheme

@Composable
fun DeviceSection(
    name: String,
    status: SensorStatus,
    onNameChange: (String) -> Unit,
    onStatusChange: (SensorStatus) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.device_label),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            BluetoothStartStopButton()
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(R.string.name_label), style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.device_name_hint)) },
            placeholder = { Text(stringResource(R.string.device_name_placeholder)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        SensorStateDropdown(
            selectedStatus = status,
            onStatusChange = onStatusChange
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceSectionPreview() {
    MyApplicationTheme {
        DeviceSection(
            name = "My Device",
            status = SensorStatus.Healthy,
            onNameChange = {},
            onStatusChange = {}
        )
    }
}
