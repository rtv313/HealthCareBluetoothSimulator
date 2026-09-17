package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.domain.repository.BleConnectionRepository
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

class ObserveReceivedSensorNameUseCase @Inject constructor(
    private val repository: BleConnectionRepository
) {
    operator fun invoke(): SharedFlow<String> {
        return repository.sensorName
    }
}
