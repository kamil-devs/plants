package com.example.pruningapp.repository

import android.util.Log
import com.example.pruningapp.BuildConfig
import com.example.pruningapp.data.PruningGuideCache
import com.example.pruningapp.data.PruningGuideCacheDao
import com.example.pruningapp.remote.PerenualApiService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.UnknownHostException

sealed class PruningGuideResult {
    object Loading : PruningGuideResult()
    object NotFound : PruningGuideResult()
    data class Error(val message: String) : PruningGuideResult()
    data class Success(
        val commonName: String,
        val pruningMonths: List<String>,
        val frequency: String?,
        val maintenanceLevel: String?,
        val description: String?,
        val imageUrl: String? = null
    ) : PruningGuideResult()
}

class PruningGuideRepository(
    private val cacheDao: PruningGuideCacheDao,
    private val api: PerenualApiService = PerenualApiService.instance
) {
    private val gson = Gson()
    private val ttlMillis = 30L * 24 * 60 * 60 * 1000

    suspend fun getPruningGuide(perenualId: Int): PruningGuideResult {
        // W trybie Debug najpierw sprawdzamy cache (bo tam może być obraz z Wiki)
        val cached = cacheDao.getById(perenualId)
        if (cached != null && System.currentTimeMillis() - cached.fetchedAt < ttlMillis) {
            return cached.toResult()
        }

        // Jeśli nie ma w cache, w trybie Debug próbujemy Mocków (tylko dla znanych ID)
        if (BuildConfig.DEBUG) {
            val mock = getMockData(perenualId)
            if (mock is PruningGuideResult.Success) return mock
        }

        if (BuildConfig.PERENUAL_API_KEY.isBlank()) {
            return PruningGuideResult.Error("Brak klucza API Perenual")
        }

        return try {
            val details = api.getSpeciesDetails(perenualId, BuildConfig.PERENUAL_API_KEY)
            val months = details.pruningMonth ?: emptyList()
            val freq = formatFrequency(details.pruningCount?.amount, details.pruningCount?.interval)
            val img = details.defaultImage?.regularUrl ?: details.defaultImage?.mediumUrl

            if (months.isEmpty() && freq == null
                && details.maintenance.isNullOrBlank()
                && details.description.isNullOrBlank()
                && img == null
            ) {
                return PruningGuideResult.NotFound
            }

            cacheDao.insert(
                PruningGuideCache(
                    perenualId = perenualId,
                    commonName = details.commonName,
                    pruningMonthsJson = gson.toJson(months),
                    frequency = freq,
                    maintenanceLevel = details.maintenance,
                    description = details.description,
                    imageUrl = img,
                    fetchedAt = System.currentTimeMillis()
                )
            )

            PruningGuideResult.Success(
                commonName = details.commonName,
                pruningMonths = months,
                frequency = freq,
                maintenanceLevel = details.maintenance,
                description = details.description,
                imageUrl = img
            )
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 429) {
                Log.e("PruningGuideRepo", "Rate limit exceeded (429)")
                val expiredCached = cacheDao.getById(perenualId)
                if (expiredCached != null) {
                    expiredCached.toResult()
                } else {
                    PruningGuideResult.Error("Przekroczono limit zapytań API. Spróbuj ponownie za minutę.")
                }
            } else {
                PruningGuideResult.Error("Błąd serwera (${e.code()})")
            }
        } catch (e: UnknownHostException) {
            Log.e("PruningGuideRepo", "No internet connection", e)
            PruningGuideResult.Error("Brak połączenia z internetem")
        } catch (e: Exception) {
            Log.e("PruningGuideRepo", "Error fetching guide", e)
            PruningGuideResult.Error("Błąd serwera lub danych: ${e.localizedMessage}")
        }
    }

    private fun PruningGuideCache.toResult(): PruningGuideResult.Success {
        val type = object : TypeToken<List<String>>() {}.type
        val months: List<String> = runCatching {
            gson.fromJson<List<String>>(pruningMonthsJson, type)
        }.getOrDefault(emptyList())
        return PruningGuideResult.Success(
            commonName = commonName,
            pruningMonths = months,
            frequency = frequency,
            maintenanceLevel = maintenanceLevel,
            description = description,
            imageUrl = imageUrl
        )
    }

    private fun formatFrequency(amount: Int?, interval: String?): String? {
        if (amount == null || interval == null) return null
        val polishInterval = when (interval.lowercase()) {
            "year", "yearly", "annually" -> "rok"
            "month", "monthly"           -> "miesiąc"
            "week", "weekly"             -> "tydzień"
            "season", "seasonal"         -> "sezon"
            else                         -> interval
        }
        return "${amount}x / $polishInterval"
    }

    private fun getMockData(id: Int): PruningGuideResult {
        return when (id) {
            4892 -> PruningGuideResult.Success(
                commonName = "Apple Tree",
                pruningMonths = listOf("February", "March", "December"),
                frequency = "1x / rok",
                maintenanceLevel = "moderate",
                description = "Jabłonie wymagają regularnego cięcia, aby zachować zdrowie i wysoką jakość owoców. Najlepszym czasem jest późna zima lub wczesna wiosna, przed rozpoczęciem wegetacji.",
                imageUrl = "https://perenual.com/storage/species_image/4892_malus_domestica/regular/malus_domestica.jpg"
            )
            7342 -> PruningGuideResult.Success(
                commonName = "Rose",
                pruningMonths = listOf("March", "April"),
                frequency = "1x / rok",
                maintenanceLevel = "high",
                description = "Róże przycinamy zazwyczaj wiosną, gdy pąki zaczynają nabrzmiewać. Usuwamy martwe i chore pędy, aby pobudzić roślinę do kwitnienia.",
                imageUrl = "https://perenual.com/storage/species_image/7342_rosa/regular/rosa.jpg"
            )
            3919 -> PruningGuideResult.Success(
                commonName = "Lavender",
                pruningMonths = listOf("April", "August", "September"),
                frequency = "2x / rok",
                maintenanceLevel = "low",
                description = "Lawendę przycinamy po kwitnieniu (późnym latem), aby zachować zwarty pokrój krzewu. Można również wykonać lekkie cięcie formujące wiosną.",
                imageUrl = "https://perenual.com/storage/species_image/3919_lavandula_angustifolia/regular/lavandula_angustifolia.jpg"
            )
            5243 -> PruningGuideResult.Success(
                commonName = "Monstera Deliciosa",
                pruningMonths = emptyList(),
                frequency = "według potrzeb",
                maintenanceLevel = "low",
                description = "Monstera nie wymaga regularnego przycinania. Usuwamy jedynie uschnięte liście lub skracamy zbyt długie pędy, jeśli roślina zajmuje za dużo miejsca.",
                imageUrl = "https://perenual.com/storage/species_image/5243_monstera_deliciosa/regular/monstera_deliciosa.jpg"
            )
            else -> PruningGuideResult.NotFound
        }
    }
}
