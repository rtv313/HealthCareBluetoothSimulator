package com.raul_t.myapplication.presentation.heart.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raul_t.myapplication.R

@Composable
fun BpmChangeRateControls(
    selectedInterval: Long,
    onIntervalChanged: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.bpm_change_rate),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            MyRadioButton(
                selected = selectedInterval == 1000L,
                onClick = { onIntervalChanged(1000L) }
            )
            Text(stringResource(R.string.interval_1_second))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            MyRadioButton(
                selected = selectedInterval == 10000L,
                onClick = { onIntervalChanged(10000L) }
            )
            Text(stringResource(R.string.interval_10_seconds))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            MyRadioButton(
                selected = selectedInterval == 60000L,
                onClick = { onIntervalChanged(60000L) }
            )
            Text(stringResource(R.string.interval_1_minute))
        }
    }
}

@Composable
fun MyRadioButton(
    selected: Boolean,
    onClick: () -> Unit
) {
    RadioButton(
        selected = selected,
        onClick = onClick,
        colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.outline,
            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Preview(showBackground = true)
@Composable
fun BpmChangeRateControlsPreview() {
    BpmChangeRateControls(
        selectedInterval = 1000L,
        onIntervalChanged = {}
    )
}
