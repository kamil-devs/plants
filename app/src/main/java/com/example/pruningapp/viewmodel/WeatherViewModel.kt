package com.example.pruningapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruningapp.App
import com.example.pruningapp.data.WeatherData
import com.example.pruningapp.ui.components.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val weatherRepository = (application as App).weatherRepository

    private val _uiState = MutableStateFlow<UiState<WeatherData?>>(UiState.Loading)
    val uiState: StateFlow<UiState<WeatherData?>> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = try {
                val data = weatherRepository.getWeather()
                UiState.Success(data)
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Nie udalo sie pobrac pogody")
            }
        }
    }
}
