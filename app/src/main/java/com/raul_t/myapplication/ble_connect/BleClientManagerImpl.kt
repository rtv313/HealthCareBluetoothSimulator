package com.raul_t.myapplication.ble_connect

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.raul_t.myapplication.ble.GattServiceConstants
import com.raul_t.myapplication.domain.model.SensorStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [BleClientManager] that handles the GATT client lifecycle.
 * This class is responsible for connecting to remote BLE devices, discovering their services,
 * and subscribing to characteristic notifications.
 */
@Singleton
class BleClientManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BleClientManager {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    
    // The BluetoothGatt object represents the active connection to a remote device.
    private var bluetoothGatt: BluetoothGatt? = null

    // StateFlow to track the current connection lifecycle (Disconnected -> Connecting -> Connected).
    private val _connectionState = MutableStateFlow<BleClientConnectionState>(BleClientConnectionState.Disconnected)
    override val connectionState: StateFlow<BleClientConnectionState> = _connectionState.asStateFlow()

    // SharedFlow to emit received Heart Rate values in real-time.
    private val _heartRate = MutableSharedFlow<Int>()
    override val heartRate: SharedFlow<Int> = _heartRate.asSharedFlow()

    // SharedFlow to emit received Sensor Status updates.
    private val _sensorStatus = MutableSharedFlow<SensorStatus>()
    override val sensorStatus: SharedFlow<SensorStatus> = _sensorStatus.asSharedFlow()

    // Using Dispatchers.IO for background data processing and flow emission.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Core callback for all GATT client events.
     * All methods here are invoked by the Android Bluetooth system on a background binder thread.
     */
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            // status != GATT_SUCCESS usually means a timeout, out of range, or hardware error.
            if (status != BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = BleClientConnectionState.Error("Connection failed with status: $status")
                disconnect()
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Once connected, we MUST discover services before we can read/write data.
                _connectionState.value = BleClientConnectionState.Connected
                Log.d("BleClientManager", "Connected to GATT server, discovering services...")
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _connectionState.value = BleClientConnectionState.Disconnected
                Log.d("BleClientManager", "Disconnected from GATT server")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BleClientManager", "Services discovered")
                // Services are ready, now we can enable notifications for the data we want.
                enableNotifications(gatt)
            } else {
                Log.e("BleClientManager", "Service discovery failed with status: $status")
            }
        }

        /**
         * Triggered whenever a characteristic we are "subscribed" to sends a new value (Notify).
         */
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            when (characteristic?.uuid) {
                GattServiceConstants.HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID -> {
                    val value = characteristic.value
                    if (value != null && value.size >= 2) {
                        // Per BLE spec: byte 0 is flags, byte 1 is BPM (if 8-bit value format)
                        val bpm = value[1].toInt() and 0xFF
                        scope.launch { _heartRate.emit(bpm) }
                    }
                }
                GattServiceConstants.SENSOR_STATUS_CHARACTERISTIC_UUID -> {
                    val statusStr = characteristic.value?.toString(Charsets.UTF_8)
                    val status = try {
                        SensorStatus.valueOf(statusStr ?: "Healthy")
                    } catch (e: Exception) {
                        SensorStatus.Healthy
                    }
                    scope.launch { _sensorStatus.emit(status) }
                }
            }
        }
    }

    /**
     * Initiates a connection to a remote BLE device using its MAC address.
     */
    @SuppressLint("MissingPermission")
    override fun connect(address: String) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionState.value = BleClientConnectionState.Error("Bluetooth is disabled")
            return
        }

        val device = bluetoothAdapter.getRemoteDevice(address)
        if (device == null) {
            _connectionState.value = BleClientConnectionState.Error("Device not found")
            return
        }

        _connectionState.value = BleClientConnectionState.Connecting
        // autoConnect = false means connect immediately (better for initial manual connections).
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    /**
     * Disconnects the current GATT session and releases hardware resources.
     */
    @SuppressLint("MissingPermission")
    override fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = BleClientConnectionState.Disconnected
    }

    /**
     * Configures the remote device to start pushing data updates automatically (Notifications).
     * This involves two steps:
     * 1. Telling the local Android OS to listen for updates.
     * 2. Writing to the remote device's CCCD descriptor to turn on the "Notification" switch.
     */
    @SuppressLint("MissingPermission")
    private fun enableNotifications(gatt: BluetoothGatt?) {
        // --- Enable Heart Rate Notifications ---
        val hrService = gatt?.getService(GattServiceConstants.HEART_RATE_SERVICE_UUID)
        val hrChar = hrService?.getCharacteristic(GattServiceConstants.HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID)
        
        if (hrChar != null) {
            // Step 1: Tell Android OS to listen
            gatt.setCharacteristicNotification(hrChar, true)
            
            // Step 2: Tell the Remote Device to send (via the CCCD descriptor)
            val descriptor = hrChar.getDescriptor(GattServiceConstants.CCCD_UUID)
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
            Log.d("BleClientManager", "Enabled HR notifications")
        }

        // --- Enable Sensor Status Notifications ---
        val sensorService = gatt?.getService(GattServiceConstants.SIMULATOR_SERVICE_UUID)
        val statusChar = sensorService?.getCharacteristic(GattServiceConstants.SENSOR_STATUS_CHARACTERISTIC_UUID)
        
        if (statusChar != null) {
            gatt.setCharacteristicNotification(statusChar, true)
            val descriptor = statusChar.getDescriptor(GattServiceConstants.CCCD_UUID)
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
            Log.d("BleClientManager", "Enabled Status notifications")
        }
    }
}
