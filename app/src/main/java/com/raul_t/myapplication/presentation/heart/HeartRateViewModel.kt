package com.raul_t.myapplication.presentation.heart

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raul_t.myapplication.domain.model.SimulationConfig
import com.raul_t.myapplication.domain.usecase.ObserveHeartRateUseCase
import com.raul_t.myapplication.domain.usecase.ObserveSimulationConfigUseCase
import com.raul_t.myapplication.domain.usecase.UpdateSimulationConfigUseCase
import com.raul_t.myapplication.service.HeartRateForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeartRateViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val observeHeartRateUseCase: ObserveHeartRateUseCase,
    private val observeSimulationConfigUseCase: ObserveSimulationConfigUseCase,
    private val updateSimulationConfigUseCase: UpdateSimulationConfigUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HeartRateUiState())
    val uiState = _uiState.asStateFlow()

    private var currentConfig = SimulationConfig()

    init {
        val intent = Intent(context, HeartRateForegroundService::class.java)
        ContextCompat.startForegroundService(context, intent)

        viewModelScope.launch {
            observeHeartRateUseCase().collect { heartRate ->
                _uiState.update { it.copy(bpm = heartRate.bpm) }
            }
        }

        viewModelScope.launch {
            observeSimulationConfigUseCase().collect { config ->
                currentConfig = config
                _uiState.update { 
                    it.copy(
                        isBpmStarted = config.isBpmStarted,
                        isFixBpmEnabled = config.isFixBpmEnabled,
                        targetBpm = config.targetBpm,
                        bpmVarianceLower = config.varianceLower,
                        bpmVarianceHigher = config.varianceHigher,
                        updateIntervalMs = config.updateIntervalMs
                    )
                }
            }
        }
    }

    fun startBpm() {
        updateConfig(currentConfig.copy(isBpmStarted = true))
    }

    fun stopBpm() {
        updateConfig(currentConfig.copy(isBpmStarted = false))
    }

    fun setFixBpm(enabled: Boolean, bpm: Int) {
        updateConfig(currentConfig.copy(isFixBpmEnabled = enabled, targetBpm = bpm))
    }

    fun setBpmVariance(lower: Int, higher: Int) {
        updateConfig(currentConfig.copy(varianceLower = lower, varianceHigher = higher))
    }

    fun setUpdateInterval(intervalMs: Long) {
        updateConfig(currentConfig.copy(updateIntervalMs = intervalMs))
    }

    private fun updateConfig(config: SimulationConfig) {
        viewModelScope.launch {
            updateSimulationConfigUseCase(config)
        }
    }
}
