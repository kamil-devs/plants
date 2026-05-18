package com.example.pruningapp.repository

import android.util.Log
import com.example.pruningapp.BuildConfig
import com.example.pruningapp.data.AppDatabase
import com.example.pruningapp.data.Plant
import com.example.pruningapp.data.PlantDatabase
import com.example.pruningapp.data.PruningRule
import com.example.pruningapp.data.Task
import com.example.pruningapp.remote.PerenualApiService
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PlantRepository(
    private val db: AppDatabase,
    private val api: PerenualApiService = PerenualApiService.instance
) {

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

    /**
     * Pobiera dane pielęgnacyjne z Perenual i zapisuje je trwale w wierszu Plant.
     * Rzuca HttpException(429) oraz UnknownHostException — PerenualSyncWorker
     * przechwytuje je i planuje automatyczne ponowienie.
     */
    suspend fun syncPlantFromApi(plantId: Long) {
        val plant = db.plantDao().getPlantById(plantId) ?: return

        val perenualId = plant.perenualId
            ?: PlantDatabase.plants.find { it.polishName == plant.name }?.perenualId
            ?: return

        if (plant.perenualId == null) {
            db.plantDao().setPerenualId(plantId, perenualId)
        }

        if (BuildConfig.DEBUG) {
            applyMockData(plantId, perenualId)
            return
        }

        if (BuildConfig.PERENUAL_API_KEY.isBlank()) {
            Log.w("PlantRepository", "Brak klucza API — pomijam sync dla plantId=$plantId")
            return
        }

        try {
            val details = api.getSpeciesDetails(perenualId, BuildConfig.PERENUAL_API_KEY)
            val imageUrl = details.defaultImage?.regularUrl ?: details.defaultImage?.mediumUrl
            db.plantDao().updateApiData(
                id = plantId,
                description = details.description,
                watering = details.watering,
                maintenance = details.maintenance,
                imageUrl = imageUrl
            )
        } catch (e: HttpException) {
            Log.e("PlantRepository", "HTTP ${e.code()} dla perenualId=$perenualId")
            throw e
        } catch (e: UnknownHostException) {
            Log.e("PlantRepository", "Brak połączenia — retry zaplanowany przez Worker")
            throw e
        }
    }

    private suspend fun applyMockData(plantId: Long, perenualId: Int) {
        data class MockData(
            val description: String,
            val watering: String,
            val maintenance: String,
            val imageUrl: String
        )

        val mock = when (perenualId) {
            4892 -> MockData(
                "Jabłonie wymagają regularnego cięcia, aby zachować zdrowie i wysoką jakość owoców. Najlepszym czasem jest późna zima lub wczesna wiosna, przed rozpoczęciem wegetacji.",
                "Average", "moderate",
                "https://perenual.com/storage/species_image/4892_malus_domestica/regular/malus_domestica.jpg"
            )
            7342 -> MockData(
                "Róże przycinamy zazwyczaj wiosną, gdy pąki zaczynają nabrzmiewać. Usuwamy martwe i chore pędy, aby pobudzić roślinę do kwitnienia.",
                "Average", "high",
                "https://perenual.com/storage/species_image/7342_rosa/regular/rosa.jpg"
            )
            3919 -> MockData(
                "Lawendę przycinamy po kwitnieniu (późnym latem), aby zachować zwarty pokrój krzewu. Można również wykonać lekkie cięcie formujące wiosną.",
                "Minimum", "low",
                "https://perenual.com/storage/species_image/3919_lavandula_angustifolia/regular/lavandula_angustifolia.jpg"
            )
            5243 -> MockData(
                "Monstera nie wymaga regularnego przycinania. Usuwamy jedynie uschnięte liście lub skracamy zbyt długie pędy, jeśli roślina zajmuje za dużo miejsca.",
                "Average", "low",
                "https://perenual.com/storage/species_image/5243_monstera_deliciosa/regular/monstera_deliciosa.jpg"
            )
            else -> return
        }

        db.plantDao().updateApiData(
            id = plantId,
            description = mock.description,
            watering = mock.watering,
            maintenance = mock.maintenance,
            imageUrl = mock.imageUrl
        )
    }
}
