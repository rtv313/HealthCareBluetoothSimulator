package com.raul_t.myapplication.presentation.heart.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.raul_t.myapplication.ui.theme.SuccessGreen

@Preview
@Composable
fun BpmChangeRateControls() {
    var selectedInterval by remember { mutableIntStateOf(1000) }

    Column {
        Text("Velocidad de actualización")

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            MyRadioButton(
                selected = selectedInterval == 250,
                onClick = { selectedInterval = 250 }
            )
            Text("250 ms")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            MyRadioButton(
                selected = selectedInterval == 500,
                onClick = { selectedInterval = 500 }
            )
            Text("500 ms")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            MyRadioButton(
                selected = selectedInterval == 1000,
                onClick = { selectedInterval = 1000 }
            )
            Text("1000 ms")
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