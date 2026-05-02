package com.example.pruningapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruningapp.data.NotifSettings
import com.example.pruningapp.data.NotificationPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = NotificationPreferences(application)

    val settings: StateFlow<NotifSettings> = prefs.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotifSettings())

    fun setActiveToday(enabled: Boolean) =
        viewModelScope.launch { prefs.setActiveToday(enabled) }

    fun setTomorrow(enabled: Boolean) =
        viewModelScope.launch { prefs.setTomorrow(enabled) }

    fun setOverdue(enabled: Boolean) =
        viewModelScope.launch { prefs.setOverdue(enabled) }

    fun setEndingSoon(enabled: Boolean) =
        viewModelScope.launch { prefs.setEndingSoon(enabled) }

    fun setWeekly(enabled: Boolean) =
        viewModelScope.launch { prefs.setWeekly(enabled) }

    fun setSmart(enabled: Boolean) =
        viewModelScope.launch { prefs.setSmart(enabled) }
}
