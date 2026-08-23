package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.domain.repository.HeartRateRepository
import javax.inject.Inject

class StartBpmUseCase @Inject constructor(private val repository: HeartRateRepository)  {
    suspend operator fun invoke() {
        return repository.setHeartRate(setBpmEnable, bpm)
    }
}