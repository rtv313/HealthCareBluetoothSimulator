package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.domain.repository.HeartRateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBpmVarianceLowerUseCase @Inject constructor(
    private val repository: HeartRateRepository
) {
    operator fun invoke(): Flow<Int> {
        return repository.observeBpmVarianceLower()
    }
}
