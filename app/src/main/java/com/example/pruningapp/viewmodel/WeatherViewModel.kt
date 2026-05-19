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

    private val _searchResults = MutableStateFlow<List<com.example.pruningapp.remote.GeocodingResponse>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

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

    fun searchCities(query: String) {
        if (query.trim().length < 2) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            try {
                _searchResults.value = weatherRepository.searchCities(query)
            } catch (_: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun updateLocation(city: String, country: String?, state: String?) {
        viewModelScope.launch {
            weatherRepository.setLocation(city, country, state)
            refresh()
        }
    }
}
