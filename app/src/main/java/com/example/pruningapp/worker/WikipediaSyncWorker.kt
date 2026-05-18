package com.example.pruningapp.worker

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.pruningapp.data.AppDatabase
import com.example.pruningapp.network.WikipediaImageProviderImpl
import com.example.pruningapp.repository.PlantRepository
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

// Atomowa jednostka robocza odpowiedzialna wyłącznie za synchronizację obrazów.
// Niezależna polityka backoff pozwala na obsługę błędów sieciowych bez blokowania
// synchronizacji danych tekstowych (GlobalSyncWorker działa niezależnie).
class WikipediaSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val wikiProvider = WikipediaImageProviderImpl.create(applicationContext)
        val repo = PlantRepository(db, wikiImageProvider = wikiProvider)

        // Etap 1: rośliny użytkownika bez obrazu
        val localPlants = db.plantDao().getAllPlantsWithoutWikiImage()
        Log.d(TAG, "Synchronizacja obrazów: ${localPlants.size} lokalnych roślin")
        for (plant in localPlants) {
            val callMade = try {
                repo.syncWikipediaImage(plant.id)
            } catch (e: Exception) {
                Log.e(TAG, "Błąd dla '${plant.name}': ${e.message}")
                false
            }
            if (callMade) delay(INTER_REQUEST_DELAY_MS)
        }

        // Etap 2: pełny katalog encyklopedii (Room jako SSOT zamiast statycznego PlantDatabase)
        val encyclopediaList = db.encyclopediaSpeciesDao().getAll()
        Log.d(TAG, "Synchronizacja obrazów: ${encyclopediaList.size} gatunków encyklopedii")
        for (species in encyclopediaList) {
            val callMade = try {
                repo.syncWikipediaImageForEncyclopedia(
                    polishName = species.polishName,
                    latinName = species.latinName,
                    perenualId = species.perenualId
                )
            } catch (e: Exception) {
                Log.e(TAG, "Błąd dla '${species.polishName}': ${e.message}")
                false
            }
            if (callMade) delay(INTER_REQUEST_DELAY_MS)
        }

        return Result.success()
    }

    companion object {
        private const val TAG = "WikipediaSyncWorker"
        private const val WORK_NAME = "wikipedia_image_sync"
        private const val INTER_REQUEST_DELAY_MS = 5_000L

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<WikipediaSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
        }
    }
}
