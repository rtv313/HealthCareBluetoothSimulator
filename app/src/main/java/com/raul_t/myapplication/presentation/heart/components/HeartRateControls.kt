package com.raul_t.myapplication.presentation.heart.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HeartRateControls(
    isFixBpmEnabled: Boolean,
    targetBpm: Int,
    onFixBpmToggled: (Boolean) -> Unit,
    onBpmChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Fix BPM Simulation",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (isFixBpmEnabled) "Manual Control Active" else "Random Simulation Active",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isFixBpmEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            }
            Switch(
                checked = isFixBpmEnabled,
                onCheckedChange = onFixBpmToggled
            )
        }

        if (isFixBpmEnabled) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Target BPM", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "$targetBpm", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = targetBpm.toFloat(),
                    onValueChange = { onBpmChanged(it.toInt()) },
                    valueRange = 40f..200f,
                    steps = 160
                )
            }
        }
    }
}