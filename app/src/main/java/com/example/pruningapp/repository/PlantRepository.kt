package com.example.pruningapp.repository

import android.util.Log
import com.example.pruningapp.BuildConfig
import com.example.pruningapp.data.AppDatabase
import com.example.pruningapp.data.Plant
import com.example.pruningapp.data.PruningGuideCache
import com.example.pruningapp.data.PruningRule
import com.example.pruningapp.data.Task
import com.example.pruningapp.domain.WikipediaImageProvider
import com.example.pruningapp.remote.PerenualApiService
import com.example.pruningapp.util.PlantDescriptionTranslator
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Lean Repository: jedyną odpowiedzialnością jest orkiestracja strumieni danych.
// Logika pobierania mediów delegowana do WikipediaImageProvider (wstrzykiwana przez konstruktor).
// Warstwa UI widzi wyłącznie modele domenowe (Plant, PruningRule, Task) — zero DTO/encji Room.
class PlantRepository(
    private val db: AppDatabase,
    private val api: PerenualApiService = PerenualApiService.instance,
    private val wikiImageProvider: WikipediaImageProvider? = null
) {

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

    // Synchronizacja obrazu Wikipedii dla rośliny użytkownika.
    // Zwraca true jeśli wykonano zapytanie sieciowe (wymaga opóźnienia w callerze).
    suspend fun syncWikipediaImage(plantId: Long): Boolean {
        val plant = db.plantDao().getPlantById(plantId) ?: return false
        if (!plant.wikiImageUrl.isNullOrBlank()) return false

        val species = db.encyclopediaSpeciesDao().getByPolishName(plant.name)
        val latinName = species?.latinName
        val perenualId = plant.perenualId ?: species?.perenualId

        if (plant.perenualId == null && perenualId != null) {
            db.plantDao().setPerenualId(plantId, perenualId)
        }

        val provider = wikiImageProvider ?: return false
        val url = provider.fetchImageUrl(plant.name, latinName) ?: return true

        Log.d(TAG, "Znaleziono obraz dla '${plant.name}': $url")
        applyWikiImageUrl(url, plant.name, plantId, perenualId)
        return true
    }

    // Synchronizacja obrazu dla gatunku z encyklopedii (bez lokalnej rośliny użytkownika).
    suspend fun syncWikipediaImageForEncyclopedia(
        polishName: String,
        latinName: String,
        perenualId: Int
    ): Boolean {
        if (perenualId > 0) {
            val cache = db.pruningGuideCacheDao().getById(perenualId)
            if (!cache?.imageUrl.isNullOrBlank()) return false
        }

        val localPlant = db.plantDao().getPlantByName(polishName)
        if (localPlant != null && !localPlant.wikiImageUrl.isNullOrBlank()) {
            if (perenualId > 0) {
                applyWikiImageUrl(localPlant.wikiImageUrl!!, polishName, null, perenualId)
            }
            return false
        }

        val provider = wikiImageProvider ?: return false
        val url = provider.fetchImageUrl(polishName, latinName) ?: return true

        applyWikiImageUrl(url, polishName, localPlant?.id, if (perenualId > 0) perenualId else localPlant?.perenualId)
        return true
    }

    private suspend fun applyWikiImageUrl(
        url: String,
        sourceName: String,
        plantId: Long?,
        perenualId: Int?
    ) {
        if (plantId != null) {
            db.plantDao().updateWikiImageUrl(plantId, url)
        }
        if (perenualId != null && perenualId > 0) {
            val existing = db.pruningGuideCacheDao().getById(perenualId)
            if (existing != null) {
                db.pruningGuideCacheDao().updateImageUrl(perenualId, url)
            } else {
                db.pruningGuideCacheDao().insert(
                    PruningGuideCache(
                        perenualId = perenualId,
                        commonName = sourceName,
                        pruningMonthsJson = "[]",
                        frequency = null,
                        maintenanceLevel = null,
                        description = null,
                        imageUrl = url,
                        fetchedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

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
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun syncPlantFromApi(plantId: Long) {
        val plant = db.plantDao().getPlantById(plantId) ?: return

        val perenualId = plant.perenualId
            ?: db.encyclopediaSpeciesDao().getByPolishName(plant.name)?.perenualId
            ?: return

        if (plant.perenualId == null) {
            db.plantDao().setPerenualId(plantId, perenualId)
        }

        if (BuildConfig.DEBUG) {
            applyMockData(plantId, perenualId)
            return
        }

        if (BuildConfig.PERENUAL_API_KEY.isBlank()) {
            Log.w(TAG, "Brak klucza API — pomijam sync dla plantId=$plantId")
            return
        }

        try {
            val details = api.getSpeciesDetails(perenualId, BuildConfig.PERENUAL_API_KEY)
            val imageUrl = details.defaultImage?.regularUrl ?: details.defaultImage?.mediumUrl
            val sunlight = details.sunlight?.joinToString(", ")
            val descriptionPl = if (!details.description.isNullOrBlank())
                PlantDescriptionTranslator.translate(details.description)
            else null
            db.plantDao().updateApiData(
                id = plantId,
                description = details.description,
                descriptionPl = descriptionPl,
                watering = details.watering,
                maintenance = details.maintenance,
                sunlight = sunlight,
                imageUrl = imageUrl
            )
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP ${e.code()} dla perenualId=$perenualId")
            throw e
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Brak połączenia")
            throw e
        }
    }

    private suspend fun applyMockData(plantId: Long, perenualId: Int) {
        data class MockData(
            val description: String,
            val watering: String,
            val maintenance: String,
            val sunlight: String,
            val imageUrl: String
        )

        val mock = when (perenualId) {
            4892 -> MockData(
                "Jabłonie wymagają regularnego cięcia, aby zachować zdrowie i wysoką jakość owoców. Najlepszym czasem jest późna zima lub wczesna wiosna, przed rozpoczęciem wegetacji.",
                "Average", "moderate", "Full sun",
                "https://perenual.com/storage/species_image/4892_malus_domestica/regular/malus_domestica.jpg"
            )
            7342 -> MockData(
                "Róże przycinamy zazwyczaj wiosną, gdy pąki zaczynają nabrzmiewać. Usuwamy martwe i chore pędy, aby pobudzić roślinę do kwitnienia.",
                "Average", "high", "Full sun, Part shade",
                "https://perenual.com/storage/species_image/7342_rosa/regular/rosa.jpg"
            )
            3919 -> MockData(
                "Lawendę przycinamy po kwitnieniu (późnym latem), aby zachować zwarty pokrój krzewu. Można również wykonać lekkie cięcie formujące wiosną.",
                "Minimum", "low", "Full sun",
                "https://perenual.com/storage/species_image/3919_lavandula_angustifolia/regular/lavandula_angustifolia.jpg"
            )
            5243 -> MockData(
                "Monstera nie wymaga regularnego przycinania. Usuwamy jedynie uschnięte liście lub skracamy zbyt długie pędy, jeśli roślina zajmuje za dużo miejsca.",
                "Average", "low", "Part shade, Filtered Indirect Light",
                "https://perenual.com/storage/species_image/5243_monstera_deliciosa/regular/monstera_deliciosa.jpg"
            )
            else -> return
        }

        db.plantDao().updateApiData(
            id = plantId,
            description = mock.description,
            descriptionPl = mock.description,
            watering = mock.watering,
            maintenance = mock.maintenance,
            sunlight = mock.sunlight,
            imageUrl = mock.imageUrl
        )
    }

    private companion object {
        const val TAG = "PlantRepository"
    }
}
