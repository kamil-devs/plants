package com.example.pruningapp.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.pruningapp.data.AppDatabase
import com.example.pruningapp.data.PlantDatabase
import com.example.pruningapp.repository.PlantRepository
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class WikipediaSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val repo = PlantRepository(db)

        // 1. Synchronizacja dla roślin w tabeli 'plants'
        val plantsInTable = db.plantDao().getAllPlantsWithoutWikiImage()
        Log.d("WikipediaSyncWorker", "Syncing images for ${plantsInTable.size} plants from local table")
        for (plant in plantsInTable) {
            val callMade = try {
                repo.syncWikipediaImage(plant.id)
            } catch (_: Exception) { false }
            
            if (callMade) delay(5000)
        }

        // 2. Synchronizacja dla bazy technicznej 'PlantDatabase'
        val dbPlants = PlantDatabase.plants
        Log.d("WikipediaSyncWorker", "Syncing images for ${dbPlants.size} species from PlantDatabase (Encyclopedia)")
        
        for (dbPlant in dbPlants) {
            val callMade = try {
                repo.syncWikipediaImageForEncyclopedia(dbPlant)
            } catch (e: Exception) {
                Log.e("WikipediaSyncWorker", "Error syncing ${dbPlant.polishName}: ${e.message}")
                false
            }
            
            if (callMade) delay(5000)
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "wikipedia_image_sync"

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
