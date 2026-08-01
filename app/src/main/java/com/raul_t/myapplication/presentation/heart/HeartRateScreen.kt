package com.raul_t.myapplication.presentation.heart

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HeartRateScreen(
    viewModel: HeartRateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    Text(text = "${uiState.bpm} BPM")
}