package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.domain.model.SimulationConfig
import com.raul_t.myapplication.domain.repository.HeartRateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSimulationConfigUseCase @Inject constructor(
    private val repository: HeartRateRepository
) {
    operator fun invoke(): Flow<SimulationConfig> {
        return repository.observeSimulationConfig()
    }
}
