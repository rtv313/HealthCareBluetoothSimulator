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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raul_t.myapplication.ui.theme.SuccessGreen

@Preview
@Composable
fun BpmChangeRateControls() {
    var selectedInterval by remember { mutableIntStateOf(1000) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Text("BPM Change Rate",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,)

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            MyRadioButton(
                selected = selectedInterval == 1000,
                onClick = { selectedInterval = 1000 }
            )
            Text("1 second")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            MyRadioButton(
                selected = selectedInterval == 10000,
                onClick = { selectedInterval = 10000 }
            )
            Text("10 seconds")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            MyRadioButton(
                selected = selectedInterval == 60000,
                onClick = { selectedInterval = 60000 }
            )
            Text("1 minute")
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
            selectedColor = SuccessGreen,
            unselectedColor = Color.Gray
        )
    )
}