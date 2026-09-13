package com.raul_t.myapplication.presentation.bluetoothClient

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BluetoothClientViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(BluetoothClientUiState())
    val uiState: StateFlow<BluetoothClientUiState> = _uiState.asStateFlow()

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
}
