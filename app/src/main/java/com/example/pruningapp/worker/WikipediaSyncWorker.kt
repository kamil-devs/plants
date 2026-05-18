package com.example.pruningapp.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.pruningapp.data.AppDatabase
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

        val withoutImage = db.plantDao().getAllPlantsWithoutWikiImage()
        Log.d("WikipediaSyncWorker", "Starting sync for ${withoutImage.size} plants")

        for ((index, plant) in withoutImage.withIndex()) {
            try {
                repo.syncWikipediaImage(plant.id)
            } catch (e: Exception) {
                Log.e("WikipediaSyncWorker", "Error for ${plant.name}: ${e.message}")
            }
            // Wikipedia API jest wrażliwe na szybkie zapytania
            if (index < withoutImage.lastIndex) delay(1500)
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
