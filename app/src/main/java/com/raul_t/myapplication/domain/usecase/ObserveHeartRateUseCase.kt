package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.domain.model.HeartRate
import com.raul_t.myapplication.domain.repository.HeartRateRepository
import javax.inject.Inject

class ObserveHeartRateUseCase @Inject constructor(
    private val repository: HeartRateRepository
) {

    suspend operator fun invoke(): HeartRate {

        return repository.getHeartRate()

    }
}