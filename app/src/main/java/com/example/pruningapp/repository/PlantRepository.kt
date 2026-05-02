package com.example.pruningapp.repository

import com.example.pruningapp.data.AppDatabase
import com.example.pruningapp.data.Plant
import com.example.pruningapp.data.PruningRule
import com.example.pruningapp.data.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PlantRepository(private val db: AppDatabase) {

    fun getAllPlants(): Flow<List<Plant>> = db.plantDao().getAllPlants()

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

    suspend fun insertPruningRule(rule: PruningRule) = db.plantDao().insertPruningRule(rule)

    suspend fun getAllPruningRules(): List<PruningRule> = db.plantDao().getAllPruningRules()

    suspend fun getPlantCount(): Int = db.plantDao().getPlantCount()

    suspend fun replacePruningRulesAndTasks(
        plantId: Long,
        rules: List<Triple<String, String, String>>
    ) {
        db.plantDao().deletePruningRulesForPlant(plantId)
        db.taskDao().deleteTasksForPlant(plantId)

        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        rules.forEach { (type, start, end) ->
            db.plantDao().insertPruningRule(
                PruningRule(plantId = plantId, startMonthDay = start, endMonthDay = end, type = type)
            )
            for (yearOffset in 0..1) {
                val year = today.year + yearOffset
                try {
                    val startDate = LocalDate.parse("$year-$start", formatter)
                    val endDate = LocalDate.parse("$year-$end", formatter)
                    if (endDate.isBefore(today)) continue
                    db.taskDao().insertTask(
                        Task(
                            plantId = plantId,
                            date = startDate.format(formatter),
                            endDate = endDate.format(formatter),
                            type = type,
                            status = "pending"
                        )
                    )
                } catch (e: Exception) {
                    // Pomiń nieprawidłowy format daty
                }
            }
        }
    }
}
