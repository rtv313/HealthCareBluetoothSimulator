package com.raul_t.myapplication.presentation.bluetoothSensorEmulator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raul_t.myapplication.domain.model.SensorStatus
import com.raul_t.myapplication.domain.usecase.ObserveSensorConfigUseCase
import com.raul_t.myapplication.domain.usecase.UpdateSensorConfigUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothSensorEmulatorViewModel @Inject constructor(
    private val observeSensorConfigUseCase: ObserveSensorConfigUseCase,
    private val updateSensorConfigUseCase: UpdateSensorConfigUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BluetoothSensorEmulatorUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeSensorConfigUseCase().collect { sensor ->
                _uiState.update { it.copy(sensor = sensor) }
            }
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch {
            updateSensorConfigUseCase { it.copy(name = name) }
        }
    }

    fun updateStatus(status: SensorStatus) {
        viewModelScope.launch {
            updateSensorConfigUseCase { it.copy(status = status) }
        }
    }

    fun toggleAdvertising(enabled: Boolean) {
        viewModelScope.launch {
            updateSensorConfigUseCase { it.copy(isAdvertising = enabled) }
        }
    }

    fun toggleAllowConnection(enabled: Boolean) {
        viewModelScope.launch {
            updateSensorConfigUseCase { it.copy(allowConnection = enabled) }
        }
    }

    fun togglePinEnabled(enabled: Boolean) {
        viewModelScope.launch {
            updateSensorConfigUseCase { it.copy(isPinEnabled = enabled) }
        }
    }

    fun updatePin(pin: String) {
        viewModelScope.launch {
            val pinInt = pin.toIntOrNull() ?: 0
            updateSensorConfigUseCase { it.copy(pin = pinInt) }
        }
    }
}
