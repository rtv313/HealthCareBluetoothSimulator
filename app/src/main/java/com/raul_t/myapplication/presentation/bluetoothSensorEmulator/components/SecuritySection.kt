package com.raul_t.myapplication.presentation.bluetoothSensorEmulator.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.enable_pin_label),
                style = MaterialTheme.typography.titleMedium
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPinEnabled) {
                    Text(
                        text = stringResource(R.string.pin_display, savedPin),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = { showPinDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit PIN",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

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
            }
        }

        if (showPinDialog) {
            SetPinDialog(
                initialPin = savedPin,
                onDismiss = {
                    showPinDialog = false
                },
                onSave = { pin ->
                    savedPin = pin
                    showPinDialog = false
                }
            )
        }
    }
}
