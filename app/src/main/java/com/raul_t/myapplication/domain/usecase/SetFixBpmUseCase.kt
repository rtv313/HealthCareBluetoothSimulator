package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.domain.repository.HeartRateRepository
import javax.inject.Inject

class SetFixBpmUseCase @Inject constructor(private val repository: HeartRateRepository) {

    suspend operator fun invoke(setBpmEnable: Boolean, bpm: Int) {
        return repository.setHeartRate(setBpmEnable, bpm)
    }
}