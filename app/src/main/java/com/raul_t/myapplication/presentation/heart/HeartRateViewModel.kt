package com.raul_t.myapplication.presentation.heart

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raul_t.myapplication.domain.usecase.ObserveHeartRateUseCase
import com.raul_t.myapplication.domain.usecase.SetFixBpmUseCase
import com.raul_t.myapplication.service.HeartRateForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeartRateViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val observeHeartRateUseCase: ObserveHeartRateUseCase,
    private val setFixBpmUseCase: SetFixBpmUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HeartRateUiState()
    )

    val uiState = _uiState.asStateFlow()

    init {
        val intent = Intent(
            context,
            HeartRateForegroundService::class.java
        )

        ContextCompat.startForegroundService(
            context,
            intent
        )

        viewModelScope.launch {
            observeHeartRateUseCase().collect { heartRate ->
                _uiState.value = _uiState.value.copy(bpm = heartRate.bpm)
            }
        }
    }

    fun setFixBpm(setBpmEnable: Boolean, bpm: Int) {
        _uiState.value = _uiState.value.copy(
            isFixBpmEnabled = setBpmEnable,
            targetBpm = bpm
        )
        viewModelScope.launch {
            setFixBpmUseCase(setBpmEnable, bpm)
        }
    }
}