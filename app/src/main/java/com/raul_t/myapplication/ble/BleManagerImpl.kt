package com.raul_t.myapplication.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.raul_t.myapplication.data.datasource.FakeSensorDataSource
import com.raul_t.myapplication.domain.model.BluetoothSensor
import com.raul_t.myapplication.domain.model.SensorStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sensorDataSource: FakeSensorDataSource
) : BleManager {

    // BluetoothManager is the entry point for all Bluetooth activities on the device.
    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    
    // BluetoothAdapter represents the local device Bluetooth adapter (the radio).
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    
    // BluetoothLeAdvertiser provides methods to start and stop advertising.
    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    
    // BluetoothGattServer allows the app to act as a Peripheral (Server) that a Central (Client) can connect to.
    private var bluetoothGattServer: BluetoothGattServer? = null

    // Reference to our simulated "Heart Rate" characteristic to update its value.
    private var heartRateCharacteristic: BluetoothGattCharacteristic? = null

    // Reference to our simulated "Sensor Status" characteristic.
    private var statusCharacteristic: BluetoothGattCharacteristic? = null
    
    // Tracks the currently connected device to send notifications to.
    private var connectedDevice: BluetoothDevice? = null

    // Tracks which devices have successfully provided the correct PIN.
    private val authenticatedDevices = mutableSetOf<String>()

    // Local copy of security setting to know if authentication is required.
    private var isPinEnabled: Boolean = false

    // Reactive state to inform the UI about what the Bluetooth radio is doing.
    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Idle)
    override val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    /**
     * Callback to receive the status of the advertisement start request.
     * Advertising is how the phone "shouts" its presence to nearby devices.
     */
    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d("BleManager", "Advertising started successfully")
            _connectionState.value = BleConnectionState.Advertising
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            val errorMessage = "Advertising failed with error code: $errorCode"
            Log.e("BleManager", errorMessage)
            _connectionState.value = BleConnectionState.Error(errorMessage)
        }
    }

    /**
     * Callback for the GATT Server. This handles connections from other devices.
     * When a device connects, it becomes the "Central" and our app is the "Peripheral".
     */
    private val gattServerCallback = object : BluetoothGattServerCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            
            // Check if a new device has established a connection
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BleManager", "Device connected: ${device?.address}")
                connectedDevice = device
                _connectionState.value = BleConnectionState.Connected(device?.name ?: "Unknown Device")
            } 
            // Check if a previously connected device has disconnected
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("BleManager", "Device disconnected")
                device?.address?.let { authenticatedDevices.remove(it) }
                connectedDevice = null
                
                // If we were advertising when they connected, we usually want to resume
                // advertising so other devices (or the same one) can find us again.
                if (bluetoothLeAdvertiser != null) {
                    _connectionState.value = BleConnectionState.Advertising
                } else {
                    _connectionState.value = BleConnectionState.Idle
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            
            if (characteristic?.uuid == GattServiceConstants.PIN_VALIDATION_CHARACTERISTIC_UUID) {
                val writtenPin = value?.toString(Charsets.UTF_8)
                val expectedPin = sensorDataSource.sensorState.value.pin.toString().padStart(4, '0')
                
                Log.d("BleManager", "PIN Validation: Written=$writtenPin, Expected=$expectedPin")
                
                if (writtenPin == expectedPin) {
                    if (device != null) {
                        authenticatedDevices.add(device.address)
                        if (responseNeeded) {
                            bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
                        }
                        Log.d("BleManager", "Device authenticated: ${device.address}")
                    }
                } else {
                    if (device != null && responseNeeded) {
                        bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, value)
                    }
                    Log.w("BleManager", "Authentication failed for: ${device?.address}")
                }
            }
        }
    }

    override fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    /**
     * Starts the BLE Peripheral mode. This makes the device discoverable and
     * sets up the "Database" (GATT Services) that other devices can read.
     */
    @SuppressLint("MissingPermission")
    override fun startAdvertising(sensor: BluetoothSensor) {
        // 1. Safety check: Ensure Bluetooth is on
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionState.value = BleConnectionState.Error("Bluetooth is disabled")
            return
        }

        // 2. Get the advertiser. Some older devices might not support Peripheral mode.
        bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
        if (bluetoothLeAdvertiser == null) {
            _connectionState.value = BleConnectionState.Error("BLE Advertising not supported")
            return
        }

        // 3. Set the name that will appear in the "Scan" list of other devices.
        bluetoothAdapter.name = sensor.name.ifBlank { "Health Sensor" }
        this.isPinEnabled = sensor.isPinEnabled

        // 4. Open the GATT Server. This is where we host our data (Services/Characteristics).
        bluetoothGattServer = bluetoothManager?.openGattServer(context, gattServerCallback)
        setupGattServices(sensor.isPinEnabled)

        // 5. Configure HOW we advertise (Speed vs Battery usage).
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) // Faster discovery
            .setConnectable(sensor.allowConnection) // Control whether devices can connect to us
            .setTimeout(0) // 0 means advertise forever
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH) // Strong signal
            .build()

        // 6. Configure WHAT we advertise (The data packets).
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true) // Include the "My Device" name in the packet
            // Include our Service UUID so healthcare apps know we are a sensor.
            .addServiceUuid(ParcelUuid(GattServiceConstants.HEART_RATE_SERVICE_UUID))
            .build()

        // 7. Tell the radio to start broadcasting.
        bluetoothLeAdvertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    /**
     * Shuts down the BLE radio and closes the GATT server to save battery.
     */
    @SuppressLint("MissingPermission")
    override fun stopAdvertising() {
        bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        bluetoothLeAdvertiser = null
        
        bluetoothGattServer?.close()
        bluetoothGattServer = null
        heartRateCharacteristic = null
        statusCharacteristic = null
        connectedDevice = null
        authenticatedDevices.clear()
        
        _connectionState.value = BleConnectionState.Idle
    }

    /**
     * Updates the local heart rate value and notifies any connected central devices.
     */
    @SuppressLint("MissingPermission")
    override fun updateHeartRate(bpm: Int) {
        val characteristic = heartRateCharacteristic ?: return
        val device = connectedDevice ?: return
        
        // If PIN is enabled, only send data to authenticated devices.
        if (isPinEnabled && !authenticatedDevices.contains(device.address)) return

        // According to Bluetooth SIG, the first byte is Flags (0x00 for 8-bit BPM).
        // The second byte is the actual BPM value.
        val data = byteArrayOf(0x00, bpm.toByte())
        
        // Update the internal value of the characteristic
        characteristic.value = data

        // Push the update to the connected device
        bluetoothGattServer?.notifyCharacteristicChanged(device, characteristic, false)
    }

    /**
     * Updates the sensor status characteristic and notifies connected devices.
     */
    @SuppressLint("MissingPermission")
    override fun updateSensorStatus(status: SensorStatus) {
        val characteristic = statusCharacteristic ?: return
        val device = connectedDevice ?: return
        
        // If PIN is enabled, only send data to authenticated devices.
        if (isPinEnabled && !authenticatedDevices.contains(device.address)) return

        val data = status.name.toByteArray(Charsets.UTF_8)
        characteristic.value = data

        bluetoothGattServer?.notifyCharacteristicChanged(device, characteristic, false)
    }

    /**
     * Defines the structure of our simulated sensor. 
     * In BLE, data is organized into Services (Folders), Characteristics (Files), 
     * and Descriptors (Metadata/Settings).
     */
    @SuppressLint("MissingPermission")
    private fun setupGattServices(isPinEnabled: Boolean) {
        // --- Heart Rate Service ---
        val hrService = BluetoothGattService(
            GattServiceConstants.HEART_RATE_SERVICE_UUID,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
        
        val hrCharacteristic = BluetoothGattCharacteristic(
            GattServiceConstants.HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            if (isPinEnabled) BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED else BluetoothGattCharacteristic.PERMISSION_READ
        )
        
        hrCharacteristic.addDescriptor(
            BluetoothGattDescriptor(
                GattServiceConstants.CCCD_UUID,
                BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
            )
        )
        
        heartRateCharacteristic = hrCharacteristic
        hrService.addCharacteristic(hrCharacteristic)
        bluetoothGattServer?.addService(hrService)

        // --- Simulator Meta Service ---
        val metaService = BluetoothGattService(
            GattServiceConstants.SIMULATOR_SERVICE_UUID,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )

        val sCharacteristic = BluetoothGattCharacteristic(
            GattServiceConstants.SENSOR_STATUS_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            if (isPinEnabled) BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED else BluetoothGattCharacteristic.PERMISSION_READ
        )

        // Add CCCD for status notifications
        sCharacteristic.addDescriptor(
            BluetoothGattDescriptor(
                GattServiceConstants.CCCD_UUID,
                BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
            )
        )

        statusCharacteristic = sCharacteristic
        metaService.addCharacteristic(sCharacteristic)

        // --- PIN Validation Characteristic ---
        val vCharacteristic = BluetoothGattCharacteristic(
            GattServiceConstants.PIN_VALIDATION_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        metaService.addCharacteristic(vCharacteristic)

        bluetoothGattServer?.addService(metaService)
    }
}
