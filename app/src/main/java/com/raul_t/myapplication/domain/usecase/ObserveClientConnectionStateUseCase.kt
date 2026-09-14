package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.ble_connect.BleClientConnectionState
import com.raul_t.myapplication.domain.repository.BleConnectionRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveClientConnectionStateUseCase @Inject constructor(
    private val repository: BleConnectionRepository
) {
    operator fun invoke(): StateFlow<BleClientConnectionState> {
        return repository.connectionState
    }
}
