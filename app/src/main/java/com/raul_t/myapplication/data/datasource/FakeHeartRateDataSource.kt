package com.raul_t.myapplication.data.datasource

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FakeHeartRateDataSource @Inject constructor() {

    private val _currentBpm = MutableStateFlow(0)
    val currentBpm: StateFlow<Int> = _currentBpm.asStateFlow()

    fun createNewHeartRate() {
        _currentBpm.value = Random.nextInt(50, 101)
    }

    fun getCurrentHeartRate(): Int {
        return _currentBpm.value
    }
}