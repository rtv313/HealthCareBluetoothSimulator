package com.raul_t.myapplication.domain.repository

import com.raul_t.myapplication.domain.model.HeartRate
import kotlinx.coroutines.flow.Flow

interface HeartRateRepository {

    fun observeHeartRate(): Flow<HeartRate>

    suspend fun getHeartRate(): HeartRate

    suspend fun setHeartRate(setBpmEnable: Boolean,bpm: Int)

    suspend fun startBpm()

    suspend fun stopBpm()

    fun observeIsBpmStarted(): Flow<Boolean>
}