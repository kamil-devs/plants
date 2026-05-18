package com.example.pruningapp.repository

import android.util.Log
import com.example.pruningapp.BuildConfig
import com.example.pruningapp.data.AppDatabase
import com.example.pruningapp.data.Plant
import com.example.pruningapp.data.PlantDatabase
import com.example.pruningapp.data.PerenualPlant
import com.example.pruningapp.data.PruningRule
import com.example.pruningapp.data.Task
import com.example.pruningapp.remote.PerenualApiService
import com.example.pruningapp.remote.WikipediaApiService
import com.example.pruningapp.util.PlantDescriptionTranslator
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

    fun getPruningGuideCache(): Flow<List<com.example.pruningapp.data.PruningGuideCache>> = 
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

    suspend fun syncWikipediaImage(plantId: Long): Boolean {
        val plant = db.plantDao().getPlantById(plantId) ?: return false
        if (!plant.wikiImageUrl.isNullOrBlank()) return false

        val dbEntry = PlantDatabase.plants.find { it.polishName.equals(plant.name, ignoreCase = true) }
        val latinName = dbEntry?.latinName
        val perenualId = plant.perenualId ?: dbEntry?.perenualId
        
        // Jeśli znaleźliśmy perenualId w bazie technicznej, a w tabeli go brak - uzupełnij go
        if (plant.perenualId == null && perenualId != null) {
            db.plantDao().setPerenualId(plantId, perenualId)
        }

        return syncWikipediaImageInternal(
            name = plant.name,
            latinName = latinName,
            perenualId = perenualId,
            plantId = plantId
        )
    }

    suspend fun syncWikipediaImageForEncyclopedia(dbPlant: PerenualPlant): Boolean {
        // Sprawdź czy już mamy obraz w cache (jeśli ID jest poprawne)
        if (dbPlant.perenualId > 0) {
            val cache = db.pruningGuideCacheDao().getById(dbPlant.perenualId)
            if (!cache?.imageUrl.isNullOrBlank()) return false
        }

        // Sprawdź czy mamy lokalną roślinę o tej nazwie, która może mieć już obraz
        val localPlant = db.plantDao().getPlantByName(dbPlant.polishName)
        if (localPlant != null && !localPlant.wikiImageUrl.isNullOrBlank()) {
             // Możemy przenieść obraz z lokalnej rośliny do cache poradnika bez wołania Wiki
             if (dbPlant.perenualId > 0) {
                 applyWikiImageUrl(localPlant.wikiImageUrl, dbPlant.polishName, null, dbPlant.perenualId)
             }
             return false
        }
        
        return syncWikipediaImageInternal(
            name = dbPlant.polishName,
            latinName = dbPlant.latinName,
            perenualId = if (dbPlant.perenualId > 0) dbPlant.perenualId else localPlant?.perenualId,
            plantId = localPlant?.id
        )
    }

    private suspend fun syncWikipediaImageInternal(
        name: String,
        latinName: String?,
        perenualId: Int?,
        plantId: Long?
    ): Boolean {
        Log.d("PlantRepository", "Attempting sync for: $name (Latin: $latinName, perenualId: $perenualId)")

        // Funkcja pomocnicza do sprawdzania czy błąd to 429
        fun isRateLimit(e: Exception): Boolean = e.message?.contains("429") == true

        // 1. Spróbuj łacińskiej nazwy na angielskiej Wikipedii
        if (latinName != null) {
            try {
                // Specjalna obsługa dla Truskawki (Fragaria × ananassa -> Fragaria x ananassa)
                val searchLatin = latinName.replace("×", "x")
                val url = fetchWikiImage(searchLatin, "https://en.wikipedia.org/w/api.php")
                if (url != null) {
                    Log.d("PlantRepository", "SUCCESS: Found EN wiki image via Latin ($searchLatin) for $name")
                    applyWikiImageUrl(url, name, plantId, perenualId)
                    return true
                }
            } catch (e: Exception) {
                if (isRateLimit(e)) return true
            }
        }

        // 2. Spróbuj polskiej nazwy na polskiej Wikipedii
        val cleanName = name.replace("(", "").replace(")", "").trim()
        try {
            val urlPl = fetchWikiImage(cleanName, "https://pl.wikipedia.org/w/api.php")
            if (urlPl != null) {
                Log.d("PlantRepository", "SUCCESS: Found PL wiki image for $cleanName")
                applyWikiImageUrl(urlPl, cleanName, plantId, perenualId)
                return true
            }
        } catch (e: Exception) {
            if (isRateLimit(e)) return true
        }

        // 3. Fallback: spróbuj tylko pierwszego członu nazwy
        val firstWord = cleanName.split(" ").firstOrNull()
        if (firstWord != null && firstWord != cleanName) {
            try {
                val urlFirst = fetchWikiImage(firstWord, "https://pl.wikipedia.org/w/api.php")
                if (urlFirst != null) {
                    Log.d("PlantRepository", "SUCCESS: Found PL wiki image via first word ($firstWord) for $name")
                    applyWikiImageUrl(urlFirst, firstWord, plantId, perenualId)
                    return true
                }
            } catch (e: Exception) {
                if (isRateLimit(e)) return true
            }
        }

        // 4. Finalny fallback dla nazw typu "Porzeczka czerwona" -> szukaj "Porzeczka" w EN
        val genericLatin = when {
            cleanName.contains("Porzeczka", true) -> "Ribes"
            cleanName.contains("Malina", true) -> "Rubus idaeus"
            cleanName.contains("Borówka", true) -> "Vaccinium"
            cleanName.contains("Wierzba", true) -> "Salix"
            cleanName.contains("Klon", true) -> "Acer"
            cleanName.contains("Wiśnia", true) -> "Prunus cerasus"
            cleanName.contains("Czereśnia", true) -> "Prunus avium"
            cleanName.contains("Grusza", true) -> "Pyrus"
            cleanName.contains("Śliwa", true) -> "Prunus domestica"
            cleanName.contains("Jabłoń", true) -> "Malus domestica"
            cleanName.contains("Aloes", true) -> "Aloe"
            else -> null
        }
        
        if (genericLatin != null) {
            try {
                val urlGeneric = fetchWikiImage(genericLatin, "https://en.wikipedia.org/w/api.php")
                if (urlGeneric != null) {
                    Log.d("PlantRepository", "SUCCESS: Found EN wiki image via Generic Latin ($genericLatin) for $name")
                    applyWikiImageUrl(urlGeneric, genericLatin, plantId, perenualId)
                    return true
                }
            } catch (e: Exception) {
                if (isRateLimit(e)) return true
            }
        }

        Log.w("PlantRepository", "FAILURE: No wiki image found for $name after all 4 stages.")
        return false
    }

    private suspend fun applyWikiImageUrl(url: String, sourceName: String, plantId: Long?, perenualId: Int?) {
        if (plantId != null) {
            db.plantDao().updateWikiImageUrl(plantId, url)
        }
        if (perenualId != null && perenualId > 0) {
            // Jeśli roślina nie istnieje w cache poradnika, musimy ją stworzyć
            val existing = db.pruningGuideCacheDao().getById(perenualId)
            if (existing != null) {
                db.pruningGuideCacheDao().updateImageUrl(perenualId, url)
            } else {
                // Tworzymy uproszczony wpis cache tylko ze zdjęciem, aby UI go widziało
                db.pruningGuideCacheDao().insert(
                    com.example.pruningapp.data.PruningGuideCache(
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

    private suspend fun fetchWikiImage(title: String, apiEndpoint: String): String? {
        Log.d("PlantRepository", "Fetching wiki image for: $title from $apiEndpoint")
        val response = WikipediaApiService.instance.getPageImages(url = apiEndpoint, titles = title)
        val imageUrl = response.query?.pages?.values
            ?.firstOrNull { (it.pageId ?: -1) > 0 }
            ?.thumbnail?.source
        Log.d("PlantRepository", "Wiki response for $title: $imageUrl")
        return imageUrl
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
                } catch (_: Exception) {
                    // Pomiń nieprawidłowy format daty
                }
            }
        }
    }

    /**
     * Pobiera dane pielęgnacyjne z Perenual i zapisuje je trwale w wierszu Plant.
     * Rzuca HttpException(429) oraz UnknownHostException — GlobalSyncWorker
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

        // Mock descriptions are already in Polish; pass them as-is for both fields
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
}
