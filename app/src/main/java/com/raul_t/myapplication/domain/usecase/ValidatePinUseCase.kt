package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.domain.repository.BleConnectionRepository
import javax.inject.Inject

class ValidatePinUseCase @Inject constructor(
    private val repository: BleConnectionRepository
) {
    operator fun invoke(pin: String) {
        repository.validatePin(pin)
    }
}
