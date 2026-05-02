package com.example.pruningapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruningapp.App
import com.example.pruningapp.data.MonthCount
import com.example.pruningapp.repository.PlantStats
import com.example.pruningapp.repository.StatsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = StatsRepository((application as App).database)

    val plantStats: StateFlow<PlantStats> = repo.getPlantStatsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlantStats(0, 0, 0))

    val doneTaskCount: StateFlow<Int> = repo.getDoneTaskCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val monthlyStats: StateFlow<List<MonthCount>> = repo.getMonthlyDoneStatsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
