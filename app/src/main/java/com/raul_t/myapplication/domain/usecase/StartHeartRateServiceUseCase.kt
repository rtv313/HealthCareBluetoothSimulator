package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.service.HeartRateServiceManager
import javax.inject.Inject

class StartHeartRateServiceUseCase @Inject constructor(
    private val serviceManager: HeartRateServiceManager
) {
    operator fun invoke() {
        serviceManager.startService()
    }
}
