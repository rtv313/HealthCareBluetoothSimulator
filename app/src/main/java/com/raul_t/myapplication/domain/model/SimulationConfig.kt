package com.raul_t.myapplication.domain.model

data class SimulationConfig(
    val isBpmStarted: Boolean = false,
    val isFixBpmEnabled: Boolean = false,
    val targetBpm: Int = 80,
    val varianceLower: Int = 0,
    val varianceHigher: Int = 0
)
