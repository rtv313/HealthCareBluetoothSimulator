package com.raul_t.myapplication.data.repository

import com.raul_t.myapplication.data.datasource.FakeHeartRateDataSource
import com.raul_t.myapplication.domain.model.HeartRate
import com.raul_t.myapplication.domain.repository.HeartRateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HeartRateRepositoryImpl @Inject constructor(
    private val dataSource: FakeHeartRateDataSource
) : HeartRateRepository {

    override fun observeHeartRate(): Flow<HeartRate> {
        return dataSource.currentBpm.map { bpm ->
            HeartRate(bpm = bpm)
        }
    }

    override suspend fun getHeartRate(): HeartRate {
        return HeartRate(
            bpm = dataSource.getCurrentHeartRate()
        )
    }

    override suspend fun setHeartRate(setBpmEnable: Boolean,bpm: Int) {
        dataSource.setHeartRate(setBpmEnable,bpm)
    }

    override suspend fun startBpm() {
        dataSource.startBpm()
    }

    override suspend fun stopBpm() {
        dataSource.stopBpm()
    }

    override fun observeIsBpmStarted(): Flow<Boolean> {
        return dataSource.isBpmStarted
    }
}