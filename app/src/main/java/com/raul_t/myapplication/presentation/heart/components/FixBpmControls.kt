package com.raul_t.myapplication.presentation.heart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raul_t.myapplication.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixBpmControls(
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
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (isFixBpmEnabled) "Manual Control Active" else "Random Simulation Active",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isFixBpmEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
            }
            Switch(
                checked = isFixBpmEnabled,
                onCheckedChange = onFixBpmToggled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SuccessGreen,
                    checkedTrackColor = Color.White,
                    checkedBorderColor = SuccessGreen,
                    uncheckedThumbColor = SuccessGreen.copy(alpha = 0.24f),
                    uncheckedTrackColor = SuccessGreen.copy(alpha = 0.24f),
                    uncheckedBorderColor = SuccessGreen.copy(alpha = 0.24f)
                )
            )
        }

        if (isFixBpmEnabled) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Target BPM", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "$targetBpm", fontSize = 24.sp, style = MaterialTheme.typography.bodyMedium)
                }
                Slider(
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    color = SuccessGreen,
                                )
                        )
                    },
                    value = targetBpm.toFloat(),
                    onValueChange = { onBpmChanged(it.toInt()) },
                    valueRange = 40f..200f,
                    steps = 160,
                    colors = SliderDefaults.colors(
                        activeTrackColor = SuccessGreen,
                        inactiveTrackColor = SuccessGreen.copy(alpha = 0.24f),
                        activeTickColor = SuccessGreen,
                        inactiveTickColor = SuccessGreen.copy(alpha = 0.24f),
                    )
                )
            }
        }
    }
}

