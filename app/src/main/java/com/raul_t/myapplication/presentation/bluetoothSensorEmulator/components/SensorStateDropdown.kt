package com.raul_t.myapplication.presentation.bluetoothSensorEmulator.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

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
