package com.raul_t.myapplication.domain.repository

import com.raul_t.myapplication.domain.model.HeartRate
import com.raul_t.myapplication.domain.model.SimulationConfig
import kotlinx.coroutines.flow.Flow

interface HeartRateRepository {

    fun observeHeartRate(): Flow<HeartRate>

    suspend fun getHeartRate(): HeartRate

    fun observeSimulationConfig(): Flow<SimulationConfig>

    suspend fun updateSimulationConfig(config: SimulationConfig)
}
