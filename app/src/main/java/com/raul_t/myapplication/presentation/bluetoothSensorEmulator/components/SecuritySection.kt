package com.raul_t.myapplication.presentation.bluetoothSensorEmulator.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
