package com.raul_t.myapplication.presentation.bluetoothClient.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.raul_t.myapplication.R
import com.raul_t.myapplication.domain.model.DiscoveredBluetoothDevice
import com.raul_t.myapplication.ui.theme.SuccessGreen

@Composable
fun ConnectionDialog(
    device: DiscoveredBluetoothDevice,
    onConnect: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = androidx.compose.ui.res.stringResource(R.string.connect_to_device_title)) },
        text = { Text(text = androidx.compose.ui.res.stringResource(R.string.connect_dialog_message, device.name, device.address)) },
        confirmButton = {
            TextButton(onClick = onConnect) {
                Text(androidx.compose.ui.res.stringResource(R.string.start), color = SuccessGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(androidx.compose.ui.res.stringResource(R.string.cancel), color = MaterialTheme.colorScheme.error)
            }
        }
    )
}
