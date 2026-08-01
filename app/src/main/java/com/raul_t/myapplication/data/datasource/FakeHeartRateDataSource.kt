package com.raul_t.myapplication.data.datasource

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeHeartRateDataSource @Inject constructor() {

    fun getHeartRate(): Int {
        return 72
    }
}