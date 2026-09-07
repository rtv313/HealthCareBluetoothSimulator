package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.service.HeartRate.HeartRateServiceManager
import javax.inject.Inject

class StopHeartRateServiceUseCase @Inject constructor(
    private val serviceManager: HeartRateServiceManager
) {
    operator fun invoke() {
        serviceManager.stopService()
    }
}
