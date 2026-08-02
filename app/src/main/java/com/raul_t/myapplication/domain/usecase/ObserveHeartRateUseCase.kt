package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.domain.model.HeartRate
import com.raul_t.myapplication.domain.repository.HeartRateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveHeartRateUseCase @Inject constructor(
    private val repository: HeartRateRepository
) {

    operator fun invoke(): Flow<HeartRate> {
        return repository.observeHeartRate()
    }
}