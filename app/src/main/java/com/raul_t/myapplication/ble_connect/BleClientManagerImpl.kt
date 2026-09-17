package com.raul_t.myapplication.ble_connect

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.util.Log
import com.raul_t.myapplication.ble.BleConstants.GATT_RETRY_DELAY_MS
import com.raul_t.myapplication.ble.BleConstants.GATT_SETTLE_DELAY_MS
import com.raul_t.myapplication.ble.BleConstants.GATT_ERROR_STALE
import com.raul_t.myapplication.ble.BleConstants.HEART_RATE_VALUE_INDEX
import com.raul_t.myapplication.ble.BleConstants.MIN_HEART_RATE_PACKET_SIZE
import com.raul_t.myapplication.ble.BleConstants.WATCHDOG_TIMEOUT_MS
import com.raul_t.myapplication.ble.GattServiceConstants
import com.raul_t.myapplication.domain.model.SensorStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
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
 */
@Singleton
class BleClientManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BleClientManager {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var bluetoothGatt: BluetoothGatt? = null

    private val _connectionState = MutableStateFlow<BleClientConnectionState>(BleClientConnectionState.Disconnected)
    override val connectionState: StateFlow<BleClientConnectionState> = _connectionState.asStateFlow()

    private val _heartRate = MutableSharedFlow<Int>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val heartRate: SharedFlow<Int> = _heartRate.asSharedFlow()

    private val _sensorStatus = MutableSharedFlow<SensorStatus>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val sensorStatus: SharedFlow<SensorStatus> = _sensorStatus.asSharedFlow()

