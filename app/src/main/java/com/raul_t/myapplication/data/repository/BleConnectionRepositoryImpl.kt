package com.raul_t.myapplication.data.repository

import com.raul_t.myapplication.data.datasource.BleConnectionDataSource
import com.raul_t.myapplication.domain.model.DiscoveredBluetoothDevice
import com.raul_t.myapplication.domain.repository.BleConnectionRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleConnectionRepositoryImpl @Inject constructor(
    private val dataSource: BleConnectionDataSource
) : BleConnectionRepository {

    override val discoveredDevices: StateFlow<List<DiscoveredBluetoothDevice>> = dataSource.discoveredDevices

    override fun startScanning() {
        dataSource.startScanning()
    }

    override fun stopScanning() {
        dataSource.stopScanning()
    }
}
