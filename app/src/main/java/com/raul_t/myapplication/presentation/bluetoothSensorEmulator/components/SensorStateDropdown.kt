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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raul_t.myapplication.R
import com.raul_t.myapplication.domain.model.SensorStatus
import com.raul_t.myapplication.ui.theme.LightSuccessGreen
import com.raul_t.myapplication.ui.theme.MyApplicationTheme

@Composable
fun SensorStateDropdown(
    selectedStatus: SensorStatus,
    onStatusChange: (SensorStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val selectedLabel = when (selectedStatus) {
        SensorStatus.Healthy -> stringResource(R.string.status_healthy)
        SensorStatus.Damaged -> stringResource(R.string.status_damaged)
        SensorStatus.Offline -> stringResource(R.string.status_offline)
    }

    Column(horizontalAlignment = Alignment.Start) {
        Text(text = stringResource(R.string.status_label), style = MaterialTheme.typography.titleMedium)

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Button(
                onClick = { expanded = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.outline,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(selectedLabel)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = LightSuccessGreen
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.status_healthy), color = Color.Black) },
                    onClick = {
                        onStatusChange(SensorStatus.Healthy)
                        expanded = false
                    }
                )

                DropdownMenuItem(
                    text = { Text(stringResource(R.string.status_damaged), color = Color.Black) },
                    onClick = {
                        onStatusChange(SensorStatus.Damaged)
                        expanded = false
                    }
                )

                DropdownMenuItem(
                    text = { Text(stringResource(R.string.status_offline), color = Color.Black) },
                    onClick = {
                        onStatusChange(SensorStatus.Offline)
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
        SensorStateDropdown(
            selectedStatus = SensorStatus.Healthy,
            onStatusChange = {}
        )
    }
}
