package com.example.pruningapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {

    @Query("SELECT * FROM plants ORDER BY name ASC")
    fun getAllPlants(): Flow<List<Plant>>

    @Query("SELECT * FROM plants WHERE id = :id")
    suspend fun getPlantById(id: Long): Plant?

    @Query("SELECT * FROM plants WHERE id = :id")
    fun getPlantByIdFlow(id: Long): Flow<Plant?>

    @Query("SELECT * FROM pruning_rules WHERE plantId = :plantId")
    fun getPruningRulesForPlantFlow(plantId: Long): Flow<List<PruningRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: Plant): Long

    @Delete
    suspend fun deletePlant(plant: Plant)

    @Update
    suspend fun updatePlant(plant: Plant)

    @Query("UPDATE plants SET owned = :owned WHERE id = :id")
    suspend fun setOwned(id: Long, owned: Boolean)

    @Query("DELETE FROM pruning_rules WHERE plantId = :plantId")
    suspend fun deletePruningRulesForPlant(plantId: Long)

    @Query("SELECT * FROM pruning_rules WHERE plantId = :plantId")
    suspend fun getPruningRulesForPlant(plantId: Long): List<PruningRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPruningRule(rule: PruningRule)

    @Query("SELECT * FROM pruning_rules")
    suspend fun getAllPruningRules(): List<PruningRule>

    @Query("SELECT COUNT(*) FROM plants")
    suspend fun getPlantCount(): Int
}
