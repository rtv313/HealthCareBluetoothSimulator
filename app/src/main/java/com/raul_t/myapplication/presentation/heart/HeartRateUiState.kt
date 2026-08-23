package com.raul_t.myapplication.presentation.heart

data class HeartRateUiState(
    val bpm: Int = 0,
    val isFixBpmEnabled: Boolean = false,
    val targetBpm: Int = 0,
    val bpmVarianceLower: Int = 0,
    val bpmVarianceHigher: Int = 0,
    val isBpmStarted: Boolean = false,
    val updateIntervalMs: Long = 1000L
)