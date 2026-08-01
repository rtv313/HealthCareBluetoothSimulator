package com.raul_t.myapplication.data.repository

import com.raul_t.myapplication.data.datasource.FakeHeartRateDataSource
import com.raul_t.myapplication.domain.model.HeartRate
import com.raul_t.myapplication.domain.repository.HeartRateRepository
import javax.inject.Inject

class HeartRateRepositoryImpl @Inject constructor(
    private val dataSource: FakeHeartRateDataSource
) : HeartRateRepository {

    override suspend fun getHeartRate(): HeartRate {

        return HeartRate(
            bpm = dataSource.getCurrentHeartRate()
        )

    }
}