package com.raul_t.myapplication.presentation.bluetoothClient.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raul_t.myapplication.domain.model.SensorStatus
import com.raul_t.myapplication.presentation.bluetoothClient.DiscoveredBluetoothDevice
import com.raul_t.myapplication.presentation.heart.components.BpmDisplay
import com.raul_t.myapplication.ui.theme.LightSuccessGreen
import com.raul_t.myapplication.ui.theme.SuccessGreen

@Composable
fun ConnectedSensorPanel(
    connectedDevice: DiscoveredBluetoothDevice,
    mockBpm: Int,
    mockStatus: SensorStatus,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = LightSuccessGreen.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Device Name:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = connectedDevice.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Sensor Status:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when(mockStatus) {
                            SensorStatus.Healthy -> "Healthy"
                            SensorStatus.Damaged -> "Damaged"
                            SensorStatus.Offline -> "Offline"
                        },
                        fontWeight = FontWeight.Bold,
                        color = if (mockStatus == SensorStatus.Healthy) SuccessGreen else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            BpmDisplay(bpm = mockBpm)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDisconnect,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Disconnect", fontWeight = FontWeight.Bold)
            }
        }
    }
}
