package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.domain.model.SimulationConfig
import com.raul_t.myapplication.domain.repository.HeartRateRepository
import javax.inject.Inject

class UpdateSimulationConfigUseCase @Inject constructor(
    private val repository: HeartRateRepository
) {
    suspend operator fun invoke(config: SimulationConfig) {
        repository.updateSimulationConfig(config)
    }
}
