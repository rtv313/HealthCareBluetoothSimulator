package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.domain.repository.HeartRateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveIsBpmStartedUseCase @Inject constructor(
    private val repository: HeartRateRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return repository.observeIsBpmStarted()
    }
}
