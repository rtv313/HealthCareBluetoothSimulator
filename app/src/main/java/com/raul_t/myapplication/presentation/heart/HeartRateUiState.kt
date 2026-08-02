package com.raul_t.myapplication.presentation.heart

data class HeartRateUiState(
    val bpm: Int = 0,
    val isFixBpmEnabled: Boolean = false,
    val targetBpm: Int = 0
)