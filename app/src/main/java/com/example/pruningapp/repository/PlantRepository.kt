package com.example.pruningapp.repository

import com.example.pruningapp.data.AppDatabase
import com.example.pruningapp.data.Plant
import com.example.pruningapp.data.PruningGuideCache
import com.example.pruningapp.data.PruningRule
import com.example.pruningapp.data.generateTasksForRule
import kotlinx.coroutines.flow.Flow

class PlantRepository(private val db: AppDatabase) {

    fun getAllPlants(): Flow<List<Plant>> = db.plantDao().getAllPlants()

    fun getPruningGuideCache(): Flow<List<PruningGuideCache>> =
        db.pruningGuideCacheDao().getAllFlow()

    suspend fun getPlantById(id: Long): Plant? = db.plantDao().getPlantById(id)

    fun getPlantByIdFlow(id: Long): Flow<Plant?> = db.plantDao().getPlantByIdFlow(id)

    fun getPruningRulesFlow(plantId: Long): Flow<List<PruningRule>> =
        db.plantDao().getPruningRulesForPlantFlow(plantId)

    suspend fun insertPlant(plant: Plant): Long = db.plantDao().insertPlant(plant)

    suspend fun deletePlant(plant: Plant) = db.plantDao().deletePlant(plant)

    suspend fun updatePlant(plant: Plant) = db.plantDao().updatePlant(plant)

    suspend fun setOwned(id: Long, owned: Boolean) = db.plantDao().setOwned(id, owned)

    suspend fun setPinned(id: Long, pinned: Boolean) = db.plantDao().setPinned(id, pinned)

    suspend fun getPruningRules(plantId: Long): List<PruningRule> =
        db.plantDao().getPruningRulesForPlant(plantId)

    suspend fun replacePruningRulesAndTasks(
        plantId: Long,
        rules: List<Triple<String, String, String>>
    ) {
        db.plantDao().deletePruningRulesForPlant(plantId)
        db.taskDao().deleteTasksForPlant(plantId)

        rules.forEach { (type, start, end) ->
            db.plantDao().insertPruningRule(
                PruningRule(plantId = plantId, startMonthDay = start, endMonthDay = end, type = type)
            )
            db.generateTasksForRule(plantId, start, end, type)
        }
    }
}
