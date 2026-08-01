package com.raul_t.myapplication.data.datasource

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FakeHeartRateDataSource @Inject constructor() {

    private var currentBpm: Int = 0
    fun createNewHeartRate() {
        currentBpm = Random.nextInt(50, 101)
    }

    fun getCurrentHeartRate(): Int {
        return currentBpm
    }
}