    private val _sensorName = MutableSharedFlow<String>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val sensorName: SharedFlow<String> = _sensorName.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var watchdogJob: kotlinx.coroutines.Job? = null
    private var hasRetriedConfig = false

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.d("BleClientManager", "onConnectionStateChange: status=$status newState=$newState (Device: ${gatt?.device?.address})")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e("BleClientManager", "GATT Error detected. Status: $status. Disconnecting...")
                hasRetriedConfig = false
                _connectionState.value = BleClientConnectionState.Error("Connection failed with status: $status")
                disconnect()
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                hasRetriedConfig = false
                _connectionState.value = BleClientConnectionState.Connected
                Log.i("BleClientManager", "CONNECTED to GATT. Discovering services...")
                gatt?.discoverServices()
                startWatchdog()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.w("BleClientManager", "STATE_DISCONNECTED received from hardware.")
                hasRetriedConfig = false
                stopWatchdog()
                _connectionState.value = BleClientConnectionState.Disconnected
                // Note: We don't call disconnect() here because the system already disconnected us.
                // We just clean up our local reference if needed in disconnect() logic.
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("BleClientManager", "Services DISCOVERED. Starting configuration chain...")
                scope.launch {
                    kotlinx.coroutines.delay(GATT_SETTLE_DELAY_MS)
                    enableHeartRateNotifications(gatt)
                }
            } else {
                Log.e("BleClientManager", "Service discovery FAILED: status $status")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            val charUuid = descriptor?.characteristic?.uuid
            Log.d("BleClientManager", "onDescriptorWrite: desc=${descriptor?.uuid} char=$charUuid status=$status")

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (descriptor?.uuid == GattServiceConstants.CCCD_UUID) {
                    when (charUuid) {
                        GattServiceConstants.HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID -> {
                            Log.i("BleClientManager", "HR notifications ENABLED. Next: Status...")
                            enableStatusNotifications(gatt)
                        }
                        GattServiceConstants.SENSOR_STATUS_CHARACTERISTIC_UUID -> {
                            Log.i("BleClientManager", "Status notifications ENABLED. Next: Name...")
                            enableNameNotifications(gatt)
                        }
                        GattServiceConstants.SENSOR_NAME_CHARACTERISTIC_UUID -> {
                            Log.i("BleClientManager", "Name notifications ENABLED. Configuration complete. Fetching initial data...")
                            readInitialValues(gatt)
                        }
                    }
                }
            } else if (status == GATT_ERROR_STALE && !hasRetriedConfig) {
                Log.w("BleClientManager", "Descriptor write 133 ERROR. Retrying HR config in 500ms...")
                hasRetriedConfig = true
                scope.launch {
                    kotlinx.coroutines.delay(GATT_RETRY_DELAY_MS)
                    enableHeartRateNotifications(gatt)
                }
            } else {
                Log.e("BleClientManager", "Descriptor write FAILED: status $status")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            Log.v("BleClientManager", "onCharacteristicChanged (Modern API): char=${characteristic.uuid} value=${value.contentToString()}")
            resetWatchdog()
            processCharacteristicUpdate(characteristic, value)
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            if (characteristic != null) {
                Log.v("BleClientManager", "onCharacteristicChanged (Legacy API): char=${characteristic.uuid}")
                resetWatchdog()
                processCharacteristicUpdate(characteristic, characteristic.value)
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                resetWatchdog()
                processCharacteristicUpdate(characteristic, value)
            }
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic != null) {
                resetWatchdog()
                processCharacteristicUpdate(characteristic, characteristic.value)
            }
        }
    }

    private fun processCharacteristicUpdate(characteristic: BluetoothGattCharacteristic, value: ByteArray?) {
        if (value == null) {
            Log.w("BleClientManager", "Received NULL value for char ${characteristic.uuid}")
            return
        }

        when (characteristic.uuid) {
            GattServiceConstants.HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID -> {
                if (value.size >= MIN_HEART_RATE_PACKET_SIZE) {
                    val bpm = value[HEART_RATE_VALUE_INDEX].toInt() and 0xFF
                    Log.d("BleClientManager", "RECEIVED BPM: $bpm")
                    scope.launch { _heartRate.emit(bpm) }
                } else {
                    Log.w("BleClientManager", "HR packet too small: ${value.size} bytes")
                }
            }
            GattServiceConstants.SENSOR_STATUS_CHARACTERISTIC_UUID -> {
                val statusStr = String(value, Charsets.UTF_8)
                Log.d("BleClientManager", "RECEIVED Status: $statusStr")
                val status = try {
                    SensorStatus.valueOf(statusStr)
                } catch (e: Exception) {
                    Log.e("BleClientManager", "Failed to parse sensor status: $statusStr")
                    SensorStatus.Healthy
                }
                scope.launch { _sensorStatus.emit(status) }
            }
            GattServiceConstants.SENSOR_NAME_CHARACTERISTIC_UUID -> {
                val name = String(value, Charsets.UTF_8)
                Log.d("BleClientManager", "RECEIVED Name: $name")
                scope.launch { _sensorName.emit(name) }
            }
        }
    }

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
        Log.i("BleClientManager", "Initiating connection to $address")
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    override fun disconnect() {
        Log.i("BleClientManager", "Disconnecting GATT")
        stopWatchdog()
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = BleClientConnectionState.Disconnected
    }

    private fun startWatchdog() {
        Log.d("BleClientManager", "Watchdog STARTED ($WATCHDOG_TIMEOUT_MS ms timeout)")
        resetWatchdog()
    }

    private fun resetWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = scope.launch {
            kotlinx.coroutines.delay(WATCHDOG_TIMEOUT_MS)
            Log.w("BleClientManager", "WATCHDOG TIMEOUT! No data received for ${WATCHDOG_TIMEOUT_MS/1000}s. Triggering disconnection...")
            _connectionState.value = BleClientConnectionState.Disconnected
            disconnect()
        }
    }

    private fun stopWatchdog() {
        Log.d("BleClientManager", "Watchdog STOPPED")
        watchdogJob?.cancel()
        watchdogJob = null
    }

    @SuppressLint("MissingPermission")
    private fun readInitialValues(gatt: BluetoothGatt?) {
        val service = gatt?.getService(GattServiceConstants.SIMULATOR_SERVICE_UUID)
        
        // Read Name first
        val nameChar = service?.getCharacteristic(GattServiceConstants.SENSOR_NAME_CHARACTERISTIC_UUID)
        if (nameChar != null) {
            Log.d("BleClientManager", "Reading initial Name...")
            gatt.readCharacteristic(nameChar)
        }
        
        // Note: In some Android versions, you should wait for the first read to finish 
        // before starting the second one. However, most modern ones queue them.
        // We'll read status too.
        val statusChar = service?.getCharacteristic(GattServiceConstants.SENSOR_STATUS_CHARACTERISTIC_UUID)
        if (statusChar != null) {
            Log.d("BleClientManager", "Reading initial Status...")
            gatt.readCharacteristic(statusChar)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableHeartRateNotifications(gatt: BluetoothGatt?) {
        val service = gatt?.getService(GattServiceConstants.HEART_RATE_SERVICE_UUID)
        val char = service?.getCharacteristic(GattServiceConstants.HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID)
        if (char != null) {
            val success = gatt.setCharacteristicNotification(char, true)
            Log.d("BleClientManager", "setCharacteristicNotification (HR) result: $success")
            
            val desc = char.getDescriptor(GattServiceConstants.CCCD_UUID)
            if (desc != null) {
                Log.d("BleClientManager", "Requesting HR notification write to CCCD")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(desc, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION")
                    desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(desc)
                }
            } else {
                Log.e("BleClientManager", "HR CCCD descriptor not found!")
            }
        } else {
            Log.e("BleClientManager", "HR characteristic not found!")
            enableStatusNotifications(gatt)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableStatusNotifications(gatt: BluetoothGatt?) {
        val service = gatt?.getService(GattServiceConstants.SIMULATOR_SERVICE_UUID)
        val char = service?.getCharacteristic(GattServiceConstants.SENSOR_STATUS_CHARACTERISTIC_UUID)
        if (char != null) {
            val success = gatt.setCharacteristicNotification(char, true)
            Log.d("BleClientManager", "setCharacteristicNotification (Status) result: $success")
            
            val desc = char.getDescriptor(GattServiceConstants.CCCD_UUID)
            if (desc != null) {
                Log.d("BleClientManager", "Requesting Status notification write to CCCD")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(desc, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION")
                    desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(desc)
                }
            } else {
                Log.e("BleClientManager", "Status CCCD descriptor not found!")
            }
        } else {
            Log.e("BleClientManager", "Status characteristic not found!")
            enableNameNotifications(gatt)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableNameNotifications(gatt: BluetoothGatt?) {
        val service = gatt?.getService(GattServiceConstants.SIMULATOR_SERVICE_UUID)
        val char = service?.getCharacteristic(GattServiceConstants.SENSOR_NAME_CHARACTERISTIC_UUID)
        if (char != null) {
            val success = gatt.setCharacteristicNotification(char, true)
            Log.d("BleClientManager", "setCharacteristicNotification (Name) result: $success")
            
            val desc = char.getDescriptor(GattServiceConstants.CCCD_UUID)
            if (desc != null) {
                Log.d("BleClientManager", "Requesting Name notification write to CCCD")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(desc, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION")
                    desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(desc)
                }
            } else {
                Log.e("BleClientManager", "Name CCCD descriptor not found!")
            }
        } else {
            Log.e("BleClientManager", "Name characteristic not found!")
        }
    }
}
