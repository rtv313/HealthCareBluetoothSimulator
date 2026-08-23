package com.raul_t.myapplication.data.datasource

import com.raul_t.myapplication.domain.model.SimulationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FakeHeartRateDataSource @Inject constructor() {

    private val _currentBpm = MutableStateFlow(0)
    val currentBpm: StateFlow<Int> = _currentBpm.asStateFlow()

    private val _config = MutableStateFlow(SimulationConfig())
    val config: StateFlow<SimulationConfig> = _config.asStateFlow()

    fun createNewHeartRate() {
        val currentConfig = _config.value
        if (!currentConfig.isBpmStarted) return

        if (!currentConfig.isFixBpmEnabled) {
            _currentBpm.value = Random.nextInt(50, 101)
        } else {
            val variance = if (currentConfig.varianceLower < currentConfig.varianceHigher) {
                Random.nextInt(currentConfig.varianceLower, currentConfig.varianceHigher + 1)
            } else {
                0
            }
            _currentBpm.value = currentConfig.targetBpm + variance
        }
    }

    fun getCurrentHeartRate(): Int {
        return _currentBpm.value
    }

    fun updateConfig(newConfig: SimulationConfig) {
        _config.value = newConfig
        
        // If fix BPM is enabled, update current BPM immediately to reflect the target
        if (newConfig.isFixBpmEnabled) {
            _currentBpm.value = newConfig.targetBpm
        }
    }
    
    fun updateConfig(update: (SimulationConfig) -> SimulationConfig) {
        _config.update(update)
        
        val newConfig = _config.value
        if (newConfig.isFixBpmEnabled) {
            _currentBpm.value = newConfig.targetBpm
        }
    }
}
