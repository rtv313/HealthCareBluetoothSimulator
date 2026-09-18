package com.raul_t.myapplication.presentation.bluetoothClient

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raul_t.myapplication.core.util.PermissionChecker
import com.raul_t.myapplication.domain.model.DiscoveredBluetoothDevice
import com.raul_t.myapplication.domain.usecase.*
import com.raul_t.myapplication.presentation.common.SimulationUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothClientViewModel @Inject constructor(
    private val observeDiscoveredDevicesUseCase: ObserveDiscoveredDevicesUseCase,
    private val startDeviceScanUseCase: StartDeviceScanUseCase,
    private val stopDeviceScanUseCase: StopDeviceScanUseCase,
    private val clearDiscoveredDevicesUseCase: ClearDiscoveredDevicesUseCase,
    private val connectToDeviceUseCase: ConnectToDeviceUseCase,
    private val disconnectFromDeviceUseCase: DisconnectFromDeviceUseCase,
    private val observeClientConnectionStateUseCase: ObserveClientConnectionStateUseCase,
    private val observeReceivedHeartRateUseCase: ObserveReceivedHeartRateUseCase,
    private val observeReceivedSensorStatusUseCase: ObserveReceivedSensorStatusUseCase,
    private val observeReceivedSensorNameUseCase: ObserveReceivedSensorNameUseCase,
    private val permissionChecker: PermissionChecker,
    private val validatePinUseCase: ValidatePinUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BluetoothClientUiState())
    val uiState: StateFlow<BluetoothClientUiState> = _uiState.asStateFlow()

    private val _event = Channel<SimulationUiEvent>()
    val event = _event.receiveAsFlow()

    init {
        // Automatically listen to the data layer's found bluetooth devices.
        viewModelScope.launch {
            observeDiscoveredDevicesUseCase().collect { devices ->
                _uiState.update { it.copy(discoveredDevices = devices) }
            }
        }

        // Observe GATT connection state
        viewModelScope.launch {
            observeClientConnectionStateUseCase().collect { state ->
                Log.d("BluetoothClientVM", "Connection State Changed: $state")
                _uiState.update { it.copy(connectionState = state) }
                
                when (state) {
                    is com.raul_t.myapplication.ble_connect.BleClientConnectionState.Connecting -> {
                        _uiState.update { 
                            it.copy(
                                mockName = "Connecting...",
                                mockBpm = -1
                            )
                        }
                    }
                    is com.raul_t.myapplication.ble_connect.BleClientConnectionState.WaitingForPin -> {
                        _uiState.update { 
                            it.copy(
                                isWaitingForPin = true,
                                pinErrorMessage = null
                            )
                        }
                    }
                    is com.raul_t.myapplication.ble_connect.BleClientConnectionState.InvalidPin -> {
                        _uiState.update { 
                            it.copy(
                                isWaitingForPin = true,
                                pinErrorMessage = "Wrong PIN"
                            )
                        }
                    }
                    is com.raul_t.myapplication.ble_connect.BleClientConnectionState.Connected -> {
                        _uiState.update { 
                            it.copy(
                                mockName = it.connectedDevice?.name ?: "Health Sensor",
                                isWaitingForPin = false,
                                pinErrorMessage = null
                            )
                        }
                    }
                    is com.raul_t.myapplication.ble_connect.BleClientConnectionState.Disconnected,
                    is com.raul_t.myapplication.ble_connect.BleClientConnectionState.Error -> {
                        if (_uiState.value.connectedDevice != null) {
                            Log.w("BluetoothClientVM", "COMMUNICATION LOST: Setting UI to Server Stopped.")
                            _uiState.update { 
                                it.copy(
                                    mockName = "Server Stopped",
                                    mockBpm = -1,
                                    mockStatus = com.raul_t.myapplication.domain.model.SensorStatus.Disconnected,
                                    isWaitingForPin = false,
                                    pinErrorMessage = null
                                )
                            }
                        } else {
                            _uiState.update { 
                                it.copy(
                                    isWaitingForPin = false,
                                    pinErrorMessage = null
                                )
                            }
                        }
                    }
                }
            }
        }

        // Observe real-time Heart Rate from connected sensor
        viewModelScope.launch {
            observeReceivedHeartRateUseCase().collect { bpm ->
                if (_uiState.value.connectionState is com.raul_t.myapplication.ble_connect.BleClientConnectionState.Connected) {
                    Log.v("BluetoothClientVM", "Received Heart Rate: $bpm")
                    _uiState.update { it.copy(mockBpm = bpm) }
                }
            }
        }

        // Observe real-time Sensor Status from connected sensor
        viewModelScope.launch {
            observeReceivedSensorStatusUseCase().collect { status ->
                if (_uiState.value.connectionState is com.raul_t.myapplication.ble_connect.BleClientConnectionState.Connected) {
                    Log.v("BluetoothClientVM", "Received Sensor Status: $status")
                    _uiState.update { it.copy(mockStatus = status) }
                }
            }
        }

        // Observe real-time Sensor Name from connected sensor
        viewModelScope.launch {
            observeReceivedSensorNameUseCase().collect { name ->
                if (_uiState.value.connectionState is com.raul_t.myapplication.ble_connect.BleClientConnectionState.Connected) {
                    Log.v("BluetoothClientVM", "Received Sensor Name: $name")
                    _uiState.update { it.copy(mockName = name) }
                }
            }
        }
        
        // Check permissions before starting hardware scanning
        checkPermissionsAndStartScan()
    }

    private fun checkPermissionsAndStartScan() {
        if (permissionChecker.hasSimulationPermissions()) {
            startScanning()
        } else {
            viewModelScope.launch {
                _event.send(SimulationUiEvent.RequestPermissions)
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            startScanning()
        }
    }

    fun startScanning() {
        startDeviceScanUseCase()
    }

    fun stopScanning() {
        stopDeviceScanUseCase()
    }

    fun refreshScanner() {
        clearDiscoveredDevicesUseCase()
        stopScanning()
        startScanning()
    }

    fun selectDevice(device: DiscoveredBluetoothDevice) {
        _uiState.update { it.copy(selectedDeviceForDialog = device) }
    }

    fun clearDialog() {
        _uiState.update { it.copy(selectedDeviceForDialog = null) }
    }

    fun connectDevice(device: DiscoveredBluetoothDevice) {
        connectToDeviceUseCase(device.address)
        _uiState.update { 
            it.copy(
                connectedDevice = device,
                selectedDeviceForDialog = null
            )
        }
    }

    fun disconnect() {
        disconnectFromDeviceUseCase()
        _uiState.update { it.copy(connectedDevice = null) }
    }

    fun enterPin(pin: String) {
        validatePinUseCase(pin)
    }

    fun cancelPinDialog() {
        disconnect()
        _uiState.update { 
            it.copy(
                isWaitingForPin = false,
                pinErrorMessage = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Always stop hardware scanning loops to preserve battery when leaving screen
        stopScanning()
    }
}
