package com.raul_t.myapplication.presentation.heart

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raul_t.myapplication.R
import com.raul_t.myapplication.presentation.heart.components.BpmChangeRateControls
import com.raul_t.myapplication.presentation.heart.components.BpmDisplay
import com.raul_t.myapplication.presentation.heart.components.BpmStartStopButton
import com.raul_t.myapplication.presentation.heart.components.FixBpmControls

import androidx.compose.ui.tooling.preview.Preview
import com.raul_t.myapplication.ui.theme.MyApplicationTheme

@Composable
fun HeartRateScreen(
    viewModel: HeartRateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HeartRateContent(
        uiState = uiState,
        onStartBpm = viewModel::startBpm,
        onStopBpm = viewModel::stopBpm,
        onSetFixBpm = viewModel::setFixBpm,
        onSetBpmVariance = viewModel::setBpmVariance,
        onSetUpdateInterval = viewModel::setUpdateInterval
    )
}

@Composable
fun HeartRateContent(
    uiState: HeartRateUiState,
    onStartBpm: () -> Unit,
    onStopBpm: () -> Unit,
    onSetFixBpm: (Boolean, Int) -> Unit,
    onSetBpmVariance: (Int, Int) -> Unit,
    onSetUpdateInterval: (Long) -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.heart_rate_monitor_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                BpmDisplay(
                    bpm = uiState.bpm,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                BpmStartStopButton(
                    isStarted = uiState.isBpmStarted,
                    onToggle = {
                        if (uiState.isBpmStarted) {
                            onStopBpm()
                        } else {
                            onStartBpm()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider()

                FixBpmControls(
                    isFixBpmEnabled = uiState.isFixBpmEnabled,
                    targetBpm = uiState.targetBpm,
                    bpmVarianceLower = uiState.bpmVarianceLower,
                    bpmVarianceHigher = uiState.bpmVarianceHigher,
                    onFixBpmToggled = { enabled ->
                        onSetFixBpm(enabled, uiState.targetBpm)
                    },
                    onBpmChanged = { bpm ->
                        onSetFixBpm(uiState.isFixBpmEnabled, bpm)
                    },
                    onVarianceChanged = onSetBpmVariance
                )
            }

            HorizontalDivider()

            BpmChangeRateControls(
                selectedInterval = uiState.updateIntervalMs,
                onIntervalChanged = onSetUpdateInterval
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HeartRateScreenPreview() {
    MyApplicationTheme {
        HeartRateContent(
            uiState = HeartRateUiState(
                bpm = 75,
                isBpmStarted = true,
                isFixBpmEnabled = false,
                targetBpm = 80,
                bpmVarianceLower = 5,
                bpmVarianceHigher = 10,
                updateIntervalMs = 1000L
            ),
            onStartBpm = {},
            onStopBpm = {},
            onSetFixBpm = { _, _ -> },
            onSetBpmVariance = { _, _ -> },
            onSetUpdateInterval = {}
        )
    }
}