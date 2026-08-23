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
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raul_t.myapplication.R
import com.raul_t.myapplication.ui.theme.SuccessGreen
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixBpmControls(
    isFixBpmEnabled: Boolean,
    targetBpm: Int,
    bpmVarianceLower: Int,
    bpmVarianceHigher: Int,
    onFixBpmToggled: (Boolean) -> Unit,
    onBpmChanged: (Int) -> Unit,
    onVarianceChanged: (Int, Int) -> Unit,
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
                    text = stringResource(R.string.fix_bpm_simulation),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (isFixBpmEnabled) stringResource(R.string.manual_control_active) else stringResource(R.string.random_simulation_active),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isFixBpmEnabled) SuccessGreen else MaterialTheme.colorScheme.onSurface,
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
                    Text(text = stringResource(R.string.target_bpm), style = MaterialTheme.typography.bodyMedium)
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
                RangeSliderBpmVariance(
                    lower = bpmVarianceLower,
                    higher = bpmVarianceHigher,
                    onVarianceChanged = onVarianceChanged
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangeSliderBpmVariance(
    lower: Int,
    higher: Int,
    onVarianceChanged: (Int, Int) -> Unit
) {
    val sliderPosition = lower.toFloat()..higher.toFloat()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = stringResource(id = R.string.bpm_variance_range, lower, higher),
             style = MaterialTheme.typography.bodyMedium)

        RangeSlider(
            startThumb = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            color = SuccessGreen,
                        )
                )
            },
            endThumb =  {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            color = SuccessGreen,
                        )
                )
            },
            value = sliderPosition,
            onValueChange = { onVarianceChanged(it.start.toInt(), it.endInclusive.toInt()) },
            valueRange = 0f..30f,
            steps = 30,
            enabled = true,
            colors = SliderDefaults.colors(
                activeTrackColor = SuccessGreen,
                inactiveTrackColor = SuccessGreen.copy(alpha = 0.24f),
                activeTickColor = SuccessGreen,
                inactiveTickColor = SuccessGreen.copy(alpha = 0.24f),
            )
        )
    }
}