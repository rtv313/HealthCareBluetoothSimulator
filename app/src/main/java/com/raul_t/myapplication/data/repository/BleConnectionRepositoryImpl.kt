package com.raul_t.myapplication.data.repository

import com.raul_t.myapplication.ble_connect.BleClientConnectionState
import com.raul_t.myapplication.data.datasource.BleConnectionDataSource
import com.raul_t.myapplication.domain.model.DiscoveredBluetoothDevice
import com.raul_t.myapplication.domain.model.SensorStatus
import com.raul_t.myapplication.domain.repository.BleConnectionRepository
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleConnectionRepositoryImpl @Inject constructor(
    private val dataSource: BleConnectionDataSource
) : BleConnectionRepository {

    override val discoveredDevices: StateFlow<List<DiscoveredBluetoothDevice>> = dataSource.discoveredDevices
    override val connectionState: StateFlow<BleClientConnectionState> = dataSource.connectionState
    override val heartRate: SharedFlow<Int> = dataSource.heartRate
    override val sensorStatus: SharedFlow<SensorStatus> = dataSource.sensorStatus
    override val sensorName: SharedFlow<String> = dataSource.sensorName

    override fun startScanning() {
        dataSource.startScanning()
    }

    override fun stopScanning() {
        dataSource.stopScanning()
    }

    override fun clearDiscoveredDevices() {
        dataSource.clearDiscoveredDevices()
    }

    override fun connect(address: String) {
        dataSource.connect(address)
    }

    override fun disconnect() {
        dataSource.disconnect()
    }
}
