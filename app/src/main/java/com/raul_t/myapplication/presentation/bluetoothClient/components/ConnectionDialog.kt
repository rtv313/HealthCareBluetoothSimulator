package com.raul_t.myapplication.presentation.bluetoothClient.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.raul_t.myapplication.presentation.bluetoothClient.DiscoveredBluetoothDevice
import com.raul_t.myapplication.ui.theme.SuccessGreen

@Composable
fun ConnectionDialog(
    device: DiscoveredBluetoothDevice,
    onConnect: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Connect to Device") },
        text = { Text(text = "Do you want to connect to ${device.name} (${device.address})?") },
        confirmButton = {
            TextButton(onClick = onConnect) {
                Text("Connect", color = SuccessGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}
