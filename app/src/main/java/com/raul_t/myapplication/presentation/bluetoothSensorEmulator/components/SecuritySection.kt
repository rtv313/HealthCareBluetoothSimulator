package com.raul_t.myapplication.presentation.bluetoothSensorEmulator.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raul_t.myapplication.R

@Composable
fun SecuritySection() {
    var isPinEnabled by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var savedPin by remember { mutableStateOf("0000") }

    Column() {
        Text(
            text = stringResource(R.string.security_label),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.enable_pin_label),
            style = MaterialTheme.typography.titleMedium
        )

        Switch(
            checked = isPinEnabled,
            onCheckedChange = { enabled ->
                isPinEnabled = enabled
                if (enabled) {
                    showPinDialog = true
                }
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.outline,
                checkedTrackColor = MaterialTheme.colorScheme.surface,
                checkedBorderColor = MaterialTheme.colorScheme.outline,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
                uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
                uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)
            )
        )

        if (showPinDialog) {
            SetPinDialog(
                onDismiss = {
                    showPinDialog = false
                    // If user cancels and we don't have a pin yet, maybe disable switch?
                    // For now just keep the switch as it was.
                },
                onSave = { pin ->
                    savedPin = pin
                    showPinDialog = false
                }
            )
        }
    }
}
