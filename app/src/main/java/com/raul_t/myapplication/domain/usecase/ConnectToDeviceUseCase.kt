package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.domain.repository.BleConnectionRepository
import javax.inject.Inject

class ConnectToDeviceUseCase @Inject constructor(
    private val repository: BleConnectionRepository
) {
    operator fun invoke(address: String) {
        repository.connect(address)
    }
}
