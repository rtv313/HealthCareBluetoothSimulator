package com.raul_t.myapplication.presentation.heart.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raul_t.myapplication.R
import com.raul_t.myapplication.ui.theme.SuccessGreen

@Composable
fun BpmStartStopButton(
    isStarted: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onToggle,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isStarted) Color.Red else SuccessGreen,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isStarted) {
                Icons.Default.Stop
            } else {
                Icons.Default.PlayArrow
            },
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))

        Text(
            text = if (isStarted) stringResource(R.string.stop) else stringResource(R.string.start)
        )
    }
}

@Preview
@Composable
fun BpmStartStopButtonPreview() {
    var isStarted by remember { mutableStateOf(false) }
    BpmStartStopButton(
        isStarted = isStarted,
        onToggle = { isStarted = !isStarted }
    )
}