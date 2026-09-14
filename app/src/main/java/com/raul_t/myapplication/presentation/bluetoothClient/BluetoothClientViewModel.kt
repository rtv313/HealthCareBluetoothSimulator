package com.raul_t.myapplication.presentation.bluetoothClient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raul_t.myapplication.core.util.PermissionChecker
import com.raul_t.myapplication.domain.model.DiscoveredBluetoothDevice
import com.raul_t.myapplication.domain.usecase.ObserveDiscoveredDevicesUseCase
import com.raul_t.myapplication.domain.usecase.StartDeviceScanUseCase
import com.raul_t.myapplication.domain.usecase.StopDeviceScanUseCase
import com.raul_t.myapplication.presentation.common.SimulationUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothClientViewModel @Inject constructor(
    private val observeDiscoveredDevicesUseCase: ObserveDiscoveredDevicesUseCase,
    private val startDeviceScanUseCase: StartDeviceScanUseCase,
    private val stopDeviceScanUseCase: StopDeviceScanUseCase,
    private val permissionChecker: PermissionChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(BluetoothClientUiState())
    val uiState: StateFlow<BluetoothClientUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<SimulationUiEvent>()
    val event: SharedFlow<SimulationUiEvent> = _event.asSharedFlow()

    init {
        // Automatically listen to the data layer's found bluetooth devices.
        // Because the data layer uses a StateFlow, this collector will trigger 
        // every time the hardware scanner finds a new device or a signal update.
        viewModelScope.launch {
            observeDiscoveredDevicesUseCase().collect { devices ->
                _uiState.update { it.copy(discoveredDevices = devices) }
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
        _uiState.update { 
            it.copy(
                connectedDevice = device,
                selectedDeviceForDialog = null
            )
        }
    }

    fun disconnect() {
        _uiState.update { it.copy(connectedDevice = null) }
    }

    override fun onCleared() {
        super.onCleared()
        // Always stop hardware scanning loops to preserve battery when leaving screen
        stopScanning()
    }
}
