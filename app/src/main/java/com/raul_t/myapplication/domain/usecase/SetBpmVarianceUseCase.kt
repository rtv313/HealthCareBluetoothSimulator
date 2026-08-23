package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.domain.repository.HeartRateRepository
import javax.inject.Inject

class SetBpmVarianceUseCase @Inject constructor(private val repository: HeartRateRepository) {
    suspend operator fun invoke(lower: Int, higher: Int) {
        repository.setBpmVariance(lower, higher)
    }
}