package com.raul_t.myapplication.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import com.raul_t.myapplication.R
import com.raul_t.myapplication.data.datasource.FakeSensorDataSource
import com.raul_t.myapplication.domain.model.BluetoothSensor
import com.raul_t.myapplication.domain.model.SensorStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [BleManager] that handles the GATT server (Emulator) lifecycle.
 */
@Singleton
class BleManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sensorDataSource: FakeSensorDataSource
) : BleManager {

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    private var bluetoothGattServer: BluetoothGattServer? = null

    private var heartRateCharacteristic: BluetoothGattCharacteristic? = null
    private var statusCharacteristic: BluetoothGattCharacteristic? = null
    private var sensorNameCharacteristic: BluetoothGattCharacteristic? = null
    
    private var connectedDevice: BluetoothDevice? = null
    private val authenticatedDevices = mutableSetOf<String>()

    // Tracks notification subscriptions to avoid redundant updates.
    private val notificationSubscriptions = mutableMapOf<String, MutableSet<java.util.UUID>>()

    private var isPinEnabled: Boolean = false
    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Idle)
    override val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private var serviceAdditionIndex = 0
    private var isPinEnabledForServices: Boolean = false
    private var currentName: String? = null

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.i("BleManager", "Advertising started SUCCESSFULLY")
            _connectionState.value = BleConnectionState.Advertising
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            val errorMessage = "Advertising FAILED: error $errorCode"
            Log.e("BleManager", errorMessage)
            _connectionState.value = BleConnectionState.Error(errorMessage)
        }
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            Log.d("BleManager", "onConnectionStateChange: ${device?.address} status=$status new=$newState")
            
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("BleManager", "Device CONNECTED: ${device?.address}")
                connectedDevice = device
                _connectionState.value = BleConnectionState.Connected(device?.name ?: context.getString(R.string.ble_unknown_device))
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("BleManager", "Device DISCONNECTED: ${device?.address}")
                device?.address?.let { 
                    authenticatedDevices.remove(it)
                    notificationSubscriptions.remove(it)
                }
                connectedDevice = null
                if (bluetoothLeAdvertiser != null) {
                    _connectionState.value = BleConnectionState.Advertising
                } else {
                    _connectionState.value = BleConnectionState.Idle
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            super.onServiceAdded(status, service)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BleManager", "Service added: ${service?.uuid}")
                serviceAdditionIndex++
                addNextService()
            } else {
                Log.e("BleManager", "Failed to add service: status $status")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
            Log.d("BleManager", "Read request for ${characteristic?.uuid}")
            if (device != null) {
                val value = characteristic?.value ?: byteArrayOf()
                bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            Log.d("BleManager", "Write request for ${characteristic?.uuid}")
            if (characteristic?.uuid == GattServiceConstants.PIN_VALIDATION_CHARACTERISTIC_UUID && value != null) {
                val writtenPin = String(value, Charsets.UTF_8)
                val expectedPin = sensorDataSource.sensorState.value.pin.toString().padStart(4, '0')
                if (writtenPin == expectedPin && device != null) {
                    authenticatedDevices.add(device.address)
                    Log.i("BleManager", "Device AUTHENTICATED: ${device.address}")
                    if (responseNeeded) bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
                } else if (device != null && responseNeeded) {
                    bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, value)
                }
            } else if (responseNeeded && device != null) {
                bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            val charUuid = descriptor?.characteristic?.uuid
            Log.d("BleManager", "Descriptor write request from ${device?.address}: ${descriptor?.uuid}")
            
            if (descriptor?.uuid == GattServiceConstants.CCCD_UUID && charUuid != null && device != null) {
                val isEnabling = value?.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) == true
                if (isEnabling) {
                    notificationSubscriptions.getOrPut(device.address) { mutableSetOf() }.add(charUuid)
                    Log.i("BleManager", "Notifications ENABLED for $charUuid on ${device.address}")
                } else {
                    notificationSubscriptions[device.address]?.remove(charUuid)
                    Log.i("BleManager", "Notifications DISABLED for $charUuid on ${device.address}")
                }
                if (responseNeeded) {
                    bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
                    Log.d("BleManager", "Sent SUCCESS response for CCCD write")
                }
            } else if (responseNeeded && device != null) {
                bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
            }
        }
    }

    override fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    override fun startAdvertising(sensor: BluetoothSensor) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser ?: return
        
        // Hardcode the discovery name as requested
        val discoveryName = "Health Sensor"
        val nameChanged = discoveryName != currentName
        
        // If GATT server is already running and discovery name hasn't changed, we don't need a full restart.
        if (bluetoothGattServer != null && !nameChanged) {
            Log.d("BleManager", "GATT server already running with name '$discoveryName'. Skipping full restart.")
            return
        }

        // If we are already advertising, stop the old one first.
        bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        
        bluetoothAdapter.name = discoveryName
        currentName = discoveryName
        this.isPinEnabled = sensor.isPinEnabled
        
        // Only open a new GATT server if one isn't already active.
        if (bluetoothGattServer == null) {
            bluetoothGattServer = bluetoothManager?.openGattServer(context, gattServerCallback)
            serviceAdditionIndex = 0
            this.isPinEnabledForServices = sensor.isPinEnabled
            addNextService()
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(sensor.allowConnection)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(GattServiceConstants.HEART_RATE_SERVICE_UUID))
            .build()
        
        Log.i("BleManager", "Starting advertising as: $discoveryName")
        bluetoothLeAdvertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    @SuppressLint("MissingPermission")
    override fun stopAdvertising() {
        bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        bluetoothLeAdvertiser = null
        
        // Force disconnect any active client before closing
        connectedDevice?.let { device ->
            Log.i("BleManager", "Forcefully disconnecting client: ${device.address}")
            bluetoothGattServer?.cancelConnection(device)
        }
        
        bluetoothGattServer?.close()
        bluetoothGattServer = null
        heartRateCharacteristic = null
        statusCharacteristic = null
        sensorNameCharacteristic = null
        connectedDevice = null
        authenticatedDevices.clear()
        notificationSubscriptions.clear()
        currentName = null
        _connectionState.value = BleConnectionState.Idle
    }

    @SuppressLint("MissingPermission")
    override fun updateHeartRate(bpm: Int) {
        val char = heartRateCharacteristic ?: return
        val device = connectedDevice ?: return
        if (notificationSubscriptions[device.address]?.contains(char.uuid) != true) {
            Log.v("BleManager", "Skipping HR update: ${device.address} not subscribed")
            return
        }
        if (isPinEnabled && !authenticatedDevices.contains(device.address)) return

        val data = byteArrayOf(0x00, bpm.toByte())
        @Suppress("DEPRECATION")
        char.value = data
        
        val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bluetoothGattServer?.notifyCharacteristicChanged(device, char, false, data)
        } else {
            @Suppress("DEPRECATION")
            bluetoothGattServer?.notifyCharacteristicChanged(device, char, false)
        }
        
        if (success == BluetoothGatt.GATT_SUCCESS || success == true) {
            Log.d("BleManager", "Notified HR: $bpm to ${device.address} - SUCCESS")
        } else {
            Log.e("BleManager", "Notified HR: $bpm to ${device.address} - FAILED ($success)")
        }
    }

    @SuppressLint("MissingPermission")
    override fun updateSensorStatus(status: SensorStatus) {
        val char = statusCharacteristic ?: return
        val device = connectedDevice ?: return
        if (notificationSubscriptions[device.address]?.contains(char.uuid) != true) return
        if (isPinEnabled && !authenticatedDevices.contains(device.address)) return

        val data = status.name.toByteArray(Charsets.UTF_8)
        @Suppress("DEPRECATION")
        char.value = data
        
        val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bluetoothGattServer?.notifyCharacteristicChanged(device, char, false, data)
        } else {
            @Suppress("DEPRECATION")
            bluetoothGattServer?.notifyCharacteristicChanged(device, char, false)
        }
        
        if (success == BluetoothGatt.GATT_SUCCESS || success == true) {
            Log.d("BleManager", "Notified Status: ${status.name} to ${device.address} - SUCCESS")
        } else {
            Log.e("BleManager", "Notified Status: ${status.name} to ${device.address} - FAILED ($success)")
        }
    }

    @SuppressLint("MissingPermission")
    override fun updateSensorName(name: String) {
        val char = sensorNameCharacteristic ?: return
        val device = connectedDevice ?: return
        if (notificationSubscriptions[device.address]?.contains(char.uuid) != true) return
        if (isPinEnabled && !authenticatedDevices.contains(device.address)) return

        val discoveryDefault = context.getString(R.string.ble_default_device_name)
        val finalName = name.ifBlank { discoveryDefault }
        val data = finalName.toByteArray(Charsets.UTF_8)
        @Suppress("DEPRECATION")
        char.value = data
        
        val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bluetoothGattServer?.notifyCharacteristicChanged(device, char, false, data)
        } else {
            @Suppress("DEPRECATION")
            bluetoothGattServer?.notifyCharacteristicChanged(device, char, false)
        }
        
        if (success == BluetoothGatt.GATT_SUCCESS || success == true) {
            Log.d("BleManager", "Notified Name: $finalName to ${device.address} - SUCCESS")
        } else {
            Log.e("BleManager", "Notified Name: $finalName to ${device.address} - FAILED ($success)")
        }
    }

    @SuppressLint("MissingPermission")
    private fun addNextService() {
        val server = bluetoothGattServer ?: return
        when (serviceAdditionIndex) {
            0 -> {
                val hrService = BluetoothGattService(GattServiceConstants.HEART_RATE_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
                val hrChar = BluetoothGattCharacteristic(GattServiceConstants.HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_NOTIFY, if (isPinEnabledForServices) BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED else BluetoothGattCharacteristic.PERMISSION_READ)
                hrChar.addDescriptor(BluetoothGattDescriptor(GattServiceConstants.CCCD_UUID, BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE))
                heartRateCharacteristic = hrChar
                hrService.addCharacteristic(hrChar)
                server.addService(hrService)
            }
            1 -> {
                val metaService = BluetoothGattService(GattServiceConstants.SIMULATOR_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
                
                // Status Characteristic
                val sChar = BluetoothGattCharacteristic(GattServiceConstants.SENSOR_STATUS_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY, if (isPinEnabledForServices) BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED else BluetoothGattCharacteristic.PERMISSION_READ)
                sChar.addDescriptor(BluetoothGattDescriptor(GattServiceConstants.CCCD_UUID, BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE))
                statusCharacteristic = sChar
                metaService.addCharacteristic(sChar)
                
                // Name Characteristic
                val nChar = BluetoothGattCharacteristic(GattServiceConstants.SENSOR_NAME_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY, if (isPinEnabledForServices) BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED else BluetoothGattCharacteristic.PERMISSION_READ)
                nChar.addDescriptor(BluetoothGattDescriptor(GattServiceConstants.CCCD_UUID, BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE))
                
                // Initialize with current name
                val discoveryDefault = context.getString(R.string.ble_default_device_name)
                val currentSensorName = sensorDataSource.sensorState.value.name.ifBlank { discoveryDefault }
                @Suppress("DEPRECATION")
                nChar.value = currentSensorName.toByteArray(Charsets.UTF_8)
                
                sensorNameCharacteristic = nChar
                metaService.addCharacteristic(nChar)
                
                metaService.addCharacteristic(BluetoothGattCharacteristic(GattServiceConstants.PIN_VALIDATION_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE))
                server.addService(metaService)
            }
            else -> Log.d("BleManager", "GATT table setup complete")
        }
    }
}
