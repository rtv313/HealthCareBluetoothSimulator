package com.raul_t.myapplication.domain.usecase

import com.raul_t.myapplication.domain.model.DiscoveredBluetoothDevice
import com.raul_t.myapplication.domain.repository.BleConnectionRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveDiscoveredDevicesUseCase @Inject constructor(
    private val repository: BleConnectionRepository
) {
    operator fun invoke(): StateFlow<List<DiscoveredBluetoothDevice>> {
        return repository.discoveredDevices
    }
}
