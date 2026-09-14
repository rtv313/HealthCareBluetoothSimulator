package com.raul_t.myapplication.presentation.bluetoothClient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raul_t.myapplication.core.util.PermissionChecker
import com.raul_t.myapplication.domain.model.DiscoveredBluetoothDevice
import com.raul_t.myapplication.domain.usecase.*
import com.raul_t.myapplication.presentation.common.SimulationUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothClientViewModel @Inject constructor(
    private val observeDiscoveredDevicesUseCase: ObserveDiscoveredDevicesUseCase,
    private val startDeviceScanUseCase: StartDeviceScanUseCase,
    private val stopDeviceScanUseCase: StopDeviceScanUseCase,
    private val connectToDeviceUseCase: ConnectToDeviceUseCase,
    private val disconnectFromDeviceUseCase: DisconnectFromDeviceUseCase,
    private val observeClientConnectionStateUseCase: ObserveClientConnectionStateUseCase,
    private val observeReceivedHeartRateUseCase: ObserveReceivedHeartRateUseCase,
    private val observeReceivedSensorStatusUseCase: ObserveReceivedSensorStatusUseCase,
    private val permissionChecker: PermissionChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(BluetoothClientUiState())
    val uiState: StateFlow<BluetoothClientUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<SimulationUiEvent>()
    val event: SharedFlow<SimulationUiEvent> = _event.asSharedFlow()

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
                _uiState.update { it.copy(connectionState = state) }
            }
        }

        // Observe real-time Heart Rate from connected sensor
        viewModelScope.launch {
            observeReceivedHeartRateUseCase().collect { bpm ->
                _uiState.update { it.copy(mockBpm = bpm) }
            }
        }

        // Observe real-time Sensor Status from connected sensor
        viewModelScope.launch {
            observeReceivedSensorStatusUseCase().collect { status ->
                _uiState.update { it.copy(mockStatus = status) }
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
                _event.emit(SimulationUiEvent.RequestPermissions)
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

    override fun onCleared() {
        super.onCleared()
        // Always stop hardware scanning loops to preserve battery when leaving screen
        stopScanning()
    }
}
