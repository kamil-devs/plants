package com.example.pruningapp.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.pruningapp.data.AppDatabase
import com.example.pruningapp.data.PlantDatabase
import com.example.pruningapp.repository.PlantRepository
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class GlobalSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val repo = PlantRepository(db)

        val pending = db.plantDao().getUnsyncedOwnedPlants()
            .filter { plant ->
                plant.perenualId != null ||
                    PlantDatabase.plants.any { it.polishName == plant.name }
            }

        for ((index, plant) in pending.withIndex()) {
            try {
                repo.syncPlantFromApi(plant.id)
            } catch (e: HttpException) {
                return if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
            } catch (e: UnknownHostException) {
                return if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
            } catch (_: Exception) { }

            if (index < pending.lastIndex) delay(5_000)
        }

        // Wikipedia images — for all owned plants that still have no image
        val withoutImage = db.plantDao().getOwnedPlantsWithoutWikiImage()
        for ((index, plant) in withoutImage.withIndex()) {
            repo.syncWikipediaImage(plant.id)
            if (index < withoutImage.lastIndex) delay(1_000)
        }

        return Result.success()
    }

    companion object {
        private const val MAX_RETRIES = 5
        private const val WORK_NAME = "global_perenual_sync"

        fun enqueue(context: Context) {
            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, buildRequest())
        }

        private fun buildRequest(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<GlobalSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
    }
}
