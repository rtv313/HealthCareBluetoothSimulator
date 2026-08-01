package com.raul_t.myapplication.presentation.heart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raul_t.myapplication.domain.usecase.ObserveHeartRateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeartRateViewModel @Inject constructor(
    private val observeHeartRateUseCase: ObserveHeartRateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HeartRateUiState()
    )

    val uiState = _uiState.asStateFlow()

    init {

        viewModelScope.launch {

            val heartRate = observeHeartRateUseCase()

            _uiState.value = HeartRateUiState(bpm = heartRate.bpm)

        }

    }
}