package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.domain.repository.HeartRateRepository
import javax.inject.Inject

class StopBpmUseCase @Inject constructor(
    private val repository: HeartRateRepository
) {
    suspend operator fun invoke() {
        repository.stopBpm()
    }
}
