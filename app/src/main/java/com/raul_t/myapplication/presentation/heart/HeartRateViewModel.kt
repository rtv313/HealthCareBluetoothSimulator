package com.raul_t.myapplication.presentation.heart

import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raul_t.myapplication.domain.usecase.ObserveHeartRateUseCase
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
    private val observeHeartRateUseCase: ObserveHeartRateUseCase
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
            val heartRate = observeHeartRateUseCase()
            _uiState.value = HeartRateUiState(bpm = heartRate.bpm)
        }
    }
}