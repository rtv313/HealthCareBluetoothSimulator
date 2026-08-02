package com.raul_t.myapplication.presentation.heart

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.raul_t.myapplication.presentation.heart.components.BpmDisplay
import com.raul_t.myapplication.presentation.heart.components.HeartRateControls

@Composable
fun HeartRateScreen(
    viewModel: HeartRateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            BpmDisplay(
                bpm = uiState.bpm,
                modifier = Modifier.weight(1f)
            )

            HorizontalDivider()

            HeartRateControls(
                isFixBpmEnabled = uiState.isFixBpmEnabled,
                targetBpm = uiState.targetBpm,
                onFixBpmToggled = { enabled ->
                    viewModel.setFixBpm(enabled, uiState.targetBpm)
                },
                onBpmChanged = { bpm ->
                    viewModel.setFixBpm(uiState.isFixBpmEnabled, bpm)
                }
            )
        }
    }
}