package com.raul_t.myapplication.presentation.bluetoothSensorEmulator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raul_t.myapplication.R


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

            SecuritySection()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceSection() {
    var text by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "Device", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        BluetoothStartStopButton()

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Name", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Device name") },
            placeholder = { Text("Write the device name") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SensorStateDropdown()

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SensorStateDropdown() {

    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("Opción 1") }

    Column(horizontalAlignment = Alignment.Start) {

        Text(text = "Status:", style = MaterialTheme.typography.titleMedium)

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Button(
                onClick = {
                    expanded = true
                }
            ) {
                Text(selectedOption)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                DropdownMenuItem(
                    text = { Text("Healthy") },
                    onClick = {
                        selectedOption = "Healthy"
                        expanded = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Damaged") },
                    onClick = {
                        selectedOption = "Damaged"
                        expanded = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Offline") },
                    onClick = {
                        selectedOption = "Offline"
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun BluetoothSection() {
    Column() {
        Text( text = "Bluetooth",
            style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Text( text = "Advertising",
            style = MaterialTheme.typography.titleMedium)

        Switch(
            checked = true,
            onCheckedChange = {  },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.outline,
                checkedTrackColor = MaterialTheme.colorScheme.surface,
                checkedBorderColor = MaterialTheme.colorScheme.outline,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
                uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
                uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)
            )
        )

        Text( text = "Allow Connection",
            style = MaterialTheme.typography.titleMedium)

        Switch(
            checked = true,
            onCheckedChange = {  },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.outline,
                checkedTrackColor = MaterialTheme.colorScheme.surface,
                checkedBorderColor = MaterialTheme.colorScheme.outline,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
                uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
                uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)
            )
        )
    }
}

@Composable
fun SecuritySection() {
    Column() {
        Text(
            text = "Security",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enable PIN",
            style = MaterialTheme.typography.titleMedium
        )

        Switch(
            checked = false,
            onCheckedChange = { },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.outline,
                checkedTrackColor = MaterialTheme.colorScheme.surface,
                checkedBorderColor = MaterialTheme.colorScheme.outline,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
                uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
                uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)
            )
        )
    }
}


@Composable
fun BluetoothStartStopButton() {
    Button(
        onClick = {  },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(4.dp),
    ) {
        Icon(
            imageVector = if (true) {
                Icons.Default.Stop
            } else {
                Icons.Default.PlayArrow
            },
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))

        Text(
            text = if (true) stringResource(R.string.stop) else stringResource(R.string.start)
        )
    }
}