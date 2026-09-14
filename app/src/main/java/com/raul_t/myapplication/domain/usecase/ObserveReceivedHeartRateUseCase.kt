package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.domain.repository.BleConnectionRepository
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

class ObserveReceivedHeartRateUseCase @Inject constructor(
    private val repository: BleConnectionRepository
) {
    operator fun invoke(): SharedFlow<Int> {
        return repository.heartRate
    }
}
