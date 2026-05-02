package com.example.pruningapp.repository

import com.example.pruningapp.data.AppDatabase
import com.example.pruningapp.data.MonthCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class PlantStats(val total: Int, val owned: Int, val userAdded: Int)

class StatsRepository(private val db: AppDatabase) {

    fun getPlantStatsFlow(): Flow<PlantStats> = combine(
        db.plantDao().getTotalPlantCountFlow(),
        db.plantDao().getOwnedPlantCountFlow(),
        db.plantDao().getUserAddedPlantCountFlow()
    ) { total, owned, userAdded -> PlantStats(total, owned, userAdded) }

    fun getDoneTaskCountFlow(): Flow<Int> = db.taskDao().getDoneTaskCountFlow()

    fun getMonthlyDoneStatsFlow(): Flow<List<MonthCount>> = db.taskDao().getMonthlyDoneStatsFlow()
}
