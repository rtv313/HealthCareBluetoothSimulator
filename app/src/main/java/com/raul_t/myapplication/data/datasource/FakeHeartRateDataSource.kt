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

    private var setBpm = 0
    private var setBpmEnable = false

    private var startBpm = false

    fun createNewHeartRate() {
        if (!setBpmEnable) {
            _currentBpm.value = Random.nextInt(50, 101)
        }
        else {
            _currentBpm.value = setBpm
        }
    }

    fun getCurrentHeartRate(): Int {
        return _currentBpm.value
    }

    fun setHeartRate(setBpmEnable: Boolean,bpm: Int) {
        this.setBpmEnable = setBpmEnable
        setBpm = bpm

        if (setBpmEnable) {
            _currentBpm.value = setBpm
        }
    }

    fun startBpm() {
        startBpm = true
    }

    fun stopBpm() {
        startBpm = false
    }
}