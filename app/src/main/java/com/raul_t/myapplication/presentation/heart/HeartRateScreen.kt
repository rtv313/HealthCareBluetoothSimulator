package com.raul_t.myapplication.presentation.heart

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raul_t.myapplication.presentation.heart.components.BpmDisplay
import com.raul_t.myapplication.presentation.heart.components.FixBpmControls

@Composable
fun HeartRateScreen(
    viewModel: HeartRateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .statusBarsPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Heart Rate Monitor",
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

            HorizontalDivider()

            FixBpmControls(
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