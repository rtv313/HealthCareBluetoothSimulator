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

    private val _bpmVarianceLower = MutableStateFlow(0)
    val bpmVarianceLower: StateFlow<Int> = _bpmVarianceLower.asStateFlow()

    private val _bpmVarianceHigher = MutableStateFlow(0)
    val bpmVarianceHigher: StateFlow<Int> = _bpmVarianceHigher.asStateFlow()


    private val _isBpmStarted = MutableStateFlow(false)
    val isBpmStarted: StateFlow<Boolean> = _isBpmStarted.asStateFlow()


    fun createNewHeartRate() {
        if (!isBpmStarted.value) return

        if (!setBpmEnable) {
            _currentBpm.value = Random.nextInt(50, 101)
        } else {
            _currentBpm.value =
                setBpm + Random.nextInt(bpmVarianceLower.value, bpmVarianceHigher.value)
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
        _isBpmStarted.value = true
    }

    fun stopBpm() {
        _isBpmStarted.value = false
    }

    fun setBpmVariance(bpmVarianceLower: Int, bpmVarianceHigher: Int) {
        _bpmVarianceLower.value = bpmVarianceLower
        _bpmVarianceHigher.value = bpmVarianceHigher
    }
}