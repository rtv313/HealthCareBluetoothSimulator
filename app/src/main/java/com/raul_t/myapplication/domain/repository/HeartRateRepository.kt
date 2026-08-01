package com.raul_t.myapplication.domain.repository

import com.raul_t.myapplication.domain.model.HeartRate

interface HeartRateRepository {

    suspend fun getHeartRate(): HeartRate

}