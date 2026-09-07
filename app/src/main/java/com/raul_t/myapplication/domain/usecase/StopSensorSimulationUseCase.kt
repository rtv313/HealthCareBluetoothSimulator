package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.domain.repository.SensorRepository
import javax.inject.Inject

class StopSensorSimulationUseCase @Inject constructor(
    private val repository: SensorRepository
) {
    suspend operator fun invoke() {
        repository.updateSensorConfig { it.copy(start = false) }
    }
}
