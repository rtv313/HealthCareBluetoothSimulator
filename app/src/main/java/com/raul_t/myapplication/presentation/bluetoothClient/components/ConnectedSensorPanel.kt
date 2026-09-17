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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raul_t.myapplication.R
import com.raul_t.myapplication.domain.model.SensorStatus
import com.raul_t.myapplication.presentation.heart.components.BpmDisplay
import com.raul_t.myapplication.ui.theme.LightSuccessGreen
import com.raul_t.myapplication.ui.theme.SuccessGreen

@Composable
fun ConnectedSensorPanel(
    mockBpm: Int,
    mockStatus: SensorStatus,
    mockName: String,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
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
                        text = stringResource(R.string.device_name_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when (mockName) {
                            "Connecting...", "Server Stopped" -> mockName
                            else -> mockName.ifBlank { stringResource(R.string.ble_default_device_name) }
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.sensor_status_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when(mockStatus) {
                            SensorStatus.Healthy -> stringResource(R.string.status_healthy)
                            SensorStatus.Damaged -> stringResource(R.string.status_damaged)
                            SensorStatus.Offline -> stringResource(R.string.status_offline)
                            SensorStatus.Disconnected -> stringResource(R.string.status_disconnected)
                        },
                        fontWeight = FontWeight.Bold,
                        color = when (mockStatus) {
                            SensorStatus.Healthy -> SuccessGreen
                            SensorStatus.Disconnected -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.error
                        },
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
                Text(text = androidx.compose.ui.res.stringResource(R.string.disconnect_button), fontWeight = FontWeight.Bold)
            }
        }
    }
}
