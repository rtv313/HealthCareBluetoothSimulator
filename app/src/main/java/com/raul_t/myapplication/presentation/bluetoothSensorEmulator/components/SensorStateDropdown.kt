package com.raul_t.myapplication.presentation.bluetoothSensorEmulator.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raul_t.myapplication.ui.theme.LightSuccessGreen
import com.raul_t.myapplication.ui.theme.MyApplicationTheme
import com.raul_t.myapplication.ui.theme.SuccessGreen

@Composable
fun SensorStateDropdown() {

    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("Healthy") }

    Column(horizontalAlignment = Alignment.Start) {

        Text(text = "Status:", style = MaterialTheme.typography.titleMedium)

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Button(
                onClick = {
                    expanded = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.outline,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(selectedOption)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                },
                containerColor = LightSuccessGreen
            ) {
                DropdownMenuItem(
                    text = { Text("Healthy", color = SuccessGreen) },
                    onClick = {
                        selectedOption = "Healthy"
                        expanded = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Damaged", color = SuccessGreen) },
                    onClick = {
                        selectedOption = "Damaged"
                        expanded = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Offline", color = SuccessGreen) },
                    onClick = {
                        selectedOption = "Offline"
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SensorStateDropdownPreview() {
    MyApplicationTheme {
        SensorStateDropdown()
    }
}
