package com.raul_t.myapplication.ble_connect

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.raul_t.myapplication.R
import com.raul_t.myapplication.ble.BleConstants.DISCOVERY_CLEANUP_INTERVAL_MS
import com.raul_t.myapplication.ble.BleConstants.DISCOVERY_STALE_THRESHOLD_MS
import com.raul_t.myapplication.ble.GattServiceConstants
import com.raul_t.myapplication.domain.model.DiscoveredBluetoothDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class BleScannerManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BleScannerManager {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bleScanner get() = bluetoothAdapter?.bluetoothLeScanner

    // MutableStateFlow holds the real-time list of discovered devices.
    private val _discoveredDevices = MutableStateFlow<List<DiscoveredBluetoothDevice>>(emptyList())
    override val discoveredDevices: StateFlow<List<DiscoveredBluetoothDevice>> = _discoveredDevices.asStateFlow()

    private var isScanning = false
    private val scannerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var cleanupJob: Job? = null

    /**
     * The scan callback is triggered by the system whenever a BLE advertisement is detected.
     * This happens continuously while the scanner is running, so no manual refresh is needed.
     */
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            val device = result?.device ?: return
            val scanRecord = result.scanRecord
            
            // Prefer name from scan record, then device name, then unknown
            val deviceName = scanRecord?.deviceName ?: device.name ?: context.getString(R.string.ble_unknown_device)
            val targetName = context.getString(R.string.ble_default_device_name)
            
            // Filter: Only display our Health Sensor devices
            if (deviceName != targetName) {
                return
            }

            val deviceAddress = device.address
            val rssi = result.rssi

            Log.d("BleScannerManager", "Discovered Sensor: $deviceName [$deviceAddress] RSSI: $rssi")

            // Updating the StateFlow triggers an automatic UI refresh.
            _discoveredDevices.update { currentList ->
                val newDevice = DiscoveredBluetoothDevice(
                    name = deviceName, 
                    address = deviceAddress, 
                    rssi = rssi,
                    lastSeen = System.currentTimeMillis()
                )
                // We filter out any previous instance of this device (same MAC address)
                // and add the new one, then sort by signal strength (RSSI).
                (currentList.filterNot { it.address == deviceAddress } + newDevice)
                    .sortedByDescending { it.rssi }
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

        Log.d("BleScannerManager", "Starting BLE Scan...")

        _discoveredDevices.value = emptyList()
        isScanning = true

        startCleanupLoop()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            // Scanning with null filters to see if any devices show up.
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
        stopCleanupLoop()
    }

    override fun clearDiscoveredDevices() {
        _discoveredDevices.value = emptyList()
    }

    /**
     * Periodically checks the discovered devices list and removes entries 
     * that haven't broadcasted in more than 3 seconds.
     * We check every 1 second for near real-time pruning.
     */
    private fun startCleanupLoop() {
        cleanupJob?.cancel()
        cleanupJob = scannerScope.launch {
            while (isActive) {
                delay(DISCOVERY_CLEANUP_INTERVAL_MS)
                val now = System.currentTimeMillis()
                _discoveredDevices.update { currentList ->
                    currentList.filter { (now - it.lastSeen) < DISCOVERY_STALE_THRESHOLD_MS }
                }
            }
        }
    }

    private fun stopCleanupLoop() {
        cleanupJob?.cancel()
        cleanupJob = null
    }
}
