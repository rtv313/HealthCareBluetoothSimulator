package com.raul_t.myapplication.ble_connect

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import com.raul_t.myapplication.R
import com.raul_t.myapplication.domain.model.DiscoveredBluetoothDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleScannerManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BleScannerManager {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bleScanner get() = bluetoothAdapter?.bluetoothLeScanner

    // MutableStateFlow holds the real-time list of discovered devices.
    // Because this is a Flow, any update to this list will automatically notify observers.
    private val _discoveredDevices = MutableStateFlow<List<DiscoveredBluetoothDevice>>(emptyList())
    override val discoveredDevices: StateFlow<List<DiscoveredBluetoothDevice>> = _discoveredDevices.asStateFlow()

    private var isScanning = false

    /**
     * The scan callback is triggered by the system whenever a BLE advertisement is detected.
     * This happens continuously while the scanner is running, so no manual refresh is needed.
     */
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            val device = result?.device ?: return
            val deviceName = device.name ?: context.getString(R.string.ble_unknown_device)
            val deviceAddress = device.address
            val rssi = result.rssi

            // Updating the StateFlow triggers an automatic UI refresh.
            _discoveredDevices.update { currentList ->
                val alreadyExists = currentList.any { it.address == deviceAddress }
                if (alreadyExists) {
                    // Update existing device (e.g., signal strength changes)
                    currentList.map {
                        if (it.address == deviceAddress) it.copy(name = deviceName, rssi = rssi) else it
                    }
                } else {
                    // Add new device to the list
                    currentList + DiscoveredBluetoothDevice(deviceName, deviceAddress, rssi)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BleScannerManager", "Scan failed with error code: $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    override fun startScanning() {
        if (isScanning) {
            Log.d("BleScannerManager", "Already scanning, ignoring start request")
            return
        }
        if (bleScanner == null) {
            Log.e("BleScannerManager", "Cannot start scan: bleScanner is null. Bluetooth might be OFF or adapter not available.")
            return
        }

        _discoveredDevices.value = emptyList()
        isScanning = true
        Log.d("BleScannerManager", "Starting BLE Scan...")

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            bleScanner?.startScan(null, settings, scanCallback)
        } catch (e: Exception) {
            Log.e("BleScannerManager", "Error starting scan", e)
            isScanning = false
        }
    }

    @SuppressLint("MissingPermission")
    override fun stopScanning() {
        if (!isScanning || bleScanner == null) return

        isScanning = false
        Log.d("BleScannerManager", "Stopping BLE Scan...")
        bleScanner?.stopScan(scanCallback)
    }
}
