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
 * Acts as the Bluetooth Central device which scans for, connects to, and handles data transfers
 * from the Peripheral server.
 */
@Singleton
class BleClientManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BleClientManager {

    companion object {
        // Tag constant derived from the file name to fulfill logging refactoring requirements.
        private const val TAG = "BleClientManagerImpl"
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var bluetoothGatt: BluetoothGatt? = null

    private val _connectionState = MutableStateFlow<BleClientConnectionState>(BleClientConnectionState.Disconnected)
    override val connectionState: StateFlow<BleClientConnectionState> = _connectionState.asStateFlow()

    // Replay flows preserving the most recent telemetry packets for new interface subscribers.
    private val _heartRate = MutableSharedFlow<Int>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val heartRate: SharedFlow<Int> = _heartRate.asSharedFlow()

    private val _sensorStatus = MutableSharedFlow<SensorStatus>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val sensorStatus: SharedFlow<SensorStatus> = _sensorStatus.asSharedFlow()

    private val _sensorName = MutableSharedFlow<String>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val sensorName: SharedFlow<String> = _sensorName.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var watchdogJob: kotlinx.coroutines.Job? = null
    private var hasRetriedConfig = false

    /**
     * Core asynchronous listener returning lower-level framework events from the Android BLE Bluetooth stack.
     */
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.d(TAG, "onConnectionStateChange: status=$status newState=$newState (Device: ${gatt?.device?.address})")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "GATT Error detected. Status: $status. Disconnecting...")
                hasRetriedConfig = false
                _connectionState.value = BleClientConnectionState.Error("Connection failed with status: $status")
                disconnect()
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                hasRetriedConfig = false
                Log.i(TAG, "CONNECTED to GATT. Discovering services...")
                gatt?.discoverServices() // Discover characteristics offered by the peripheral.
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.w(TAG, "STATE_DISCONNECTED received from hardware.")
                hasRetriedConfig = false
                stopWatchdog()
                _connectionState.value = BleClientConnectionState.Disconnected
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Services DISCOVERED. Checking if PIN is required...")
                scope.launch {
                    kotlinx.coroutines.delay(GATT_SETTLE_DELAY_MS)
                    checkIfPinRequired(gatt) // Assess secure security status first before streaming.
                }
            } else {
                Log.e(TAG, "Service discovery FAILED: status $status")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            val charUuid = descriptor?.characteristic?.uuid
            Log.d(TAG, "onDescriptorWrite: desc=${descriptor?.uuid} char=$charUuid status=$status")

            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Sequentially chain configuration descriptors (CCCD) writes to safely initialize multiple notifications.
                if (descriptor?.uuid == GattServiceConstants.CCCD_UUID) {
                    when (charUuid) {
                        GattServiceConstants.HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID -> {
                            Log.i(TAG, "HR notifications ENABLED. Next: Status...")
                            enableStatusNotifications(gatt)
                        }
                        GattServiceConstants.SENSOR_STATUS_CHARACTERISTIC_UUID -> {
                            Log.i(TAG, "Status notifications ENABLED. Next: Name...")
                            enableNameNotifications(gatt)
                        }
                        GattServiceConstants.SENSOR_NAME_CHARACTERISTIC_UUID -> {
                            Log.i(TAG, "Name notifications ENABLED. Configuration complete. Fetching initial data...")
                            readInitialValues(gatt)
                        }
                    }
                }
            } else if (status == GATT_ERROR_STALE && !hasRetriedConfig) {
                Log.w(TAG, "Descriptor write 133 ERROR. Retrying HR config in 500ms...")
                hasRetriedConfig = true
                scope.launch {
                    kotlinx.coroutines.delay(GATT_RETRY_DELAY_MS)
                    enableHeartRateNotifications(gatt)
                }
            } else {
                Log.e(TAG, "Descriptor write FAILED: status $status")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            Log.v(TAG, "onCharacteristicChanged (Modern API): char=${characteristic.uuid} value=${value.contentToString()}")
            resetWatchdog() // Keep connection open as telemetry arrives.
            processCharacteristicUpdate(characteristic, value)
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            if (characteristic != null) {
                Log.v(TAG, "onCharacteristicChanged (Legacy API): char=${characteristic.uuid}")
                resetWatchdog()
                processCharacteristicUpdate(characteristic, characteristic.value)
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                resetWatchdog()
                if (characteristic.uuid == GattServiceConstants.PIN_REQUIRED_CHARACTERISTIC_UUID) {
                    val isRequired = value.firstOrNull() == 0x01.toByte()
                    Log.d(TAG, "PIN Required read result (modern): $isRequired")
                    if (isRequired) {
                        _connectionState.value = BleClientConnectionState.WaitingForPin
                    } else {
                        _connectionState.value = BleClientConnectionState.Connected
                        enableHeartRateNotifications(gatt)
                        startWatchdog()
                    }
                    return
                }
                processCharacteristicUpdate(characteristic, value)
            }
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic != null) {
                resetWatchdog()
                if (characteristic.uuid == GattServiceConstants.PIN_REQUIRED_CHARACTERISTIC_UUID) {
                    val isRequired = characteristic.value?.firstOrNull() == 0x01.toByte()
                    Log.d(TAG, "PIN Required read result (legacy): $isRequired")
                    if (isRequired) {
                        _connectionState.value = BleClientConnectionState.WaitingForPin
                    } else {
                        _connectionState.value = BleClientConnectionState.Connected
                        enableHeartRateNotifications(gatt)
                        startWatchdog()
                    }
                    return
                }
                processCharacteristicUpdate(characteristic, characteristic.value)
            }
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (gatt != null && characteristic != null) {
                handlePinValidationResult(gatt, characteristic, status)
            }
        }

        private fun handlePinValidationResult(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            val uuid = characteristic.uuid
            Log.d(TAG, "onCharacteristicWrite: char=$uuid status=$status")
            if (uuid == GattServiceConstants.PIN_VALIDATION_CHARACTERISTIC_UUID) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "PIN validated SUCCESSFULLY. Proceeding to notifications...")
                    _connectionState.value = BleClientConnectionState.Connected
                    enableHeartRateNotifications(gatt)
                    startWatchdog()
                } else {
                    Log.w(TAG, "PIN validation FAILED with status $status")
                    _connectionState.value = BleClientConnectionState.InvalidPin
                }
            }
        }
    }

    /**
     * Reads security configurations from peripheral attributes to decide if a PIN entry flow is required.
     */
    @SuppressLint("MissingPermission")
    private fun checkIfPinRequired(gatt: BluetoothGatt?) {
        val service = gatt?.getService(GattServiceConstants.SIMULATOR_SERVICE_UUID)
        val pinReqChar = service?.getCharacteristic(GattServiceConstants.PIN_REQUIRED_CHARACTERISTIC_UUID)
        if (pinReqChar != null) {
            Log.d(TAG, "Reading PIN required characteristic...")
            gatt.readCharacteristic(pinReqChar)
        } else {
            Log.w(TAG, "PIN required characteristic not found, assuming PIN not required.")
            _connectionState.value = BleClientConnectionState.Connected
            enableHeartRateNotifications(gatt)
            startWatchdog()
        }
    }

    /**
     * De-serializes incoming characteristic byte arrays back into typed system model representations.
     */
    private fun processCharacteristicUpdate(characteristic: BluetoothGattCharacteristic, value: ByteArray?) {
        if (value == null) {
            Log.w(TAG, "Received NULL value for char ${characteristic.uuid}")
            return
        }

        when (characteristic.uuid) {
            GattServiceConstants.HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID -> {
                if (value.size >= MIN_HEART_RATE_PACKET_SIZE) {
                    val bpm = value[HEART_RATE_VALUE_INDEX].toInt() and 0xFF
                    Log.d(TAG, "RECEIVED BPM: $bpm")
                    scope.launch { _heartRate.emit(bpm) }
                } else {
                    Log.w(TAG, "HR packet too small: ${value.size} bytes")
                }
            }
            GattServiceConstants.SENSOR_STATUS_CHARACTERISTIC_UUID -> {
                val statusStr = String(value, Charsets.UTF_8)
                Log.d(TAG, "RECEIVED Status: $statusStr")
                val status = try {
                    SensorStatus.valueOf(statusStr)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse sensor status: $statusStr")
                    SensorStatus.Healthy
                }
                scope.launch { _sensorStatus.emit(status) }
            }
            GattServiceConstants.SENSOR_NAME_CHARACTERISTIC_UUID -> {
                val name = String(value, Charsets.UTF_8)
                Log.d(TAG, "RECEIVED Name: $name")
                scope.launch { _sensorName.emit(name) }
            }
        }
    }

    /**
     * Submits a payload string to the peripheral's secret attribute to gain data stream validation clearances.
     */
    @SuppressLint("MissingPermission")
    override fun validatePin(pin: String) {
        val gatt = bluetoothGatt ?: return
        val service = gatt.getService(GattServiceConstants.SIMULATOR_SERVICE_UUID)
        val pinChar = service?.getCharacteristic(GattServiceConstants.PIN_VALIDATION_CHARACTERISTIC_UUID)
        if (pinChar != null) {
            Log.i(TAG, "Writing PIN for validation: $pin")
            val bytes = pin.toByteArray(Charsets.UTF_8)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeCharacteristic(pinChar, bytes, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            } else {
                @Suppress("DEPRECATION")
                pinChar.value = bytes
                @Suppress("DEPRECATION")
                gatt.writeCharacteristic(pinChar)
            }
        } else {
            Log.e(TAG, "PIN validation characteristic not found!")
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
        Log.i(TAG, "Initiating connection to $address")
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    override fun disconnect() {
        Log.i(TAG, "Disconnecting GATT")
        stopWatchdog()
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = BleClientConnectionState.Disconnected
    }

    private fun startWatchdog() {
        Log.d(TAG, "Watchdog STARTED ($WATCHDOG_TIMEOUT_MS ms timeout)")
        resetWatchdog()
    }

    private fun resetWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = scope.launch {
            kotlinx.coroutines.delay(WATCHDOG_TIMEOUT_MS)
            Log.w(TAG, "WATCHDOG TIMEOUT! No data received for ${WATCHDOG_TIMEOUT_MS/1000}s. Triggering disconnection...")
            _connectionState.value = BleClientConnectionState.Disconnected
            disconnect()
        }
    }

    private fun stopWatchdog() {
        Log.d(TAG, "Watchdog STOPPED")
        watchdogJob?.cancel()
        watchdogJob = null
    }

    @SuppressLint("MissingPermission")
    private fun readInitialValues(gatt: BluetoothGatt?) {
        val service = gatt?.getService(GattServiceConstants.SIMULATOR_SERVICE_UUID)
        
        val nameChar = service?.getCharacteristic(GattServiceConstants.SENSOR_NAME_CHARACTERISTIC_UUID)
        if (nameChar != null) {
            Log.d(TAG, "Reading initial Name...")
            gatt.readCharacteristic(nameChar)
        }
        
        val statusChar = service?.getCharacteristic(GattServiceConstants.SENSOR_STATUS_CHARACTERISTIC_UUID)
        if (statusChar != null) {
            Log.d(TAG, "Reading initial Status...")
            gatt.readCharacteristic(statusChar)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableHeartRateNotifications(gatt: BluetoothGatt?) {
        val service = gatt?.getService(GattServiceConstants.HEART_RATE_SERVICE_UUID)
        val char = service?.getCharacteristic(GattServiceConstants.HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID)
        if (char != null) {
            val success = gatt.setCharacteristicNotification(char, true)
            Log.d(TAG, "setCharacteristicNotification (HR) result: $success")
            
            val desc = char.getDescriptor(GattServiceConstants.CCCD_UUID)
            if (desc != null) {
                Log.d(TAG, "Requesting HR notification write to CCCD")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(desc, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION")
                    desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(desc)
                }
            } else {
                Log.e(TAG, "HR CCCD descriptor not found!")
            }
        } else {
            Log.e(TAG, "HR characteristic not found!")
            enableStatusNotifications(gatt)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableStatusNotifications(gatt: BluetoothGatt?) {
        val service = gatt?.getService(GattServiceConstants.SIMULATOR_SERVICE_UUID)
        val char = service?.getCharacteristic(GattServiceConstants.SENSOR_STATUS_CHARACTERISTIC_UUID)
        if (char != null) {
            val success = gatt.setCharacteristicNotification(char, true)
            Log.d(TAG, "setCharacteristicNotification (Status) result: $success")
            
            val desc = char.getDescriptor(GattServiceConstants.CCCD_UUID)
            if (desc != null) {
                Log.d(TAG, "Requesting Status notification write to CCCD")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(desc, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION")
                    desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(desc)
                }
            } else {
                Log.e(TAG, "Status CCCD descriptor not found!")
            }
        } else {
            Log.e(TAG, "Status characteristic not found!")
            enableNameNotifications(gatt)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableNameNotifications(gatt: BluetoothGatt?) {
        val service = gatt?.getService(GattServiceConstants.SIMULATOR_SERVICE_UUID)
        val char = service?.getCharacteristic(GattServiceConstants.SENSOR_NAME_CHARACTERISTIC_UUID)
        if (char != null) {
            val success = gatt.setCharacteristicNotification(char, true)
            Log.d(TAG, "setCharacteristicNotification (Name) result: $success")
            
            val desc = char.getDescriptor(GattServiceConstants.CCCD_UUID)
            if (desc != null) {
                Log.d(TAG, "Requesting Name notification write to CCCD")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(desc, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION")
                    desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(desc)
                }
            } else {
                Log.e(TAG, "Name CCCD descriptor not found!")
            }
        } else {
            Log.e(TAG, "Name characteristic not found!")
        }
    }
}
