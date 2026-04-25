package com.example.pruningapp.repository

import com.example.pruningapp.data.AppDatabase
import com.example.pruningapp.data.Plant
import com.example.pruningapp.data.PruningRule
import kotlinx.coroutines.flow.Flow

class PlantRepository(private val db: AppDatabase) {

    fun getAllPlants(): Flow<List<Plant>> = db.plantDao().getAllPlants()

    suspend fun getPlantById(id: Long): Plant? = db.plantDao().getPlantById(id)

    suspend fun insertPlant(plant: Plant): Long = db.plantDao().insertPlant(plant)

    suspend fun deletePlant(plant: Plant) = db.plantDao().deletePlant(plant)

    suspend fun setOwned(id: Long, owned: Boolean) = db.plantDao().setOwned(id, owned)

    suspend fun getPruningRules(plantId: Long): List<PruningRule> =
        db.plantDao().getPruningRulesForPlant(plantId)

    suspend fun insertPruningRule(rule: PruningRule) = db.plantDao().insertPruningRule(rule)

    suspend fun getAllPruningRules(): List<PruningRule> = db.plantDao().getAllPruningRules()

    suspend fun getPlantCount(): Int = db.plantDao().getPlantCount()
}
